package com.opuscapita.peppol.outbound;

import com.opuscapita.peppol.commons.queue.consume.CommonMessageReceiver;
import com.opuscapita.peppol.commons.queue.consume.ContainerMessageConsumer;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

import javax.xml.bind.DatatypeConverter;
import java.io.File;
import java.io.IOException;

@EnableCaching
@SpringBootApplication
@ComponentScan({"com.opuscapita.peppol.outbound", "com.opuscapita.peppol.commons"})
public class OutboundApp {

    @Value("${peppol.outbound.queue.in.name}")
    private String queueIn;

    private ContainerMessageConsumer consumer;

    @Autowired
    public OutboundApp(ContainerMessageConsumer consumer) {
        this.consumer = consumer;
    }

    public static void main(String[] args) {
        try {
            prepareOxalisHomeDirectory();
        } catch (Exception e) {
            e.printStackTrace();
        }

        SpringApplication.run(OutboundApp.class, args);
    }

    @Bean
    public SimpleMessageListenerContainer container(ConnectionFactory connectionFactory, MessageListenerAdapter listenerAdapter) {
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.setQueueNames(queueIn);
        container.setPrefetchCount(10);
        container.setMessageListener(listenerAdapter);
        return container;
    }

    @Bean
    public MessageListenerAdapter listenerAdapter(@NotNull CommonMessageReceiver receiver) {
        receiver.setContainerMessageConsumer(consumer);
        return new MessageListenerAdapter(receiver, "receiveMessage");
    }

    @Bean
    public Queue queue() {
        return new Queue(queueIn);
    }

    /**
     * A bit tricky thing, Local build and testing uses docker compose
     * This is the workaround to inject keys since compose secrets failed.
     */
    private static void prepareOxalisHomeDirectory() throws IOException {
        String oxalisHome = System.getenv("OXALIS_HOME");
        if (StringUtils.isBlank(oxalisHome)) {
            return;
        }

        String conf = System.getenv("OXALIS_CONF");
        String cert = System.getenv("OXALIS_CERT");

        if (StringUtils.isNotBlank(conf)) {
            File file = new File(oxalisHome + "/oxalis.conf");
            FileUtils.writeByteArrayToFile(file, DatatypeConverter.parseBase64Binary(conf));
        }
        if (StringUtils.isNotBlank(cert)) {
            File file = new File(oxalisHome + "/oxalis-keystore.jks");
            FileUtils.writeByteArrayToFile(file, DatatypeConverter.parseBase64Binary(cert));
        }
    }

}
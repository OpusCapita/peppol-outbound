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
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

import javax.xml.bind.DatatypeConverter;
import java.io.File;
import java.io.IOException;

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
            prepareKeystore();
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

    // This is the workaround to inject key for compose since we use docker secrets
    private static void prepareKeystore() throws IOException {
        String cert = System.getenv("PEPPOL_KEYSTORE");
        if (StringUtils.isNotBlank(cert)) {
            File file = new File("/run/secrets/oxalis-keystore-07082020.jks");
            FileUtils.writeByteArrayToFile(file, DatatypeConverter.parseBase64Binary(cert));
        }
    }
}
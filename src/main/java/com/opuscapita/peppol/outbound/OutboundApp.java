package com.opuscapita.peppol.outbound;

import com.opuscapita.peppol.commons.queue.consume.CommonMessageReceiver;
import com.opuscapita.peppol.commons.queue.consume.ContainerMessageConsumer;
import com.opuscapita.peppol.outbound.util.FileUpdateUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.TrustStrategy;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;

import javax.net.ssl.SSLContext;
import javax.xml.bind.DatatypeConverter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;

@SpringBootApplication
@ComponentScan({"com.opuscapita.peppol.outbound", "com.opuscapita.peppol.commons"})
public class OutboundApp {

    private final static Logger logger = LoggerFactory.getLogger(OutboundApp.class);

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

    @Bean
    // creates ssl validation disabled request factory
    public HttpComponentsClientHttpRequestFactory requestFactory() {
        HttpComponentsClientHttpRequestFactory requestFactory = null;
        try {
            TrustStrategy acceptingTrustStrategy = (X509Certificate[] chain, String authType) -> true;
            SSLContext sslContext = org.apache.http.ssl.SSLContexts.custom().loadTrustMaterial(null, acceptingTrustStrategy).build();
            SSLConnectionSocketFactory csf = new SSLConnectionSocketFactory(sslContext);
            CloseableHttpClient httpClient = HttpClients.custom().setSSLSocketFactory(csf).build();
            requestFactory = new HttpComponentsClientHttpRequestFactory();
            requestFactory.setHttpClient(httpClient);

        } catch (NoSuchAlgorithmException | KeyManagementException | KeyStoreException e) {
            logger.error("Failed to disable SSL Cert Validation for A2A Endpoint", e);
        }
        return requestFactory;
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
            String key = "oxalis.database.jdbc.password";

            File file = new File(oxalisHome + "/oxalis.conf");
            InputStream content = new ByteArrayInputStream(DatatypeConverter.parseBase64Binary(conf));
            InputStream updated = FileUpdateUtils.startAndReplace(content, key, key + "=test");
            FileUtils.copyInputStreamToFile(updated, file);
        }
        if (StringUtils.isNotBlank(cert)) {
            File file = new File(oxalisHome + "/oxalis-keystore.jks");
            FileUtils.writeByteArrayToFile(file, DatatypeConverter.parseBase64Binary(cert));
        }
    }
}
package com.opuscapita.peppol.outbound.sender.business;

import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.TrustStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLContext;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.Base64;

@Configuration
public class A2AConfiguration {

    private final static Logger logger = LoggerFactory.getLogger(A2AConfiguration.class);

    @Value("${a2a.host:''}")
    String host;

    @Value("${a2a.username:''}")
    String username;

    @Value("${a2a.password:''}")
    String password;

    @Bean
    public RestTemplate restTemplate() throws Exception {
        try {
            TrustStrategy acceptingTrustStrategy = (X509Certificate[] chain, String authType) -> true;
            SSLContext sslContext = org.apache.http.ssl.SSLContexts.custom().loadTrustMaterial(null, acceptingTrustStrategy).build();
            SSLConnectionSocketFactory csf = new SSLConnectionSocketFactory(sslContext);
            CloseableHttpClient httpClient = HttpClients.custom().setSSLSocketFactory(csf).build();
            HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
            requestFactory.setHttpClient(httpClient);
            return new RestTemplate(requestFactory);

        } catch (NoSuchAlgorithmException | KeyManagementException | KeyStoreException e) {
            logger.error("Failed to disable SSL Cert Validation for A2A Endpoint", e);
            throw e;
        }
    }

//    @Bean
//    public RestTemplate restTemplate() throws Exception {
//        ClassPathResource classPathResource = new ClassPathResource("a2a.cer");
//        SSLContext sslContext = SSLContexts
//                .custom()
//                .loadTrustMaterial(classPathResource.getFile())
//                .build();
//        final CloseableHttpClient client = HttpClients
//                .custom()
//                .setSSLContext(sslContext)
//                .build();
//        return new RestTemplate(new HttpComponentsClientHttpRequestFactory(client));
//    }

    String getAuthHeader() {
        byte[] basicAuthValue = (username + ":" + password).getBytes();
        return "Basic " + Base64.getEncoder().encodeToString(basicAuthValue);
    }
}
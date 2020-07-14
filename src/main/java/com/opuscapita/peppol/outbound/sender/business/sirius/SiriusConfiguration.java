package com.opuscapita.peppol.outbound.sender.business.sirius;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;

@Configuration
@RefreshScope
public class SiriusConfiguration {

    private final static Logger logger = LoggerFactory.getLogger(SiriusConfiguration.class);

    @Value("${sirius.url:''}")
    private String url;

    @Value("${sirius.username:''}")
    private String username;

    @Value("${sirius.password:''}")
    private String password;

    @Value("${sirius.size-limit:5242880}")
    private Long sizeLimit;

    @Value("${sirius.timeout:3}")
    private int timeout;

    @Value("${sirius.retry-count:10}")
    private int retryCount;

    @Value("${sirius.retry-delay:1200000}")
    private int retryDelay;

    private RequestConfig getRequestConfig() {
        return RequestConfig.custom()
                .setConnectionRequestTimeout(timeout * 60 * 1000)
                .setConnectTimeout(timeout * 60 * 1000)
                .setSocketTimeout(timeout * 60 * 1000)
                .build();
    }

    private PoolingHttpClientConnectionManager getConnectionManager() {
        PoolingHttpClientConnectionManager manager = new PoolingHttpClientConnectionManager();
        manager.setMaxTotal(100);
        manager.setDefaultMaxPerRoute(100);
        return manager;
    }

    @Bean
    @Qualifier("siriusRestTemplate")
    public RestTemplate siriusRestTemplate() throws Exception {
        CloseableHttpClient httpClient = HttpClientBuilder.create()
                .setConnectionManager(getConnectionManager())
                .setDefaultRequestConfig(getRequestConfig())
                .build();

        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
        requestFactory.setHttpClient(httpClient);
        return new RestTemplate(requestFactory);
    }

    public String getUrl() {
        return url;
    }

    public Long getSizeLimit() {
        return sizeLimit;
    }

    public String getAuthHeader() {
        byte[] basicAuthValue = (username + ":" + password).getBytes();
        return "Basic " + Base64.getEncoder().encodeToString(basicAuthValue);
    }

    public int getRetryCount() {
        return retryCount;
    }

    public int getRetryDelay() {
        return retryDelay;
    }
}

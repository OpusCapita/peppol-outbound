package com.opuscapita.peppol.outbound.sender;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;

@Configuration
@RefreshScope
public class NetworkConfiguration {

    @Value("${network.retry-count:10}")
    private int retryCount;

    @Value("${network.retry-delay:1800000}")
    private int retryDelay;

    public int getRetryCount() {
        return retryCount;
    }

    public int getRetryDelay() {
        return retryDelay;
    }
}

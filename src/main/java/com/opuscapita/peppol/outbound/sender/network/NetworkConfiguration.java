package com.opuscapita.peppol.outbound.sender.network;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;

@Configuration
@RefreshScope
public class NetworkConfiguration {

    /*
        fakes sending to the given platforms
        always returning positive response
        testing purposes
        example value: `A2A,NETWORK`
     */
    @Value("${fake-sending:''}")
    private String fakeConfig;

    /*
        stops sending to the given platforms
        throws exception and marks the message as failed
        will create a ticket and require manual handling
        example value: `A2A,XIB`
     */
    @Value("${stop-sending:''}")
    private String stopConfig;

    /*
        retry count for the messages routed to NETWORK
        note: works only for retryable exceptions
     */
    @Value("${network.retry-count:10}")
    private int retryCount;

    /*
        delay between each retry for the messages routed to NETWORK
     */
    @Value("${network.retry-delay:1800000}")
    private int retryDelay;

    public String getFakeConfig() {
        return fakeConfig;
    }

    public String getStopConfig() {
        return stopConfig;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public int getRetryDelay() {
        return retryDelay;
    }
}

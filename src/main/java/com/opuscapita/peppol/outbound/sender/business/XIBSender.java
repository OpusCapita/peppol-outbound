package com.opuscapita.peppol.outbound.sender.business;

import com.opuscapita.peppol.commons.auth.AuthorizationService;
import com.opuscapita.peppol.commons.container.ContainerMessage;
import com.opuscapita.peppol.outbound.sender.Sender;
import no.difi.oxalis.api.outbound.TransmissionResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class XIBSender implements Sender {

    private final static Logger logger = LoggerFactory.getLogger(XIBSender.class);

    private final RestTemplate restTemplate;
    private final AuthorizationService authService;

    @Autowired
    public XIBSender(AuthorizationService authService, RestTemplateBuilder restTemplateBuilder) {
        this.authService = authService;
        this.restTemplate = restTemplateBuilder.build();
    }

    @Override
    public TransmissionResponse send(ContainerMessage cm) throws Exception {
        String endpoint = getEndpoint(cm.getFileName());
        logger.debug("Sending upload-file request to endpoint: " + endpoint + " for file: " + cm.getFileName());

        HttpHeaders headers = new HttpHeaders();
        authService.setAuthorizationHeader(headers);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>("", headers);
        logger.debug("Wrapped and set the request body as string");

        try {
            ResponseEntity<String> result = restTemplate.exchange(endpoint, HttpMethod.POST, entity, String.class);
            logger.debug("Upload-file request successfully sent, got response: " + result.toString());
        } catch (Exception e) {
            logger.error("Error occurred while trying to send the file to XIB", e);
            throw new BusinessDeliveryException("Error occurred while trying to send the file to XIB", e);
        }

        logger.info("XIBSender delivered message: " + cm.getFileName());
        return new BusinessResponse();
    }

    private String getEndpoint(String filename) {
        return UriComponentsBuilder
                .fromUriString("http://peppol-xib-adaptor")
                .port(3043)
                .path("/api/upload-file")
                .queryParam("path", filename)
                .toUriString();
    }
}

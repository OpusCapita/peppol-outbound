package com.opuscapita.peppol.outbound.sender.business;

import com.opuscapita.peppol.commons.container.ContainerMessage;
import com.opuscapita.peppol.commons.storage.Storage;
import com.opuscapita.peppol.outbound.sender.Sender;
import no.difi.oxalis.api.outbound.TransmissionResponse;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.InputStream;

@Component
public class A2ASender implements Sender {

    private final static Logger logger = LoggerFactory.getLogger(A2ASender.class);

    private final Storage storage;
    private final A2AConfiguration config;
    private final RestTemplate restTemplate;

    @Autowired
    public A2ASender(Storage storage, RestTemplate restTemplate, A2AConfiguration config) {
        this.config = config;
        this.storage = storage;
        this.restTemplate = restTemplate;
    }

    @Override
    public TransmissionResponse send(ContainerMessage cm) throws Exception {
        logger.info("A2ASender.send called for the message: " + cm.getFileName());

        int i = 0;
        Exception t;
        do {
            try {
                sendRequest(cm);
                return new BusinessResponse();
            } catch (Exception e) {
                t = e;
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ignored) {
                }
            }
        } while (++i < 5);

        throw t;
    }

    private void sendRequest(ContainerMessage cm) throws Exception {
        logger.info("A2ASender.sendRequest called for the message: " + cm.getFileName());

        try (InputStream content = storage.get(cm.getFileName())) {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Transfer-Encoding", "chunked");
            headers.set("Document-Path", getDocumentPath(cm));
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.set("Authorization", config.getAuthHeader());
            HttpEntity<Resource> entity = new HttpEntity<>(new InputStreamResource(content), headers);
            logger.info("A2ASender wrapped and set the request body for the message: " + cm.getFileName());

            try {
                ResponseEntity<String> result = restTemplate.exchange(config.host, HttpMethod.POST, entity, String.class);
                logger.info("File successfully sent to A2A, got response: " + result.toString());
            } catch (Exception e) {
                logger.error("Error occurred while trying to send the file to A2A", e);
                throw new BusinessDeliveryException("Error occurred while trying to send the file to A2A", e);
            }
        }
    }

    private String getDocumentPath(ContainerMessage cm) {
        if (cm.getMetadata() == null) {
            return "unknown";
        }
        if (cm.getMetadata().getValidationRule() == null) {
            return "unknown";
        }

        String archetype = cm.getMetadata().getValidationRule().getArchetype();
        archetype = "CENBII".equals(archetype) ? "PEPPOL_BIS" : archetype; // just in case
        String type = cm.getMetadata().getValidationRule().getLocalName();
        String filename = FilenameUtils.getName(cm.getFileName());
        return String.format("/%s/%s/%s", archetype, type, filename);
    }

}

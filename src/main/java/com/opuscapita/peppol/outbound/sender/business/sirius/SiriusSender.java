package com.opuscapita.peppol.outbound.sender.business.sirius;

import com.opuscapita.peppol.commons.container.ContainerMessage;
import com.opuscapita.peppol.commons.storage.Storage;
import com.opuscapita.peppol.outbound.sender.Sender;
import com.opuscapita.peppol.outbound.sender.business.BusinessDeliveryException;
import com.opuscapita.peppol.outbound.sender.business.BusinessResponse;
import no.difi.oxalis.api.outbound.TransmissionResponse;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.InputStream;

@Component
public class SiriusSender implements Sender {

    private final static Logger logger = LoggerFactory.getLogger(SiriusSender.class);

    private final Storage storage;
    private final RestTemplate restTemplate;
    private final SiriusConfiguration config;

    @Autowired
    public SiriusSender(Storage storage, @Qualifier("siriusRestTemplate") RestTemplate restTemplate, SiriusConfiguration config) {
        this.config = config;
        this.storage = storage;
        this.restTemplate = restTemplate;
    }

    @Override
    public TransmissionResponse send(ContainerMessage cm) throws Exception {
        logger.info("SiriusSender.send called for the message: " + cm.getFileName());

        if (storage.size(cm.getFileName()) < config.getSizeLimit()) {
            sendFile(cm);
        } else {
            sendReference(cm);
        }

        return new BusinessResponse();
    }

    private void sendFile(ContainerMessage cm) throws Exception {
        try (InputStream content = storage.get(cm.getFileName())) {
            HttpHeaders headers = getHeaders(cm);
            HttpEntity<Resource> entity = new HttpEntity<>(new InputStreamResource(content), headers);

            try {
                ResponseEntity<String> result = restTemplate.exchange(config.getUrl(), HttpMethod.POST, entity, String.class);
                logger.info("File successfully sent to Sirius, got response: " + result.toString());
            } catch (Exception e) {
                logger.error("Error occurred while trying to send the file to Sirius", e);
                throw new BusinessDeliveryException("Error occurred while trying to send the file to Sirius", e);
            }
        }
    }

    private void sendReference(ContainerMessage cm) throws Exception {
        HttpHeaders headers = getHeaders(cm);
        headers.set("Payload-Path", "/api/c_opuscapita/files/" + cm.getFileName());
        HttpEntity<Resource> entity = new HttpEntity<>(null, headers);

        try {
            ResponseEntity<String> result = restTemplate.exchange(config.getUrl(), HttpMethod.POST, entity, String.class);
            logger.info("File reference successfully sent to Sirius, got response: " + result.toString());
        } catch (Exception e) {
            logger.error("Error occurred while trying to send the file to Sirius", e);
            throw new BusinessDeliveryException("Error occurred while trying to send the file to Sirius", e);
        }
    }

    private HttpHeaders getHeaders(ContainerMessage cm) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Transfer-Encoding", "chunked");
        headers.set("Authorization", config.getAuthHeader());
        headers.set("Content-Type", "application/octet-stream");
        headers.set("Process-Id", cm.getMetadata().getProfileTypeIdentifier());
        headers.set("Document-Id", cm.getMetadata().getDocumentTypeIdentifier());
        headers.set("File-Name", FilenameUtils.getName(cm.getFileName()));
        headers.set("Message-Id", cm.getMetadata().getMessageId());
        headers.set("Receiver-Id", cm.getMetadata().getRecipientId());
        headers.set("Sender-Id", cm.getMetadata().getSenderId());
        headers.set("senderApplication", "PEPPOL-AP");
        return headers;
    }

    @Override
    public int getRetryCount() {
        return config.getRetryCount();
    }

    @Override
    public int getRetryDelay() {
        return config.getRetryDelay();
    }
}

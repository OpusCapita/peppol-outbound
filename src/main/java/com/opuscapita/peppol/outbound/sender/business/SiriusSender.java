package com.opuscapita.peppol.outbound.sender.business;

import com.opuscapita.peppol.commons.container.ContainerMessage;
import com.opuscapita.peppol.commons.storage.Storage;
import com.opuscapita.peppol.outbound.sender.Sender;
import no.difi.oxalis.api.outbound.TransmissionResponse;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.InputStream;
import java.time.Duration;
import java.util.Base64;

@Component
@RefreshScope
public class SiriusSender implements Sender {

    private final static Logger logger = LoggerFactory.getLogger(SiriusSender.class);

    @Value("${sirius.url:''}")
    private String url;

    @Value("${sirius.username:''}")
    private String username;

    @Value("${sirius.password:''}")
    private String password;

    @Value("${sirius.size-limit:5242880}")
    private Long sizeLimit;

    private final Storage storage;
    private final RestTemplate restTemplate;

    @Autowired
    public SiriusSender(Storage storage, RestTemplateBuilder restTemplateBuilder) {
        this.storage = storage;
        this.restTemplate = restTemplateBuilder.setConnectTimeout(Duration.ofMinutes(3)).build();
    }

    @Override
    public TransmissionResponse send(ContainerMessage cm) throws Exception {
        logger.info("SiriusSender.send called for the message: " + cm.getFileName());

        if (storage.size(cm.getFileName()) < sizeLimit) {
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
                ResponseEntity<String> result = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
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
            ResponseEntity<String> result = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
            logger.info("File reference successfully sent to Sirius, got response: " + result.toString());
        } catch (Exception e) {
            logger.error("Error occurred while trying to send the file to Sirius", e);
            throw new BusinessDeliveryException("Error occurred while trying to send the file to Sirius", e);
        }
    }

    private HttpHeaders getHeaders(ContainerMessage cm) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Transfer-Encoding", "chunked");
        headers.set("Authorization", "Basic " + Base64.getEncoder().encodeToString((username + ":" + password).getBytes()));
        headers.set("Content-Type", "application/octet-stream");
        headers.set("Process-Id", cm.getMetadata().getProfileTypeIdentifier());
        headers.set("Document-Id", cm.getMetadata().getDocumentTypeIdentifier());
        headers.set("File-Name", FilenameUtils.getName(cm.getFileName()));
        headers.set("Message-Id", cm.getMetadata().getMessageId());
        headers.set("Receiver-Id", cm.getMetadata().getRecipientId());
        headers.set("Sender-Id", cm.getMetadata().getSenderId());
        headers.set("Sender-Application", "PEPPOL-AP");
        return headers;
    }
}

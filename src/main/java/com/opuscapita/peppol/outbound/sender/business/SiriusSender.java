package com.opuscapita.peppol.outbound.sender.business;

import com.opuscapita.peppol.commons.container.ContainerMessage;
import com.opuscapita.peppol.commons.storage.Storage;
import com.opuscapita.peppol.outbound.sender.Sender;
import no.difi.oxalis.api.outbound.TransmissionResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.InputStream;
import java.util.Base64;

@Component
public class SiriusSender implements Sender {

    private final static Logger logger = LoggerFactory.getLogger(SiriusSender.class);

    @Value("${sirius.host:''}")
    private String host;

    @Value("${sirius.username:''}")
    private String username;

    @Value("${sirius.password:''}")
    private String password;

    private final Storage storage;
    private final RestTemplate restTemplate;

    @Autowired
    public SiriusSender(Storage storage, RestTemplateBuilder restTemplateBuilder) {
        this.storage = storage;
        this.restTemplate = restTemplateBuilder.build();
    }

    @Override
    public TransmissionResponse send(ContainerMessage cm) throws Exception {
        logger.info("SiriusSender.send called for the message: " + cm.getFileName());

        try (InputStream content = storage.get(cm.getFileName())) {

            String endpoint = "http://localhost:55555/rest/_Sandbox_ext_moso.Peppol_AP.PepData"; // todo: put the url to config repo

            HttpHeaders headers = new HttpHeaders();
            headers.set("Transfer-Encoding", "chunked");
            headers.set("Authorization", "Basic " + Base64.getEncoder().encodeToString((username + ":" + password).getBytes()));
            headers.set("Content-Type", "application/octet-stream");
            headers.set("Process-Id", cm.getMetadata().getProfileTypeIdentifier());
            headers.set("Document-Id", cm.getMetadata().getDocumentTypeIdentifier());

            // todo: set the header if the file is large, othervise set the body
            headers.set("Payload-Path", "/api/c_opuscapita/files/" + cm.getFileName()); // todo: get the url from storage
            HttpEntity<Resource> entity = new HttpEntity<>(new InputStreamResource(content), headers);

            try {
                ResponseEntity<String> result = restTemplate.exchange(endpoint, HttpMethod.POST, entity, String.class);
                logger.info("File successfully sent to Sirius, got response: " + result.toString());
            } catch (Exception e) {
                logger.error("Error occurred while trying to send the file to Sirius", e);
                throw new BusinessDeliveryException("Error occurred while trying to send the file to Sirius", e);
            }
        }

        return new BusinessResponse();
    }
}

package com.opuscapita.peppol.outbound.controller;

import com.google.inject.Injector;
import no.difi.oxalis.outbound.OxalisOutboundComponent;
import no.difi.vefa.peppol.common.model.*;
import no.difi.vefa.peppol.lookup.LookupClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class OutboundRestController {

    private static final Logger logger = LoggerFactory.getLogger(OutboundRestController.class);

    private LookupClient lookupClient;

    public OutboundRestController() {
        OxalisOutboundComponent oxalis = new OxalisOutboundComponent();
        Injector injector = oxalis.getInjector();
        this.lookupClient = injector.getInstance(LookupClient.class);
    }

    @GetMapping("/public/lookup/{icd}/{identifier}")
    public ResponseEntity<?> lookupParticipant(@PathVariable String icd, @PathVariable String identifier) throws Exception {
        try {
            ParticipantIdentifier participantIdentifier = ParticipantIdentifier.of(icd + ":" + identifier);
            logger.info("Sending lookup request for " + participantIdentifier.toString());
            LookupResponseDto response = new LookupResponseDto(participantIdentifier);

            List<ServiceReference> serviceReferences = lookupClient.getServiceReferences(participantIdentifier);
            for (ServiceReference serviceReference : serviceReferences) {
                ServiceMetadata metadata = lookupClient.getServiceMetadata(participantIdentifier, serviceReference.getDocumentTypeIdentifier());
                for (ProcessMetadata<Endpoint> processMetadata : metadata.getProcesses()) {

                    LookupResponseDocumentTypeDto responseDocumentType = new LookupResponseDocumentTypeDto(metadata.getDocumentTypeIdentifier());
                    responseDocumentType.setProcessIdentifier(processMetadata.getProcessIdentifier() != null && !processMetadata.getProcessIdentifier().isEmpty() ? processMetadata.getProcessIdentifier().get(0) : null);

                    for (Endpoint endpoint : processMetadata.getEndpoints()) {
                        LookupResponseEndpointDto responseEndpoint = new LookupResponseEndpointDto();
                        responseEndpoint.setAddress(endpoint.getAddress().toASCIIString());
                        responseEndpoint.setTransportProfile(endpoint.getTransportProfile().getIdentifier());
                        responseEndpoint.setCertificateSubject(endpoint.getCertificate().getSubjectX500Principal().getName());
                        responseEndpoint.setCertificateValidityStartDate(endpoint.getCertificate().getNotBefore());
                        responseEndpoint.setCertificateValidityEndDate(endpoint.getCertificate().getNotAfter());

                        responseDocumentType.getEndpointList().add(responseEndpoint);
                    }
                    response.getDocumentTypeList().add(responseDocumentType);
                }
            }
            return wrap(response);

        } catch (Exception e) {
            LookupResponseDto response = new LookupResponseDto();
            response.setErrorMessage(e.getMessage());
            return wrap(response);
        }
    }

    private <T> ResponseEntity<T> wrap(T body) {
        if (body != null) {
            return ResponseEntity.ok(body);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

}

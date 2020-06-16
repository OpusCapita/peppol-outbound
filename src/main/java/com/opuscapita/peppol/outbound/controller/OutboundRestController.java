package com.opuscapita.peppol.outbound.controller;

import com.google.inject.Injector;
import no.difi.oxalis.outbound.OxalisOutboundComponent;
import no.difi.vefa.peppol.common.model.ParticipantIdentifier;
import no.difi.vefa.peppol.common.model.ServiceReference;
import no.difi.vefa.peppol.lookup.LookupClient;
import no.difi.vefa.peppol.lookup.api.LookupException;
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

    @GetMapping("/public/lookup/{icd}/{identifier}")
    public ResponseEntity<?> lookupParticipant(@PathVariable String icd, @PathVariable String identifier) throws Exception {
        OxalisOutboundComponent oxalis = new OxalisOutboundComponent();
        Injector injector = oxalis.getInjector();
        LookupClient lookupClient = injector.getInstance(LookupClient.class);

        try {
            ParticipantIdentifier p = ParticipantIdentifier.of(icd + ":" + identifier);
            logger.info("Sending lookup request for " + p.toString());
            List<ServiceReference> serviceReferences = lookupClient.getServiceReferences(p);
            return wrap(serviceReferences);
        } catch (LookupException e) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(e.getMessage());
        }
    }

    private <T> ResponseEntity<T> wrap(T body) {
        if (body != null) {
            return ResponseEntity.ok(body);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

}

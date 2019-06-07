package com.opuscapita.peppol.outbound.sender;

import com.opuscapita.peppol.commons.container.ContainerMessage;
import com.opuscapita.peppol.commons.storage.Storage;
import no.difi.oxalis.api.outbound.TransmissionRequest;
import no.difi.oxalis.api.outbound.TransmissionResponse;
import no.difi.oxalis.outbound.OxalisOutboundComponent;
import no.difi.oxalis.outbound.transmission.TransmissionRequestBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;

public class RealSender implements Sender {

    private final static Logger logger = LoggerFactory.getLogger(RealSender.class);

    private final Storage storage;
    private final OxalisOutboundComponent oxalis;

    RealSender(Storage storage, OxalisOutboundComponent oxalis) {
        this.oxalis = oxalis;
        this.storage = storage;
    }

    @Override
    public TransmissionResponse send(ContainerMessage cm) throws Exception {
        logger.info("RealSender.send called for the message: " + cm.getFileName());

        TransmissionRequestBuilder requestBuilder = oxalis.getTransmissionRequestBuilder();
        try (InputStream payload = storage.get(cm.getFileName())) {
            requestBuilder.payLoad(payload);
        }
        TransmissionRequest request = requestBuilder.build();
        logger.info("RealSender created request for the message: " + cm.getFileName());

        String endpoint = request.getEndpoint().getAddress().toASCIIString();
        String subject = request.getEndpoint().getCertificate().getSubjectX500Principal().getName();
        logger.info("RealSender is about to deliver message: " + cm.getFileName() + " to endpoint: " + endpoint + " [" + subject + "]");

        return oxalis.getTransmitter().transmit(request);
    }

}

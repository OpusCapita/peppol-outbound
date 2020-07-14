package com.opuscapita.peppol.outbound.sender.network;

import com.opuscapita.peppol.commons.container.ContainerMessage;
import com.opuscapita.peppol.commons.storage.Storage;
import com.opuscapita.peppol.outbound.sender.Sender;
import no.difi.oxalis.api.outbound.TransmissionRequest;
import no.difi.oxalis.api.outbound.TransmissionResponse;
import no.difi.oxalis.outbound.OxalisOutboundComponent;
import no.difi.oxalis.outbound.transmission.TransmissionRequestBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;

public class NetworkSender implements Sender {

    private final static Logger logger = LoggerFactory.getLogger(NetworkSender.class);

    private final Storage storage;
    private final NetworkConfiguration config;
    private final OxalisOutboundComponent oxalis;

    public NetworkSender(Storage storage, NetworkConfiguration networkConfiguration, OxalisOutboundComponent oxalis) {
        this.oxalis = oxalis;
        this.storage = storage;
        this.config = networkConfiguration;
    }

    @Override
    public TransmissionResponse send(ContainerMessage cm) throws Exception {
        logger.debug("NetworkSender.send called for the message: " + cm.getFileName());

        TransmissionRequestBuilder requestBuilder = oxalis.getTransmissionRequestBuilder();
        try (InputStream payload = storage.get(cm.getFileName())) {
            requestBuilder.payLoad(payload);
        }
        TransmissionRequest request = requestBuilder.build();
        logger.debug("NetworkSender created request for the message: " + cm.getFileName());

        String endpoint = request.getEndpoint().getAddress().toASCIIString();
        String subject = request.getEndpoint().getCertificate().getSubjectX500Principal().getName();
        logger.info("NetworkSender is about to deliver message: " + cm.getFileName() + " to endpoint: " + endpoint + " [" + subject + "]");

        cm.getMetadata().setReceivingAccessPoint(subject);
        return oxalis.getTransmitter().transmit(request);
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

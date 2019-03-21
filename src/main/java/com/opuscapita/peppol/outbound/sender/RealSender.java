package com.opuscapita.peppol.outbound.sender;

import com.opuscapita.peppol.commons.container.ContainerMessage;
import com.opuscapita.peppol.commons.storage.Storage;
import no.difi.oxalis.api.outbound.TransmissionRequest;
import no.difi.oxalis.api.outbound.TransmissionResponse;
import no.difi.oxalis.outbound.OxalisOutboundComponent;
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
        InputStream payload = storage.get(cm.getFileName());
        TransmissionRequest request = oxalis.getTransmissionRequestBuilder().payLoad(payload).build();
        logger.info("RealSender about to deliver message: " + cm.getFileName() + " to endpoint: " + request.getEndpoint());
        return oxalis.getTransmitter().transmit(request);
    }

}

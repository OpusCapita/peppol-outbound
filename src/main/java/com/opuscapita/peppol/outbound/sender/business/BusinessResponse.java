package com.opuscapita.peppol.outbound.sender.business;

import no.difi.oxalis.api.model.TransmissionIdentifier;
import no.difi.oxalis.api.outbound.TransmissionResponse;
import no.difi.vefa.peppol.common.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Return this after sending file to A2A and XIB
 */
public class BusinessResponse implements TransmissionResponse {

    private static final Logger logger = LoggerFactory.getLogger(BusinessResponse.class);

    private TransmissionIdentifier identifier;

    public BusinessResponse() {
        this.identifier = TransmissionIdentifier.generateUUID();
    }

    public BusinessResponse(String identifier) {
        this.identifier = TransmissionIdentifier.of(identifier);
    }

    @Override
    public TransmissionIdentifier getTransmissionIdentifier() {
        return identifier;
    }

    @Override
    public Header getHeader() {
        return null;
    }

    @Override
    public Date getTimestamp() {
        return new Date();
    }

    @Override
    public Digest getDigest() {
        return null;
    }

    @Override
    public TransportProtocol getTransportProtocol() {
        return TransportProtocol.INTERNAL;
    }

    @Override
    public List<Receipt> getReceipts() {
        return Arrays.asList(primaryReceipt());
    }

    @Override
    public Receipt primaryReceipt() {
        return Receipt.of("test".getBytes());
    }

    @Override
    public Endpoint getEndpoint() {
        return null;
    }

}

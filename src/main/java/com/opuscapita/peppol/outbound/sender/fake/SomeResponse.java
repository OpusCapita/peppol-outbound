package com.opuscapita.peppol.outbound.sender.fake;

import com.opuscapita.peppol.commons.container.ContainerMessage;
import no.difi.oxalis.api.model.TransmissionIdentifier;
import no.difi.oxalis.api.outbound.TransmissionResponse;
import no.difi.vefa.peppol.common.code.DigestMethod;
import no.difi.vefa.peppol.common.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * A dummy response that has all necessary fields for testing purposes
 */
public class SomeResponse implements TransmissionResponse {

    private static final Logger logger = LoggerFactory.getLogger(SomeResponse.class);

    private TransmissionIdentifier identifier;

    public SomeResponse() {
        this.identifier = TransmissionIdentifier.generateUUID();
    }

    public SomeResponse(String identifier) {
        this.identifier = TransmissionIdentifier.of(identifier);
    }

    @Override
    public TransmissionIdentifier getTransmissionIdentifier() {
        return identifier;
    }

    @Override
    public Header getHeader() {
        Header header = Header.newInstance();
        header.identifier(InstanceIdentifier.generateUUID());
        header.sender(ParticipantIdentifier.of("test"));
        header.receiver(ParticipantIdentifier.of("test"));
        header.process(ProcessIdentifier.NO_PROCESS);
        header.documentType(DocumentTypeIdentifier.of("urn:oasis:names:specification:ubl:schema:xsd:Invoice-2::Invoice##urn:www.cenbii.eu:transaction:biitrns010:ver2.0:extended:urn:www.peppol.eu:bis:peppol5a:ver2.0:extended:urn:www.difi.no:ehf:faktura:ver2.0::2.1"));
        header.instanceType(InstanceType.of("urn:oasis:names:specification:ubl:schema:xsd:Invoice-2", "Invoice", "2.1"));
        return header;
    }

    @Override
    public Date getTimestamp() {
        return new Date();
    }

    @Override
    public Digest getDigest() {
        return Digest.of(DigestMethod.SHA256, "test".getBytes());
    }

    @Override
    public TransportProtocol getTransportProtocol() {
        return TransportProtocol.AS2;
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
        return Endpoint.of(TransportProfile.AS2_1_0, null, new SomeCertificate());
    }

    /**
     * Throws specific type of exception when filename includes specific keyword for testing purposes
     */
    public static void throwExceptionIfExpectedInFilename(ContainerMessage cm) throws IOException {
        if (cm.getFileName().contains("-fail-me-unknown-recipient-")) {
            logger.info("Rejecting message with LookupException as requested by the file name");
            throw new RuntimeException("This sending expected to fail with LookupException: Identifier 9908:919779446 is not registered in SML test mode");
        }
        if (cm.getFileName().contains("-fail-me-unsupported-data-format-")) {
            logger.info("Rejecting message with LookupException as requested by the file name");
            throw new RuntimeException("This sending expected to fail with LookupException: Combination of receiver 9908:919779446 and document type identifier peppol-bis-v3 is not supported test mode");
        }
        if (cm.getFileName().contains("-fail-me-receiving-ap-error-")) {
            logger.info("Rejecting message as requested by the file name");
            throw new RuntimeException("This sending expected to fail with exception: Receiving server does not seem to be running test mode");
        }
        if (cm.getFileName().contains("-fail-me-io-")) {
            logger.info("Rejecting message with I/O error as requested by the file name");
            throw new IOException("This sending expected to fail I/O in test mode");
        }
        if (cm.getFileName().contains("-fail-me-")) {
            logger.info("Rejecting message as requested by the file name");
            throw new IllegalStateException("This sending expected to fail in test mode");
        }
    }

}

package com.opuscapita.peppol.outbound.sender;

import com.opuscapita.peppol.commons.container.ContainerMessage;
import com.opuscapita.peppol.commons.storage.Storage;
import com.opuscapita.peppol.outbound.sender.fakes.SomeResponse;
import com.opuscapita.peppol.outbound.util.FileUpdateUtils;
import no.difi.oxalis.api.outbound.TransmissionRequest;
import no.difi.oxalis.api.outbound.TransmissionResponse;
import no.difi.oxalis.outbound.OxalisOutboundComponent;
import no.difi.oxalis.outbound.transmission.TransmissionRequestBuilder;
import no.difi.vefa.peppol.common.model.Endpoint;
import no.difi.vefa.peppol.common.model.ParticipantIdentifier;
import no.difi.vefa.peppol.common.model.TransportProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

/**
 * More advanced test sender than the {@link FakeSender}.
 * It really sends data through the network but always uses the selected recipient instead of the real one.
 */
public class TestSender implements Sender {

    private static final Logger logger = LoggerFactory.getLogger(TestSender.class);

    private final String receiver;
    private final Storage storage;
    private final OxalisOutboundComponent oxalis;

    TestSender(String receiver, Storage storage, OxalisOutboundComponent oxalis) {
        this.oxalis = oxalis;
        this.storage = storage;
        this.receiver = receiver;
    }

    @Override
    public TransmissionResponse send(ContainerMessage cm) throws Exception {
        SomeResponse.throwExceptionIfExpectedInFilename(cm);

        if (cm.getFileName().contains("-integration-test-")) {
            logger.info("TestSender returning fake TransmissionResponse for integration tests");
            return new SomeResponse(cm.getFileName());
        }

        TransmissionRequestBuilder requestBuilder = oxalis.getTransmissionRequestBuilder();
        requestBuilder.setTransmissionBuilderOverride(true);
        requestBuilder = requestBuilder.receiver(ParticipantIdentifier.of(receiver));
        requestBuilder = requestBuilder.payLoad(getUpdatedFileContent(cm));
//        requestBuilder = sendRequestToGivenUrl(requestBuilder, "http://localhost:3037/public/as2");
        TransmissionRequest request = requestBuilder.build();

        String endpoint = request.getEndpoint().getAddress().toASCIIString();
        String subject = request.getEndpoint().getCertificate().getSubjectX500Principal().getName();
        logger.info("TestSender is about to deliver message: " + cm.getFileName() + " to endpoint: " + endpoint + "[" + subject + "]");

        return oxalis.getTransmitter().transmit(request);
    }

    private InputStream getUpdatedFileContent(ContainerMessage cm) throws IOException {
        InputStream payload = storage.get(cm.getFileName());
        String recipientId = cm.getMetadata().getRecipientId();

        return FileUpdateUtils.searchAndReplace(payload, recipientId, receiver);
    }

    /**
     * This method can be used to forward the request to any given endpoint ignoring lookup... localhost for instance
     */
    private TransmissionRequestBuilder sendRequestToGivenUrl(TransmissionRequestBuilder requestBuilder, String url) {
        X509Certificate certificate = null;
        try {
            CertificateFactory fact = CertificateFactory.getInstance("X.509");
            FileInputStream is = new FileInputStream("C:\\Users\\ibilge\\.oxalis\\oxalis.cer");
            certificate = (X509Certificate) fact.generateCertificate(is);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return requestBuilder.overrideAs2Endpoint(Endpoint.of(TransportProfile.AS2_1_0, URI.create(url), certificate));
    }

}

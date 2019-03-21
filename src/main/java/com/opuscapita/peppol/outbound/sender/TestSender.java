package com.opuscapita.peppol.outbound.sender;

import com.opuscapita.peppol.commons.container.ContainerMessage;
import com.opuscapita.peppol.commons.storage.Storage;
import no.difi.oxalis.api.outbound.TransmissionRequest;
import no.difi.oxalis.api.outbound.TransmissionResponse;
import no.difi.oxalis.outbound.OxalisOutboundComponent;
import no.difi.oxalis.outbound.transmission.TransmissionRequestBuilder;
import no.difi.vefa.peppol.common.model.Endpoint;
import no.difi.vefa.peppol.common.model.ParticipantIdentifier;
import no.difi.vefa.peppol.common.model.TransportProfile;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
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

        logger.info("TestSender about to deliver message: " + cm.getFileName() + " to endpoint: " + request.getEndpoint());
        return oxalis.getTransmitter().transmit(request);
    }

    private InputStream getUpdatedFileContent(ContainerMessage cm) throws IOException {
        InputStream payload = storage.get(cm.getFileName());
        String recipientId = cm.getMetadata().getRecipientId();

        ByteArrayOutputStream result = new ByteArrayOutputStream();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(payload))) {
            String line = reader.readLine();
            while (line != null) {
                line = StringUtils.replace(line, recipientId, receiver);
                result.write(line.getBytes());
                result.write("\n".getBytes());
                line = reader.readLine();
            }
        }

        return new ByteArrayInputStream(result.toByteArray());
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

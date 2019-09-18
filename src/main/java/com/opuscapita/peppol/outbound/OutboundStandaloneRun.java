package com.opuscapita.peppol.outbound;

import no.difi.oxalis.api.outbound.TransmissionRequest;
import no.difi.oxalis.api.outbound.TransmissionResponse;
import no.difi.oxalis.outbound.OxalisOutboundComponent;
import no.difi.oxalis.outbound.transmission.TransmissionRequestBuilder;
import no.difi.vefa.peppol.common.model.Endpoint;
import no.difi.vefa.peppol.common.model.TransportProfile;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URI;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

//@Component    // enable annotation and run, it will do the job
public class OutboundStandaloneRun implements CommandLineRunner {

    private static final String filename = "/test-material/sample-file-to-peppol.xml";

    @Override
    public void run(String... args) throws Exception {
        OxalisOutboundComponent oxalis = new OxalisOutboundComponent();
        TransmissionRequestBuilder requestBuilder = oxalis.getTransmissionRequestBuilder();
        requestBuilder.setTransmissionBuilderOverride(true);
        requestBuilder = sendRequestToGivenUrl(requestBuilder, "http://localhost:3037/public/as2");

        File file = new File(getClass().getResource(filename).getFile());
        try (InputStream payload = new FileInputStream(file)) {
            requestBuilder.payLoad(payload);
        }

        TransmissionRequest request = requestBuilder.build();
        TransmissionResponse response = oxalis.getTransmitter().transmit(request);
        System.out.println(response.toString());
    }

    private TransmissionRequestBuilder sendRequestToGivenUrl(TransmissionRequestBuilder requestBuilder, String url) {
        X509Certificate certificate = null;
        try {
            CertificateFactory fact = CertificateFactory.getInstance("X.509");
            FileInputStream is = new FileInputStream("oxalis/oxalis.cer");
            certificate = (X509Certificate) fact.generateCertificate(is);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return requestBuilder.overrideAs2Endpoint(Endpoint.of(TransportProfile.AS2_1_0, URI.create(url), certificate));
    }
}

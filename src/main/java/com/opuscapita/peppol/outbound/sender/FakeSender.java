package com.opuscapita.peppol.outbound.sender;

import com.opuscapita.peppol.commons.container.ContainerMessage;
import no.difi.oxalis.api.outbound.TransmissionResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FakeSender implements Sender {

    private static final Logger logger = LoggerFactory.getLogger(FakeSender.class);

    @Override
    public TransmissionResponse send(ContainerMessage cm) throws Exception {
        SomeResponse.throwExceptionIfExpectedInFilename(cm);

        logger.info("FakeSender emulated sending, returning some transmission response");
        return new SomeResponse();
    }

}

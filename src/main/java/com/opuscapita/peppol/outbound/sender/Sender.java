package com.opuscapita.peppol.outbound.sender;

import com.opuscapita.peppol.commons.container.ContainerMessage;
import no.difi.oxalis.api.lang.OxalisContentException;
import no.difi.oxalis.api.lang.OxalisTransmissionException;
import no.difi.oxalis.api.outbound.TransmissionResponse;

import java.io.IOException;

@FunctionalInterface
public interface Sender {

    TransmissionResponse send(ContainerMessage cm) throws Exception;

}

package com.opuscapita.peppol.outbound.sender.business.a2a;

import com.opuscapita.peppol.commons.container.ContainerMessage;
import no.difi.oxalis.api.outbound.TransmissionResponse;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;

@FunctionalInterface
public interface IA2ASender {

    @Retryable(value = {Exception.class}, maxAttempts = 5, backoff = @Backoff(delay = 1000))
    TransmissionResponse retrySend(ContainerMessage cm) throws Exception;

}

package com.opuscapita.peppol.outbound.consumer;

import com.opuscapita.peppol.commons.container.ContainerMessage;
import com.opuscapita.peppol.commons.container.state.ProcessStep;
import com.opuscapita.peppol.commons.eventing.EventReporter;
import com.opuscapita.peppol.commons.queue.consume.ContainerMessageConsumer;
import com.opuscapita.peppol.outbound.errors.OutboundErrorHandler;
import com.opuscapita.peppol.outbound.sender.Sender;
import com.opuscapita.peppol.outbound.sender.SenderFactory;
import no.difi.oxalis.api.outbound.TransmissionResponse;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class OutboundMessageConsumer implements ContainerMessageConsumer {

    private static final Logger logger = LoggerFactory.getLogger(OutboundMessageConsumer.class);

    private EventReporter eventReporter;
    private SenderFactory senderFactory;
    private OutboundErrorHandler errorHandler;

    @Autowired
    public OutboundMessageConsumer(SenderFactory senderFactory, EventReporter eventReporter, OutboundErrorHandler errorHandler) {
        this.eventReporter = eventReporter;
        this.senderFactory = senderFactory;
        this.errorHandler = errorHandler;
    }

    @Override
    public void consume(@NotNull ContainerMessage cm) throws Exception {
        cm.setStep(ProcessStep.OUTBOUND);
        String destination = cm.getRoute().getDestination();
        cm.getHistory().addInfo("Received and started transmission");
        logger.info("Outbound received the message: " + cm.toKibana() + " destination: " + destination);

        if (StringUtils.isBlank(cm.getFileName())) {
            throw new IllegalArgumentException("File name is empty in received message: " + cm.toKibana());
        }

        try {
            Sender sender = senderFactory.getSender(cm, destination);

            cm.getHistory().addInfo("About to send file using: " + sender.getClass().getSimpleName());
            TransmissionResponse response = sender.send(cm);

            cm.setStep(ProcessStep.NETWORK);
            cm.getHistory().addInfo("Successfully delivered to " + destination);
            logger.info("The message " + cm.toKibana() + " successfully delivered to " + destination + " with transmission ID = " + response.getTransmissionIdentifier());
            logger.debug("MDN Receipt(s) for " + cm.getFileName() + " is = " + response.getReceipts().stream().map(r -> new String(r.getValue())).collect(Collectors.joining(", ")));

        } catch (Exception exception) {
            cm.getHistory().addInfo("Message delivery failed");
            logger.warn("Sending of the message " + cm.getFileName() + " failed with " + exception.getClass().getSimpleName());
            errorHandler.handle(cm, exception);
        }

        eventReporter.reportStatus(cm);
    }

}

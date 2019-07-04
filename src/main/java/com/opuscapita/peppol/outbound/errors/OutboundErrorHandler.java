package com.opuscapita.peppol.outbound.errors;

import com.opuscapita.peppol.commons.container.ContainerMessage;
import com.opuscapita.peppol.commons.container.state.Route;
import com.opuscapita.peppol.commons.eventing.TicketReporter;
import com.opuscapita.peppol.commons.queue.MessageQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

@Component
public class OutboundErrorHandler {

    private static final Logger logger = LoggerFactory.getLogger(OutboundErrorHandler.class);

    @Value("${peppol.outbound.queue.in.name}")
    private String queueIn;

    private final MessageQueue messageQueue;
    private final TicketReporter ticketReporter;
    private final OxalisErrorRecognizer errorRecognizer;

    @Autowired
    public OutboundErrorHandler(MessageQueue messageQueue, TicketReporter ticketReporter, OxalisErrorRecognizer errorRecognizer) {
        this.messageQueue = messageQueue;
        this.ticketReporter = ticketReporter;
        this.errorRecognizer = errorRecognizer;
    }

    public void handle(ContainerMessage cm, Exception exception) {
        OutboundError errorType = errorRecognizer.recognize(exception);

        if (errorType.isRetryable()) {
            Route route = cm.getRoute();
            if (route.incrementAndGetCurrent() <= route.getRetry()) {
                cm.getHistory().addInfo("Sent to outbound retry queue");
                logger.info("The message " + cm.getFileName() + " sent to retry queue");
                sendToRetry(cm, route.getDelay());
//                String retryQueue = String.format("%s:exchange=%s,x-delay=%d", queueIn, retryExchange, route.getDelay());
//                messageQueue.convertAndSend(retryQueue, cm);
                return;
            }

            logger.info("No (more) retries possible, reporting exception for file: " + cm.getFileName());
        } else {
            logger.info("Exception of type " + errorType + " registered as non-retriable, reporting exception for file: " + cm.getFileName());
        }

        String errorMessage = errorType + ": " + exception.getMessage();
        cm.getHistory().addSendingError(errorMessage);

        if (errorType.requiresTicketCreation()) {
            createSNCTicket(cm, exception, errorMessage);
        }
    }

    private void createSNCTicket(ContainerMessage cm, Throwable e, String errorMessage) {
        try {
            ticketReporter.reportWithContainerMessage(cm, e, errorMessage);
        } catch (Exception weird) {
            logger.error("Reporting to ServiceNow threw exception: ", weird);
        }
    }

    /* temporary solution, need a delayed queue */
    private void sendToRetry(ContainerMessage cm, int delay) {
        new Thread(() -> {
            try {
                Thread.sleep(delay);
                messageQueue.convertAndSend(queueIn, cm);
            } catch (IOException | TimeoutException | InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }
}
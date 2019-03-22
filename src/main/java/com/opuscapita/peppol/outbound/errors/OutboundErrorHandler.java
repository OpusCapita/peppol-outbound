package com.opuscapita.peppol.outbound.errors;

import com.opuscapita.peppol.commons.container.ContainerMessage;
import com.opuscapita.peppol.commons.eventing.TicketReporter;
import com.opuscapita.peppol.commons.queue.MessageQueue;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class OutboundErrorHandler {

    private static final Logger logger = LoggerFactory.getLogger(OutboundErrorHandler.class);

    @Value("${peppol.email-sender.queue.in.name}")
    private String emailSenderQueue;

    private final MessageQueue messageQueue;
    private final TicketReporter ticketReporter;
    private final OxalisErrorRecognizer errorRecognizer;

    @Autowired
    public OutboundErrorHandler(MessageQueue messageQueue, TicketReporter ticketReporter, OxalisErrorRecognizer errorRecognizer) {
        this.messageQueue = messageQueue;
        this.ticketReporter = ticketReporter;
        this.errorRecognizer = errorRecognizer;
    }

    public void handle(ContainerMessage cm, Exception exception) throws Exception {
        OutboundError errorType = errorRecognizer.recognize(exception);

        if (errorType.isRetryable()) {
            String retry = cm.popRoute();
            if (StringUtils.isNotBlank(retry)) {
                cm.getHistory().addInfo("Sent to outbound retry queue");
                logger.info("The message " + cm.getFileName() + " sent to retry queue");
                messageQueue.convertAndSend(retry, cm);
                return;
            }

            logger.info("No (more) retries possible, reporting exception for file: " + cm.getFileName());
        } else {
            logger.info("Exception of type " + errorType + " registered as non-retriable, reporting exception for file: " + cm.getFileName());
        }

        String errorMessage = errorType + ": " + exception.getMessage();
        cm.getHistory().addSendingError(errorMessage);

        if (errorType.requiresNotification()) {
            sendEmailNotification(cm, errorType);
        }
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

    private void sendEmailNotification(ContainerMessage cm, OutboundError errorType) {
        try {
            logger.info("Sending message to email notificator since error: " + errorType + " requires notification");
            messageQueue.convertAndSend(emailSenderQueue, cm);
        } catch (Exception weird) {
            logger.error("Reporting to email-notificator threw exception: ", weird);
        }
    }

}

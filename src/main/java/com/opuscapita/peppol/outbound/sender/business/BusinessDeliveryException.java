package com.opuscapita.peppol.outbound.sender.business;

import java.io.IOException;

public class BusinessDeliveryException extends IOException {

    public BusinessDeliveryException() {
        super();
    }

    public BusinessDeliveryException(String message) {
        super(message);
    }

    public BusinessDeliveryException(String message, Throwable cause) {
        super(message, cause);
    }

    public BusinessDeliveryException(Throwable cause) {
        super(cause);
    }
}

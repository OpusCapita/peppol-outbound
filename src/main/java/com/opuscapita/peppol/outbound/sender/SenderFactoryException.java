package com.opuscapita.peppol.outbound.sender;

import java.io.IOException;

public class SenderFactoryException extends IOException {

    public SenderFactoryException() {
        super();
    }

    public SenderFactoryException(String message) {
        super(message);
    }

    public SenderFactoryException(String message, Throwable cause) {
        super(message, cause);
    }

    public SenderFactoryException(Throwable cause) {
        super(cause);
    }
}

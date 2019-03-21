package com.opuscapita.peppol.outbound.errors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class OxalisErrorRecognizer {

    private final static Logger logger = LoggerFactory.getLogger(OxalisErrorRecognizer.class);

    private final OxalisErrorConfiguration errorConfiguration;

    @Autowired
    public OxalisErrorRecognizer(OxalisErrorConfiguration errorConfiguration) {
        this.errorConfiguration = errorConfiguration;
    }

    public OutboundError recognize(Throwable exception) {
        if (exception.getMessage() == null) {
            return OutboundError.OTHER_ERROR;
        }
        return recognize(exception.getMessage());
    }

    private OutboundError recognize(String message) {
        if (errorConfiguration.getList() != null) {
            for (OxalisErrorConfiguration.OxalisError known : errorConfiguration.getList()) {
                logger.debug("Trying " + known.getMask() + " as " + known.getType());
                if (StringUtils.isNotBlank(known.getMask())) {
                    if (message.replaceAll("\n", " ").replaceAll("\r", " ").matches(known.getMask())) {
                        logger.debug("Exception message '" + StringUtils.substring(message, 0, 64) + "...' recognized as " + known.getType());
                        return known.getType();
                    }
                }
            }
        }

        logger.warn("Failed to recognize error message: " + StringUtils.substring(message, 0, 64));
        return OutboundError.OTHER_ERROR;
    }

}

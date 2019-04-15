package com.opuscapita.peppol.outbound.sender;

import com.opuscapita.peppol.commons.container.ContainerMessage;
import com.opuscapita.peppol.commons.storage.Storage;
import no.difi.oxalis.outbound.OxalisOutboundComponent;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class SenderFactory {

    private final static Logger logger = LoggerFactory.getLogger(SenderFactory.class);

    @Value("${sending-enabled:false}")
    private boolean sendingEnabled;

    @Value("${test-recipient:}")
    private String testRecipient;

    private Sender fakeSender;
    private Sender testSender;
    private Sender realSender;

    private Storage storage;
    private OxalisOutboundComponent oxalis;

    @Autowired
    public SenderFactory(Storage storage) {
        this.storage = storage;
        this.oxalis = new OxalisOutboundComponent();
    }

    @PostConstruct
    public void initSenders() {
        this.fakeSender = new FakeSender();
        this.realSender = new RealSender(storage, oxalis);
        this.testSender = new TestSender(testRecipient, storage, oxalis);
    }

    public Sender getSender(ContainerMessage cm) {
        String destination = cm.getRoute().getDestination();

        if (!sendingEnabled) {
            logger.debug("Selected to send via FAKE sender for file: " + cm.getFileName());
            return fakeSender;
        }

        if ("a2a".equals(destination)) {
            // return a2a sender
            return null;
        }
        if ("xib".equals(destination)) {
            // return xib sender
            return null;
        }

        if (StringUtils.isNotBlank(testRecipient)) {
            logger.debug("Selected to send via TEST sender for file: " + cm.getFileName());
            return testSender;
        }

        logger.debug("Selected to send via REAL sender for file: " + cm.getFileName());
        return realSender;
    }

}

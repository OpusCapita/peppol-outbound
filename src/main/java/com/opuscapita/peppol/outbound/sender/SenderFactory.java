package com.opuscapita.peppol.outbound.sender;

import com.opuscapita.peppol.commons.container.ContainerMessage;
import com.opuscapita.peppol.commons.storage.Storage;
import com.opuscapita.peppol.outbound.sender.business.A2ASender;
import com.opuscapita.peppol.outbound.sender.business.SiriusSender;
import com.opuscapita.peppol.outbound.sender.business.XIBSender;
import no.difi.oxalis.outbound.OxalisOutboundComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
@RefreshScope
public class SenderFactory {

    private final static Logger logger = LoggerFactory.getLogger(SenderFactory.class);

    @Value("${fake-sending:''}")
    private String fakeConfig;

    @Value("${stop-sending:''}")
    private String stopConfig;

    private Sender fakeSender;
    private Sender realSender;

    private A2ASender a2aSender;
    private XIBSender xibSender;
    private SiriusSender siriusSender;

    private Storage storage;
    private OxalisOutboundComponent oxalis;

    @Autowired
    public SenderFactory(Storage storage, A2ASender a2aSender, XIBSender xibSender, SiriusSender siriusSender) {
        this.storage = storage;
        this.a2aSender = a2aSender;
        this.xibSender = xibSender;
        this.siriusSender = siriusSender;
        this.oxalis = new OxalisOutboundComponent();
    }

    @PostConstruct
    public void initSenders() {
        this.fakeSender = new FakeSender();
        this.realSender = new RealSender(storage, oxalis);
    }

    public Sender getSender(ContainerMessage cm, String destination) throws Exception {
        if (stopConfig.contains(destination)) {
            throw new SenderFactoryException("Stopped delivery to " + destination + " as requested");
        }

        if (fakeConfig.contains(destination)) {
            logger.info("Selected to send via FAKE sender for file: " + cm.getFileName());
            return fakeSender;
        }

        if ("xib".equals(destination)) {
            logger.info("Selected to send via XIB sender for file: " + cm.getFileName());
            return xibSender;
        }

        if ("a2a".equals(destination)) {
            logger.info("Selected to send via A2A sender for file: " + cm.getFileName());
            return a2aSender;
        }

        if ("sirius".equals(destination)) {
            logger.info("Selected to send via Sirius sender for file: " + cm.getFileName());
            return siriusSender;
        }

        if ("network".equals(destination)) {
            logger.info("Selected to send via REAL sender for file: " + cm.getFileName());
            return realSender;
        }

        throw new RuntimeException("This poor lonely document has nowhere to go!");
    }

}

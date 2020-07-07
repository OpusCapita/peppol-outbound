package com.opuscapita.peppol.outbound.sender;

import com.opuscapita.peppol.commons.container.ContainerMessage;
import com.opuscapita.peppol.commons.container.state.Source;
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
    private Sender networkSender;
    private A2ASender a2aSender;
    private XIBSender xibSender;
    private SiriusSender siriusSender;
    private NetworkConfiguration networkConfiguration;

    private Storage storage;
    private OxalisOutboundComponent oxalis;

    @Autowired
    public SenderFactory(Storage storage, A2ASender a2aSender, XIBSender xibSender, SiriusSender siriusSender,
                         NetworkConfiguration networkConfiguration) {
        this.storage = storage;
        this.a2aSender = a2aSender;
        this.xibSender = xibSender;
        this.siriusSender = siriusSender;
        this.networkConfiguration = networkConfiguration;
        this.oxalis = new OxalisOutboundComponent();
    }

    @PostConstruct
    public void initSenders() {
        this.fakeSender = new FakeSender();
        this.networkSender = new NetworkSender(storage, networkConfiguration, oxalis);
    }

    public Sender getSender(ContainerMessage cm, Source destination) throws Exception {
        if (stopConfig.contains(destination.name())) {
            throw new SenderFactoryException("Stopped delivery to " + destination + " as requested");
        }

        if (fakeConfig.contains(destination.name())) {
            logger.info("Selected to send via FAKE sender for file: " + cm.getFileName());
            return fakeSender;
        }

        Sender sender;
        switch (destination) {
            case NETWORK:
                sender = networkSender;
                break;
            case SIRIUS:
                sender = siriusSender;
                break;
            case XIB:
                sender = xibSender;
                break;
            case A2A:
                sender = a2aSender;
                break;
            default:
                throw new RuntimeException("This poor lonely document has nowhere to go!");
        }

        if (cm.getRoute().getCurrent() == -1) {
            cm.getRoute().initiate(sender.getRetryCount(), sender.getRetryDelay());
        }

        return sender;
    }

}

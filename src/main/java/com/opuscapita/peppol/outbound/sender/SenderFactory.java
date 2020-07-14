package com.opuscapita.peppol.outbound.sender;

import com.opuscapita.peppol.commons.container.ContainerMessage;
import com.opuscapita.peppol.commons.container.state.Source;
import com.opuscapita.peppol.commons.storage.Storage;
import com.opuscapita.peppol.outbound.sender.business.a2a.A2ASender;
import com.opuscapita.peppol.outbound.sender.business.sirius.SiriusSender;
import com.opuscapita.peppol.outbound.sender.business.xib.XIBSender;
import com.opuscapita.peppol.outbound.sender.fake.FakeSender;
import com.opuscapita.peppol.outbound.sender.network.NetworkConfiguration;
import com.opuscapita.peppol.outbound.sender.network.NetworkSender;
import no.difi.oxalis.outbound.OxalisOutboundComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class SenderFactory {

    private final static Logger logger = LoggerFactory.getLogger(SenderFactory.class);

    private Storage storage;
    private Sender fakeSender;
    private Sender networkSender;
    private A2ASender a2aSender;
    private XIBSender xibSender;
    private SiriusSender siriusSender;
    private NetworkConfiguration configuration;

    @Autowired
    public SenderFactory(Storage storage, A2ASender a2aSender, XIBSender xibSender, SiriusSender siriusSender,
                         NetworkConfiguration networkConfiguration) {
        this.storage = storage;
        this.a2aSender = a2aSender;
        this.xibSender = xibSender;
        this.siriusSender = siriusSender;
        this.configuration = networkConfiguration;
    }

    @PostConstruct
    public void initSenders() {
        this.fakeSender = new FakeSender();
        this.networkSender = new NetworkSender(storage, configuration, new OxalisOutboundComponent());
    }

    public Sender getSender(ContainerMessage cm, Source destination) throws Exception {
        if (configuration.getStopConfig().contains(destination.name())) {
            throw new SenderFactoryException("Stopped delivery to " + destination + " as requested");
        }

        if (configuration.getFakeConfig().contains(destination.name())) {
            logger.info("Selected to send via FAKE sender for file: " + cm.getFileName());
            return fakeSender;
        }

        switch (destination) {
            case NETWORK:
                return networkSender;
            case SIRIUS:
                return siriusSender;
            case XIB:
                return xibSender;
            case A2A:
                return a2aSender;
        }

        throw new RuntimeException("This poor lonely document has nowhere to go!");
    }

}

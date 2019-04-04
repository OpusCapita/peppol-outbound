package com.opuscapita.peppol.outbound;

import com.opuscapita.peppol.commons.container.ContainerMessage;
import com.opuscapita.peppol.commons.container.metadata.MetadataExtractor;
import com.opuscapita.peppol.commons.container.metadata.PeppolMessageMetadata;
import com.opuscapita.peppol.commons.container.state.Endpoint;
import com.opuscapita.peppol.commons.container.state.ProcessFlow;
import com.opuscapita.peppol.commons.container.state.ProcessStep;
import com.opuscapita.peppol.commons.container.state.Source;
import com.opuscapita.peppol.commons.queue.consume.ContainerMessageConsumer;
import com.opuscapita.peppol.commons.storage.Storage;
import com.opuscapita.peppol.commons.storage.StorageException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

//@Component    // enable annotation and run, it will do the job
public class OutboundStandaloneRun implements CommandLineRunner {

    private static final String filename = "C:\\artifacts\\test\\peppol-bis.xml";

    private Storage storage;
    private MetadataExtractor extractor;
    private ContainerMessageConsumer consumer;

    @Autowired
    public OutboundStandaloneRun(Storage storage, MetadataExtractor extractor, ContainerMessageConsumer consumer) {
        this.storage = storage;
        this.extractor = extractor;
        this.consumer = consumer;
    }

    @Override
    public void run(String... args) throws Exception {
        ContainerMessage cm = initContainerMessage();
        consumer.consume(cm);
    }

    private ContainerMessage initContainerMessage() throws Exception {
        File file = new File(filename);
        InputStream inputStream = new FileInputStream(file);

        Endpoint endpoint = new Endpoint(Source.UNKNOWN, ProcessStep.OUTBOUND);
        ContainerMessage cm = new ContainerMessage(file.getName(), endpoint);
        cm.getHistory().addInfo("Local received and stored");

        PeppolMessageMetadata metadata = extractor.extract(inputStream);
        cm.setMetadata(metadata);

        String path = storeFile(cm, inputStream);
        cm.setFileName(path);

        return cm;
    }

    private String storeFile(ContainerMessage cm, InputStream inputStream) throws StorageException {
        return storage.putToPermanent(inputStream, cm.getFileName(), cm.getMetadata().getSenderId(), cm.getMetadata().getRecipientId());
    }

}

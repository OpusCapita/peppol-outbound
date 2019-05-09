package com.opuscapita.peppol.outbound;

import com.opuscapita.peppol.commons.container.ContainerMessage;
import com.opuscapita.peppol.commons.container.metadata.ContainerMessageMetadata;
import com.opuscapita.peppol.commons.container.metadata.MetadataExtractor;
import com.opuscapita.peppol.commons.container.state.ProcessStep;
import com.opuscapita.peppol.commons.container.state.Source;
import com.opuscapita.peppol.commons.queue.consume.ContainerMessageConsumer;
import com.opuscapita.peppol.commons.storage.Storage;
import com.opuscapita.peppol.commons.storage.StorageException;
import com.opuscapita.peppol.commons.storage.StorageUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

//@Component    // enable annotation and run, it will do the job
public class OutboundStandaloneRun implements CommandLineRunner {

    private static final String filename = "C:\\artifacts\\test\\peppol-bis.xml";

    @Value("${peppol.storage.blob.hot:hot}")
    private String hotFolder;

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

        ContainerMessage cm = new ContainerMessage(file.getName(), Source.A2A, ProcessStep.OUTBOUND);
        cm.getHistory().addInfo("Local received and stored");

        ContainerMessageMetadata metadata = extractor.extract(inputStream);
        cm.setMetadata(metadata);

        String path = storeFile(cm, inputStream);
        cm.setFileName(path);

        return cm;
    }

    private String storeFile(ContainerMessage cm, InputStream inputStream) throws StorageException {
        String path = hotFolder + StorageUtils.FILE_SEPARATOR + cm.getSource().name().toLowerCase();
        path = StorageUtils.createDailyPath(path, "");
        return storage.put(inputStream, path, cm.getFileName());
    }
}

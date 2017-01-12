package com.kincaidweb.nlp.uima.leo;

import avro.shaded.com.google.common.base.Throwables;
import com.beust.jcommander.IStringConverter;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.kincaidweb.nlp.uima.readers.readers.AvroFileCollectionReader;
import gov.va.vinci.leo.Client;
import gov.va.vinci.leo.cr.BaseLeoCollectionReader;
import gov.va.vinci.leo.listener.SimpleXmiListener;
import org.apache.uima.aae.client.UimaAsBaseCallbackListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * A Leo client which reads documents from an Avro file and writes the resulting CAS objects to XMI files.
 */
public class AvroToXmiClient {
    private static final Logger logger = LoggerFactory.getLogger(AvroToXmiClient.class);

    @Parameter(names = { "-b", "--brokerUrl"}, description = "The URL of the broker application", converter = URIConverter.class)
    private URI broker = URI.create("tcp://localhost:61616");

    @Parameter(names = { "-e", "--endpoint"}, description = "The name of the broker endpoint.")
    private String endpoint = "leoQueueName";

    @Parameter(names = {"--casPoolSize"}, description = "The CAS pool size. Make sure this is big enough. It should match at least the number of instances of the service the client is connecting to.")
    private Integer casPoolSize = 5;

    @Parameter(names = {"--ccTimeout"}, description = "The CC timeout value.")
    private Integer ccTimeout = 1000;

    @Parameter(names = {"-i", "--inputFile"}, description = "The path to the input file or files.", required = true)
    private String inputFile;

    @Parameter(names = {"-o", "--outputDir"}, description = "The output directory for the annotated XMI files.")
    private String outputDir;

    @Parameter(names = {"--textFieldName"}, description = "The name of the field in the Avro file containing the text.")
    private String textFieldName = "mnText";

    @Parameter(names = {"--idFieldName"}, description = "The name of the field in the Avro file containing the document id.")
    private String idFieldName = "mnRowKey";

    @Parameter(names = {"-h", "--help"}, help = true)
    private boolean help;

    public void run() {
        BaseLeoCollectionReader reader = new AvroFileCollectionReader(textFieldName, idFieldName, inputFile);

        Path outputPath = null;

        if (outputDir == null) {
            try {
                outputPath = Files.createTempDirectory("tempLeoXmiOutput");
            } catch (IOException e) {
                logger.error("Error creating a temporary directory for the output!", e);
                throw Throwables.propagate(e);
            }
        } else {
            outputPath = Paths.get(outputDir);
        }

        UimaAsBaseCallbackListener listener = new SimpleXmiListener(outputPath.toFile());

        Client leoClient = new Client();
        leoClient.setLeoCollectionReader(reader);
        leoClient.addUABListener(listener);
        leoClient.setEndpoint(endpoint);
        leoClient.setBrokerURL(broker.toString());
        leoClient.setCasPoolSize(casPoolSize);
        leoClient.setCCTimeout(ccTimeout);


        logger.info("Starting to read files from {} and submit to the service. Results will be written to {}.", inputFile, outputPath);
        try {
            leoClient.run();
        } catch (Exception e) {
            logger.error("Exception caught while running the LEO client! {}", e);
            throw Throwables.propagate(e);
        }
        logger.info("Client finished reading documents and receiving annotations.");
    }

    public static void main(String[] args) {
        AvroToXmiClient avroToXmiClient = new AvroToXmiClient();
        JCommander jCommander = new JCommander(avroToXmiClient, args);
        jCommander.setProgramName(AvroToXmiClient.class.getSimpleName());

        if (avroToXmiClient.help) {
            jCommander.usage();
            System.exit(0);
        }

        if (avroToXmiClient.inputFile == null) {
            System.err.println("-inputFile parameter must be provided!");
            jCommander.usage();
            System.exit(1);
        }

        avroToXmiClient.run();
    }

    public static final class URIConverter implements IStringConverter<URI> {
        @Override
        public URI convert(String value) {
            return URI.create(value);
        }
    }
}

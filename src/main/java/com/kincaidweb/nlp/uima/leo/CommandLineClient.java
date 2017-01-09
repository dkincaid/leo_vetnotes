package com.kincaidweb.nlp.uima.leo;

import avro.shaded.com.google.common.base.Throwables;
import com.beust.jcommander.IStringConverter;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.kincaidweb.nlp.uima.readers.AvroFileCollectionReader;
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
 * Command line version of the client program
 */
public class CommandLineClient {
    private static final Logger logger = LoggerFactory.getLogger(CommandLineClient.class);

    @Parameter(names = { "-brokerUrl"}, description = "The URL of the broker application", converter = URIConverter.class)
    private URI broker = URI.create("tcp://localhost:61616");

    @Parameter(names = { "-endpoint"}, description = "The name of the endpoint.")
    private String endpoint = "leoQueueName";

    @Parameter(names = {"-casPoolSize"}, description = "The CAS pool size.")
    private Integer casPoolSize = 4;

    @Parameter(names = {"-ccTimeout"}, description = "The CC timeout value.")
    private Integer ccTimeout = 1000;

    @Parameter(names = {"-inputFile"}, description = "The path to the input file or files.")
    private String inputFile;

    @Parameter(names = {"-outputDir"}, description = "The output directory for the annotated XMI files.")
    private String outputDir;

    @Parameter(names = {"-textFieldName"}, description = "The name of the field in the Avro file containing the text.")
    private String textFieldName = "mnText";

    @Parameter(names = {"-idFieldName"}, description = "The name of the field in the Avro file containing the document id.")
    private String idFieldName = "mnRowKey";


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
        CommandLineClient commandLineClient = new CommandLineClient();
        new JCommander(commandLineClient, args);
        commandLineClient.run();
    }

    public static final class URIConverter implements IStringConverter<URI> {
        @Override
        public URI convert(String value) {
            return URI.create(value);
        }
    }
}

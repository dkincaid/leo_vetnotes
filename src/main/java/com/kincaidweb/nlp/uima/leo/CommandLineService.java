package com.kincaidweb.nlp.uima.leo;

import avro.shaded.com.google.common.base.Throwables;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.kincaidweb.nlp.uima.leo.ae.FirstTestAnnotator;
import gov.va.vinci.leo.Service;
import gov.va.vinci.leo.descriptors.LeoAEDescriptor;
import gov.va.vinci.leo.descriptors.LeoTypeSystemDescription;
import gov.va.vinci.leo.descriptors.TypeSystemFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;

/**
 * Created by davek on 1/1/17.
 */
public class CommandLineService {
    private static final Logger logger = LoggerFactory.getLogger(CommandLineService.class);

    @Parameter(names = {"-brokerUrl"}, description = "The URL of the broker service.")
    private URI brokerUri = URI.create("tcp://localhost:61616");

    @Parameter(names = {"-endpoint"}, description = "The endpoint name in the broker service to use.")
    private String endpoint = "leoQueueName";

    @Parameter(names = {"-casPoolSize"}, description = "The CAS pool size.")
    private Integer casPoolSize = 4;

    @Parameter(names = {"-ccTimeout"}, description = "The CC timeout value.")
    private Integer ccTimeout = 1000;



    public void run() {
        Service service = null;

        try {
            service = new Service();

            service.setBrokerURL(brokerUri.toString());
            service.setEndpoint(endpoint);
            service.setCasPoolSize(casPoolSize);
            service.setCCTimeout(ccTimeout);

            LeoAEDescriptor pipeline = new LeoAEDescriptor();
            pipeline.addDelegate(new FirstTestAnnotator().getLeoAEDescriptor());

            pipeline.setIsAsync(false);
            pipeline.setNumberOfInstances(5);

            service.deploy(
                    new LeoTypeSystemDescription(TypeSystemFactory.generateTypeSystemDescription(
                            "/home/davek/src/other-peoples/ctakes/ctakes-type-system/target/classes/org/apache/ctakes/typesystem/types/TypeSystem.xml", false)),
                    new FirstTestAnnotator());

            System.out.println("\r\nDeployment: " + service.getDeploymentDescriptorFile());
            System.out.println("Aggregate: " + service.getAggregateDescriptorFile());

            System.out.println("Service running, press enter in this console to stop.");
            System.in.read();

            System.exit(0);

        } catch (Exception e) {
            logger.error("Exception while creating the Leo service! {}", e);
            throw Throwables.propagate(e);
        }
    }

    public static void main(String[] args) {
        CommandLineService commandLineService = new CommandLineService();
        new JCommander(commandLineService, args);
        commandLineService.run();
    }
}

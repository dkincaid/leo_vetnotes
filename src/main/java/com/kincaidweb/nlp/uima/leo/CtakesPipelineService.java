package com.kincaidweb.nlp.uima.leo;

import avro.shaded.com.google.common.base.Throwables;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import gov.va.vinci.leo.Service;
import gov.va.vinci.leo.descriptors.LeoAEDescriptor;
import gov.va.vinci.leo.descriptors.LeoTypeSystemDescription;
import gov.va.vinci.leo.descriptors.TypeSystemFactory;
import org.apache.ctakes.assertion.medfacts.cleartk.*;
import org.apache.ctakes.chunker.ae.Chunker;
import org.apache.ctakes.chunker.ae.adjuster.ChunkAdjuster;
import org.apache.ctakes.contexttokenizer.ae.ContextDependentTokenizerAnnotator;
import org.apache.ctakes.core.ae.SentenceDetector;
import org.apache.ctakes.core.ae.SimpleSegmentAnnotator;
import org.apache.ctakes.core.ae.TokenizerAnnotatorPTB;
import org.apache.ctakes.dictionary.lookup2.ae.DefaultJCasTermAnnotator;
import org.apache.ctakes.lvg.ae.LvgAnnotator;
import org.apache.ctakes.postagger.POSTagger;
import org.apache.uima.resource.ResourceInitializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;

/**
 * Leo UIMA-AS service which runs a cTAKES pipeline.
 */
public class CtakesPipelineService {
    private static final Logger logger = LoggerFactory.getLogger(CtakesPipelineService.class);

    @Parameter(names = {"-brokerUrl"}, description = "The URL of the broker service.")
    private URI brokerUri = URI.create("tcp://localhost:61616");

    @Parameter(names = {"-endpoint"}, description = "The endpoint name in the broker service to use.")
    private String endpoint = "leoQueueName";

    @Parameter(names = {"-casPoolSize"}, description = "The CAS pool size.")
    private Integer casPoolSize = 5;

    @Parameter(names = {"-ccTimeout"}, description = "The CC timeout value.")
    private Integer ccTimeout = 1000;

    @Parameter(names = {"-numInstances"}, description = "The number of instance to run.")
    private Integer numInstances = 5;

    @Parameter(names = {"-dictionaryDescriptor"}, description = "The location of the dictionary descriptor XML file.")
    private String dictionaryDescriptor;


    public void run() {
        Service service = null;

        try {
            service = new Service();

            service.setBrokerURL(brokerUri.toString());
            service.setEndpoint(endpoint);
            service.setCasPoolSize(casPoolSize);
            service.setCCTimeout(ccTimeout);

            LeoTypeSystemDescription typeSystem = new LeoTypeSystemDescription(TypeSystemFactory.generateTypeSystemDescription(
                    "org/apache/ctakes/typesystem/types/TypeSystem", true));

            LeoAEDescriptor pipeline = getPipelineDescriptor(typeSystem);

            service.deploy(pipeline);

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

    private LeoAEDescriptor getPipelineDescriptor(LeoTypeSystemDescription typeSystem) {
        LeoAEDescriptor pipeline = new LeoAEDescriptor();
        pipeline.setTypeSystemDescription(typeSystem);

        LeoAEDescriptor simpleSegmentAnnotator = new LeoAEDescriptor(
                "SimpleSegmentAnnotator", SimpleSegmentAnnotator.class.getName());
        simpleSegmentAnnotator.setTypeSystemDescription(typeSystem);

        LeoAEDescriptor sentenceDetectorDescriptor = new LeoAEDescriptor(
                "SentenceDetector", SentenceDetector.class.getName());
        sentenceDetectorDescriptor.setTypeSystemDescription(typeSystem);

        LeoAEDescriptor tokenizerDescriptor = new LeoAEDescriptor(
                "Tokenizer", TokenizerAnnotatorPTB.class.getName());
        tokenizerDescriptor.setTypeSystemDescription(typeSystem);

        LeoAEDescriptor lvgAnnotator = new LeoAEDescriptor("LvgAnnotator", LvgAnnotator.class.getName());
        lvgAnnotator.setTypeSystemDescription(typeSystem);

        LeoAEDescriptor contextDependentTokenizer = new LeoAEDescriptor(
                "ContextDependentTokenizer", ContextDependentTokenizerAnnotator.class.getName());
        contextDependentTokenizer.setTypeSystemDescription(typeSystem);

        LeoAEDescriptor posTagger = new LeoAEDescriptor(
                "POSTagger", POSTagger.class.getName());
        posTagger.setTypeSystemDescription(typeSystem);

        LeoAEDescriptor chunker = new LeoAEDescriptor(
                "Chunker", Chunker.class.getName());
        chunker.setTypeSystemDescription(typeSystem);

        LeoAEDescriptor adjustNounPhraseToIncludeFollowingNP = null;
        try {
            adjustNounPhraseToIncludeFollowingNP = new LeoAEDescriptor(
                    ChunkAdjuster.createAnnotatorDescription(new String[]{"NP","NP"}, 1));
            adjustNounPhraseToIncludeFollowingNP.setTypeSystemDescription(typeSystem);
        } catch (ResourceInitializationException e) {
            logger.error("Error initializing ChunkAdjuster! {}", e);
            throw Throwables.propagate(e);
        }

        LeoAEDescriptor adjustNounPhraseToIncludeFollowingPPNP = null;
        try {
            adjustNounPhraseToIncludeFollowingPPNP = new LeoAEDescriptor(
                    ChunkAdjuster.createAnnotatorDescription(new String[]{"NP", "PP","NP"}, 2));
            adjustNounPhraseToIncludeFollowingPPNP.setTypeSystemDescription(typeSystem);
        } catch (ResourceInitializationException e) {
            logger.error("Error initializing ChunkAdjuster! {}", e);
            throw Throwables.propagate(e);
        }

        LeoAEDescriptor dictionaryLookup = null;
        try {
            if (dictionaryDescriptor == null) {
                dictionaryLookup = new LeoAEDescriptor(DefaultJCasTermAnnotator.createAnnotatorDescription());
            } else {
                dictionaryLookup = new LeoAEDescriptor(DefaultJCasTermAnnotator.createAnnotatorDescription(dictionaryDescriptor));
            }
        } catch (ResourceInitializationException e) {
            logger.error("Error initializing dictionary lookup. {}", e);
            throw Throwables.propagate(e);
        }

        LeoAEDescriptor genericAssertion = null;
        LeoAEDescriptor historyAssertion = null;
        LeoAEDescriptor polarityAssertion = null;
        LeoAEDescriptor subjectAssertion = null;
        LeoAEDescriptor uncertaintyAssertion = null;

        try {
            genericAssertion = new LeoAEDescriptor(GenericCleartkAnalysisEngine.createAnnotatorDescription());
            genericAssertion.setTypeSystemDescription(typeSystem);

            historyAssertion = new LeoAEDescriptor(HistoryCleartkAnalysisEngine.createAnnotatorDescription());
            historyAssertion.setTypeSystemDescription(typeSystem);

            polarityAssertion = new LeoAEDescriptor(PolarityCleartkAnalysisEngine.createAnnotatorDescription());
            polarityAssertion.setTypeSystemDescription(typeSystem);

            subjectAssertion = new LeoAEDescriptor(SubjectCleartkAnalysisEngine.createAnnotatorDescription());
            subjectAssertion.setTypeSystemDescription(typeSystem);

            uncertaintyAssertion = new LeoAEDescriptor(UncertaintyCleartkAnalysisEngine.createAnnotatorDescription());
            uncertaintyAssertion.setTypeSystemDescription(typeSystem);
        } catch (ResourceInitializationException e) {
            logger.error("Error initializing assertion module! {}", e);
            throw Throwables.propagate(e);
        }

        pipeline.addDelegate(simpleSegmentAnnotator);
        pipeline.addDelegate(sentenceDetectorDescriptor);
        pipeline.addDelegate(tokenizerDescriptor);
        //pipeline.addDelegate(lvgAnnotator);
        pipeline.addDelegate(contextDependentTokenizer);
        pipeline.addDelegate(posTagger);
        pipeline.addDelegate(chunker);
        pipeline.addDelegate(adjustNounPhraseToIncludeFollowingNP);
        pipeline.addDelegate(adjustNounPhraseToIncludeFollowingPPNP);
        pipeline.addDelegate(dictionaryLookup);
        pipeline.addDelegate(genericAssertion);
        pipeline.addDelegate(historyAssertion);
        pipeline.addDelegate(polarityAssertion);
        pipeline.addDelegate(subjectAssertion);
        pipeline.addDelegate(uncertaintyAssertion);

        pipeline.setIsAsync(false);
        pipeline.setNumberOfInstances(numInstances);
        return pipeline;
    }

    public static void main(String[] args) {
        CtakesPipelineService ctakesPipelineService = new CtakesPipelineService();
        new JCommander(ctakesPipelineService, args);
        ctakesPipelineService.run();
    }
}

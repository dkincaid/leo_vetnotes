package com.kincaidweb.nlp.uima.leo.ae;

import gov.va.vinci.leo.ae.LeoBaseAnnotator;
import org.apache.ctakes.typesystem.type.textspan.Sentence;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by davek on 1/3/17.
 */
public class FirstTestAnnotator extends LeoBaseAnnotator {
    private static final Logger logger = LoggerFactory.getLogger(FirstTestAnnotator.class);

    @Override
    public void annotate(JCas jCas) throws AnalysisEngineProcessException {
        String document = jCas.getDocumentText();

        int patientIndex = document.indexOf("patient");
        Sentence sentence = new Sentence(jCas, patientIndex, patientIndex + 4);
        jCas.addFsToIndexes(sentence);
    }
}

package com.kincaidweb.nlp.uima.readers;

import org.apache.uima.UIMAException;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.jcas.JCas;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.testng.Assert.assertEquals;

/**
 * Created by davek on 1/1/17.
 */
public class AvroReaderTest {


    private String inputDirectory;


    @BeforeClass
    public void setup() {
        inputDirectory = getClass().getResource("/input-test-files/avro").getFile();
    }

    @Test
    public void testDirectoryRead() throws IOException, UIMAException {
        AvroFileCollectionReader fileCollectionReader = new AvroFileCollectionReader("mnText", inputDirectory);

        int recordCount = 0;
        JCas jCas = JCasFactory.createJCas();
        while (fileCollectionReader.hasNext()) {
            jCas.reset();
            fileCollectionReader.getNext(jCas.getCas());
            recordCount++;
        }

        assertEquals(recordCount, 30);
    }

    @Test
    public void testSingleRecordRead() throws IOException, UIMAException {
        Path path = Paths.get(inputDirectory, "sample-mednotes.avro");
        AvroFileCollectionReader fileCollectionReader = new AvroFileCollectionReader("mnText", path.toString());

        int recordCount = 0;
        JCas jCas = JCasFactory.createJCas();
        while (fileCollectionReader.hasNext()) {
            jCas.reset();
            fileCollectionReader.getNext(jCas.getCas());
            recordCount++;
        }

        assertEquals(recordCount, 15);
    }
}

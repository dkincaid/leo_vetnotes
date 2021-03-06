package readers;

import com.kincaidweb.nlp.uima.readers.readers.AvroFileCollectionReader;
import org.apache.uima.UIMAException;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.jcas.JCas;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

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
        AvroFileCollectionReader fileCollectionReader =
                (AvroFileCollectionReader) new AvroFileCollectionReader("mnText", inputDirectory)
                        .produceCollectionReader();

        int recordCount = 0;
        JCas jCas = JCasFactory.createJCas();
        while (fileCollectionReader.hasNext()) {
            jCas.reset();
            fileCollectionReader.getNext(jCas.getCas());
            recordCount++;
        }

        Assert.assertEquals(recordCount, 30);
    }

    @Test
    public void testSingleRecordRead() throws IOException, UIMAException {
        Path path = Paths.get(inputDirectory, "sample-mednotes.avro");
        AvroFileCollectionReader fileCollectionReader =
                (AvroFileCollectionReader) new AvroFileCollectionReader("mnText", path.toString())
                        .produceCollectionReader();

        int recordCount = 0;
        JCas jCas = JCasFactory.createJCas();
        while (fileCollectionReader.hasNext()) {
            jCas.reset();
            fileCollectionReader.getNext(jCas.getCas());
            recordCount++;
        }

        Assert.assertEquals(recordCount, 15);
    }
}

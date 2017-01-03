package com.kincaidweb.nlp.uima.readers;

import avro.shaded.com.google.common.base.Throwables;
import gov.va.vinci.leo.cr.BaseLeoCollectionReader;
import gov.va.vinci.leo.descriptors.LeoConfigurationParameter;
import org.apache.avro.file.DataFileReader;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.DatumReader;
import org.apache.ctakes.typesystem.type.structured.DocumentID;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.util.Progress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * UIMA Collection Reader that reads Avro files. It can read either a single file or multiple files from a directory.
 */
public class AvroFileCollectionReader extends BaseLeoCollectionReader {
    private static final Logger logger = LoggerFactory.getLogger(AvroFileCollectionReader.class);

    private Queue<Path> paths;
    private DataFileReader<GenericRecord> dataFileReader;

    @LeoConfigurationParameter(mandatory = true)
    private String textFieldName;

    @LeoConfigurationParameter
    private String idFieldName;

    @LeoConfigurationParameter(mandatory = true)
    private String avroDir;

    public AvroFileCollectionReader() {

    }

    /**
     *
     * Creates the reader with a document id field. Be sure to pass in the field names in the Avro file for the
     * textField and document id.
     */
    public AvroFileCollectionReader(String textFieldName, String idFieldName, String avroDir) {
        this.textFieldName = textFieldName;
        this.idFieldName = idFieldName;
        this.avroDir = avroDir;

        Path avroPath = Paths.get(avroDir);

        paths = new ConcurrentLinkedQueue<>();

        if (Files.isDirectory(avroPath)) {
            paths.addAll(getFiles(avroPath));
        } else {
            paths.add(avroPath);
        }

        dataFileReader = openReader(paths.remove());
    }

    /**
     * Creates the reader without a document id field. Be sure to pass in the field name in the Avro file for the
     * textField.
     */
    public AvroFileCollectionReader(String textFieldName, String avroDir) {
        this.textFieldName = textFieldName;
        this.idFieldName = null;
        this.avroDir = avroDir;

        Path avroPath = Paths.get(avroDir);

        paths = new ConcurrentLinkedQueue<>();

        if (Files.isDirectory(avroPath)) {
            paths.addAll(getFiles(avroPath));
        } else {
            paths.add(avroPath);
        }

        dataFileReader = openReader(paths.remove());
    }

    private DataFileReader<GenericRecord> openReader(Path path) {
        DatumReader<GenericRecord> datumReader = new GenericDatumReader<>();
        DataFileReader<GenericRecord> dfr;

        try {
            dfr = new DataFileReader<>(path.toFile(), datumReader);
        } catch (IOException e) {
            logger.error("IOException trying to read Avro file {}. {}", path, e);
            throw Throwables.propagate(e);
        }

        return dfr;
    }

    private List<Path> getFiles(Path directory) {
        List<Path> paths = new ArrayList<>();
        try {
            Files.newDirectoryStream(directory, path -> path.toString().endsWith(".avro"))
                    .forEach(paths::add);
        } catch (IOException e) {
            logger.error("Error while reading the entries in the directory {}! {}", directory, e);
            throw Throwables.propagate(e);
        }

        return paths;
    }

    public void getNext(CAS cas) throws CollectionException, IOException {
        GenericRecord nextRecord = dataFileReader.next();
        String text = String.valueOf(nextRecord.get(textFieldName));

        cas.setDocumentText(text);

        if (idFieldName != null) {
            String id = String.valueOf(nextRecord.get(idFieldName));
            DocumentID documentID;
            try {
                documentID = new DocumentID(cas.getJCas());
                documentID.setDocumentID(id);
                documentID.addToIndexes();
            } catch (CASException e) {
                logger.warn("CAS Exception while creating the DocumentID! {}", e);
            }
        }
    }

    public boolean hasNext() throws IOException, CollectionException {
        if (dataFileReader.hasNext()) {
            return true;
        } else if (!paths.isEmpty()) {
            dataFileReader.close();
            dataFileReader = openReader(paths.remove());
            return true;
        } else {
            return false;
        }
    }

    public Progress[] getProgress() {
        return new Progress[0];
    }

    @Override
    public void close() {
        try {
            dataFileReader.close();
        } catch (IOException e) {
            logger.warn("IOException while closing the Avro file! {}", e);
        }
    }
}

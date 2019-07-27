package de.julielab.jcore.reader.trecpmextra;

import de.julielab.jcore.types.*;
import org.apache.uima.UimaContext;
import org.apache.uima.fit.component.JCasCollectionReader_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.ResourceMetaData;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSArray;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.Progress;
import org.apache.uima.util.ProgressImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

@ResourceMetaData(name = "JCoRe TREC PM Extra Abstracts Reader", description = "This is a reader for the simple format of the AACR/ASCO abstracts delivered for the TREC PM challenges in 2017 and 2018. For more information refer to http://trec-cds.appspot.com/2018.html.")
public class TRECPMExtraReader extends JCasCollectionReader_ImplBase {

    public static final String PARAM_INPUT = "InputFile";
    private final static Logger log = LoggerFactory.getLogger(TRECPMExtraReader.class);
    @ConfigurationParameter(name = PARAM_INPUT, description = "The extra abstracts from the TREC PM 17/18 challenge packaged as a ZIP archive.")
    private String inputFile;

    private Iterator<ExtraAbstract> entryStreamIterator;

    private int abstractsRead;
    private ZipFile zip;

    private Matcher yearMatcher = Pattern.compile("[0-9]+").matcher("");

    /**
     * This method is called a single time by the framework at component
     * creation. Here, descriptor parameters are read and initial setup is done.
     */
    @Override
    public void initialize(UimaContext context) throws ResourceInitializationException {
        super.initialize(context);
        inputFile = (String) context.getConfigParameterValue(PARAM_INPUT);

        log.info("Reading TREC PM extra abstracts from {}", inputFile);

        if (inputFile.toLowerCase().endsWith(".zip")) {
            try {
                zip = new ZipFile(inputFile);
                Enumeration<? extends ZipEntry> entries = zip.entries();
                entryStreamIterator = new Iterator<ExtraAbstract>() {

                    private ZipEntry currentEntry;

                    @Override
                    public boolean hasNext() {
                        if (currentEntry == null && entries.hasMoreElements())
                            currentEntry = entries.nextElement();
                        while ((currentEntry == null || !currentEntry.getName().endsWith(".txt")) && entries.hasMoreElements())
                            currentEntry = entries.nextElement();
                        return currentEntry != null && currentEntry.getName().endsWith(".txt");
                    }

                    @Override
                    public ExtraAbstract next() {
                        if (hasNext()) {
                            try {
                                return new ExtraAbstract(zip.getInputStream(currentEntry), currentEntry.getName());
                            } catch (IOException e) {
                                log.error("Could not read entry with name {} from file {}. Error is:", currentEntry != null ? currentEntry.getName() : "<entry object is null>", inputFile, e);
                            } finally {
                                currentEntry = null;
                            }
                        }
                        return null;
                    }
                };
            } catch (IOException e) {
                log.error("Cannot read ZIP file {}:", inputFile, e);
            }
        } else {
            throw new ResourceInitializationException(new IllegalArgumentException("The input file is not a ZIP archive."));
        }
    }

    /**
     * This method is called for each document going through the component. This
     * is where the actual work happens.
     */
    @Override
    public void getNext(JCas jCas) {
        if (!hasNext())
            return;

        ExtraAbstract extraAbstract = entryStreamIterator.next();
        Header header = new Header(jCas);
        String entryName = extraAbstract.getEntryName();
        // From http://trec-cds.appspot.com/2018.html:
        // "the file name (without extension) should be used as the ID"
        String id = entryName.substring(0, entryName.indexOf(".txt"));
        header.setDocId(id);
        header.addToIndexes();

        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(extraAbstract.getContentStream()));
            StringBuilder sb = new StringBuilder();
            br.lines().forEach(line -> {
                if (line.startsWith("Meeting:")) {
                    String meeting = line.substring(9).trim();
                    OtherPub publication = new OtherPub(jCas);
                    publication.setName(meeting);
                    FSArray pub = new FSArray(jCas, 1);
                    pub.set(0, publication);
                    yearMatcher.reset(meeting);
                    yearMatcher.find();
                    Date date = new Date(jCas);
                    date.setYear(Integer.parseInt(yearMatcher.group()));
                    date.setMonth(1);
                    date.setDay(1);
                    publication.setPubDate(date);
                    header.setPubTypeList(pub);
                } else if (JCasUtil.select(jCas, Title.class).isEmpty() && line.startsWith("Title:") && line.length() > 6) {
                    String titleStr = line.substring(7).trim();
                    sb.append(titleStr);
                    sb.append(System.getProperty("line.separator"));
                    Title title = new Title(jCas, 0, titleStr.length());
                    title.setTitleType("document");
                    title.addToIndexes();
                } else {
                    line = line.trim();
                    if (!line.isEmpty()) {
                        sb.append(line);
                    }
                }
            });
            jCas.setDocumentText(sb.toString());
            Title title = JCasUtil.selectSingle(jCas, Title.class);
            int titleend = title.getEnd();
            // add one for the newline
            int abstractbegin = titleend + 1;
            int abstractend = sb.length();
            AbstractText abstractText = new AbstractText(jCas, abstractbegin, abstractend);
            abstractText.addToIndexes();
        } catch (Exception e) {
            log.error("Exception raised for document {}", id, e);
            throw e;
        }

        ++abstractsRead;
    }

    @Override
    public void close() throws IOException {
        zip.close();
    }

    @Override
    public Progress[] getProgress() {
        return new Progress[]{new ProgressImpl(abstractsRead, -1, "documents")};
    }

    @Override
    public boolean hasNext() {
        return entryStreamIterator.hasNext();
    }

    private class ExtraAbstract {
        private InputStream contentStream;
        private String entryName;

        public ExtraAbstract(InputStream contentStream, String entryName) {
            this.contentStream = contentStream;
            this.entryName = entryName;
            if (this.entryName.contains(File.separator))
                this.entryName = entryName.substring(entryName.lastIndexOf(File.separator) + 1);
        }

        public InputStream getContentStream() {
            return contentStream;
        }

        public String getEntryName() {
            return entryName;
        }
    }
}

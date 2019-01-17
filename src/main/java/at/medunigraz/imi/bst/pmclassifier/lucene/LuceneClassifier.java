package at.medunigraz.imi.bst.pmclassifier.lucene;

import at.medunigraz.imi.bst.pmclassifier.*;
import cc.mallet.classify.Classification;
import cc.mallet.types.Instance;
import cc.mallet.types.Label;
import de.julielab.java.utilities.CLIInteractionUtilities;
import de.julielab.java.utilities.FileUtilities;
import org.apache.logging.log4j.LogManager;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.*;
import org.apache.lucene.search.*;
import org.apache.lucene.store.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class LuceneClassifier implements PMClassifier {
    private static final org.apache.logging.log4j.Logger LOG = LogManager.getLogger();
    private Directory dir;

    public void train(File documentJsonZip, File gsTable) throws DataReadingException {
        Map<String, Document> docsById = DataReader.readDocuments(documentJsonZip);
        DataReader.addPMLabels(gsTable, docsById);

        train(docsById);
    }

    @Override
    public void train(Map<String, Document> labeledDocuments) {
        final IndexWriterConfig indexWriterConfig = new IndexWriterConfig(new StandardAnalyzer());
        indexWriterConfig.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
        try {
            dir = new RAMDirectory();
            final IndexWriter iw = new IndexWriter(dir, indexWriterConfig);
            List<org.apache.lucene.document.Document> ldocs = new ArrayList<>();
            for (Document doc : labeledDocuments.values()) {
                final org.apache.lucene.document.Document ldoc = new org.apache.lucene.document.Document();
                final TextField field = new TextField(doc.getPmLabel(), doc.getTitle() + " " + doc.getAbstractText(), Field.Store.NO);
                final TextField allfield = new TextField("all", doc.getTitle() + " " + doc.getAbstractText(), Field.Store.NO);
                final StringField labelField = new StringField("label", doc.getPmLabel(), Field.Store.YES);
                ldoc.add(field);
                ldoc.add(allfield);
                ldoc.add(labelField);
                ldocs.add(ldoc);
            }
            iw.addDocuments(ldocs);
            iw.close();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public String predict(Document document) {
        return predictProbabiltyForPM(document) > 0 ? "PM" : "Not PM";
    }

    private BooleanQuery getQuery(TokenStream ts, String field) throws IOException {
        final BooleanQuery.Builder b = new BooleanQuery.Builder();
        ts.reset();
        while (ts.incrementToken()) {
            final CharTermAttribute cta = ts.getAttribute(CharTermAttribute.class);
            b.add(new TermQuery(new Term(field, cta.toString())), BooleanClause.Occur.SHOULD);
        }
        return b.build();
    }

    /**
     * Does not really return a probability but just the difference of the scores for PM and Not PM. Thus: PM iff > 0.
     *
     * @param document
     * @return
     */
    public double predictProbabiltyForPM(Document document) {
        try {
            BooleanQuery.setMaxClauseCount(10000);
            final DirectoryReader reader = DirectoryReader.open(dir);
            final IndexSearcher indexSearcher = new IndexSearcher(reader);

            String text = document.getTitle() + " " + document.getAbstractText();
            float pmScore = 0;
            float notPmScore = 0;
            final StandardAnalyzer a = new StandardAnalyzer();
            final TokenStream ts = a.tokenStream("", text);
            final BooleanQuery query = getQuery(ts, "all");
            final TopDocs search = indexSearcher.search(query, 50);
            for (ScoreDoc hit : search.scoreDocs) {
                final org.apache.lucene.document.Document doc = reader.document(hit.doc);
                final String label = doc.get("label");
                if (label.equals("PM")) {
                    pmScore += 1;
                }
                else {
                    notPmScore += 1;
                }
            }

            final StandardAnalyzer a2 = new StandardAnalyzer();
            final TokenStream ts2 = a2.tokenStream("", text);
            final BooleanQuery notPmQuery = getQuery(ts2, "Not PM");

            final TopDocs pmSearch = indexSearcher.search(query, 20);
            final TopDocs notPmSearch = indexSearcher.search(notPmQuery, 20);

            int pmScore2 = 0;
            for (ScoreDoc hit : pmSearch.scoreDocs)
                pmScore2 += hit.score;
            pmScore += pmScore2;


            int notPmScore2 = 0;
            for (ScoreDoc hit : notPmSearch.scoreDocs)
                notPmScore2 += hit.score;
            notPmScore += notPmScore2;

            return pmScore - notPmScore;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public void setInstancePreparator(InstancePreparator instancePreparator) {
// nothing
    }

    public void writeClassifier(File destination) throws IOException {
        LOG.info("Writing classifier to " + destination.getAbsolutePath());
        dir.copyFrom(dir, "ram", destination.getAbsolutePath(), IOContext.DEFAULT);
    }



    public void readClassifier(File source) throws IOException, ClassNotFoundException {
        LOG.info("Reading classifier from " + source.getAbsolutePath());
        dir =  FSDirectory.open(source.toPath());
    }

}

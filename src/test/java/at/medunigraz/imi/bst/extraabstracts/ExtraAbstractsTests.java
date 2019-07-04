package at.medunigraz.imi.bst.extraabstracts;

import at.medunigraz.imi.bst.config.TrecConfig;
import at.medunigraz.imi.bst.medline.PubMedArticle;
import org.junit.Test;

public class ExtraAbstractsTests {

    @Test
    public void readOneFile() throws Exception {

        PubMedArticle extraAbstract = Indexing.getExtraAbstractFromFile(TrecConfig.SAMPLE_EXTRA_ABSTRACT_TXT);

    }
}

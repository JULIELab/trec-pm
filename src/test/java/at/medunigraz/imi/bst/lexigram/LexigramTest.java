package at.medunigraz.imi.bst.lexigram;

import org.junit.*;

import java.util.List;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;


public class LexigramTest {

    @Before
    public void SetUp() {
        Assume.assumeTrue(Lexigram.isAPIKeyLoaded());
    }

    @Test
    public void preferredTerm() {
        Assert.assertEquals(Lexigram.getPreferredTerm("cervical cancer"), "carcinoma of cervix");
        Assert.assertEquals(Lexigram.getPreferredTerm("cholangiocarcinoma"), "cholangiocarcinoma of biliary tract");
        Assert.assertEquals(Lexigram.getPreferredTerm("notfoundlabel"), "notfoundlabel");
    }

    @Test
    public void addSynonyms() {
        Assert.assertThat(Lexigram.addSynonymsFromBestConceptMatch("cholangiocarcinoma"),
                containsInAnyOrder("cholangiocellular carcinoma",
                        "cholangiocarcinoma of biliary tract",  // preferred term
                        "bile duct carcinoma",
                        "cholangiocarcinoma",   // original label, but also a synonym
                        "bile duct adenocarcinoma"));
        Assert.assertThat(Lexigram.addSynonymsFromBestConceptMatch("notfoundlabel"),
                containsInAnyOrder("notfoundlabel"));
    }

    @Test
    public void getSynonymsFromBestConceptMatch() {
        Assert.assertThat(Lexigram.getSynonymsFromBestConceptMatch("cholangiocarcinoma"),
                containsInAnyOrder("cholangiocellular carcinoma",
                        "bile duct carcinoma",
                        "cholangiocarcinoma",
                        "bile duct adenocarcinoma"));

        Assert.assertThat(Lexigram.getSynonymsFromBestConceptMatch("liposarcoma"),
                containsInAnyOrder("liposarcoma",
                        "fibroliposarcoma"));

        Assert.assertThat(Lexigram.getSynonymsFromBestConceptMatch("meningioma"),
                containsInAnyOrder("secretory meningioma",
                        "mgm",
                        "meningioma",
                        "meningiomas",
                        "benign meningioma",
                        "lymphoplasmocyte-rich meningioma",
                        "microcystic meningioma",
                        "metaplastic meningioma"));

        Assert.assertThat(Lexigram.getSynonymsFromBestConceptMatch("glioma"),
                containsInAnyOrder("glioma",
                        "gliomas",
                        "malignant glioma"));

        Assert.assertThat(Lexigram.getSynonymsFromBestConceptMatch("glioblastoma"),
                containsInAnyOrder("gbm",
                        "glioblastoma",
                        "glm",
                        "spongioblastoma multiforme"));

        Assert.assertThat(Lexigram.getSynonymsFromBestConceptMatch("sarcoma"),
                containsInAnyOrder("soft tissue tumour",
                        "sarcoma of soft tissue",
                        "sarcoma",
                        "malignant tumour of soft tissue",
                        "mesenchymal tumor",
                        "malignant mesenchymal tumor",
                        "malignant neoplasm of soft tissue",
                        "malignant mesenchymal tumour",
                        "malignant tumor of soft tissue",
                        "mesenchymal tumour",
                        "soft tissue sarcoma",
                        "cancer of soft tissue",
                        "soft tissue tumor"));

        Assert.assertThat(Lexigram.getSynonymsFromBestConceptMatch("breast cancer"),
                containsInAnyOrder("brest cancer",
                        "breast cancer"));

        Assert.assertThat(Lexigram.getSynonymsFromBestConceptMatch("notfoundlabel"),
                empty());
    }

    @Test
    public void getAncestorsFromBestConceptMatch() {
        Assert.assertThat(Lexigram.getAncestorsFromBestConceptMatch("cholangiocarcinoma"),
                hasItems("malignant neoplasm of digestive system",
                        "abdominal mass",
                        "epithelial neoplasm",
                        "disorder of biliary tract",
                        "neoplasm of digestive organ",
                        "finding of biliary tract",
                        "gastrointestinal tract finding")
                );

        Assert.assertThat(Lexigram.getAncestorsFromBestConceptMatch("notfoundlabel"),
                empty());
    }
}

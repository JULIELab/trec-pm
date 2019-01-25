package at.medunigraz.imi.bst.trec.expansion;

import org.junit.Test;

import java.util.Set;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class DGIdbTest {

    private static final DGIdb DGI_DB = new DGIdb();

    @Test
    public void getInteractions() {
        // Baseline, all results
        Set<String> actual = DGI_DB.getDrugInteractions("BRAF");
        assertThat(actual, hasItems("selumetinib", "dabrafenib", "bevacizumab", "obatoclax"));

        // Only expert curated
        actual = DGI_DB.getDrugInteractions("BRAF", true);
        assertThat(actual, hasItems("selumetinib", "dabrafenib"));
        assertThat(actual, not(hasItems("bevacizumab", "obatoclax")));  // Bevacizumab and obatoclax are not expert curated

        // Minimal score, expert curated only
        actual = DGI_DB.getDrugInteractions("BRAF", true, 100);
        assertThat(actual, hasItems("dabrafenib"));
        assertThat(actual, not(hasItems("selumetinib", "bevacizumab", "obatoclax")));   // selumetinib has score 21

        // No results
        actual = DGI_DB.getDrugInteractions("LDH");
        assertThat(actual, is(empty()));

        // TODO test sorting by score
    }

    @Test
    public void getPublications() {
        // Baseline, all results
        Set<String> actual = DGI_DB.getPublications("BRAF");
        assertThat(actual, hasItems("26343583", "22197931", "23020132", "2015", "21216929", "22460902"));

        // Only expert curated
        actual = DGI_DB.getPublications("BRAF", true);
        assertThat(actual, hasItems("26343583", "22197931", "23020132", "2015"));
        assertThat(actual, hasItems("21216929"));   // 21216929 is for bevacizumab (not expert curated), but also temsirolums (still expert curated)
        assertThat(actual, not(hasItems("22460902")));  // 22460902 is only for obatoclax, not expert curated

        // Minimal score, expert curated only
        actual = DGI_DB.getPublications("BRAF", true, 100);
        assertThat(actual, hasItems("26343583", "23020132", "2015"));
        assertThat(actual, not(hasItems("22460902", "22197931", "21216929")));  // 22197931 has score 16 and 21216929 has score 10/2

        // No results
        actual = DGI_DB.getPublications("LDH");
        assertThat(actual, is(empty()));

        // TODO test sorting by score
    }

}
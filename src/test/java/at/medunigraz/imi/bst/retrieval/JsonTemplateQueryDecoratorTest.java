package at.medunigraz.imi.bst.retrieval;

import at.medunigraz.imi.bst.TestTopic;
import at.medunigraz.imi.bst.config.TrecConfig;
import at.medunigraz.imi.bst.trec.query.DummyElasticSearchQuery;
import org.junit.BeforeClass;
import org.junit.Test;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

public class JsonTemplateQueryDecoratorTest {

    @BeforeClass
    public static void before() {
        TrecConfig.SUBTEMPLATES_FOLDER = "/test-subtemplates-truejson/";
    }

    @Test
    public void simpleExpansion() {
        TestTopic topic = new TestTopic().withStopFilteredTermList("wand", "harry", "snape");
        String mappedTemplate = new JsonTemplateQueryDecorator<>(Path.of("src", "test", "resources", "test-templates-truejson", "simple.json").toString(), new DummyElasticSearchQuery<>()).expandTemplateExpressions(topic);
        assertThat(mappedTemplate).contains(": [\"wand\",\"harry\",\"snape\"]");
    }

    @Test
    public void loopList() {
        TestTopic topic = new TestTopic().withFriends(new String[]{"hermione", "ron"}, new String[]{"mcgonagall", "dumbledore"});
        String mappedTemplate = new JsonTemplateQueryDecorator<>(Path.of("src", "test", "resources", "test-templates-truejson", "nestedList.json").toString(), new DummyElasticSearchQuery<>(), false, true).expandTemplateExpressions(topic);
        assertThat(mappedTemplate).isEqualTo("{\"bool\":{\"must\":[{\"dis_max\":{\"queries\":[{\"match\":{\"title\":{\"query\":\"hermione\"}}},{\"match\":{\"title\":{\"query\":\"ron\"}}}]}},{\"dis_max\":{\"queries\":[{\"match\":{\"title\":{\"query\":\"mcgonagall\"}}},{\"match\":{\"title\":{\"query\":\"dumbledore\"}}}]}}]}}");
    }

    @Test
    public void loopList2() {
        TestTopic topic = new TestTopic().withFriends(new String[]{"hermione" }, new String[]{"mcgonagall", "dumbledore", "snape"});
        String mappedTemplate = new JsonTemplateQueryDecorator<>(Path.of("src", "test", "resources", "test-templates-truejson", "nestedList.json").toString(), new DummyElasticSearchQuery<>(), false, true).expandTemplateExpressions(topic);
        assertThat(mappedTemplate).isEqualTo("{\"bool\":{\"must\":[{\"dis_max\":{\"queries\":[{\"match\":{\"title\":{\"query\":\"hermione\"}}}]}},{\"dis_max\":{\"queries\":[{\"match\":{\"title\":{\"query\":\"mcgonagall\"}}},{\"match\":{\"title\":{\"query\":\"dumbledore\"}}},{\"match\":{\"title\":{\"query\":\"snape\"}}}]}}]}}");
    }

    @Test
    public void simpleTemplateInsertion() {
        TestTopic topic = new TestTopic().withQuery("hogwards england");
        String mappedTemplate = new JsonTemplateQueryDecorator<>(Path.of("src", "test", "resources", "test-templates-truejson", "simpleTemplateInsertion.json").toString(), new DummyElasticSearchQuery<>(), false, true).expandTemplateExpressions(topic);
        assertThat(mappedTemplate).isEqualTo("{\"title\":{\"match\":{\"title\":{\"query\":\"hogwards england\"}}}}");
    }

    @Test
    public void doubleForIndexInWithElementReference() {
        TestTopic topic = new TestTopic().withFriends(new String[]{"hermione", "ron"}, new String[]{"mcgonagall", "dumbledore"}).withStopFilteredTermArray("expelliarmus", "spark");
        String mappedTemplate = new JsonTemplateQueryDecorator<>(Path.of("src", "test", "resources", "test-templates-truejson", "doubleForIndexInExpression.json").toString(), new DummyElasticSearchQuery<>(), false, true).expandTemplateExpressions(topic);
        assertThat(mappedTemplate).isEqualTo("{\"bool\":{\"should\":[{\"match\":{\"title\":{\"query\":\"expelliarmus\"}}},{\"match\":{\"title\":{\"query\":\"spark\"}}}],\"must\":[{\"dis_max\":{\"queries\":[{\"match\":{\"title\":{\"query\":\"hermione\"}}},{\"match\":{\"title\":{\"query\":\"ron\"}}}]}},{\"dis_max\":{\"queries\":[{\"match\":{\"title\":{\"query\":\"mcgonagall\"}}},{\"match\":{\"title\":{\"query\":\"dumbledore\"}}}]}}]}}");
    }

    @Test
    public void nestedDoubleForIndexInWithElementReference() {
        // This test is a bit confusing because the templates are overly complex just in order to prove the case.
        // The thing to watch out for is that match queries work independent from their sibling bool queries.
        // We just want to make sure that nested FOR INDEX IN structures don't influence one another.
        TestTopic topic = new TestTopic().withFriends(new String[]{"hermione", "ron"}, new String[]{"mcgonagall", "dumbledore"}).withStopFilteredTermArray("expelliarmus", "spark");
        String mappedTemplate = new JsonTemplateQueryDecorator<>(Path.of("src", "test", "resources", "test-templates-truejson", "nestedForIndexIn.json").toString(), new DummyElasticSearchQuery<>(), false, true).expandTemplateExpressions(topic);
        assertThat(mappedTemplate).isEqualTo("{\"bool\":{\"must\":[{\"bool\":{\"should\":[{\"bool\":{\"should\":\"hermione ron\"}},{\"bool\":{\"should\":\"mcgonagall dumbledore\"}}],\"must\":[{\"match\":{\"title\":{\"query\":\"hermione\"}}},{\"match\":{\"title\":{\"query\":\"ron\"}}}]}},{\"bool\":{\"should\":[{\"bool\":{\"should\":\"hermione ron\"}},{\"bool\":{\"should\":\"mcgonagall dumbledore\"}}],\"must\":[{\"match\":{\"title\":{\"query\":\"mcgonagall\"}}},{\"match\":{\"title\":{\"query\":\"dumbledore\"}}}]}}]}}");
    }
}
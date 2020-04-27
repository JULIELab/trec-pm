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
        String mappedTemplate = new JsonTemplateQueryDecorator<>(Path.of("src", "test", "resources", "test-templates-truejson", "nestedList.json").toString(), new DummyElasticSearchQuery<>(), true, true).expandTemplateExpressions(topic);
        System.out.println(mappedTemplate);
    }
}
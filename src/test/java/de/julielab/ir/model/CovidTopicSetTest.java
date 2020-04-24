package de.julielab.ir.model;

import at.medunigraz.imi.bst.trec.model.Challenge;
import org.junit.Test;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
public class CovidTopicSetTest {
    @Test
    public void fromXml() {
        CovidTopicSet covidTopics = new CovidTopicSet(Path.of("src", "main", "resources", "topics", "topics-rnd1.xml").toString(), Challenge.COVID, 2020);
        assertThat(covidTopics).hasSize(30);
        CovidTopic topic0 = covidTopics.get(0);
        assertThat(topic0).extracting(CovidTopic::getNumber, CovidTopic::getQuery, CovidTopic::getQuestion, CovidTopic::getNarrative)
                .containsExactly(1,
                        "coronavirus origin",
                        "what is the origin of COVID-19",
                        "seeking range of information about the SARS-CoV-2 virus's origin, including its evolution, animal source, and first transmission into humans");
        CovidTopic topic14 = covidTopics.get(14);
        assertThat(topic14).extracting(CovidTopic::getNumber, CovidTopic::getQuery, CovidTopic::getQuestion, CovidTopic::getNarrative)
                .containsExactly(
                        15,
                        "coronavirus outside body",
                        "how long can the coronavirus live outside the body",
                        "seeking range of information on the SARS-CoV-2's virus's survival in different environments (surfaces, liquids, etc.) outside the human body while still being viable for transmission to another human"
                );
        CovidTopic topic29 = covidTopics.get(29);
        assertThat(topic29.getFlattenedAttributes()).containsEntry("number", "30")
                .containsEntry("query", "coronavirus remdesivir")
                .containsEntry("question", "is remdesivir an effective treatment for COVID-19")
                .containsEntry("narrative", "seeking specific information on clinical outcomes in COVID-19 patients treated with remdesivir");

    }
}
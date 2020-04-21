package de.julielab.ir.nlp;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;
import static org.assertj.core.api.Assertions.*;

public class PosTaggingServiceTest {

    @Test
    public void tag() {
        String sentence = "Studies of people who are known to be infected with Covid-19 but show no symptoms?";
        List<NLPToken> taggedSentence = PosTaggingService.getInstance().tag(sentence);
        assertThat(taggedSentence).hasSize(16);
        assertThat(taggedSentence).extracting(NLPToken::getToken).containsExactly("Studies", "of", "people", "who", "are", "known", "to", "be", "infected", "with", "Covid-19", "but", "show", "no", "symptoms", "?");
        assertThat(taggedSentence).extracting(NLPToken::getPosTag).containsExactly("NNS", "IN", "NNS", "WP", "VBP", "VBN", "TO", "VB", "VBN", "IN", "NN", "CC", "VBP", "DT", "NNS", ".");
    }
}
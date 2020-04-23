package de.julielab.ir.utils;

import at.medunigraz.imi.bst.trec.model.TrecCovidTopicSetFactory;
import de.julielab.ir.TrecCacheConfiguration;
import de.julielab.ir.model.CovidTopic;
import de.julielab.ir.model.CovidTopicSet;
import de.julielab.ir.nlp.NLPToken;
import de.julielab.ir.nlp.PosTaggingService;
import de.julielab.ir.umls.UmlsSynsetProvider;
import de.julielab.java.utilities.cache.CacheService;

import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class CovidUmlsExpansionPrinter {
    public static void main(String args[]) {
        CacheService.initialize(new TrecCacheConfiguration());
        UmlsSynsetProvider.setDefaultSynsetFile("resources/umlsCovidSynsets.txt.gz");
        UmlsSynsetProvider provider = UmlsSynsetProvider.getInstance();
        CovidTopicSet topics = TrecCovidTopicSetFactory.topicsRound1();
        Set<String> allMappings = new TreeSet<>();
        for (CovidTopic t : topics) {
            System.out.println("topic " + t.getNumber() + ": ");
            System.out.println("\tquery: " + t.getQuery());
            for (NLPToken w : PosTaggingService.getInstance().tag(t.getQuery())) {
                printSynset(provider, allMappings, w.getToken());
            }
            System.out.println("\tquestion: " + t.getQuestion());
            for (NLPToken w : PosTaggingService.getInstance().tag(t.getQuestion())) {
                printSynset(provider, allMappings, w.getToken());
            }
            System.out.println("\tnarrative: " + t.getNarrative());
            for (NLPToken w : PosTaggingService.getInstance().tag(t.getNarrative())) {
                printSynset(provider, allMappings, w.getToken());
            }
        }
        System.out.println();
        System.out.println("All synsets:");
        for (String s : allMappings)
            System.out.println(s.replaceAll("^\t\t", ""));

        CacheService.getInstance().commitAllCaches();
    }

    private static void printSynset(UmlsSynsetProvider provider, Set<String> allMappings, String w) {
        if (w.length() > 1) {
            String synonyms = provider.getSynsets(w).stream().flatMap(Collection::stream).collect(Collectors.joining(" - "));
            String output = "\t\t" + w + "\t" + synonyms;
            allMappings.add(output);
            System.out.println(output);
        }
    }
}

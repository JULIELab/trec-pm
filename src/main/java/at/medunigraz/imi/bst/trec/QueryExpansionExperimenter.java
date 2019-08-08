package at.medunigraz.imi.bst.trec;

import at.medunigraz.imi.bst.config.TrecConfig;
import at.medunigraz.imi.bst.trec.experiment.Experiment;
import at.medunigraz.imi.bst.trec.experiment.TrecPmRetrieval;
import at.medunigraz.imi.bst.trec.model.Topic;
import at.medunigraz.imi.bst.trec.query.SolidTumorQueryDecorator;
import at.medunigraz.imi.bst.trec.search.ElasticClientFactory;
import de.julielab.ir.OriginalDocumentRetrieval;
import de.julielab.ir.goldstandards.GoldStandard;
import de.julielab.ir.goldstandards.TrecPMGoldStandardFactory;

import java.io.File;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

public class QueryExpansionExperimenter {
    private static final File EMBEDDINGS = new File(SolidTumorQueryDecorator.class.getResource("/synonyms/embeddings.txt").getFile());

    private static final File TEMPLATE = new File(
            QueryExpansionExperimenter.class.getResource("/templates/query_expansion/ba.json").getFile());

    private static final GoldStandard<Topic> GOLD_STANDARD = TrecPMGoldStandardFactory.pubmedOfficialAggregated();

    public static void main(String[] args) {
        TrecPmRetrieval baseline = new TrecPmRetrieval(TrecConfig.ELASTIC_BA_INDEX, TrecConfig.SIZE)
                .withExperimentName("baseline")
                .withSubTemplate(TEMPLATE)
                .withWordRemoval();

        TrecPmRetrieval embeddings = new TrecPmRetrieval(TrecConfig.ELASTIC_BA_INDEX, TrecConfig.SIZE)
                .withExperimentName("embeddings")
                .withSubTemplate(TEMPLATE)
                .withWordRemoval()
                .withSynonymList(EMBEDDINGS);

        TrecPmRetrieval terminologies = new TrecPmRetrieval(TrecConfig.ELASTIC_BA_INDEX, TrecConfig.SIZE)
                .withExperimentName("terminologies")
                .withSubTemplate(TEMPLATE)
                .withWordRemoval()
                .withGeneSynonym()
                .withDiseasePreferredTerm()
                .withDiseaseSynonym();

        TrecPmRetrieval rules = new TrecPmRetrieval(TrecConfig.ELASTIC_BA_INDEX, TrecConfig.SIZE)
                .withExperimentName("rules")
                .withSubTemplate(TEMPLATE)
                .withWordRemoval()
                .withSolidTumor()
                .withGeneFamily();

        Set<TrecPmRetrieval> retrievalSet = new LinkedHashSet<>(Arrays.asList(baseline, terminologies, embeddings, rules));
        for (TrecPmRetrieval retrieval : retrievalSet) {
            new Experiment<>(GOLD_STANDARD, retrieval).run();
        }

        ElasticClientFactory.getClient().close();
        OriginalDocumentRetrieval.getInstance().shutdown();
    }
}

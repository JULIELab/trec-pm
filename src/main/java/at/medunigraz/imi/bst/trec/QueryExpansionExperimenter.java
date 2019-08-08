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

    private static final String BOOST = "0.5";

    private static final File BA_TEMPLATE = new File(
            QueryExpansionExperimenter.class.getResource("/templates/query_expansion/ba.json").getFile());
    private static final File CT_TEMPLATE = new File(
            QueryExpansionExperimenter.class.getResource("/templates/query_expansion/ct.json").getFile());

    private static final GoldStandard<Topic> GOLD_STANDARD = TrecPMGoldStandardFactory.pubmedOfficialAggregated();

    public static void main(String[] args) {
        // Biomedical Articles
        TrecPmRetrieval baBaseline = new TrecPmRetrieval(TrecConfig.ELASTIC_BA_INDEX, TrecConfig.SIZE)
                .withExperimentName("ba-baseline")
                .withProperties("boost", BOOST)
                .withSubTemplate(BA_TEMPLATE)
                .withWordRemoval();

        TrecPmRetrieval baEmbeddings = new TrecPmRetrieval(TrecConfig.ELASTIC_BA_INDEX, TrecConfig.SIZE)
                .withExperimentName("ba-embeddings")
                .withProperties("boost", BOOST)
                .withSubTemplate(BA_TEMPLATE)
                .withWordRemoval()
                .withSynonymList(EMBEDDINGS);

        TrecPmRetrieval baTerminologies = new TrecPmRetrieval(TrecConfig.ELASTIC_BA_INDEX, TrecConfig.SIZE)
                .withExperimentName("ba-terminologies")
                .withProperties("boost", BOOST)
                .withSubTemplate(BA_TEMPLATE)
                .withWordRemoval()
                .withGeneSynonym()
                .withDiseasePreferredTerm()
                .withDiseaseSynonym();

        TrecPmRetrieval baRules = new TrecPmRetrieval(TrecConfig.ELASTIC_BA_INDEX, TrecConfig.SIZE)
                .withExperimentName("ba-rules")
                .withProperties("boost", BOOST)
                .withSubTemplate(BA_TEMPLATE)
                .withWordRemoval()
                .withSolidTumor()
                .withGeneFamily();

        // Clinical Trials
        TrecPmRetrieval ctBaseline = new TrecPmRetrieval(TrecConfig.ELASTIC_CT_INDEX, TrecConfig.SIZE)
                .withExperimentName("ct-baseline")
                .withProperties("boost", BOOST)
                .withSubTemplate(CT_TEMPLATE)
                .withWordRemoval();

        TrecPmRetrieval ctEmbeddings = new TrecPmRetrieval(TrecConfig.ELASTIC_CT_INDEX, TrecConfig.SIZE)
                .withExperimentName("ct-embeddings")
                .withProperties("boost", BOOST)
                .withSubTemplate(CT_TEMPLATE)
                .withWordRemoval()
                .withSynonymList(EMBEDDINGS);

        TrecPmRetrieval ctTerminologies = new TrecPmRetrieval(TrecConfig.ELASTIC_CT_INDEX, TrecConfig.SIZE)
                .withExperimentName("ct-terminologies")
                .withProperties("boost", BOOST)
                .withSubTemplate(CT_TEMPLATE)
                .withWordRemoval()
                .withGeneSynonym()
                .withDiseasePreferredTerm()
                .withDiseaseSynonym();

        TrecPmRetrieval ctRules = new TrecPmRetrieval(TrecConfig.ELASTIC_CT_INDEX, TrecConfig.SIZE)
                .withExperimentName("ct-rules")
                .withProperties("boost", BOOST)
                .withSubTemplate(CT_TEMPLATE)
                .withWordRemoval()
                .withSolidTumor()
                .withGeneFamily();

        Set<TrecPmRetrieval> retrievalSet = new LinkedHashSet<>(Arrays.asList(
                baBaseline, baEmbeddings, baTerminologies, baRules,
                ctBaseline, ctEmbeddings, ctTerminologies, ctRules));

        for (TrecPmRetrieval retrieval : retrievalSet) {
            new Experiment<>(GOLD_STANDARD, retrieval).run();
        }

        ElasticClientFactory.getClient().close();
        OriginalDocumentRetrieval.getInstance().shutdown();
    }
}

package at.medunigraz.imi.bst.trec.experiment.registry;

import at.medunigraz.imi.bst.config.TrecConfig;
import at.medunigraz.imi.bst.trec.experiment.Cord19Retrieval;
import at.medunigraz.imi.bst.trec.model.Result;
import de.julielab.ir.goldstandards.AggregatedTrecQrelGoldStandard;
import de.julielab.ir.goldstandards.TrecCovidGoldStandardFactory;

import java.util.function.Function;

public final class Cord19RetrievalRegistry {

    private static final String TEMPLATE_BASE_RND2 = "/templates/cord19/jlbase-rnd2.json";

    private static final String TEMPLATE_BASE = "/templates/cord19/jlbase.json";
    private static final String TEMPLATE_PREC = "/templates/cord19/jlprec.json";
    private static final String TEMPLATE_RECALL = "/templates/cord19/jlrecall.json";
    private static Function<Result, String> cord19DocIdFunction = r -> (String) r.getSourceFields().get("cord19_uid");

    public static Cord19Retrieval jlbaseRound1() {
        return new Cord19Retrieval(TrecConfig.ELASTIC_CORD19_INDEX).withExperimentName("jlbase")
                .withSize(1500)
                .withResultListSizeCutoff(1000)
                .withStoredFields("cord19_uid", "abstract")
                .withDocIdFunction(r -> (String) r.getSourceFields().get("cord19_uid"))
                .withValidDocIds("/valid-result-docs/docids-rnd1.txt", cord19DocIdFunction)
                .withUnifyingField("cord19_uid")
                .withSubTemplate(TEMPLATE_BASE);
    }

    public static Cord19Retrieval jlQERound1() {
        return new Cord19Retrieval(TrecConfig.ELASTIC_CORD19_INDEX).withExperimentName("jlbase-QE")
                .withSize(1500)
                .withResultListSizeCutoff(1000)
                .withStoredFields("cord19_uid", "abstract")
                .withDocIdFunction(r -> (String) r.getSourceFields().get("cord19_uid"))
                .withValidDocIds("/valid-result-docs/docids-rnd1.txt", cord19DocIdFunction)
                .withUnifyingField("cord19_uid")
                .withSubTemplate(TEMPLATE_BASE)
                //.withNarrativeSynonymDecorator()
                .withWordRemoval()
                ;
    }

    public static Cord19Retrieval jlQERound2() {
        return new Cord19Retrieval(TrecConfig.ELASTIC_CORD19_INDEX).withExperimentName("jlbase-QE-rnd2")
                .withSize(1500)
                .withResultListSizeCutoff(1000)
                .withStoredFields("cord19_uid", "abstract")
                .withDocIdFunction(r -> (String) r.getSourceFields().get("cord19_uid"))
                .withGoldstandardFilter(TrecCovidGoldStandardFactory.round1())
                .withUnifyingField("cord19_uid")
                .withSubTemplate(TEMPLATE_BASE)
                .withValidDocIds("/valid-result-docs/docids-rnd2.txt", cord19DocIdFunction)
                .withWordRemoval();
    }

    public static Cord19Retrieval jlQERound3() {
        return new Cord19Retrieval(TrecConfig.ELASTIC_CORD19_INDEX).withExperimentName("jlQErnd3")
                .withSize(1500)
                .withResultListSizeCutoff(1000)
                .withStoredFields("cord19_uid", "abstract")
                .withDocIdFunction(r -> (String) r.getSourceFields().get("cord19_uid"))
                .withGoldstandardFilter(new AggregatedTrecQrelGoldStandard<>(TrecCovidGoldStandardFactory.round1(), TrecCovidGoldStandardFactory.round2()))
                .withUnifyingField("cord19_uid")
                .withSubTemplate(TEMPLATE_BASE)
                .withValidDocIds("/valid-result-docs/docids-rnd3.txt", cord19DocIdFunction)
                .withWordRemoval();
    }

    public static Cord19Retrieval jlprecRound1() {
        return new Cord19Retrieval(TrecConfig.ELASTIC_CORD19_INDEX).withExperimentName("jlprec")
                .withSize(1500)
                .withResultListSizeCutoff(1000)
                .withStoredFields("cord19_uid")
                .withDocIdFunction(r -> (String) r.getSourceFields().get("cord19_uid"))
                .withValidDocIds("/valid-result-docs/docids-rnd1.txt", cord19DocIdFunction)
                .withUnifyingField("cord19_uid")
                .withSubTemplate(TEMPLATE_PREC)
                .withNarrativeSynonymDecorator()
                .withWordRemoval();
    }

    public static Cord19Retrieval jlrecallRound1() {
        return new Cord19Retrieval(TrecConfig.ELASTIC_CORD19_INDEX).withExperimentName("jlrecall")
                .withSize(1500)
                .withResultListSizeCutoff(1000)
                .withStoredFields("cord19_uid")
                .withDocIdFunction(r -> (String) r.getSourceFields().get("cord19_uid"))
                .withValidDocIds("/valid-result-docs/docids-rnd1.txt", cord19DocIdFunction)
                .withUnifyingField("cord19_uid")
                .withSubTemplate(TEMPLATE_RECALL)
                .withQueryQuestionSynonyms();
    }

    public static Cord19Retrieval jlbasernd2() {
        return new Cord19Retrieval(TrecConfig.ELASTIC_CORD19_INDEX.split(","))
                .withExperimentName("jlbasernd2")
                .withSize(6000)
                .withStoredFields("cord19_uid", "text")
                .withDocIdFunction(cord19DocIdFunction)
                .withGoldstandardFilter(TrecCovidGoldStandardFactory.round1())
                .withUnifyingField("cord19_uid")
                .withJsonTemplate(TEMPLATE_BASE_RND2, true, true)
                .withQueryQuestionBoW()
                .withValidDocIds("/valid-result-docs/docids-rnd2.txt", cord19DocIdFunction)
                .withResultListSizeCutoff(1000);
    }

    public static Cord19Retrieval jlbasernd3() {
        return new Cord19Retrieval(TrecConfig.ELASTIC_CORD19_INDEX.split(","))
                .withExperimentName("jlbasernd3")
                .withSize(6000)
                .withStoredFields("cord19_uid", "text")
                .withDocIdFunction(r -> (String) r.getSourceFields().get("cord19_uid"))
                .withGoldstandardFilter(new AggregatedTrecQrelGoldStandard<>(TrecCovidGoldStandardFactory.round1(), TrecCovidGoldStandardFactory.round2()))
                .withUnifyingField("cord19_uid")
                .withJsonTemplate(TEMPLATE_BASE_RND2, true, true)
                .withQueryQuestionBoW()
                .withValidDocIds("/valid-result-docs/docids-rnd3.txt", cord19DocIdFunction)
                .withResultListSizeCutoff(1000);
    }

}

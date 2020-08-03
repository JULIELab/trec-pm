package at.medunigraz.imi.bst.trec.experiment.registry;

import at.medunigraz.imi.bst.config.TrecConfig;
import at.medunigraz.imi.bst.trec.experiment.Cord19Retrieval;
import at.medunigraz.imi.bst.trec.model.Result;
import de.julielab.ir.goldstandards.AggregatedTrecQrelGoldStandard;
import de.julielab.ir.goldstandards.TrecCovidGoldStandardFactory;

import java.util.function.Function;

public final class Cord19RetrievalRegistry {

    private static final String TEMPLATE_BASE_RND2 = "/templates/cord19/jlbase-rnd2.json";
    private static final String TEMPLATE_QE_RND4 = "/templates/cord19/jlbase-QE-rnd4.json";
    private static final String TEMPLATE_BASE_RND5 = "/templates/cord19/jlbase-rnd5.json";
    private static final String TEMPLATE_BASE_RND5_UDEL = "/templates/cord19/jlbase-rnd5-udel.json";

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
                .withGoldstandardFilter(new AggregatedTrecQrelGoldStandard<>(TrecCovidGoldStandardFactory.round1(), TrecCovidGoldStandardFactory.round2()), "/id-map/covid-rnd3-changedIds-May19.csv")
                .withUnifyingField("cord19_uid")
                .withSubTemplate(TEMPLATE_BASE)
                .withValidDocIds("/valid-result-docs/docids-rnd3.txt", cord19DocIdFunction)
                .withWordRemoval();
    }

    public static Cord19Retrieval jlQERound4() {
        return new Cord19Retrieval(TrecConfig.ELASTIC_CORD19_INDEX).withExperimentName("jlQErnd4")
                .withSize(1500)
                .withResultListSizeCutoff(1000)
                .withStoredFields("cord19_uid", "abstract")
                .withDocIdFunction(r -> (String) r.getSourceFields().get("cord19_uid"))
                .withGoldstandardFilter(TrecCovidGoldStandardFactory.round3Cumulative())
                .withUnifyingField("cord19_uid")
                .withSubTemplate(TEMPLATE_BASE)
                .withValidDocIds("/valid-result-docs/docids-rnd4.txt", cord19DocIdFunction)
                .withWordRemoval();
    }

    /**
     * Took the configuration from Luise but used a template that seems to make more sense
     * @return
     */
    public static Cord19Retrieval jlQEErikRound3() {
        return new Cord19Retrieval(TrecConfig.ELASTIC_CORD19_INDEX).withExperimentName("jlQEErikrnd3")
                .withSize(1500)
                .withResultListSizeCutoff(1000)
                .withStoredFields("cord19_uid", "abstract")
                .withDocIdFunction(r -> (String) r.getSourceFields().get("cord19_uid"))
                .withGoldstandardFilter(new AggregatedTrecQrelGoldStandard<>(TrecCovidGoldStandardFactory.round1(), TrecCovidGoldStandardFactory.round2()), "/id-map/covid-rnd3-changedIds-May19.csv")
                .withUnifyingField("cord19_uid")
                .withJsonTemplate(TEMPLATE_QE_RND4)
                .withValidDocIds("/valid-result-docs/docids-rnd3.txt", cord19DocIdFunction)
                .withWordRemoval();
    }
    /**
     * Took the configuration from Luise but used a template that seems to make more sense
     * @return
     */
    public static Cord19Retrieval jlQEErikRound4() {
        return new Cord19Retrieval(TrecConfig.ELASTIC_CORD19_INDEX).withExperimentName("jlQErnd4")
                .withSize(1500)
                .withResultListSizeCutoff(1000)
                .withStoredFields("cord19_uid", "abstract")
                .withDocIdFunction(r -> (String) r.getSourceFields().get("cord19_uid"))
                .withGoldstandardFilter(TrecCovidGoldStandardFactory.round3Cumulative())
                .withUnifyingField("cord19_uid")
                .withJsonTemplate(TEMPLATE_QE_RND4)
                .withValidDocIds("/valid-result-docs/docids-rnd4.txt", cord19DocIdFunction)
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
                .withGoldstandardFilter(new AggregatedTrecQrelGoldStandard<>(TrecCovidGoldStandardFactory.round1(), TrecCovidGoldStandardFactory.round2()), "/id-map/covid-rnd3-changedIds-May19.csv")
                .withUnifyingField("cord19_uid")
                .withJsonTemplate(TEMPLATE_BASE_RND2, true, true)
                .withQueryQuestionBoW()
                .withValidDocIds("/valid-result-docs/docids-rnd3.txt", cord19DocIdFunction)
                .withResultListSizeCutoff(1000);
    }

    public static Cord19Retrieval jlbasernd4() {
        return new Cord19Retrieval(TrecConfig.ELASTIC_CORD19_INDEX.split(","))
                .withExperimentName("jlbasernd4")
                .withSize(6000)
                .withStoredFields("cord19_uid", "text")
                .withDocIdFunction(r -> (String) r.getSourceFields().get("cord19_uid"))
                .withGoldstandardFilter(TrecCovidGoldStandardFactory.round3Cumulative())
                .withUnifyingField("cord19_uid")
                .withJsonTemplate(TEMPLATE_BASE_RND2, true, true)
                .withQueryQuestionBoW()
                .withValidDocIds("/valid-result-docs/docids-rnd4.txt", cord19DocIdFunction)
                .withResultListSizeCutoff(1000);
    }

    public static Cord19Retrieval jlbasernd5() {
        return new Cord19Retrieval(TrecConfig.ELASTIC_CORD19_INDEX.split(","))
                .withExperimentName("jlbasernd5")
                .withSize(2000)
                .withStoredFields("cord19_uid", "text")
                .withDocIdFunction(r -> (String) r.getSourceFields().get("cord19_uid"))
                .withGoldstandardFilter(TrecCovidGoldStandardFactory.round4Cumulative())
                .withUnifyingField("cord19_uid")
                .withJsonTemplate(TEMPLATE_BASE_RND5, true, true)
                .withQueryQuestionBoW()
                .withValidDocIds("/valid-result-docs/docids-rnd5.txt", cord19DocIdFunction)
                .withResultListSizeCutoff(1000);
    }

    public static Cord19Retrieval jlQERound5() {
        return new Cord19Retrieval(TrecConfig.ELASTIC_CORD19_INDEX).withExperimentName("jlQErnd5")
                .withSize(2000)
                .withStoredFields("cord19_uid")
                .withDocIdFunction(r -> (String) r.getSourceFields().get("cord19_uid"))
                .withGoldstandardFilter(TrecCovidGoldStandardFactory.round4Cumulative())
                .withUnifyingField("cord19_uid")
                .withSubTemplate(TEMPLATE_BASE)
                .withValidDocIds("/valid-result-docs/docids-rnd5.txt", cord19DocIdFunction)
                .withWordRemoval()
                .withResultListSizeCutoff(1000);
    }

    public static Cord19Retrieval jlbasernd5UdelTopics() {
        return new Cord19Retrieval(TrecConfig.ELASTIC_CORD19_INDEX.split(","))
                .withExperimentName("jlbasernd5udeltopics")
                .withSize(2000)
                .withStoredFields("cord19_uid", "title", "abstract", "text")
                .withDocIdFunction(r -> (String) r.getSourceFields().get("cord19_uid"))
                .withGoldstandardFilter(TrecCovidGoldStandardFactory.round4Cumulative())
                .withUnifyingField("cord19_uid")
                .withJsonTemplate(TEMPLATE_BASE_RND5_UDEL, true, true)
                .withRound5Decorator()
                .withValidDocIds("/valid-result-docs/docids-rnd5.txt", cord19DocIdFunction)
                .withResultListSizeCutoff(1000);
    }

}

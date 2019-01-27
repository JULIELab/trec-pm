package at.medunigraz.imi.bst.trec;

import at.medunigraz.imi.bst.trec.experiment.Experiment;
import at.medunigraz.imi.bst.trec.experiment.ExperimentsBuilder;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

public class SuperSigirPubmedRecallExperimenter {


    protected static void runExperiments(Map<String, String> templateProperties, boolean wordremoval, Experiment.GoldStandard goldStandard, Experiment.Task target, int year, String what, String suffix) {
        if (templateProperties.size() > 50)
            throw new IllegalArgumentException("There are more key in the properties map as there are known properties: " + templateProperties.keySet());

        ExperimentsBuilder builder = new ExperimentsBuilder();
        if (!StringUtils.isBlank(what)) {
            builder.setDefaultStatsDir("stats_" + what+"_"+year);
            builder.setDefaultResultsDir("results_" + what+"_"+year);
        }
        final Map<String, TemplateSet> sigirTemplates = getSigirTemplates();

        //addExperimentsWithoutPmClassifier(sigirTemplates, templateProperties, goldStandard, wordremoval, target, year, builder, suffix);
        addExperimentsWithCustomPmClassifierShould(sigirTemplates, templateProperties, goldStandard, wordremoval,  target, year, builder, suffix);
        addExperimentsWithCustomPmClassifierMust(sigirTemplates, templateProperties, goldStandard, wordremoval,  target, year, builder, suffix);

        Set<Experiment> experiments = builder.build();

        for (Experiment exp : experiments) {
            exp.start();
            try {
                exp.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private static Map<String, TemplateSet> getSigirTemplates() {
        final File file = new File(PubmedExperimenter.class.getResource("/templates/sigir19_experiments_biomed").getFile());
        final File[] templateFiles = file.listFiles(f -> !f.getName().equals(".DS_Store"));
        Map<String, TemplateSet> templateMap = new HashMap<>();
        for (File template : templateFiles) {
            String base = template.getName().replace(".json", "").replace("_gspm_must", "").replace("_gspm_should", "").replace("_custompm_must", "").replace("_custompm_should", "");
            final TemplateSet set = templateMap.compute(base, (k, v) -> v != null ? v : new TemplateSet());
            if (template.getName().contains("gspm_must"))
                set.setGspmMust(template);
            else if (template.getName().contains("gspm_should"))
                set.setGspmShould(template);
            else if (template.getName().contains("custompm_must"))
                set.setCusompmMust(template);
            else if (template.getName().contains("custompm_should"))
                set.setCustompmShould(template);
            else
                set.setBase(template);
            templateMap.put(base, set);
        }

        return templateMap;
    }

    private static void addExperimentsWithoutPmClassifier(Map<String, TemplateSet> templates, Map<String, String> templateProperties, Experiment.GoldStandard goldStandard, boolean wordremoval, Experiment.Task target, int year, ExperimentsBuilder builder, String suffix) {
        Function<String, File> getTemplate = name -> templates.get(name).getBase();
        addExperiments(templateProperties, goldStandard, wordremoval,  target, year, builder, getTemplate, suffix);
    }

    private static void addExperimentsWithCustomPmClassifierShould(Map<String, TemplateSet> templates, Map<String, String> templateProperties, Experiment.GoldStandard goldStandard, boolean wordremoval, Experiment.Task target, int year, ExperimentsBuilder builder, String suffix) {
        Function<String, File> getTemplate = name -> templates.get(name).getCustompmShould();
        addExperiments(templateProperties, goldStandard, wordremoval,  target, year, builder, getTemplate, "_custompm_should" + suffix);
    }

    private static void addExperimentsWithCustomPmClassifierMust(Map<String, TemplateSet> templates, Map<String, String> templateProperties, Experiment.GoldStandard goldStandard, boolean wordremoval, Experiment.Task target, int year, ExperimentsBuilder builder, String suffix) {
        Function<String, File> getTemplate = name -> templates.get(name).getCustompmMust();
        addExperiments(templateProperties, goldStandard, wordremoval,  target, year, builder, getTemplate, "_custompm_must" + suffix);
    }

    private static void addExperiments(Map<String, String> templateProperties, Experiment.GoldStandard goldStandard, boolean wordremoval, Experiment.Task target, int year, ExperimentsBuilder builder, Function<String, File> getTemplate, String suffix) {
        File baseline = getTemplate.apply("baseline");
        File baseline_plus_genefield = getTemplate.apply("baseline_plus_genefield");
        File with_pos_boosters = getTemplate.apply("with_pos_boosters");
        File with_pos_neg_boosters = getTemplate.apply("with_pos_neg_boosters");
        File with_pos_neg_boosters_additional_signals = getTemplate.apply("with_pos_neg_boosters_additional_signals");
        File with_pos_neg_boosters_additional_signals_extra = getTemplate.apply("with_pos_neg_boosters_additional_signals_extra");
        File with_pos_neg_boosters_additional_signals_extra_nonmel = getTemplate.apply("with_pos_neg_boosters_additional_signals_extra_nonmel");

        builder.newExperiment().withName("baseline" + suffix).withYear(year).withGoldStandard(goldStandard).withTarget(target)
                .withSubTemplate(baseline, templateProperties);
        if (wordremoval) builder.withWordRemoval();

        builder.newExperiment().withName("dissyn" + suffix).withYear(year).withGoldStandard(goldStandard).withTarget(target)
                .withSubTemplate(baseline, templateProperties).withDiseaseSynonym();
        if (wordremoval) builder.withWordRemoval();

        builder.newExperiment().withName("dishyper" + suffix).withYear(year).withGoldStandard(goldStandard).withTarget(target)
                .withSubTemplate(baseline, templateProperties).withDiseaseHypernym();
        if (wordremoval) builder.withWordRemoval();

        builder.newExperiment().withName("dissynpt" + suffix).withYear(year).withGoldStandard(goldStandard).withTarget(target)
                .withSubTemplate(baseline, templateProperties).withDiseasePreferredTerm().withDiseaseSynonym();
        if (wordremoval) builder.withWordRemoval();

        builder.newExperiment().withName("dissynpthyper" + suffix).withYear(year).withGoldStandard(goldStandard).withTarget(target)
                .withSubTemplate(baseline, templateProperties).withDiseasePreferredTerm().withDiseaseSynonym().withDiseaseHypernym();
        if (wordremoval) builder.withWordRemoval();

        builder.newExperiment().withName("gensyn" + suffix).withYear(year).withGoldStandard(goldStandard).withTarget(target)
                .withSubTemplate(baseline, templateProperties).withGeneSynonym();
        if (wordremoval) builder.withWordRemoval();

        builder.newExperiment().withName("gendis" + suffix).withYear(year).withGoldStandard(goldStandard).withTarget(target)
                .withSubTemplate(baseline, templateProperties).withDiseasePreferredTerm().withDiseaseSynonym().withDiseaseHypernym().withGeneSynonym().withGeneDescription();
        if (wordremoval) builder.withWordRemoval();






        builder.newExperiment().withName("gensyndesc" + suffix).withYear(year).withGoldStandard(goldStandard).withTarget(target)
                .withSubTemplate(baseline, templateProperties).withGeneSynonym().withGeneDescription();
        if (wordremoval) builder.withWordRemoval();

        builder.newExperiment().withName("gensyndescplus" + suffix).withYear(year).withGoldStandard(goldStandard).withTarget(target)
                .withSubTemplate(baseline_plus_genefield, templateProperties).withGeneSynonym().withGeneDescription();
        if (wordremoval) builder.withWordRemoval();

        builder.newExperiment().withName("dgint" + suffix).withYear(year).withGoldStandard(goldStandard).withTarget(target)
                .withSubTemplate(baseline, templateProperties).withDrugInteraction();
        if (wordremoval) builder.withWordRemoval();

        builder.newExperiment().withName("gendisdgint" + suffix).withYear(year).withGoldStandard(goldStandard).withTarget(target)
                .withSubTemplate(baseline, templateProperties).withDiseasePreferredTerm().withDiseaseSynonym().withDiseaseHypernym().withGeneSynonym().withGeneDescription().withDrugInteraction();
        if (wordremoval) builder.withWordRemoval();






//        builder.newExperiment().withName("genedispb" + suffix).withYear(year).withGoldStandard(goldStandard).withTarget(target)
//                .withSubTemplate(with_pos_boosters, templateProperties).withDiseasePreferredTerm().withDiseaseSynonym().withGeneSynonym().withGeneDescription();
//
//        builder.newExperiment().withName("genespb" + suffix).withYear(year).withGoldStandard(goldStandard).withTarget(target)
//                .withSubTemplate(with_pos_boosters, templateProperties).withGeneSynonym().withGeneDescription();
//
//        builder.newExperiment().withName("genedispbnb" + suffix).withYear(year).withGoldStandard(goldStandard).withTarget(target)
//                .withSubTemplate(with_pos_neg_boosters, templateProperties).withGeneSynonym().withGeneDescription().withDiseaseSynonym().withDiseasePreferredTerm();
//
//        builder.newExperiment().withName("posnegbstadd" + suffix).withYear(year).withGoldStandard(goldStandard).withTarget(target)
//                .withSubTemplate(with_pos_neg_boosters_additional_signals, templateProperties).withGeneSynonym().withGeneDescription().withDiseaseSynonym().withDiseasePreferredTerm();
//
//        builder.newExperiment().withName("posnegbstaddextra" + suffix).withYear(year).withGoldStandard(goldStandard).withTarget(target)
//                .withSubTemplate(with_pos_neg_boosters_additional_signals_extra, templateProperties).withGeneSynonym().withGeneDescription().withDiseaseSynonym().withDiseasePreferredTerm();
//
//        builder.newExperiment().withName("addextranonmel" + suffix).withYear(year).withGoldStandard(goldStandard).withTarget(target)
//                .withSubTemplate(with_pos_neg_boosters_additional_signals_extra_nonmel, templateProperties).withGeneSynonym().withGeneDescription().withDiseaseSynonym().withDiseasePreferredTerm();
//
//        builder.newExperiment().withName("addextranonmelshould" + suffix).withYear(year).withGoldStandard(goldStandard).withTarget(target)
//                .withSubTemplate(with_pos_neg_boosters_additional_signals_extra_nonmel_should, templateProperties).withGeneSynonym().withGeneDescription().withDiseaseSynonym().withDiseasePreferredTerm();


//        builder.newExperiment().withName("hpipubnone_replique").withYear(year).withGoldStandard(goldStandard).withTarget(target)
//                .withSubTemplate(with_pos_neg_boosters_additional_signals_extra_nonmel, templateProperties).withWordRemoval().withGeneSynonym()
//                .withDiseasePreferredTerm().withGeneDescription().withDiseaseSynonym();

//        builder.newExperiment().withName("hpipubnone_replique" + suffix).withYear(year).withGoldStandard(goldStandard).withTarget(target)
//                .withSubTemplate(with_pos_neg_boosters_additional_signals_extra_nonmel, templateProperties).withWordRemoval().withGeneSynonym()
//                .withDiseasePreferredTerm().withGeneDescription().withDiseaseSynonym();
    }
    private static class TemplateSet {
        private File base;
        private File gspmMust;
        private File gspmShould;
        private File custompmShould;
        private File custompmMust;


        public File getBase() {
            return base;
        }

        public void setBase(File base) {
            this.base = base;
        }

        public File getGspmMust() {
            if (gspmMust == null)
                throw new IllegalStateException("The GSPM MUST template for the base template " + base.getAbsolutePath() + " is null");
            return gspmMust;
        }

        public void setGspmMust(File gspmMust) {
            this.gspmMust = gspmMust;
        }

        public File getGspmShould() {
            if (gspmShould == null)
                throw new IllegalStateException("The GSPM SHOULD template for the base template " + base.getAbsolutePath() + " is null");
            return gspmShould;
        }

        public void setGspmShould(File gspmShould) {
            this.gspmShould = gspmShould;
        }

        public File getCustompmShould() {
            return custompmShould;
        }

        public void setCustompmShould(File custompmShould) {

            this.custompmShould = custompmShould;
        }

        public File getCustompmMust() {
            return custompmMust;
        }

        public void setCusompmMust(File custompmMust) {

            this.custompmMust = custompmMust;
        }
    }
}

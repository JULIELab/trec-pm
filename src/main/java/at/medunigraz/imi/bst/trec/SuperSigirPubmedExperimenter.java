package at.medunigraz.imi.bst.trec;

import at.medunigraz.imi.bst.trec.experiment.Experiment;
import at.medunigraz.imi.bst.trec.experiment.ExperimentsBuilder;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

public class SuperSigirPubmedExperimenter {


    protected static void runExperiments(Map<String, String> templateProperties, Experiment.GoldStandard goldStandard, Experiment.Task target, int year, String what, String suffix) {
        if (templateProperties.size() > 22)
            throw new IllegalArgumentException("There are more key in the properties map as there are known properties: " + templateProperties.keySet());

        ExperimentsBuilder builder = new ExperimentsBuilder();
        if (!StringUtils.isBlank(what))
            builder.setDefaultStatsDir("stats_" + what);
        final Map<String, TemplateSet> sigirTemplates = getSigirTemplates();

        addExperimentsWithoutPmClassifier(sigirTemplates, templateProperties, goldStandard, target, year, builder, suffix);
//        addExperimentsWithGoldStandardPmClassifierShould(sigirTemplates, templateProperties, goldStandard, target, year, builder, suffix);
//        addExperimentsWithGoldStandardPmClassifierMust(sigirTemplates, templateProperties, goldStandard, target, year, builder, suffix);
//        addExperimentsWithCustomPmClassifierShould(sigirTemplates, templateProperties, goldStandard, target, year, builder, suffix);
//        addExperimentsWithCustomPmClassifierMust(sigirTemplates, templateProperties, goldStandard, target, year, builder, suffix);

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

    private static void addExperimentsWithoutPmClassifier(Map<String, TemplateSet> templates, Map<String, String> templateProperties, Experiment.GoldStandard goldStandard, Experiment.Task target, int year, ExperimentsBuilder builder, String suffix) {
        Function<String, File> getTemplate = name -> templates.get(name).getBase();
        addExperiments(templateProperties, goldStandard, target, year, builder, getTemplate, suffix);
    }

    private static void addExperimentsWithGoldStandardPmClassifierShould(Map<String, TemplateSet> templates, Map<String, String> templateProperties, Experiment.GoldStandard goldStandard, Experiment.Task target, int year, ExperimentsBuilder builder, String suffix) {
        Function<String, File> getTemplate = name -> templates.get(name).getGspmShould();
        addExperiments(templateProperties, goldStandard, target, year, builder, getTemplate, "_gspm_should" + suffix);
    }

    private static void addExperimentsWithGoldStandardPmClassifierMust(Map<String, TemplateSet> templates, Map<String, String> templateProperties, Experiment.GoldStandard goldStandard, Experiment.Task target, int year, ExperimentsBuilder builder, String suffix) {
        Function<String, File> getTemplate = name -> templates.get(name).getGspmMust();
        addExperiments(templateProperties, goldStandard, target, year, builder, getTemplate, "_gspm_must" + suffix);
    }

    private static void addExperimentsWithCustomPmClassifierShould(Map<String, TemplateSet> templates, Map<String, String> templateProperties, Experiment.GoldStandard goldStandard, Experiment.Task target, int year, ExperimentsBuilder builder, String suffix) {
        Function<String, File> getTemplate = name -> templates.get(name).getCustompmShould();
        addExperiments(templateProperties, goldStandard, target, year, builder, getTemplate, "_custompm_should" + suffix);
    }

    private static void addExperimentsWithCustomPmClassifierMust(Map<String, TemplateSet> templates, Map<String, String> templateProperties, Experiment.GoldStandard goldStandard, Experiment.Task target, int year, ExperimentsBuilder builder, String suffix) {
        Function<String, File> getTemplate = name -> templates.get(name).getCustompmMust();
        addExperiments(templateProperties, goldStandard, target, year, builder, getTemplate, "_custompm_must" + suffix);
    }

    private static void addExperiments(Map<String, String> templateProperties, Experiment.GoldStandard goldStandard, Experiment.Task target, int year, ExperimentsBuilder builder, Function<String, File> getTemplate, String suffix) {
        File baseline = getTemplate.apply("baseline");
        File baseline_only_genefield = getTemplate.apply("baseline_only_genefield");
        File baseline_plus_genefield = getTemplate.apply("baseline_plus_genefield");
        File with_pos_boosters = getTemplate.apply("with_pos_boosters");
        File with_pos_neg_boosters = getTemplate.apply("with_pos_neg_boosters");
        File with_pos_neg_boosters_additional_signals = getTemplate.apply("with_pos_neg_boosters_additional_signals");
        File with_pos_neg_boosters_additional_signals_extra = getTemplate.apply("with_pos_neg_boosters_additional_signals_extra");
        File with_pos_neg_boosters_additional_signals_extra_nonmel = getTemplate.apply("with_pos_neg_boosters_additional_signals_extra_nonmel");

//        builder.newExperiment().withName("baseline" + suffix).withYear(year).withGoldStandard(goldStandard).withTarget(target)
//                .withSubTemplate(baseline, templateProperties);
//
//        builder.newExperiment().withName("baselinewr" + suffix).withYear(year).withGoldStandard(goldStandard).withTarget(target)
//                .withSubTemplate(baseline, templateProperties).withWordRemoval();
//
//        builder.newExperiment().withName("diseases" + suffix).withYear(year).withGoldStandard(goldStandard).withTarget(target)
//                .withSubTemplate(baseline, templateProperties).withWordRemoval().withDiseasePreferredTerm().withDiseaseSynonym();
//
//        builder.newExperiment().withName("diseasesptb" + suffix).withYear(year).withGoldStandard(goldStandard).withTarget(target)
//                .withSubTemplate(baseline, templateProperties).withWordRemoval().withDiseasePreferredTerm().withDiseaseSynonym();
//
//        builder.newExperiment().withName("genesnodesc" + suffix).withYear(year).withGoldStandard(goldStandard).withTarget(target)
//                .withSubTemplate(baseline, templateProperties).withWordRemoval().withGeneSynonym();
//
//        builder.newExperiment().withName("genes" + suffix).withYear(year).withGoldStandard(goldStandard).withTarget(target)
//                .withSubTemplate(baseline, templateProperties).withWordRemoval().withGeneSynonym().withGeneDescription();
//
//        builder.newExperiment().withName("genesplus" + suffix).withYear(year).withGoldStandard(goldStandard).withTarget(target)
//                .withSubTemplate(baseline_plus_genefield, templateProperties).withWordRemoval().withGeneSynonym().withGeneDescription();
//
//        builder.newExperiment().withName("genesonly" + suffix).withYear(year).withGoldStandard(goldStandard).withTarget(target)
//                .withSubTemplate(baseline_only_genefield, templateProperties).withWordRemoval().withGeneSynonym().withGeneDescription();
//
//        builder.newExperiment().withName("genedis" + suffix).withYear(year).withGoldStandard(goldStandard).withTarget(target)
//                .withSubTemplate(baseline, templateProperties).withWordRemoval().withDiseasePreferredTerm().withDiseaseSynonym().withGeneSynonym().withGeneDescription();
//
//        builder.newExperiment().withName("genedispb" + suffix).withYear(year).withGoldStandard(goldStandard).withTarget(target)
//                .withSubTemplate(with_pos_boosters, templateProperties).withWordRemoval().withDiseasePreferredTerm().withDiseaseSynonym().withGeneSynonym().withGeneDescription();
//
//        builder.newExperiment().withName("genespb" + suffix).withYear(year).withGoldStandard(goldStandard).withTarget(target)
//                .withSubTemplate(with_pos_boosters, templateProperties).withWordRemoval().withGeneSynonym().withGeneDescription();
//
//        builder.newExperiment().withName("genedispbnb" + suffix).withYear(year).withGoldStandard(goldStandard).withTarget(target)
//                .withSubTemplate(with_pos_neg_boosters, templateProperties).withWordRemoval().withGeneSynonym().withGeneDescription().withDiseaseSynonym().withDiseasePreferredTerm();
//
//        builder.newExperiment().withName("posnegbstadd" + suffix).withYear(year).withGoldStandard(goldStandard).withTarget(target)
//                .withSubTemplate(with_pos_neg_boosters_additional_signals, templateProperties).withWordRemoval().withGeneSynonym().withGeneDescription().withDiseaseSynonym().withDiseasePreferredTerm();
//
//        builder.newExperiment().withName("posnegbstaddextra" + suffix).withYear(year).withGoldStandard(goldStandard).withTarget(target)
//                .withSubTemplate(with_pos_neg_boosters_additional_signals_extra, templateProperties).withWordRemoval().withGeneSynonym().withGeneDescription().withDiseaseSynonym().withDiseasePreferredTerm();

        builder.newExperiment().withName("addextranonmel" + suffix).withYear(year).withGoldStandard(goldStandard).withTarget(target)
                .withSubTemplate(with_pos_neg_boosters_additional_signals_extra_nonmel, templateProperties).withWordRemoval().withGeneSynonym().withGeneDescription().withDiseaseSynonym().withDiseasePreferredTerm();
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

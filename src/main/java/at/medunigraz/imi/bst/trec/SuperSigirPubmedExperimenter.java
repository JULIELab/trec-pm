package at.medunigraz.imi.bst.trec;

import at.medunigraz.imi.bst.trec.experiment.Experiment;
import at.medunigraz.imi.bst.trec.experiment.ExperimentsBuilder;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

public class SuperSigirPubmedExperimenter {


    protected static void runExperiments(Map<String, String> templateProperties, Experiment.GoldStandard goldStandard, Experiment.Task target, int year) {


        ExperimentsBuilder builder = new ExperimentsBuilder();
        final Map<String, TemplateSet> sigirTemplates = getSigirTemplates();

        addExperimentsWithoutPmClassifier(sigirTemplates, templateProperties, goldStandard, target, year, builder);
        addExperimentsWithGoldStandardPmClassifierShould(sigirTemplates, templateProperties, goldStandard, target, year, builder);
        addExperimentsWithGoldStandardPmClassifierMust(sigirTemplates, templateProperties, goldStandard, target, year, builder);


//        builder.newExperiment().withName("hpipubboost_must").withYear(year).withGoldStandard(goldStandard).withTarget(target)
//                .withSubTemplate(hpipubboost_must).withWordRemoval().withGeneSynonym()
//                .withDiseasePreferredTerm().withGeneDescription().withDiseaseSynonym();
//
//        builder.newExperiment().withName("hpipubgspm").withYear(year).withGoldStandard(goldStandard).withTarget(target)
//                .withSubTemplate(hpipub_gspm_must, "pm_gs_boost", "1").withWordRemoval().withGeneSynonym()
//                .withDiseasePreferredTerm().withGeneDescription().withDiseaseSynonym();
//
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
            String base = template.getName().replace("gspm_must", "").replace("gspm_should", "");
            final TemplateSet set = templateMap.compute(base, (k, v) -> v != null ? v : new TemplateSet());
            if (template.getName().contains("gspm_must"))
                set.setGspmMust(template);
            else if (template.getName().contains("gspm_should"))
                set.setGspmShould(template);
            else set.setBase(template);
            templateMap.put(base, set);
        }

        return templateMap;
    }

    private static void addExperimentsWithoutPmClassifier(Map<String, TemplateSet> templates, Map<String, String> templateProperties, Experiment.GoldStandard goldStandard, Experiment.Task target, int year, ExperimentsBuilder builder) {
        Function<String, File> getTemplate = name -> templates.get(name).getBase();
        addExperiments(templateProperties, goldStandard, target, year, builder, getTemplate);
    }

    private static void addExperimentsWithGoldStandardPmClassifierShould(Map<String, TemplateSet> templates, Map<String, String> templateProperties, Experiment.GoldStandard goldStandard, Experiment.Task target, int year, ExperimentsBuilder builder) {
        Function<String, File> getTemplate = name -> templates.get(name).getGspmShould();
        addExperiments(templateProperties, goldStandard, target, year, builder, getTemplate);
    }

    private static void addExperimentsWithGoldStandardPmClassifierMust(Map<String, TemplateSet> templates, Map<String, String> templateProperties, Experiment.GoldStandard goldStandard, Experiment.Task target, int year, ExperimentsBuilder builder) {
        Function<String, File> getTemplate = name -> templates.get(name).getGspmMust();
        addExperiments(templateProperties, goldStandard, target, year, builder, getTemplate);
    }

    private static void addExperiments(Map<String, String> templateProperties, Experiment.GoldStandard goldStandard, Experiment.Task target, int year, ExperimentsBuilder builder, Function<String, File> getTemplate) {
        File baseline = getTemplate.apply("baseline");
        File baseline_only_genefield = getTemplate.apply("baseline_only_genefield");
        File baseline_plus_genefield = getTemplate.apply("baseline_plus_genefield");
        File with_pos_boosters = getTemplate.apply("with_pos_boosters");
        File with_pos_neg_boosters = getTemplate.apply("with_pos_neg_boosters");
        File with_pos_neg_boosters_additional_signals = getTemplate.apply("with_pos_neg_boosters_additional_signals");

        builder.newExperiment().withName("baseline").withYear(year).withGoldStandard(goldStandard).withTarget(target)
                .withSubTemplate(baseline, templateProperties);

        builder.newExperiment().withName("baselinewr").withYear(year).withGoldStandard(goldStandard).withTarget(target)
                .withSubTemplate(baseline, templateProperties).withWordRemoval();

        builder.newExperiment().withName("diseases").withYear(year).withGoldStandard(goldStandard).withTarget(target)
                .withSubTemplate(baseline, templateProperties).withWordRemoval().withDiseasePreferredTerm().withDiseaseSynonym();

        builder.newExperiment().withName("diseaseslptb").withYear(year).withGoldStandard(goldStandard).withTarget(target)
                .withSubTemplate(baseline, templateProperties).withWordRemoval().withDiseasePreferredTerm().withDiseaseSynonym();

        builder.newExperiment().withName("genesnodesc").withYear(year).withGoldStandard(goldStandard).withTarget(target)
                .withSubTemplate(baseline, templateProperties).withWordRemoval().withGeneSynonym();

        builder.newExperiment().withName("genes").withYear(year).withGoldStandard(goldStandard).withTarget(target)
                .withSubTemplate(baseline, templateProperties).withWordRemoval().withGeneSynonym().withGeneDescription();

        builder.newExperiment().withName("genesplus").withYear(year).withGoldStandard(goldStandard).withTarget(target)
                .withSubTemplate(baseline_plus_genefield, templateProperties).withWordRemoval().withGeneSynonym().withGeneDescription();

        builder.newExperiment().withName("genesonly").withYear(year).withGoldStandard(goldStandard).withTarget(target)
                .withSubTemplate(baseline_only_genefield, templateProperties).withWordRemoval().withGeneSynonym().withGeneDescription();

        builder.newExperiment().withName("genedis").withYear(year).withGoldStandard(goldStandard).withTarget(target)
                .withSubTemplate(baseline, templateProperties).withWordRemoval().withDiseasePreferredTerm().withDiseaseSynonym().withGeneSynonym().withGeneDescription();

        builder.newExperiment().withName("genedispb").withYear(year).withGoldStandard(goldStandard).withTarget(target)
                .withSubTemplate(with_pos_boosters, templateProperties).withWordRemoval().withDiseasePreferredTerm().withDiseaseSynonym().withGeneSynonym().withGeneDescription();

        builder.newExperiment().withName("genespb").withYear(year).withGoldStandard(goldStandard).withTarget(target)
                .withSubTemplate(with_pos_boosters, templateProperties).withWordRemoval().withGeneSynonym().withGeneDescription();

        builder.newExperiment().withName("genedispb").withYear(year).withGoldStandard(goldStandard).withTarget(target)
                .withSubTemplate(with_pos_boosters, templateProperties).withWordRemoval().withGeneSynonym().withGeneDescription().withDiseaseSynonym().withDiseasePreferredTerm();

        builder.newExperiment().withName("genedispbnb").withYear(year).withGoldStandard(goldStandard).withTarget(target)
                .withSubTemplate(with_pos_neg_boosters, templateProperties).withWordRemoval().withGeneSynonym().withGeneDescription().withDiseaseSynonym().withDiseasePreferredTerm();

        builder.newExperiment().withName("genedisbstadd").withYear(year).withGoldStandard(goldStandard).withTarget(target)
                .withSubTemplate(with_pos_neg_boosters_additional_signals, templateProperties).withWordRemoval().withGeneSynonym().withGeneDescription().withDiseaseSynonym().withDiseasePreferredTerm();
    }

    private static class TemplateSet {
        private File base;
        private File gspmMust;
        private File gspmShould;


        public File getBase() {
            return base;
        }

        public void setBase(File base) {
            this.base = base;
        }

        public File getGspmMust() {
            return gspmMust;
        }

        public void setGspmMust(File gspmMust) {
            this.gspmMust = gspmMust;
        }

        public File getGspmShould() {
            return gspmShould;
        }

        public void setGspmShould(File gspmShould) {
            this.gspmShould = gspmShould;
        }
    }
}

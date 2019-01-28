package at.medunigraz.imi.bst.trec;

import at.medunigraz.imi.bst.trec.experiment.Experiment;
import at.medunigraz.imi.bst.trec.experiment.ExperimentsBuilder;
import bsh.commands.dir;
import com.google.common.collect.Sets;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

public class SuperSigirPubmedRecallExperimenter {
    private static final Logger LOG = LogManager.getLogger();
    private static int numProperties = 51;

    protected static void runPmClassifierExperiments(File template, Map<String, String> templateProperties, Experiment.GoldStandard goldStandard, Experiment.Task target, int year, String what, String suffix) {
        if (templateProperties.size() > numProperties)
            throw new IllegalArgumentException("There are more key in the properties map as there are known properties: " + templateProperties.keySet());

        ExperimentsBuilder builder = new ExperimentsBuilder();
        if (!StringUtils.isBlank(what)) {
            builder.setDefaultStatsDir("stats_" + what + "_" + year);
            builder.setDefaultResultsDir("results_" + what + "_" + year);
        }
        final Map<String, TemplateSet> sigirTemplates = getSigirTemplates("/templates/sigir19_pmclass_biomed");

        final Set<Set<Expansion>> expansionSets = new HashSet<>(Sets.powerSet(EnumSet.of(Expansion.DGI, Expansion.GDE, Expansion.GSY, Expansion.DSY, Expansion.WR)));

        addExperiments(template, sigirTemplates, templateProperties, expansionSets, goldStandard, target, year, builder, suffix);

        Set<Experiment> experiments = builder.build();
        final ExecutorService executorService = Executors.newFixedThreadPool(2);
        List<Future<?>> futures = new ArrayList<>();
        for (Experiment exp : experiments) {
            futures.add(executorService.submit(exp));
        }

        for (Future<?> f : futures) {
            try {
                f.get();
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        }
        executorService.shutdown();
    }

    /**
     * For experiments that try different query boosts.
     *
     * @param template
     * @param templateProperties
     * @param goldStandard
     * @param target
     * @param year
     * @param what
     * @param suffix
     */
    protected static void runBoostExperiments(String template, Map<String, String> templateProperties, Experiment.GoldStandard goldStandard, Experiment.Task target, int year, String what, String suffix) {
        if (templateProperties.size() > numProperties)
            throw new IllegalArgumentException("There are more key in the properties map as there are known properties: " + templateProperties.keySet());

        ExperimentsBuilder builder = new ExperimentsBuilder();
        if (!StringUtils.isBlank(what)) {
            builder.setDefaultStatsDir("stats_" + what + "_" + year);
            builder.setDefaultResultsDir("results_" + what + "_" + year);
        }
        final File file = new File(PubmedExperimenter.class.getResource(template).getFile());
        Map<String, TemplateSet> sigirTemplates;
        if (file.isDirectory()) {
            sigirTemplates = getSigirTemplates(template);
        } else {
            final TemplateSet templateSet = new TemplateSet();
            templateSet.setBase(file);
            sigirTemplates = new HashMap<>();
            sigirTemplates.put(file.getName(), templateSet);
        }


        // Switch everything on - except word removal - so that the boosters actually have a point.
        final Set<Set<Expansion>> expansionSets = new HashSet<>(Arrays.asList(EnumSet.complementOf(EnumSet.of(Expansion.WR))));

        addExperiments(null, sigirTemplates, templateProperties, expansionSets, goldStandard, target, year, builder, suffix);

        Set<Experiment> experiments = builder.build();
        final ExecutorService executorService = Executors.newFixedThreadPool(5);
        List<Future<?>> futures = new ArrayList<>();
        for (Experiment exp : experiments) {
            futures.add(executorService.submit(exp));
        }

        for (Future<?> f : futures) {
            try {
                f.get();
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        }
        executorService.shutdown();
    }

    /**
     * For experiments with different boosting signals in the form of additional SHOULD queries.
     *
     * @param templateProperties
     * @param goldStandard
     * @param target
     * @param year
     * @param what
     * @param suffix
     */
    protected static void runTermBoostExperiments(Map<String, String> templateProperties, Experiment.GoldStandard goldStandard, Experiment.Task target, int year, String what, String suffix) {
        if (templateProperties.size() > numProperties)
            throw new IllegalArgumentException("There are more key in the properties map as there are known properties: " + templateProperties.keySet());

        ExperimentsBuilder builder = new ExperimentsBuilder();
        if (!StringUtils.isBlank(what)) {
            builder.setDefaultStatsDir("stats_" + what + "_" + year);
            builder.setDefaultResultsDir("results_" + what + "_" + year);
        }
        final Map<String, TemplateSet> sigirTemplates = getSigirTemplates("/templates/sigir19_experiments_biomed");

        final Set<Set<Expansion>> expansionSets = new HashSet<>(Sets.powerSet(EnumSet.of(Expansion.DGI, Expansion.GDE, Expansion.GSY, Expansion.DSY, Expansion.WR)));
        // Only make those sets that actually contain a positive boost, namely the drug interactions and the gene descriptions
        for (Iterator<Set<Expansion>> it = expansionSets.iterator(); it.hasNext(); ) {
            final Set<Expansion> set = it.next();
            if (!set.contains(Expansion.DGI) && !set.contains(Expansion.GDE))
                it.remove();
        }

        addExperiments(null, sigirTemplates, templateProperties, expansionSets, goldStandard, target, year, builder, suffix);

        Set<Experiment> experiments = builder.build();
        final ExecutorService executorService = Executors.newFixedThreadPool(5);
        List<Future<?>> futures = new ArrayList<>();
        for (Experiment exp : experiments) {
            futures.add(executorService.submit(exp));
        }

        for (Future<?> f : futures) {
            try {
                f.get();
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        }
        executorService.shutdown();
    }

    /**
     * Experiments with the base template and different settings for topic expansions
     *
     * @param templateProperties
     * @param goldStandard
     * @param target
     * @param year
     * @param what
     * @param suffix
     */
    protected static void runRecallExperiments(Map<String, String> templateProperties, Experiment.GoldStandard goldStandard, Experiment.Task target, int year, String what, String suffix) {
        if (templateProperties.size() > numProperties)
            throw new IllegalArgumentException("There are more key in the properties map as there are known properties: " + templateProperties.keySet());

        ExperimentsBuilder builder = new ExperimentsBuilder();
        if (!StringUtils.isBlank(what)) {
            builder.setDefaultStatsDir("stats_" + what + "_" + year);
            builder.setDefaultResultsDir("results_" + what + "_" + year);
        }
        final Map<String, TemplateSet> sigirTemplates = getSigirTemplates("/templates/sigir19_experiments_biomed");

        final Set<Set<Expansion>> expansionSets = Sets.powerSet(EnumSet.complementOf(EnumSet.of(Expansion.DGI, Expansion.GDE)));

        File baselineTemplate = new File(PubmedExperimenter.class.getResource("/templates/sigir19_experiments_biomed/baseline.json").getFile());
        addExperiments(baselineTemplate, sigirTemplates, templateProperties, expansionSets, goldStandard, target, year, builder, suffix);

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

    private static Map<String, TemplateSet> getSigirTemplates(String dir) {
        final File file = new File(PubmedExperimenter.class.getResource(dir).getFile());
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


    private static void addExperiments(File singleTemplate, Map<String, TemplateSet> templates, Map<String, String> templateProperties, Set<Set<Expansion>> expansionSets, Experiment.GoldStandard goldStandard, Experiment.Task target, int year, ExperimentsBuilder builder, String suffix) {
        Map<String, TemplateSet> effectiveTemplates = templates;
        if (singleTemplate != null) {
            effectiveTemplates = new HashMap<>();
            final TemplateSet templateSet = new TemplateSet();
            templateSet.setBase(singleTemplate);
        }
        for (TemplateSet templateSet : effectiveTemplates.values()) {
            final File template = templateSet.getBase();
            if (template == null) {
                LOG.debug("Skipping template set {} because it does not have the required template derivative", templateSet);
                continue;
            }

            LOG.debug("Creating experiments with template {}", template);
            for (Set<Expansion> expansions : expansionSets) {
                builder.newExperiment().withYear(year).withGoldStandard(goldStandard).withTarget(target)
                        .withSubTemplate(template, templateProperties);
                // This is the default name to indicate that no expansion is enabled
                builder.withName(template.getName() + "-NONE" + suffix);

                for (Expansion expansion : expansions) {
                    switch (expansion) {
                        case DSY:
                            builder.withDiseaseSynonym();
                            break;
                        case DHY:
                            builder.withDiseaseHypernym();
                            break;
                        case DP:
                            builder.withDiseasePreferredTerm();
                            break;
                        case GSY:
                            builder.withGeneSynonym();
                            break;
                        case GDE:
                            builder.withGeneDescription();
                            break;
                        case WR:
                            builder.withWordRemoval();
                            break;
                        case DGI:
                            builder.withDrugInteraction();
                            break;
                    }
                    String name = expansions.stream().sorted().map(Expansion::name).collect(Collectors.joining("_"));
                    builder.withName(template.getName() + "-" + name + suffix);
                }
            }
        }
    }

    public enum Expansion {DSY, DHY, DP, GSY, GDE, WR, DGI}

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

        @Override
        public String toString() {
            return "TemplateSet{" +
                    "base=" + base +
                    ", gspmMust=" + gspmMust +
                    ", gspmShould=" + gspmShould +
                    ", custompmShould=" + custompmShould +
                    ", custompmMust=" + custompmMust +
                    '}';
        }

        public File getCustompmMust() {
            return custompmMust;
        }

        public void setCusompmMust(File custompmMust) {

            this.custompmMust = custompmMust;
        }
    }
}

package at.medunigraz.imi.bst.trec.experiment;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import at.medunigraz.imi.bst.trec.model.Gene;
import at.medunigraz.imi.bst.trec.query.*;

public class ExperimentsBuilder {

    private Set<Experiment> experiments = new HashSet<>();

    private Experiment buildingExp = null;

    public ExperimentsBuilder() {
    }

    public ExperimentsBuilder newExperiment() {
        validate();
        buildingExp = new Experiment();
        return this;
    }

    public ExperimentsBuilder withName(String name) {
        buildingExp.setExperimentName(name);
        return this;
    }

    @Deprecated
    public ExperimentsBuilder withDecorator(Query decorator) {
        buildingExp.setDecorator(decorator);
        return this;
    }

    public ExperimentsBuilder withTemplate(File template) {
        Query previousDecorator = buildingExp.getDecorator();
        buildingExp.setDecorator(new TemplateQueryDecorator(template, previousDecorator));
        return this;
    }

    public ExperimentsBuilder withTemplate(File template, String... properties) {
        Query previousDecorator = buildingExp.getDecorator();
        buildingExp.setDecorator(new TemplateQueryDecorator(template, previousDecorator, array2Map(properties)));
        return this;
    }

    public ExperimentsBuilder withSubTemplate(File template) {
        Query previousDecorator = buildingExp.getDecorator();
        buildingExp.setDecorator(new SubTemplateQueryDecorator(template, previousDecorator));
        return this;
    }

    public ExperimentsBuilder withSubTemplate(File template, Map<String, String> properties) {
        Query previousDecorator = buildingExp.getDecorator();
        buildingExp.setDecorator(new SubTemplateQueryDecorator(template, previousDecorator, properties));
        return this;
    }

    public ExperimentsBuilder withSubTemplate(File template, String... properties) {
        return withSubTemplate(template, array2Map(properties));
    }

    public ExperimentsBuilder withKeyword(String value) {
        Query previousDecorator = buildingExp.getDecorator();

        Map<String, String> keymap = new HashMap<>();
        keymap.put("keyword", value);
        buildingExp.setDecorator(new StaticMapQueryDecorator(keymap, previousDecorator));

        return this;
    }

    public ExperimentsBuilder withWordRemoval() {
        Query previousDecorator = buildingExp.getDecorator();
        buildingExp.setDecorator(new WordRemovalQueryDecorator(previousDecorator));
        return this;
    }

    public ExperimentsBuilder withGeneExpansion(Gene.Field[] expandTo) {
        Query previousDecorator = buildingExp.getDecorator();
        buildingExp.setDecorator(new GeneExpanderQueryDecorator(expandTo, previousDecorator));
        return this;
    }

    public ExperimentsBuilder withDiseaseReplacer() {
        Query previousDecorator = buildingExp.getDecorator();
        buildingExp.setDecorator(new DiseaseReplacerQueryDecorator(previousDecorator));
        return this;
    }

    public ExperimentsBuilder withDiseaseExpander() {
        Query previousDecorator = buildingExp.getDecorator();
        buildingExp.setDecorator(new DiseaseExpanderQueryDecorator(previousDecorator));
        return this;
    }

    public ExperimentsBuilder withDiseasePreferredTerm() {
        Query previousDecorator = buildingExp.getDecorator();
        buildingExp.setDecorator(new DiseasePreferredTermQueryDecorator(previousDecorator));
        return this;
    }

    public ExperimentsBuilder withDiseaseSynonym() {
        Query previousDecorator = buildingExp.getDecorator();
        buildingExp.setDecorator(new DiseaseSynonymQueryDecorator(previousDecorator));
        return this;
    }

    public ExperimentsBuilder withGeneSynonym() {
        Query previousDecorator = buildingExp.getDecorator();
        buildingExp.setDecorator(new GeneSynonymQueryDecorator(previousDecorator));
        return this;
    }

    public ExperimentsBuilder withGeneDescription() {
        Query previousDecorator = buildingExp.getDecorator();
        buildingExp.setDecorator(new GeneDescriptionQueryDecorator(previousDecorator));
        return this;
    }

    public ExperimentsBuilder withDiseaseHypernym() {
        Query previousDecorator = buildingExp.getDecorator();
        buildingExp.setDecorator(new DiseaseHypernymQueryDecorator(previousDecorator));
        return this;
    }

    public ExperimentsBuilder withSolidTumor() {
        Query previousDecorator = buildingExp.getDecorator();
        buildingExp.setDecorator(new SolidTumorQueryDecorator(previousDecorator));
        return this;
    }

    public ExperimentsBuilder withGeneFamily() {
        Query previousDecorator = buildingExp.getDecorator();
        buildingExp.setDecorator(new GeneFamilyQueryDecorator(previousDecorator));
        return this;
    }

    public ExperimentsBuilder withGoldStandard(Experiment.GoldStandard gold) {
        buildingExp.setGoldStandard(gold);
        return this;
    }

    public ExperimentsBuilder withTarget(Experiment.Task task) {
        buildingExp.setTask(task);
        if (task != Experiment.Task.PUBMED_ONLINE)
            buildingExp.setDecorator(new ElasticSearchQuery(buildingExp.getIndexName(), buildingExp.getTypes()));
        else
            buildingExp.setDecorator(new PubMedOnlineQuery());
        return this;
    }

    public ExperimentsBuilder withYear(int year) {
        buildingExp.setYear(year);
        return this;
    }

    public Set<Experiment> build() {
        validate();
        return experiments;
    }

    private void validate() {
        if (buildingExp != null) {
            this.experiments.add(buildingExp);
            return;
        }
    }

    private Map<String, String> array2Map(String[] mapItems) {
        Map<String, String> map = new HashMap<>();
        for (int i = 0; i < mapItems.length; i++) {
            if (i % 2 == 1) {
                String key = mapItems[i - 1];
                String value = mapItems[i];
                map.put(key, value);
            }
        }
        return map;
    }
}

package at.medunigraz.imi.bst.trec.experiment;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import at.medunigraz.imi.bst.retrieval.*;
import at.medunigraz.imi.bst.trec.model.Gene;
import at.medunigraz.imi.bst.trec.model.GoldStandard;
import at.medunigraz.imi.bst.trec.model.Task;
import at.medunigraz.imi.bst.trec.query.*;

public class ExperimentsBuilder {

    private Set<Experiment> experiments = new HashSet<>();

    private Experiment buildingExp = null;
    private String statsDir;
    private String resultsDir;
    private TrecPmRetrieval retrieval;

    public ExperimentsBuilder() {
    }

    public ExperimentsBuilder newExperiment() {
        validate();
        buildingExp = new Experiment();
        retrieval = new TrecPmRetrieval();
        if (statsDir != null)
            withStatsDir(statsDir);
        if (resultsDir != null)
            withResultsDir(resultsDir);
        return this;
    }

    public ExperimentsBuilder withName(String name) {
        buildingExp.setExperimentName(name);
        return this;
    }

    @Deprecated
    public ExperimentsBuilder withDecorator(Query decorator) {
        buildingExp.setDecorator(decorator);

        retrieval.setQuery(decorator);
        return this;
    }

    public ExperimentsBuilder withTemplate(File template) {
        Query previousDecorator = buildingExp.getDecorator();
        buildingExp.setDecorator(new TemplateQueryDecorator(template, previousDecorator));

        retrieval.withTemplate(template);
        return this;
    }


    public ExperimentsBuilder withSubTemplate(File template) {
        Query previousDecorator = buildingExp.getDecorator();
        buildingExp.setDecorator(new SubTemplateQueryDecorator(template, previousDecorator));

        retrieval.withSubTemplate(template);
        return this;
    }

    /**
     * <p>
     * The given map will be used to fill variables/properties in the used template by the mapped values.
     * </p>
     *
     * @param templateProperties A map holding the poperties from the template and the values they should be replaced with.
     * @return This ExperimentsBuilder.
     */
    public ExperimentsBuilder withProperties(Map<String, String> templateProperties) {
        Query previousDecorator = buildingExp.getDecorator();
        buildingExp.setDecorator(new StaticMapQueryDecorator(templateProperties, previousDecorator));

        retrieval.withProperties(templateProperties);
        return this;
    }

    /**
     * <p>
     * The given map will be used to fill variables/properties in the used template by the mapped values.
     * </p>
     * <p>The parameters of the method are property-value pairs were the property always comes first and then the associated value.</p>
     *
     * @param templatePropertiesAndValues An array holding the poperties from the template and the values they should be replaced with.
     * @return This ExperimentsBuilder.
     */
    public ExperimentsBuilder withProperties(String... templatePropertiesAndValues) {
        Query previousDecorator = buildingExp.getDecorator();
        buildingExp.setDecorator(new StaticMapQueryDecorator(array2Map(templatePropertiesAndValues), previousDecorator));

        retrieval.withProperties(templatePropertiesAndValues);
        return this;
    }

    @Deprecated
    public ExperimentsBuilder withWordRemoval() {
        Query previousDecorator = buildingExp.getDecorator();
        buildingExp.setDecorator(new WordRemovalQueryDecorator(previousDecorator));
        return this;
    }

    @Deprecated
    public ExperimentsBuilder withGeneExpansion(Gene.Field[] expandTo) {
        Query previousDecorator = buildingExp.getDecorator();
        buildingExp.setDecorator(new GeneExpanderQueryDecorator(expandTo, previousDecorator));
        return this;
    }

    @Deprecated
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

    public ExperimentsBuilder withDrugInteraction() {
        Query previousDecorator = buildingExp.getDecorator();
        buildingExp.setDecorator(new DrugInteractionQueryDecorator(previousDecorator));
        return this;
    }

    public ExperimentsBuilder withGoldStandard(GoldStandard gold) {
        buildingExp.setGoldStandard(gold);
        return this;
    }

    public ExperimentsBuilder withTarget(Task task) {
        buildingExp.setTask(task);
        if (task != Task.PUBMED_ONLINE)
            buildingExp.setDecorator(new ElasticSearchQuery(buildingExp.getGoldStandard()));
        else
            buildingExp.setDecorator(new PubMedOnlineQuery());
        return this;
    }

    public ExperimentsBuilder withYear(int year) {
        buildingExp.setYear(year);
        return this;
    }

    public ExperimentsBuilder withStatsDir(String dir) {
        buildingExp.setStatsDir(dir);
        return this;
    }

    public ExperimentsBuilder withResultsDir(String dir) {
        buildingExp.setResultsDir(dir);
        return this;
    }

    public Set<Experiment> build() {
        validate();
        return experiments;
    }

    public Experiment getCurrentExperiment() {
        return buildingExp;
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

    public void setDefaultStatsDir(String statsDir) {
        this.statsDir = statsDir;
    }

    public void setDefaultResultsDir(String resultsDir) {
        this.resultsDir = resultsDir;
    }
}

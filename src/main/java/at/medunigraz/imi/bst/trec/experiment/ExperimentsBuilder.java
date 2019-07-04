package at.medunigraz.imi.bst.trec.experiment;

import at.medunigraz.imi.bst.retrieval.ElasticSearchQuery;
import at.medunigraz.imi.bst.retrieval.PubMedOnlineQuery;
import at.medunigraz.imi.bst.retrieval.Query;
import at.medunigraz.imi.bst.trec.model.Gene;
import at.medunigraz.imi.bst.trec.model.GoldStandard;
import at.medunigraz.imi.bst.trec.model.Task;

import java.io.File;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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
        buildingExp.setRetrieval(retrieval);
        if (statsDir != null)
            withStatsDir(statsDir);
        if (resultsDir != null)
            withResultsDir(resultsDir);
        return this;
    }

    public ExperimentsBuilder withName(String name) {
        retrieval.withExperimentName(name);
        return this;
    }

    public ExperimentsBuilder withDecorator(Query decorator) {
        retrieval.setQuery(decorator);
        return this;
    }

    public ExperimentsBuilder withTemplate(File template) {
        retrieval.withTemplate(template);
        return this;
    }


    public ExperimentsBuilder withSubTemplate(File template) {
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
        retrieval.withProperties(templatePropertiesAndValues);
        return this;
    }

    public ExperimentsBuilder withWordRemoval() {
        retrieval.withWordRemoval();
        return this;
    }

    public ExperimentsBuilder withGeneExpansion(Gene.Field[] expandTo) {
        retrieval.withGeneExpansion(expandTo);
        return this;
    }

    public ExperimentsBuilder withDiseaseReplacer() {
        retrieval.withDiseaseReplacer();
        return this;
    }

    public ExperimentsBuilder withDiseaseExpander() {
        retrieval.withDiseaseExpander();
        return this;
    }

    public ExperimentsBuilder withDiseasePreferredTerm() {
        retrieval.withDiseasePreferredTerm();
        return this;
    }

    public ExperimentsBuilder withDiseaseSynonym() {
        retrieval.withDiseaseSynonym();
        return this;
    }

    public ExperimentsBuilder withResistantDrugs() {
        retrieval.withResistantDrugs();
        return this;
    }

    public ExperimentsBuilder withGeneSynonym() {
        retrieval.withGeneSynonym();
        return this;
    }

    public ExperimentsBuilder withGeneDescription() {
        retrieval.withGeneDescription();
        return this;
    }

    public ExperimentsBuilder withDiseaseHypernym() {
        retrieval.withDiseaseHypernym();
        return this;
    }

    public ExperimentsBuilder withSolidTumor() {
        retrieval.withSolidTumor();
        return this;
    }

    public ExperimentsBuilder withGeneFamily() {
        retrieval.withGeneFamily();
        return this;
    }

    public ExperimentsBuilder withDrugInteraction() {
        retrieval.withDrugInteraction();
        return this;
    }

    public ExperimentsBuilder withGoldStandard(GoldStandard gold) {
        buildingExp.setGoldStandard(gold);
        return this;
    }

    public ExperimentsBuilder withTarget(Task task) {
        buildingExp.setTask(task);
        if (task != Task.PUBMED_ONLINE)
            retrieval.setQuery(new ElasticSearchQuery(buildingExp.getGoldStandard()));
        else
            retrieval.setQuery(new PubMedOnlineQuery());
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

    public void setDefaultStatsDir(String statsDir) {
        this.statsDir = statsDir;
    }

    public void setDefaultResultsDir(String resultsDir) {
        this.resultsDir = resultsDir;
    }
}

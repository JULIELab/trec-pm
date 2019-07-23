package at.medunigraz.imi.bst.trec.experiment;

import at.medunigraz.imi.bst.retrieval.Query;
import at.medunigraz.imi.bst.trec.model.Gene;
import at.medunigraz.imi.bst.trec.model.GoldStandard;
import at.medunigraz.imi.bst.trec.model.Task;

import java.io.File;
import java.util.Map;

public class ExperimentBuilder {

    private Experiment buildingExp = new Experiment();
    private TrecPmRetrieval retrieval = new TrecPmRetrieval();

    public ExperimentBuilder() {
        buildingExp.setRetrieval(retrieval);
    }

    public ExperimentBuilder withName(String name) {
        retrieval.withExperimentName(name);
        return this;
    }

    public ExperimentBuilder withDecorator(Query decorator) {
        retrieval.setQuery(decorator);
        return this;
    }

    public ExperimentBuilder withTemplate(File template) {
        retrieval.withTemplate(template);
        return this;
    }


    public ExperimentBuilder withSubTemplate(File template) {
        retrieval.withSubTemplate(template);
        return this;
    }

    /**
     * <p>
     * The given map will be used to fill variables/properties in the used template by the mapped values.
     * </p>
     *
     * @param templateProperties A map holding the poperties from the template and the values they should be replaced with.
     * @return This ExperimentBuilder.
     */
    public ExperimentBuilder withProperties(Map<String, String> templateProperties) {
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
     * @return This ExperimentBuilder.
     */
    public ExperimentBuilder withProperties(String... templatePropertiesAndValues) {
        retrieval.withProperties(templatePropertiesAndValues);
        return this;
    }

    public ExperimentBuilder withWordRemoval() {
        retrieval.withWordRemoval();
        return this;
    }

    public ExperimentBuilder withGeneExpansion(Gene.Field[] expandTo) {
        retrieval.withGeneExpansion(expandTo);
        return this;
    }

    public ExperimentBuilder withDiseaseReplacer() {
        retrieval.withDiseaseReplacer();
        return this;
    }

    public ExperimentBuilder withDiseaseExpander() {
        retrieval.withDiseaseExpander();
        return this;
    }

    public ExperimentBuilder withDiseasePreferredTerm() {
        retrieval.withDiseasePreferredTerm();
        return this;
    }

    public ExperimentBuilder withDiseaseSynonym() {
        retrieval.withDiseaseSynonym();
        return this;
    }

    public ExperimentBuilder withResistantDrugs() {
        retrieval.withResistantDrugs();
        return this;
    }

    public ExperimentBuilder withGeneSynonym() {
        retrieval.withGeneSynonym();
        return this;
    }

    public ExperimentBuilder withGeneDescription() {
        retrieval.withGeneDescription();
        return this;
    }

    public ExperimentBuilder withDiseaseHypernym() {
        retrieval.withDiseaseHypernym();
        return this;
    }

    public ExperimentBuilder withSolidTumor() {
        retrieval.withSolidTumor();
        return this;
    }

    public ExperimentBuilder withGeneFamily() {
        retrieval.withGeneFamily();
        return this;
    }

    public ExperimentBuilder withDrugInteraction() {
        retrieval.withDrugInteraction();
        return this;
    }

    public ExperimentBuilder withGoldStandard(GoldStandard gold) {
        buildingExp.setGoldStandard(gold);
        return this;
    }

    public ExperimentBuilder withTarget(Task task) {
        buildingExp.setTask(task);
        retrieval.withTarget(task);
        return this;
    }

    public ExperimentBuilder withYear(int year) {
        buildingExp.setYear(year);
        return this;
    }

    public ExperimentBuilder withStatsDir(String dir) {
        buildingExp.setStatsDir(dir);
        return this;
    }

    public ExperimentBuilder withResultsDir(String dir) {
        buildingExp.setResultsDir(dir);
        return this;
    }

    public ExperimentBuilder withSize(int size) {
        retrieval.withSize(size);
        return this;
    }

    public Experiment build() {
        return buildingExp;
    }
}

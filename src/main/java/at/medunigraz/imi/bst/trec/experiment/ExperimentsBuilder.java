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

	public ExperimentsBuilder withSubTemplate(File template) {
		Query previousDecorator = buildingExp.getDecorator();
		buildingExp.setDecorator(new SubTemplateQueryDecorator(template, previousDecorator));
		return this;
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

	public ExperimentsBuilder withGoldStandard(Experiment.GoldStandard gold) {
		buildingExp.setGoldStandard(gold);
		return this;
	}

	public ExperimentsBuilder withTarget(Experiment.Task task) {
		buildingExp.setTask(task);
		buildingExp.setDecorator(new ElasticSearchQuery(buildingExp.getIndexName(), buildingExp.getTypes()));
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
}

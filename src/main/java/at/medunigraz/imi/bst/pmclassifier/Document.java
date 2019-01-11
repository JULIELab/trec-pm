package at.medunigraz.imi.bst.pmclassifier;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class Document {
    @JsonProperty("pubmedId")
    private String id;
    private String title;
    @JsonProperty("abstract")
    private String abstractText;
    private String alltextreplacedentities;
    private List<String> genes;
    private List<String> organisms;
    private List<String> meshTags;
    private List<String> meshTagsMajor;
    private List<String> keywords;
    private String pmLabel;
    private List<String> meshMinor;
    private List<String> publicationTypes;
    @JsonIgnore
    private double[] topicWeight;

    public String getAlltextreplacedentities() {
        return alltextreplacedentities;
    }

    public void setAlltextreplacedentities(String alltextreplacedentities) {
        this.alltextreplacedentities = alltextreplacedentities;
    }

    public double[] getTopicWeight() {
        return topicWeight;
    }

    public void setTopicWeight(double[] topicWeight) {
        this.topicWeight = topicWeight;
    }

    public List<String> getPublicationTypes() {
        return publicationTypes;
    }

    public void setPublicationTypes(List<String> publicationTypes) {
        this.publicationTypes = publicationTypes;
    }

    public List<String> getKeywords() {
        return keywords;
    }

    public void setKeywords(List<String> keywords) {
        this.keywords = keywords;
    }

    public List<String> getMeshTags() {
        return meshTags;
    }

    public void setMeshTags(List<String> meshTags) {
        this.meshTags = meshTags;
    }

    public List<String> getMeshTagsMajor() {
        return meshTagsMajor;
    }

    public void setMeshTagsMajor(List<String> meshTagsMajor) {
        this.meshTagsMajor = meshTagsMajor;
    }

    public List<String> getMeshMinor() {
        return meshMinor;
    }

    public void setMeshMinor(List<String> meshMinor) {
        this.meshMinor = meshMinor;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAbstractText() {
        return abstractText;
    }

    public void setAbstractText(String abstractText) {
        this.abstractText = abstractText;
    }

    public List<String> getGenes() {
        return genes;
    }

    public void setGenes(List<String> genes) {
        this.genes = genes;
    }

    public List<String> getOrganisms() {
        return organisms;
    }

    public void setOrganisms(List<String> organisms) {
        this.organisms = organisms;
    }


    public String getPmLabel() {
        return pmLabel;
    }

    public void setPMLabel(String pmLabel) {
        this.pmLabel = pmLabel;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Document document = (Document) o;
        return Objects.equals(id, document.id) &&
                Arrays.equals(topicWeight, document.topicWeight);
    }

    @Override
    public int hashCode() {

        return Objects.hash(id);
    }
}

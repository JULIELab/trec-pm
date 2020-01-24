package de.julielab.ir.experiments.ablation;

import java.io.Serializable;
import java.util.Map;
import java.util.Objects;

public class AblationExperimentCacheKey implements Serializable {
    private String ablationGroupName;
    private String instance;
    private String indexSuffix;
    private String endpoint;
    private String metricsToReturn;
    private Map<String, String> referenceParameters;
    private Map<String, String> ablationParameters;

    @Override
    public String toString() {
        return "AblationExperimentCacheKey{" +
                "ablationGroupName='" + ablationGroupName + '\'' +
                ", instance='" + instance + '\'' +
                ", indexSuffix='" + indexSuffix + '\'' +
                ", endpoint='" + endpoint + '\'' +
                ", metricsToReturn='" + metricsToReturn + '\'' +
                ", referenceParameters=" + referenceParameters +
                ", ablationParameters=" + ablationParameters +
                '}';
    }

    public AblationExperimentCacheKey(String ablationGroupName, String instance, String indexSuffix, String endpoint, String metricsToReturn, Map<String, String> referenceParameters, Map<String, String> ablationParameters) {
        this.ablationGroupName = ablationGroupName;
        this.instance = instance;
        this.indexSuffix = indexSuffix;
        this.endpoint = endpoint;
        this.metricsToReturn = metricsToReturn;
        this.referenceParameters = referenceParameters;
        this.ablationParameters = ablationParameters;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AblationExperimentCacheKey that = (AblationExperimentCacheKey) o;
        return Objects.equals(ablationGroupName, that.ablationGroupName) &&
                Objects.equals(instance, that.instance) &&
                Objects.equals(indexSuffix, that.indexSuffix) &&
                Objects.equals(endpoint, that.endpoint) &&
                Objects.equals(metricsToReturn, that.metricsToReturn) &&
                Objects.equals(referenceParameters, that.referenceParameters) &&
                Objects.equals(ablationParameters, that.ablationParameters);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ablationGroupName, instance, indexSuffix, endpoint, metricsToReturn, referenceParameters, ablationParameters);
    }
}
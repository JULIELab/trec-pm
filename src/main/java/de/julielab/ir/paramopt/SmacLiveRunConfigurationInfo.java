package de.julielab.ir.paramopt;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SmacLiveRunConfigurationInfo {
    @JsonProperty("@rc-id")
    private int rcid;
    @JsonProperty("rc-pc")
    private SmacParameterConfiguration configuration;

    public int getRcid() {
        return rcid;
    }

    public SmacParameterConfiguration getConfiguration() {
        return configuration;
    }
}

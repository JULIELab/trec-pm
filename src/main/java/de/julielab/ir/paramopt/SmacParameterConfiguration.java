package de.julielab.ir.paramopt;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SmacParameterConfiguration {
    @JsonProperty("@pc-id")
    private int pcid;
    @JsonProperty("pc-settings")
    private Map<String, String> settings;
    @JsonProperty("pm-active-parameters")
    private List<String> activeParameters;
    @JsonProperty("pc-forbidden")
    private boolean forbidden;
    @JsonProperty("pc-default")
    private boolean defaultConfig;

    public int getPcid() {
        return pcid;
    }

    public void setPcid(int pcid) {
        this.pcid = pcid;
    }

    public Map<String, String> getSettings() {
        return settings;
    }

    public void setSettings(Map<String, String> settings) {
        this.settings = settings;
    }

    public List<String> getActiveParameters() {
        return activeParameters;
    }

    public void setActiveParameters(List<String> activeParameters) {
        this.activeParameters = activeParameters;
    }

    public boolean isForbidden() {
        return forbidden;
    }

    public void setForbidden(boolean forbidden) {
        this.forbidden = forbidden;
    }

    public boolean isDefaultConfig() {
        return defaultConfig;
    }

    public void setDefaultConfig(boolean defaultConfig) {
        this.defaultConfig = defaultConfig;
    }
}

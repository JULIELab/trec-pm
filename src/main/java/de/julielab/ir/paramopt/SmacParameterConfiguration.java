package de.julielab.ir.paramopt;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.ImmutableConfiguration;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.tree.ImmutableNode;

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

    public HierarchicalConfiguration<ImmutableNode> getSettingsAsXmlConfiguration() throws ConfigurationException {
        String[] p = new String[5 + settings.size()*2];
        int i = 5;
        for (String key : settings.keySet()) {
            p[i] = "-"+key;
            p[i + 1] = settings.get(key);
            i += 2;
        }
        HierarchicalConfiguration<ImmutableNode> c = SmacWrapperBase.parseConfiguration(p);
        return c;
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

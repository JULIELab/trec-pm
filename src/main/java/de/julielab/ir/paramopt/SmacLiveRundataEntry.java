package de.julielab.ir.paramopt;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SmacLiveRundataEntry {
    @JsonProperty("r-rc")
    private SmacLiveRunConfigurationInfo runInfo;
    @JsonProperty("r-quality")
    private double rQuality;
    @JsonProperty("r-wallclock-time")
    private double wallclockTime;

    public SmacLiveRunConfigurationInfo getRunInfo() {
        return runInfo;
    }

    public double getrQuality() {
        return rQuality;
    }


    public double getWallclockTime() {
        return wallclockTime;
    }


}

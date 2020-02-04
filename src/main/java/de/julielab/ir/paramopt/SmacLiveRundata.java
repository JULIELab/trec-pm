package de.julielab.ir.paramopt;

import java.util.ArrayList;

public class SmacLiveRundata extends ArrayList<SmacLiveRundataEntry> {
    public SmacLiveRundataEntry getEntryWithBestScore() {
        SmacLiveRundataEntry bestEntry = null;
        for (SmacLiveRundataEntry entry : this) {
            if (bestEntry == null || bestEntry.getrQuality() > entry.getrQuality())
                bestEntry = entry;
        }
        return bestEntry;
    }
}

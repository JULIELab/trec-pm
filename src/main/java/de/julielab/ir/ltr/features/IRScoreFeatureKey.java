package de.julielab.ir.ltr.features;

import java.util.Objects;

public class IRScoreFeatureKey {
    private IRScore scoreType;
    private TrecPmQueryPart queryPart;

    public IRScoreFeatureKey(IRScore scoreType, TrecPmQueryPart queryPart) {
        this.scoreType = scoreType;
        this.queryPart = queryPart;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IRScoreFeatureKey that = (IRScoreFeatureKey) o;
        return scoreType == that.scoreType &&
                queryPart == that.queryPart;
    }

    public IRScore getScoreType() {
        return scoreType;
    }

    public TrecPmQueryPart getQueryPart() {
        return queryPart;
    }

    @Override
    public int hashCode() {

        return Objects.hash(scoreType, queryPart);
    }

    @Override
    public String toString() {
        return scoreType.name() + "-" + queryPart.name();

    }
}

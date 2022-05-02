package SketchAnalysisOnHealthcare.SketchAnalysisOnHealthcare;

import java.text.DecimalFormat;
import java.util.HashSet;
import java.util.Set;

public class PureFIS {
    private int level;
    private String rule;
    private Set<DimensionValue> setOfDimValForRule;
    private double goodCount;
    private double badCount;
    private double total;
    private double badOverTotal;

    public int getLevel() {
        return level;
    }

    public String getRule() {
        return rule;
    }

    public double getGoodCount() {
        return goodCount;
    }

    public double getBadCount() {
        return badCount;
    }

    public double getTotal() {
        return total;
    }

    public double getBadOverTotal() {
        return badOverTotal;
    }

    public double getGoodOverTotal() {
        return goodCount / total;
    }

    public Set<DimensionValue> getSetOfDimValForRule() {
        if (setOfDimValForRule == null) {
            setOfDimValForRule = getConstituentsOfRule(this.rule);
        }
        return setOfDimValForRule;
    }

    private Set<DimensionValue> getConstituentsOfRule(String rule) {
        HashSet<DimensionValue> retVal = new HashSet<>();
        String [] dimVals = rule.split(FISLookupIndex.ruleSeparator, -1);
        for(String dimVal : dimVals) {
            String [] dimSeparateVal = dimVal.split(FISLookupIndex.dimValSeparator, -1);
            retVal.add(new DimensionValue(dimSeparateVal[0].trim(), dimSeparateVal[1].trim()));
        }
        return retVal;
    }

    public PureFIS (int level, String rule, double goodCount,
        double badCount, double total) {
        this.level = level;
        this.rule = rule;
        this.goodCount = goodCount;
        this.badCount = badCount;
        this.total = total;
        this.badOverTotal = badCount / total;
        this.setOfDimValForRule = null;
    }

    private static final DecimalFormat df = new DecimalFormat("#.000");
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(level);
        sb.append(",");
        sb.append(rule);
        sb.append(",");
        sb.append(goodCount);
        sb.append(",");
        sb.append(badCount);
        sb.append(",");
        sb.append(total);
        sb.append(",");
        sb.append(Float.valueOf(df.format(badOverTotal)));
        return sb.toString();
    }
}

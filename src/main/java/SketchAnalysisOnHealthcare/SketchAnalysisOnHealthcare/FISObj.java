package SketchAnalysisOnHealthcare.SketchAnalysisOnHealthcare;

import org.apache.datasketches.theta.Sketch;
import org.jetbrains.annotations.NotNull;

public class FISObj implements Comparable<FISObj>{
    private final String key;

    public String getKey() {
        return key;
    }
    public Sketch getValue() {
        return value;
    }
    private final Sketch value;
    public FISObj(String k, Sketch v) {
        key = k;
        value = v;
    }

    @Override
    public int compareTo(@NotNull FISObj o) {
        return Double.compare(o.value.getEstimate(), this.value.getEstimate());
    }
}
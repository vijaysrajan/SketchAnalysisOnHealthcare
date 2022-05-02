package SketchAnalysisOnHealthcare.SketchAnalysisOnHealthcare;

public class DimensionValue extends java.lang.Object {

    private String dimension;
    private String value;

    public DimensionValue(String dim, String val) {
        this.dimension = dim.trim();
        this.value = val.trim();
    }

    public String getDimension() {
        return dimension;
    }

    public String getValue() {
        return value;
    }

    public String toString() {
        return this.dimension + "=" + this.value;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        DimensionValue dimensionValue = other instanceof DimensionValue ? (DimensionValue) other : null;
        if (dimensionValue == null) {
            return false;
        } else {
            if ( (this.dimension.equals(dimensionValue.dimension)) && (this.value.equals(dimensionValue.value))) {
                return true;
            } else {
                return false;
            }
        }
    }

    @Override
    public final int hashCode() {
        int result = 17;
        if (dimension != null) {
            result = 31 * result + dimension.hashCode();
        }
        if (value != null) {
            result = 31 * result + value.hashCode();
        }
        return result;
    }
}

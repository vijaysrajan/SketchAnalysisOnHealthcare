package SketchAnalysisOnHealthcare.SketchAnalysisOnHealthcare;
import java.util.ArrayList;
import java.util.HashSet;

public class FISObjWithMetrics extends java.lang.Object
        implements Comparable<FISObjWithMetrics> {
    private final int level;
    private final String key;
    private final ArrayList<Double> values;

    public int getLevel(){
        return level;
    }
    public String getKey() {
        return key;
    }
    public ArrayList<Double> getValues() {
        return values;
    }

    public FISObjWithMetrics(int l, String k, ArrayList<Double> v) {
        level = l;
        key = k;
        values = v;
    }

    @Override
    public int compareTo(FISObjWithMetrics o) {
        if (level != o.level) {
            return this.level - o.level;
        } else {
            return o.key.compareTo(this.key);
        }
    }

    @Override
    public final int hashCode() {
        StringBuilder sb = new StringBuilder();
        sb.append(level);
        sb.append(",");
        sb.append(key);
        return sb.toString().hashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        FISObjWithMetrics fisObjWithMetrics = other instanceof FISObjWithMetrics ? (FISObjWithMetrics) other : null;
        if (fisObjWithMetrics == null) {
            return false;
        } else {
            if ( (this.level == fisObjWithMetrics.level)
                    && (this.key.equals(fisObjWithMetrics.key))) {
                return true;
            } else {
                return false;
            }
        }
    }

    public boolean contains(FISObjWithMetrics newObj) {
        HashSet<String> myDimValHashSet = new HashSet<>();
        StaticUtils.getConstituentDimVal(this.key, myDimValHashSet, " and ");
        HashSet<String> hisDimValHashSet = new HashSet<>();
        StaticUtils.getConstituentDimVal(newObj.key, hisDimValHashSet, " and ");

        return (myDimValHashSet.containsAll(hisDimValHashSet));
    }

    public String toString() {
        StringBuilder retVal = new StringBuilder();
        retVal.append(this.level);
        retVal.append(",");
        retVal.append(this.key);
        retVal.append(",");
        for (Double val : values) {
            retVal.append(val);
            retVal.append(",");
        }
        retVal.delete(retVal.length() -1, retVal.length());
        return retVal.toString();
    }

    public static void main(String []  args) {
        //2,zone_demand_popularity=POPULARITY_INDEX_5 and pickUpHourOfDay=VERYLATE,65.0,100.0,165.0,39.39393939
        //3,zone_demand_popularity=POPULARITY_INDEX_5 and pickUpHourOfDay=VERYLATE and sla=Scheduled,
        FISObjWithMetrics a = new FISObjWithMetrics(3, "estimated_usage_bins=LTE_1_HOUR and city=Delhi NCR and pickUpHourOfDay=VERYLATE", null);
        FISObjWithMetrics b = new FISObjWithMetrics(4, "estimated_usage_bins=LTE_1_HOUR and city=Delhi NCR and dayType=WEEKEND and pickUpHourOfDay=VERYLATE", null);
        System.out.println(b.contains(a));
        System.out.println(a.contains(b));
//        System.out.println(a.contains(a));
//        System.out.println(b.contains(b));
    }
}

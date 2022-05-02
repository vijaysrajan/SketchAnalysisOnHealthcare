package SketchAnalysisOnHealthcare.SketchAnalysisOnHealthcare;


import java.util.ArrayList;
import java.util.HashMap;

/*
Class that will read an FIS file with the following format
<Level>,Rule,GoodMetric,BadMetric,Total,BadMetric/Total
and output the rules that stand alone based on param_min_total and
param_min_pct.

The algo starts from level 1 and matches the conditions of param_min_Total and param_min_pct.
At level 2, all rules containing rules in level 1 are ignored as well as those level 2
rules that have a high intersection with level 1 to form level 3 rules.
At level 3, all rules containing level 1 and level 2 rules are ignored as well as those
level 3 rules that have high intersection with level 1 to form level 4 and those
level 3 rules that have high intersection with level 2 to form level 5 rules.
 */
public class ExtractBestRulesFromFISForClassification {

    static String defaultFISFileName = "/Users/vijayrajan/Documents/workspace/gist_0.5/data/rules4.csv";
    static double defaultParamMinPct = 15.0;
    static double defaultParamMaxPct = 45.0;
    static double defaultParamMinTotal = 1000;
    static FISLookupIndex fisLookupIndex;
    public static void main (String [] args) throws Exception {
        //read in FIS File
        String FISInputFileName = defaultFISFileName;
        if (args.length > 0) {
            FISInputFileName = args[0];
        }
        fisLookupIndex = StaticUtils.readFISFile(FISInputFileName);

        //read in param_min_pct
        double param_min_pct = defaultParamMinPct;
        if (args.length > 1) {
            param_min_pct = Double.parseDouble(args[1]);
        }

        //read in param_max_pct
        double param_max_pct = defaultParamMaxPct;
        if (args.length > 1) {
            param_max_pct = Double.parseDouble(args[2]);
        }

        //read in param_min_total
        double param_min_total = defaultParamMinTotal;
        if (args.length > 1) {
            param_min_total = Double.parseDouble(args[3]);
        }

        //start tree building
        String rule = "repo_mgr_grade=JuniorFrontlineOfficer and incentives_diff_median_team_bin=(-10000.0-0.0]";

//        ArrayList<PureFIS> test
//                = fisLookupIndex.getRelevantFISviaIntersect(3,
//                rule);
//
//        System.out.println(test.size());
//        test.forEach(e -> System.out.println(e.toString()));


        HashMap<String,ArrayList<PureFIS>> output = fisLookupIndex.getRelevantFISForLevelN(3, rule);
        ArrayList<PureFIS> thisRulePureFISObj = fisLookupIndex.getRelevantFISviaIntersect(2, rule);

        double parentTotal = thisRulePureFISObj.get(0).getTotal();
        String bestDim = "";
        double minGini = 1;
        for (String dim : output.keySet()) {
            double temp = getInfoGainBasedOnGiniImpurity(dim, output.get(dim), parentTotal);
            if (temp < minGini) {
                bestDim = dim;
                minGini = temp;
            }
        }
        System.out.println(bestDim);
        for (PureFIS pf : output.get(bestDim)) {
            System.out.println(pf.toString());
        }


        output = fisLookupIndex.getRelevantFirstLevelFIS();
        parentTotal = 9505;
        bestDim = "";
        minGini = 1;
        for (String dim : output.keySet()) {
            if (output.get(dim).size() <= 1) { continue; }
            double temp = getInfoGainBasedOnGiniImpurity(dim, output.get(dim), parentTotal);
            if (temp < minGini) {
                bestDim = dim;
                minGini = temp;
            }
        }
        System.out.println(bestDim);
        for (PureFIS pf : output.get(bestDim)) {
            System.out.println(pf.toString());
        }
    }

    //computed based on https://www.youtube.com/watch?v=6qDCGI3l-Oo
    //gini for one dimVal (say city=Mumbai) = Summation of category purity  i.e (good/total)^2 + (bad/total)^2
    //gini impurity for one dimVal (say city=Mumbai) = 1 - gini
    //weighted gini impurity for dim (say city) = summation of gini impurity for each city
    public static double getInfoGainBasedOnGiniImpurity(String dim, ArrayList<PureFIS> possibilities, double parentTotal) {
        double retVal = 0;
        for (PureFIS pf : possibilities) {
            retVal +=  (pf.getTotal() / parentTotal) *
                       (1 - (pf.getBadOverTotal() * pf.getBadOverTotal() + pf.getGoodOverTotal() * pf.getGoodOverTotal()));
        }
        return retVal;
    }



    /*
    The return value is the list of dimension and value on which to do the split of the subtree should be done.
     */
//    private static ArrayList<PureFIS> getSplitDimension(int level, String parentDimVal) {
//        ArrayList<PureFIS> retVal = new ArrayList<>();
//        HashMap<String,ArrayList<PureFIS>> toEvaluate;
//        if (parentDimVal.trim().length() == 0 && level == 1) {
//            toEvaluate = fisLookupIndex.getRelevantFirstLevelFIS();
//        } else {
//
//        }
//    }



//    private static String findBestByInfoGain (int level, ArrayList<String> dimValPairs) {
//        String returnDimension;
//
//        for (String dim : fisLookupIndex.getDimensions()) {
//            double giniImpurity = 1;
//            for (String val : fisLookupIndex.getValues(dim)) {
//
//            }
//        }
//
//
//
//        return returnDimension;
//    }

}

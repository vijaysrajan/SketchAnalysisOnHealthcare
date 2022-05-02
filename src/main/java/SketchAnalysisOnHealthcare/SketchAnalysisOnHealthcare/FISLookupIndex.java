package SketchAnalysisOnHealthcare.SketchAnalysisOnHealthcare;

import java.util.*;

public class FISLookupIndex {

    public static final String ruleSeparator = " and ";
    public static final String dimValSeparator = "=";
    private ArrayList<PureFIS> listOfFIS = new ArrayList<>();
    private HashMap<String, HashMap<String,ArrayList<HashSet<Integer>>>> dimValMap = new HashMap<>();

    void updateIndex(int level, String rule, Double goodCnt, Double badCnt, Double total)
            throws Exception{
        listOfFIS.add(new PureFIS(level, rule, goodCnt, badCnt, total));
        int index = listOfFIS.size() - 1;

        String [] dimValPairs = rule.split(ruleSeparator, -1);

        for(String dimValPair : dimValPairs) {
            String [] dimAndVal = dimValPair.split(dimValSeparator, -1);
            if (dimAndVal.length > 2) {
                throw new Exception("Expecting only two values. " + dimValPair);
            }
            String dim = dimAndVal[0];
            String val = dimAndVal[1];
            if (dimValMap.containsKey(dim)) {
                if (dimValMap.get(dim).containsKey(val)) {
                    ArrayList<HashSet<Integer>> levels = dimValMap.get(dim).get(val);
                    levels.get(level).add(listOfFIS.size() - 1);
                } else {
                    HashMap<String,ArrayList<HashSet<Integer>>> valMap = dimValMap.get(dim);
                    ArrayList<HashSet<Integer>> levels = new ArrayList<>();
                    for (int i = 0; i <= 15; i++) {
                        levels.add(new HashSet<Integer>());
                    }
                    levels.get(level).add(listOfFIS.size() - 1);
                    valMap.put(val,levels);
                    dimValMap.put(dim, valMap);
                }
            } else {
                HashMap<String,ArrayList<HashSet<Integer>>> valMap = new HashMap<>();
                ArrayList<HashSet<Integer>> levels = new ArrayList<>();
                for (int i = 0; i <= 15; i++) {
                    levels.add(new HashSet<Integer>());
                }
                levels.get(level).add(listOfFIS.size() - 1);
                valMap.put(val,levels);
                dimValMap.put(dim, valMap);
            }
        }
    }
    public HashSet<String> getDimensions() {
        HashSet<String> retVal = new HashSet<>();
        retVal.addAll(dimValMap.keySet());
        return retVal;
    }

    public HashSet<String> getValues(String dimension) {
        HashSet<String> retVal = new HashSet<>();
        retVal.addAll(dimValMap.get(dimension).keySet());
        return retVal;
    }

    public HashMap<String,ArrayList<PureFIS>> getRelevantFirstLevelFIS() {
        HashMap<String,ArrayList<PureFIS>> retVal = new HashMap<>();
        for (String dim : dimValMap.keySet()) {
            ArrayList<PureFIS> retValArr = new ArrayList<>();
            for (String val : dimValMap.get(dim).keySet()) {
                retVal.put(dim, retValArr);
                for (Integer i : dimValMap.get(dim).get(val).get(1)) {
                    retValArr.add(listOfFIS.get(i));
                }
            }
        }
        return retVal;
    }

    public HashMap<String,ArrayList<PureFIS>> getRelevantFISForLevelN(int level, String parentRule) {

        //ideally compare use Pure FIS instead of the parameter StringParentRule
        HashMap<String,ArrayList<PureFIS>> retVal = new HashMap<>();
        ArrayList<PureFIS> children = getRelevantFISviaIntersect(level, parentRule);
        PureFIS parentRuleAsPF = new PureFIS(level, parentRule, 1,1,2);
        HashSet<DimensionValue> possibleDimVals = new HashSet<>();
        for (PureFIS child : children) {

            possibleDimVals.addAll(child.getSetOfDimValForRule());
            possibleDimVals.removeAll(parentRuleAsPF.getSetOfDimValForRule());

            //if size > 1 then should throw exception
            // this loop must run only once
            for (DimensionValue dv : possibleDimVals) {
                if (retVal.containsKey(dv.getDimension())) {
                    ArrayList<PureFIS> mapValue = retVal.get(dv.getDimension());
                    mapValue.add(child);
                } else {
                    ArrayList<PureFIS> mapValue = new ArrayList<>();
                    mapValue.add(child);
                    retVal.put(dv.getDimension(), mapValue);
                }
            }

            possibleDimVals.clear();
        }
        return retVal;
    }


    public ArrayList<PureFIS> getRelevantFISviaIntersect(int level, String parentRule) {
        ArrayList<PureFIS> retVal = new ArrayList<>();
        ArrayList<DimensionValue> dimVals = parseParentRule(parentRule);
        Set<Integer> intersect = new HashSet<>();
        int start = 0;
        for (DimensionValue dimVal : dimVals) {
            String dim = dimVal.getDimension();
            String val = dimVal.getValue();
            HashSet hs = (dimValMap.get(dim).get(val)).get(level);
//            dimValMap.get(dim).get(val).forEach( e-> System.out.print (e.size() + " ") );
//            System.out.println();
            
            if (start == 0) {
                start++;
                intersect.addAll(hs);
            } else {
                intersect.retainAll(hs);
            }
        }

        intersect.forEach( e -> retVal.add(listOfFIS.get(e)));

        return retVal;
    }

    private ArrayList<DimensionValue> parseParentRule(String parentRule) {
        ArrayList<DimensionValue> retVal = new ArrayList<>();
        String [] dimVals = parentRule.split(ruleSeparator,-1);
        for (String dimVal : dimVals) {
            String [] splitDimAndVal = dimVal.split(dimValSeparator, -1);
            DimensionValue dimValue = new DimensionValue(splitDimAndVal[0], splitDimAndVal[1]);
            retVal.add(dimValue);
        }
        return retVal;
    }

}

package SketchAnalysisOnHealthcare.SketchAnalysisOnHealthcare;

import java.io.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class ComputeLiftConfidenceJaccard {
    private static final HashMap<String,Double> fisHashMapLevelAll = new HashMap<>();
    private static final ArrayList<String> listOfFISGreaterThanLevel2 = new ArrayList<>();
    private static double total_records = 0;
    private static final DecimalFormat df = new DecimalFormat("###.##");
    private static final String defaultInputFileName = "/Users/vijayrajan/healthcare/FIS_WithoutDecimal_5levels_point75support.csv";
    private static final boolean defaultExplode = true;

    public static void main (String [] args) throws Exception {
        String inputFileName = defaultInputFileName;
        if (args.length >= 1) {
            inputFileName = args[0];
        }
        boolean explode = defaultExplode;
        if (args.length >= 2) {
            if (args[1].equalsIgnoreCase("true")
               || args[1].equalsIgnoreCase("false")) {
                explode = Boolean.parseBoolean(args[1]);
            } else {
                explode = false;
            }
        }

        readFISFile(inputFileName);
        boolean finalExplode = explode;
        listOfFISGreaterThanLevel2.forEach(s -> computeConfidenceAndLift(s, finalExplode));
    }
    private static void readFISFile (String fName)  throws IOException {
        // Creating an object of BufferedReader class
        File file = new File(fName);
        BufferedReader br = new BufferedReader(new FileReader(file));

        int index = 0;
        int lineNumber = 0;
        String st;
        while ((st = br.readLine()) != null) {
            String [] lineElements = st.split(",", -1);
            if (lineNumber == 0) {
                total_records = Double.parseDouble(lineElements[2]);
                lineNumber++;
                continue;
            }
            if (lineElements[0].equals("1")) {
                index++;
            } else {
                listOfFISGreaterThanLevel2.add(lineElements[1]);
            }
            if (!lineElements[0].equals("0")) {
                fisHashMapLevelAll.put(lineElements[1], Double.parseDouble(lineElements[2]));
            }
        }
        br.close();
    }

    private static void explodeFISRuleForAllSubsets(ArrayList<String> workingSet, ArrayList<String> baseSet,
                                                    ArrayList<String> temp, int index) {
        if (index >= baseSet.size()){
            return;
        }
        temp.clear();
        for (int i = index; i < baseSet.size(); i++) {
            if (workingSet.size() == (baseSet.size() -1)) {
                return;
            }
            workingSet.add(baseSet.get(i));
            temp = (ArrayList<String>) baseSet.clone();
            temp.removeAll(workingSet);
            System.out.println(baseSet.size() + ","
                    + StaticUtils.orderTheFIS(buildFIS(baseSet), " & ")
                    + "," + fisHashMapLevelAll.get(buildFIS(baseSet))
                    + "," + workingSet.size() + ","
                    + StaticUtils.orderTheFIS(buildFIS(workingSet), " & ")
                    + "," + fisHashMapLevelAll.get(buildFIS(workingSet))
                    + "," + temp.size()
                    + "," + StaticUtils.orderTheFIS(buildFIS(temp), " & ")
                    + "," + fisHashMapLevelAll.get(buildFIS(temp))
                    + "," + df.format(confidence(baseSet, workingSet)* 100.0)
                    + "," + df.format(confidence(baseSet, temp)* 100.0)
                    + "," + df.format(lift(baseSet, workingSet, temp)));
            explodeFISRuleForAllSubsets(workingSet, baseSet, temp,i + 1);
            workingSet.remove(workingSet.size() - 1);
        }
    }

    private static void explodeFISRuleForOneLevelSubsets(ArrayList<String> workingSet,
                                                         ArrayList<String> baseSet
                                                         ) {
        for (int i = 0; i < baseSet.size(); i++) {
            workingSet.add(baseSet.get(i));
            ArrayList<String> temp = (ArrayList<String>) baseSet.clone();
            temp.removeAll(workingSet);
            System.out.println(baseSet.size() + ","
                    + StaticUtils.orderTheFIS(buildFIS(baseSet), " & ")
                    + "," + fisHashMapLevelAll.get(buildFIS(baseSet))
                    + "," + temp.size()
                    + "," + StaticUtils.orderTheFIS(buildFIS(temp), " & ")
                    + "," + fisHashMapLevelAll.get(buildFIS(temp))
                    + "," + workingSet.size() + ","
                    + StaticUtils.orderTheFIS(buildFIS(workingSet), " & ")
                    + "," + fisHashMapLevelAll.get(buildFIS(workingSet))
                    + "," + df.format(confidence(baseSet, temp)* 100.0)
                    + "," + df.format(lift(baseSet, workingSet, temp)));
            workingSet.clear();
        }
    }

    private static String buildFIS(ArrayList<String> a) {
        StringBuilder strA = new StringBuilder();
        for (int i = 0; i < a.size(); i++) {
            strA.append(a.get(i));
            if ( a.size() > 1 && i < a.size() - 1) {
                strA.append(" & ");
            }
        }
        return strA.toString();
    }

    private static double confidence (ArrayList<String> a, ArrayList<String> b) {
        String strA = buildFIS(a);
        String strB = buildFIS(b);
        try {
            return fisHashMapLevelAll.get(strA) * 1.0 / fisHashMapLevelAll.get(strB);
        } catch (Exception e) {
            //System.out.println("strA = " + strA);
            //System.out.println("strB = " + strB);
            return 0;
        }
    }

    private static double lift (ArrayList<String> base, ArrayList<String> antecedent, ArrayList<String> consequent) {
        String strBase = buildFIS(base);
        String strConsequent = buildFIS(consequent);
        String strAntecedent = buildFIS(antecedent);
        try {
            return fisHashMapLevelAll.get(strBase) / total_records /
                    ((fisHashMapLevelAll.get(strConsequent) / total_records) * (fisHashMapLevelAll.get(strAntecedent) / total_records));
        } catch (Exception e) {
            //System.out.println("strBase = " + strBase);
            //System.out.println("strConsequent = " + strConsequent);
            //System.out.println("strAntecedent = " + strAntecedent);
            return 0.0;
        }
    }

    private static void computeConfidenceAndLift(String fis, boolean explode) {
        String [] elements = fis.split(" & ", -1);
        ArrayList<String> temp = new ArrayList<>();
        ArrayList<String> baseSet = new ArrayList<>();
        Collections.addAll(baseSet, elements);
        ArrayList<String> workingSet = new ArrayList<>();
        if (explode) {
            explodeFISRuleForAllSubsets(workingSet, baseSet, temp, 1);
        } else {
            explodeFISRuleForOneLevelSubsets(workingSet, baseSet);
        }
    }
}

package SketchAnalysisOnHealthcare.SketchAnalysisOnHealthcare;

import java.io.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class ComputeLiftConfidenceJaccard {
    private static final HashMap<String,Double> fisHashMapLevelAll = new HashMap<>();
//    private static final HashMap<String,Double> fisHashMapLevel1 = new HashMap<>();
//    private static final HashMap<String,Integer> fisHashMapLevel1Index = new HashMap<>();
    private static final ArrayList<String> listOfFISGreaterThanLevel2 = new ArrayList<>();
    private static double total_records = 0;
    private static final DecimalFormat df = new DecimalFormat("###.##");
    private static final String defaultInputFileName = "/Users/vijayrajan/healthcare/FIS_WithoutDecimal_5levels_point75support.csv";

    public static void main (String [] args) throws Exception {
        String inputFileName = defaultInputFileName;
        if (args.length >= 1) {
            inputFileName = args[0];
        }

        readFISFile(inputFileName);
        listOfFISGreaterThanLevel2.forEach(ComputeLiftConfidenceJaccard::computeConfidenceAndLift);
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
//                fisHashMapLevel1.put(lineElements[1], Double.parseDouble(lineElements[2]));
//                fisHashMapLevel1Index.put(lineElements[1],index);
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


    private static void recurseThroughList(ArrayList<String> workingSet, ArrayList<String> baseSet,
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
                    + buildFIS(baseSet) + "," + fisHashMapLevelAll.get(buildFIS(baseSet))
                    + "," + workingSet.size() + ","
                    + buildFIS(workingSet)  + "," + fisHashMapLevelAll.get(buildFIS(workingSet))
                    + "," + temp.size()
                    + "," + buildFIS(temp) + "," + fisHashMapLevelAll.get(buildFIS(temp))
                    + "," + df.format(confidence(baseSet, workingSet)* 100.0)
                    + "," + df.format(lift(baseSet, workingSet, temp)));
            recurseThroughList(workingSet, baseSet, temp,i + 1);
            workingSet.remove(workingSet.size() - 1);
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
        return fisHashMapLevelAll.get(strA) * 1.0 / fisHashMapLevelAll.get(strB);
    }

    private static double lift (ArrayList<String> base, ArrayList<String> antecedent, ArrayList<String> consequent) {
        String strBase = buildFIS(base);
        String strConsequent = buildFIS(consequent);
        String strAntecedent = buildFIS(antecedent);
        return  fisHashMapLevelAll.get(strBase)/total_records /
                ((fisHashMapLevelAll.get(strConsequent) / total_records) *  (fisHashMapLevelAll.get(strAntecedent) / total_records));
    }

    private static void computeConfidenceAndLift(String fis) {
        String [] elements = fis.split(" & ", -1);
        ArrayList<String> temp = new ArrayList<>();
        ArrayList<String> baseSet = new ArrayList<>();
        Collections.addAll(baseSet, elements);
        ArrayList<String> workingSet = new ArrayList<>();
        recurseThroughList(workingSet, baseSet, temp, 0);
    }
}

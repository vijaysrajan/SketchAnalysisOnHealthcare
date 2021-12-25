package SketchAnalysisOnHealthcare.SketchAnalysisOnHealthcare;

import java.io.*;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;

public class ComputeLiftConfidenceJaccard {
    private static HashMap<String,Integer> fisHashMapLevel2AndAbove = new HashMap<>();
    private static HashMap<String,Integer> fisHashMapLevel1 = new HashMap<>();
    private static HashMap<String,Integer> fisHashMapLevel1Index = new HashMap<>();

    public static void main (String [] args) throws Exception {
        //readFISFile(args[0]);
        ArrayList<String> baseSet = new ArrayList<>();
        ArrayList<String> workingSet = new ArrayList<>();
        baseSet.add("a");
        baseSet.add("b");
        baseSet.add("c");
        baseSet.add("d");
        baseSet.add("e");
        recurseThroughList(workingSet, baseSet, 0);
    }
    private static void recurseThroughList(ArrayList<String> workingSet, ArrayList<String> baseSet, int index) {
        int length = baseSet.size();
        for (int i = 0; i < length; i++) {
            if (baseSet.size() > 1) {
                if (workingSet.size() > 0 && baseSet.get(0).compareTo(workingSet.get(workingSet.size() - 1)) <0) {
                    continue;
                }
                workingSet.add(baseSet.remove(i));
                System.out.println(workingSet + " --> " + baseSet);
                recurseThroughList(workingSet, baseSet, index + 1);
                baseSet.add(i, workingSet.remove(workingSet.size() - 1));
            }
        }
    }

    private static void readFISFile (String fName)  throws FileNotFoundException, IOException {
        String fileName = "/Users/vijayrajan/" + fName;
        // Creating an object of BufferedReader class
        File file = new File(fileName);
        BufferedReader br = new BufferedReader(new FileReader(file));

        // Declaring a string variable
        String st = br.readLine();
        int index = 0;
        while ((st = br.readLine()) != null) {
            String [] lineElements = st.split(",", -1);
            if (lineElements[0].equals("1")) {
                fisHashMapLevel1.put(lineElements[1], Integer.parseInt(lineElements[2]));
                fisHashMapLevel1Index.put(lineElements[1],index);
                index++;
            } else {
                fisHashMapLevel2AndAbove.put(lineElements[1], Integer.parseInt(lineElements[2]));
            }
        }
        br.close();
    }


    private static void computeConfidence(String fis) {
        String [] elements = fis.split(" & ", -1);


        return;
    }
}

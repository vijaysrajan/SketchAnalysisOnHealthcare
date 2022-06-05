package SketchAnalysisOnHealthcare.SketchAnalysisOnHealthcare;


//This class takes a set of frequent item sets used in classification and compresses them based on
//minimum desired metric value to find maximal item sets for the rule. Subsequently, a decision
//tree is made from this
//Test case with driveU classification rules

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.*;

public class CrunchFISBasedRules {

    //Split all rules into its constituents
    //sort based on levels
    //[optional pruning step] for each level, check if every FIS is a subset of the next level rule.
    // - If so, eliminate the higher level FIS
    //Do alphabetical sorting based on FIS
    //Group the FIS till their first constituent is the same and make that a node of the tree
    //Recurse for each group and build subtree

    //read in FIS based rules
    //

    //The Frequent ItemSets read into this collection below after
    //applying filtered constraints. The key of the HashMap is only
    //contains the rule in string form for the FIS
    private static ArrayList<FISObjWithMetrics> FISToCrunch = new ArrayList<>();

    //Complex structure that is used as an index
    //The outer level HashMap key (the first String) is the
      // first level DimVal
    //The second HapMap has the level(Integer) and
      //The HashSet is a collection of FIS object
    //    private static HashMap<String, HashMap<Integer, HashSet<FISObjWithMetrics>>> FISIndex
    //        = new HashMap<>();


    public static void main(String [] args) throws Exception {
        // input file name
        String inputFileNameFullPath = args[0];
        String outputFileNameFullPath = args[1];
        ArrayList<Double> metricConstraints = new ArrayList<>();
        for (int i = 2; i < args.length; i++) {
            metricConstraints.add(i-2,Double.parseDouble(args[i]));
        }

        //System.out.println(metricConstraints);

        //read input file and pass in the metric constraints and store in
        // FISToCrunch after applying constraints. Most importantly add to FISIndex
        readFileToCrunch(inputFileNameFullPath,metricConstraints);
        //Loop through FISToCrunch and Using FISIndex and the set within it to
        // remove from FISToCrunch
        loopToCrunch();

        //Write teh residual FISToCrunch to outputFile
        FISToCrunch.forEach(e -> System.out.println(e.toString()));
    }

    private static void readFileToCrunch(String inputFileName,
                                         ArrayList<Double> metricConstraints
                                        ) throws Exception {
        File file = new File(inputFileName);
        BufferedReader br
                = new BufferedReader(new FileReader(file));

        // Declaring a string variable
        String st = "";

        while ((st = br.readLine()) != null) {
            String[] lineParts = st.split(",", -1);
            int level = Integer.parseInt(lineParts[0]);
            String rule = lineParts[1];
            boolean keep = true;
            for (int i = 2; i < lineParts.length; i++) {
                Double d = Double.parseDouble(lineParts[i]);
                if (d < metricConstraints.get(i-2)) {
                    keep = false;
                    break;
                }
            }
            if (keep) {
                ArrayList<Double> metrics = new ArrayList<>();
                for (int i = 2; i < lineParts.length; i++) {
                    metrics.add(i-2, Double.parseDouble(lineParts[i]));
                }
                FISToCrunch.add( new FISObjWithMetrics(level, rule, metrics));
                HashSet<String> dimSet = new HashSet<>();
                StaticUtils.getConstituentDimVal(rule, dimSet, " and ");
            }
        }
    }

    private static void loopToCrunch() {
        Collections.sort(FISToCrunch);

        ArrayList<Integer> indexesToDelete = new ArrayList<>();
        int len = FISToCrunch.size();
        int outerLoop = 0;
        int innerLoop = outerLoop + 1;
        while (outerLoop < len - 1) {
            while (innerLoop < len) {
                if (FISToCrunch.get(innerLoop).contains(FISToCrunch.get(outerLoop))){
                    len--;
                    //System.out.println("Removing " + FISToCrunch.get(innerLoop) + " because of " + FISToCrunch.get(outerLoop));
                    FISToCrunch.remove(innerLoop);
                } else {
                    innerLoop++;
                }
            }
            outerLoop++;
            innerLoop = outerLoop + 1;
        }
    }
}

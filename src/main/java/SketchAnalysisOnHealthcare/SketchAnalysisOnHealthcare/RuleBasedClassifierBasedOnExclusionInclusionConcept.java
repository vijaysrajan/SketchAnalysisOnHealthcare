package SketchAnalysisOnHealthcare.SketchAnalysisOnHealthcare;


import org.apache.datasketches.theta.Sketch;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.stream.Collectors;

public class RuleBasedClassifierBasedOnExclusionInclusionConcept {

    public static final String defaultSketchFirstLevelFileName
            = "/Users/vijayrajan/healthcare/SketchFileTruncatedICDCodes.txt";

    public static final String defaultFISFilteredFileName
            = "/Users/vijayrajan/healthcare/FISFiltered.csv";

    public static final String defaultFISReverseFilteredFileName
            = "/Users/vijayrajan/healthcare/FISReverseFiltered.csv";

    public static final double defaultSupportThresholdPercentage = 0.5;

    public static final int defaultNumberOfLevels = 4;

    public static final String defaultFilterExpr = "I10";


    public static void main (String [] args) throws Exception {
        //read in sketches file into ArrayListOfFISObj
        String sketchInputFileName = defaultSketchFirstLevelFileName;
        if (args.length > 0) {
            sketchInputFileName = args[0];
        }
        //readInFileNameToStoreFISAfterApplyingFilter
        String FISFilteredFileName = defaultFISFilteredFileName;
        if (args.length > 1) {
            FISFilteredFileName = args[1];
        }
        //readInFileNameToStoreFISAfterApplyingReverseFilter
        String FISReverseFilteredFileName = defaultFISReverseFilteredFileName;
        if (args.length > 2) {
            FISReverseFilteredFileName = args[2];
        }
        //read in support percentage
        double supportLevelPercentage = defaultSupportThresholdPercentage;
        if (args.length > 3) {
            supportLevelPercentage = Double.parseDouble(args[3]);
        }
        //read in number of levels
        int numberOfLevels = defaultNumberOfLevels;
        if (args.length > 4) {
            numberOfLevels = Integer.parseInt(args[4]);
        }
        //read in expression
        String filterExpr = defaultFilterExpr;
        if (args.length > 5) {
            filterExpr = args[5];
        }

        HashMap<String, Sketch> mapOfDimValToSketchFiltered = StaticUtils.readFile(sketchInputFileName);
        HashMap<String, Sketch> mapOfDimValToSketchReverseFiltered = StaticUtils.readFile(sketchInputFileName);
        applyFilter(filterExpr, mapOfDimValToSketchFiltered);
        applyReverseFilter(filterExpr, mapOfDimValToSketchReverseFiltered);

        ArrayList<String> filterOperands =
                ExpressionParserEvaluator.getOperands(filterExpr);
        ArrayList<FISObj> firstLevelFiltered = new ArrayList<>();
        mapOfDimValToSketchFiltered.forEach((k,v) -> {
            if (v.getEstimate() > 0) {
                firstLevelFiltered.add(new FISObj(k,v));
            }
        });

        ArrayList<FISObj> firstLevelReverseFiltered = new ArrayList<>();
        mapOfDimValToSketchReverseFiltered.forEach((k,v) -> {
            if (v.getEstimate() > 0) {
                firstLevelReverseFiltered.add(new FISObj(k,v));
            }
        });

        //remove operands of the filter from the FirstLevels
        for (String s : filterOperands) {
           for (int i = 0; i < firstLevelFiltered.size(); i++) {
               if (s.equals(firstLevelFiltered.get(i).getKey())) {
                   firstLevelFiltered.remove(i);
               }
           }
            for (int i = 0; i < firstLevelReverseFiltered.size(); i++) {
                if (s.equals(firstLevelReverseFiltered.get(i).getKey())) {
                    firstLevelReverseFiltered.remove(i);
                }
            }
        }

        //computeFIS and store for both forward and reverse filters
        createFIS(firstLevelFiltered, numberOfLevels, new ArrayList<>(), supportLevelPercentage, FISFilteredFileName);
        createFIS(firstLevelReverseFiltered, numberOfLevels, new ArrayList<>(), supportLevelPercentage, FISReverseFilteredFileName);
    }

    public static void createFIS(ArrayList<FISObj> firstLevel,
                                 int numberOfLevels,
                                 ArrayList<FISObj> levelN,
                                 double supportLevelPercentage,
                                 String fName) throws Exception {

        File fOut = new File(fName);
        FileOutputStream fos = new FileOutputStream(fOut);
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
        CreateFISFromSketches.computeFIS(firstLevel, numberOfLevels, levelN, supportLevelPercentage, bw);
        bw.close();
    }

    public static void applyFilter(String expr,
                                   HashMap<String, Sketch> mapOfDimValToSketch) throws Exception {
        Sketch filter = ExpressionParserEvaluator.evaluateExpression(expr, mapOfDimValToSketch);
        mapOfDimValToSketch.forEach((k,v) -> mapOfDimValToSketch.put(k, StaticUtils.doIntersection(v, filter)));
    }

    public static void applyReverseFilter(String expr,
                                          HashMap<String, Sketch> mapOfDimValToSketch) throws Exception {
        String sb = "!(" +
                expr +
                ")";
        applyFilter(sb, mapOfDimValToSketch);
    }


}

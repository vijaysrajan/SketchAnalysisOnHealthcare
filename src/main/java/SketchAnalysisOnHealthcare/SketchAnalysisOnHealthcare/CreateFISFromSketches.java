package SketchAnalysisOnHealthcare.SketchAnalysisOnHealthcare;

import org.apache.datasketches.memory.Memory;
import org.apache.datasketches.theta.Intersection;
import org.apache.datasketches.theta.SetOperation;
import org.apache.datasketches.theta.Sketch;
import org.apache.datasketches.theta.Sketches;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class CreateFISFromSketches {

    private static final HashMap<String, Integer> mapOfFirstLevelIndex = new HashMap<>();
    private static final double defaultSupportLevelPercentage = 0.5;
    private static final int defaultNumberOfLevels = 5;
    private static final String defaultInputFileName = "/Users/vijayrajan/healthcare/SketchFile.txt";

    public static void main(String [] args) throws Exception {
        String inputFileName = defaultInputFileName;
        int numberOfLevels = defaultNumberOfLevels;
        double supportLevelPercentage = defaultSupportLevelPercentage;
        ArrayList<FISObj> levelN = new ArrayList<>();
        if (args.length >= 1) {
            inputFileName = args[0];
        }
        if (args.length >=2) {
            numberOfLevels = Integer.parseInt(args[1]);
        }
        if (args.length >=3) {
            supportLevelPercentage = Double.parseDouble(args[2]);
        }
        ArrayList<FISObj> firstLevel = new ArrayList<>();
        readFile(inputFileName, supportLevelPercentage, firstLevel);
        computeFIS(firstLevel, numberOfLevels, levelN, supportLevelPercentage, null);
    }

    public static void computeFIS(ArrayList<FISObj> firstLevel,
                                  int numberOfLevels,
                                  ArrayList<FISObj> levelN,
                                  double supportLevelPercentage,
                                  BufferedWriter outputFileToWrite) {

        sortFirstLevel(firstLevel);
        int supportLevel = (int)Math.round(supportLevelPercentage / 100 * firstLevel.get(0).getValue().getEstimate());

        cleanUpFirstLevel(firstLevel, outputFileToWrite, supportLevel);
        ArrayList<FISObj> level_N_Minus1 = new ArrayList<>();
        doFISMiningLevel2(firstLevel, level_N_Minus1, outputFileToWrite, supportLevel);
        for (int i = 3; i <= numberOfLevels; i++) {
            doFISMiningLevel3AndBeyond(firstLevel, level_N_Minus1, levelN, i, outputFileToWrite, supportLevel);
            ArrayList<FISObj> temp = level_N_Minus1;
            temp.clear();
            level_N_Minus1 = levelN;
            levelN = temp;
        }
    }

    public static void readFile (String fileName,
                                 double supportLevelPercentage,
                                 ArrayList<FISObj> firstLevel)
            throws IOException {
        // Creating an object of BufferedReader class
        File file = new File(fileName);
        BufferedReader br
                = new BufferedReader(new FileReader(file));
        String st;
        byte[] binSketch;
        Sketch sketch;
        int i = 0;
        while ((st = br.readLine()) != null) {
            String [] lineElements = st.split(",", -1);
            if (i == 0) {
                binSketch = Base64.getDecoder().decode(lineElements[1].getBytes(StandardCharsets.UTF_8));
                sketch = Sketches.wrapSketch(Memory.wrap(binSketch));
                StaticUtils.writeToFileOrStdOut(null, 0,
                        "",sketch.getEstimate());
                i++;
                //continue;
            }
            binSketch = Base64.getDecoder().decode(lineElements[1].getBytes(StandardCharsets.UTF_8));
            sketch = Sketches.wrapSketch(Memory.wrap(binSketch));
            firstLevel.add(new FISObj(lineElements[0], sketch));
        }
        br.close();
    }

    private static void sortFirstLevel(ArrayList<FISObj> firstLevel) {
        Collections.sort(firstLevel);
    }

    private static void cleanUpFirstLevel(ArrayList<FISObj> firstLevel, BufferedWriter outputFileToWrite, int supportLevel) {
        firstLevel.removeIf(f -> f.getValue().getEstimate() < supportLevel || f.getKey().equals(""));
        for (int i = 0; i < firstLevel.size(); i++) {
            mapOfFirstLevelIndex.put(firstLevel.get(i).getKey(), i);
        }
        firstLevel.forEach(o -> StaticUtils.writeToFileOrStdOut(outputFileToWrite,
                1, o.getKey(), o.getValue().getEstimate()));
    }

    private static void doFISMiningLevel2(ArrayList<FISObj> firstLevel, ArrayList<FISObj> level_N_Minus1,
                                          BufferedWriter outputFileToWrite, int supportLevel) {
        for (int j = 0; j < (firstLevel.size() - 1); j++) {
            FISObj f1 = firstLevel.get(j);
            if (f1.getValue().getEstimate() < supportLevel || f1.getKey().equals("")) {
                continue;
            }
            for (int i = j + 1; i < firstLevel.size(); i++) {

                FISObj f2 = firstLevel.get(i);
                if (!Objects.equals(f1.getKey(), f2.getKey())) {
                    if (f2.getValue().getEstimate() >= supportLevel) {
                        Intersection intersection = SetOperation.builder().buildIntersection();
                        intersection.intersect(f1.getValue());
                        intersection.intersect(f2.getValue());
                        Sketch intersectionResult = intersection.getResult();
                        if (intersectionResult.getEstimate() >= supportLevel) {
                            level_N_Minus1.add(new FISObj(f1.getKey() + " & " + f2.getKey(), intersectionResult));
                        }
                    }
                }
            }
        }
        level_N_Minus1.forEach(f -> StaticUtils.writeToFileOrStdOut(outputFileToWrite,
                2, f.getKey(), f.getValue().getEstimate()));
    }

    private static int getLargestIndex(String fis) {
        String [] items = fis.split(" & ", -1);
        int maxIndex = -1;
        for (String item : items) {
            if (mapOfFirstLevelIndex.get(item) > maxIndex) {
                maxIndex = mapOfFirstLevelIndex.get(item);
            }
        }
        return maxIndex;
    }

    private static void doFISMiningLevel3AndBeyond(ArrayList<FISObj> firstLevel,
                                                   ArrayList<FISObj> currentLevel,
                                                   ArrayList<FISObj> nextLevel,
                                                   int level,
                                                   BufferedWriter outputFileToWrite, int supportLevel) {

        for (FISObj f1 : currentLevel) {
            if (f1.getKey().equals("")) continue;
            for (int i = getLargestIndex(f1.getKey()) + 1; i < firstLevel.size(); i++) {
                FISObj f2 = firstLevel.get(i);
                if (!f1.getKey().contains(f2.getKey()) && (f2.getValue().getEstimate() >= supportLevel)) {
                    Intersection intersection = SetOperation.builder().buildIntersection();
                    intersection.intersect(f1.getValue());
                    intersection.intersect(f2.getValue());
                    Sketch intersectionResult = intersection.getResult();
                    if (intersectionResult.getEstimate() >= supportLevel) {
                        nextLevel.add(new FISObj(f1.getKey() + " & " + f2.getKey(), intersectionResult));
                    }
                }
            }
        }
        nextLevel.forEach(f -> StaticUtils.writeToFileOrStdOut(outputFileToWrite,
                level, f.getKey(),f.getValue().getEstimate()));
    }
}

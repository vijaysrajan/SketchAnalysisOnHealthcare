package SketchAnalysisOnHealthcare.SketchAnalysisOnHealthcare;

import org.apache.datasketches.memory.Memory;
import org.apache.datasketches.theta.Intersection;
import org.apache.datasketches.theta.SetOperation;
import org.apache.datasketches.theta.Sketch;
import org.apache.datasketches.theta.UpdateSketch;
import org.apache.datasketches.theta.Sketches;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.*;

public class CreateFISFromSketches {

    static class FISObj implements Comparable<FISObj>{
        private final String key;

        public String getKey() {
            return key;
        }

        public Sketch getValue() {
            return value;
        }

        private final Sketch value;
        public FISObj(String k, Sketch v) {
            key = k;
            value = v;
        }

        @Override
        public int compareTo(FISObj o) {
            return Double.compare(o.value.getEstimate(), this.value.getEstimate());
        }
    }
    private static final HashMap<String, Integer> mapOfFirstLevelIndex = new HashMap<>();
    private static final double defaultSupportLevelPercentage = 0.5;
    private static final int defaultNumberOfLevels = 5;
    private static int supportLevel = -1;
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
        sortAndCleanUpFirstLevel(firstLevel);
        ArrayList<FISObj> level_N_Minus1 = new ArrayList<>();
        doFISMiningLevel2(firstLevel, level_N_Minus1);
        for (int i = 3; i <= numberOfLevels; i++) {
            doFISMiningLevel3AndBeyond(firstLevel, level_N_Minus1, levelN, i);
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
                supportLevel = (int)Math.round(supportLevelPercentage / 100 * sketch.getEstimate());
                System.out.println("0," + "," +  sketch.getEstimate());
                i++;
                continue;
            }
            binSketch = Base64.getDecoder().decode(lineElements[1].getBytes(StandardCharsets.UTF_8));
            sketch = Sketches.wrapSketch(Memory.wrap(binSketch));
            firstLevel.add(new FISObj(lineElements[0], sketch));
        }
        br.close();
    }

    private static void sortAndCleanUpFirstLevel(ArrayList<FISObj> firstLevel) {
        Collections.sort(firstLevel);
        firstLevel.removeIf(f -> f.getValue().getEstimate() < supportLevel || f.getKey().equals(""));
        for (int i = 0; i < firstLevel.size(); i++) {
            mapOfFirstLevelIndex.put(firstLevel.get(i).getKey(), i);
        }
        firstLevel.forEach(o -> System.out.println("1," + o.getKey() + "," + o.getValue().getEstimate()));
    }

    private static void doFISMiningLevel2(ArrayList<FISObj> firstLevel, ArrayList<FISObj> level_N_Minus1) {
        for (int j = 0; j < (firstLevel.size() - 1); j++) {
            FISObj f1 = firstLevel.get(j);
            if (f1.getValue().getEstimate() < supportLevel) {
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
        level_N_Minus1.forEach(f -> System.out.println("2," +f.getKey() + "," + f.getValue().getEstimate()));
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
                                                   int level) {

        for (FISObj f1 : currentLevel) {
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
        nextLevel.forEach(f -> System.out.println(level + "," + f.getKey() + "," + f.getValue().getEstimate()));
    }
}

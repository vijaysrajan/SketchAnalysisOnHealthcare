package SketchAnalysisOnHealthcare.SketchAnalysisOnHealthcare;

import java.io.*;
import java.util.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.apache.datasketches.theta.Sketch;
import org.apache.datasketches.theta.UpdateSketch;
import org.apache.datasketches.theta.Intersection;
import org.apache.datasketches.theta.SetOperation;

public class SketchAnalysisOnHealthcare {
    private static final HashMap<String, UpdateSketch> mapOfDiseaseToSketch = new HashMap<>();
    private static final HashMap<String, Integer> mapOfFirstLevelIndex = new HashMap<>();
    private static final ArrayList<FISObj> firstLevel = new ArrayList<>();
    private static final ArrayList<FISObj> secondLevel = new ArrayList<>();
    private static final ArrayList<FISObj> thirdLevel = new ArrayList<>();
    private static final ArrayList<FISObj> fourthLevel = new ArrayList<>();
    private static final ArrayList<FISObj> fifthLevel = new ArrayList<>();
    private static final int supportLevel = 25;

    public static void main(String [] args) throws Exception {
        readFile();
        doFISMiningLevel2();
        doFISMiningLevel3AndBeyond(secondLevel, thirdLevel,3);
        doFISMiningLevel3AndBeyond(thirdLevel, fourthLevel,4);
        doFISMiningLevel3AndBeyond(fourthLevel, fifthLevel, 5);
    }

    public static void readFile () throws IOException, ParseException {
        String fileName = "/Users/vijayrajan/healthcare/5000_Charts_Data.csv";
        // Creating an object of BufferedReader class
        File file = new File(fileName);
        BufferedReader br
                = new BufferedReader(new FileReader(file));

        // Declaring a string variable
        String st = br.readLine();
        String [] header = st.split(",",-1);

        while ((st = br.readLine()) != null) {
            String [] lineElements = st.split(",", -1);
            //create a pivot file for gist

            //create an update sketch and store in hashmap
            String diseaseKey = header[3] + " = " + lineElements[3];
            String genderKey = header[2] + " = " + lineElements[2];
            String ageGroupKey = "ageGroup = " + StaticUtils.getAgeGroup(lineElements[1]);
            updateSketchInMap("", lineElements[0]);
            updateSketchInMap(diseaseKey, lineElements[0]);
            updateSketchInMap(genderKey, lineElements[0]);
            updateSketchInMap(ageGroupKey, lineElements[0]);
        }
        br.close();
    }

    private static void updateSketchInMap(String key, String patientInfo) {
        UpdateSketch updateSketch;
        if (!mapOfDiseaseToSketch.containsKey(key)) {
            updateSketch = UpdateSketch.builder().build();
            mapOfDiseaseToSketch.put(key, updateSketch);
        } else {
            updateSketch = mapOfDiseaseToSketch.get(key);
        }
        updateSketch.update(patientInfo.hashCode());
    }

    private static void sortAndCleanUpFirstLevel() {
        mapOfDiseaseToSketch.forEach((k,v) -> firstLevel.add(new FISObj(k,v)));
        Collections.sort(firstLevel);
        System.out.println("0," + "," + mapOfDiseaseToSketch.get("").getEstimate());
        firstLevel.removeIf(f -> f.getValue().getEstimate() < supportLevel || f.getKey().equals(""));
        for (int i = 0; i < firstLevel.size(); i++) {
            mapOfFirstLevelIndex.put(firstLevel.get(i).getKey(), i);
        }
    }

    private static void doFISMiningLevel2() {
        sortAndCleanUpFirstLevel();
        firstLevel.forEach(o -> System.out.println("1," + o.getKey() + "," + o.getValue().getEstimate()));

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
                            secondLevel.add(new FISObj(f1.getKey() + " & " + f2.getKey(), intersectionResult));
                        }
                    }
                }
            }
        }
        secondLevel.forEach(f -> System.out.println("2," +f.getKey() + "," + f.getValue().getEstimate()));
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

    private static void doFISMiningLevel3AndBeyond(ArrayList<FISObj> currentLevel, ArrayList<FISObj> nextLevel, int level) {

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
        currentLevel.clear();
        nextLevel.forEach(f -> System.out.println(level + "," + f.getKey() + "," + f.getValue().getEstimate()));
    }
}

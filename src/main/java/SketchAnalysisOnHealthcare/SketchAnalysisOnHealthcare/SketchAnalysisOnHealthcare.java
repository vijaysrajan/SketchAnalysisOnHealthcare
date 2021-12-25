package SketchAnalysisOnHealthcare.SketchAnalysisOnHealthcare;

import java.io.*;
import java.util.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.stream.Collectors;

import org.apache.datasketches.memory.Memory;
import org.apache.datasketches.theta.Sketch;
import org.apache.datasketches.theta.Sketches;
import org.apache.datasketches.theta.UpdateSketch;
import org.apache.datasketches.theta.Union;
import org.apache.datasketches.theta.Intersection;
import org.apache.datasketches.theta.SetOperation;

public class SketchAnalysisOnHealthcare {
    private static HashMap<String, UpdateSketch> mapOfDiseaseToSketch = new HashMap<>();
    private static HashMap<String, Integer> mapOfFirstLevelIndex = new HashMap<>();
    private static ArrayList<FISObj> firstLevel = new ArrayList<>();
    private static ArrayList<FISObj> secondLevel = new ArrayList<>();
    private static ArrayList<FISObj> thirdLevel = new ArrayList<>();
    private static ArrayList<FISObj> fourthLevel = new ArrayList<>();
    private static ArrayList<FISObj> fifthLevel = new ArrayList<>();
    private static int supportLevel = 29;

    public static void main(String [] args) throws Exception {
        readFile();
        //mapOfDiseaseToSketch.forEach((key,value) -> System.out.println(key + " = " + value.getEstimate()));
        doFISMiningLevel2();
        doFISMiningLevel3AndBeyond(secondLevel, thirdLevel,3);
        doFISMiningLevel3AndBeyond(thirdLevel, fourthLevel,4);
        doFISMiningLevel3AndBeyond(fourthLevel, fifthLevel, 5);
    }

    public static void readFile () throws FileNotFoundException, IOException, ParseException {
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
            String ageGroupKey = "ageGroup = " + getAgeGroup(lineElements[1]);
            //System.out.println(diseaseKey + "," + genderKey + "," + ageGroupKey);
            updateSketchInMap(diseaseKey, lineElements[0]);
            updateSketchInMap(genderKey, lineElements[0]);
            updateSketchInMap(ageGroupKey, lineElements[0]);
        }
        br.close();
    }

    private static void updateSketchInMap(String key, String patientInfo) {
        UpdateSketch updateSketch = null;
        if (!mapOfDiseaseToSketch.containsKey(key)) {
            updateSketch = UpdateSketch.builder().build();
            mapOfDiseaseToSketch.put(key, updateSketch);
        } else {
            updateSketch = mapOfDiseaseToSketch.get(key);
        }
        updateSketch.update(patientInfo.hashCode());
    }

    private static String getAgeGroup(String dob) throws ParseException {
        String returnVal = "";
        SimpleDateFormat sdf
                = new SimpleDateFormat("MM/dd/yyyy");
        Date _dob = sdf.parse(dob);
        Date today = sdf.parse("12/31/2021");
        long difference_In_Time = today.getTime() - _dob.getTime();
        long difference_In_Years = (difference_In_Time / (1000l * 60 * 60 * 24 * 365));
        if (difference_In_Years <= 5 ){
            returnVal = "less_than_5";
        } else if (difference_In_Years > 5 && difference_In_Years <= 10) {
            returnVal = "between_5_and_10";
        } else if (difference_In_Years > 10 && difference_In_Years <= 15) {
            returnVal = "between_10_and_15";
        } else if (difference_In_Years > 15 && difference_In_Years <= 20) {
            returnVal = "between_15_and_20";
        } else if (difference_In_Years > 20 && difference_In_Years <= 30) {
            returnVal = "between_20_and_30";
        } else if (difference_In_Years > 30 && difference_In_Years <= 40) {
            returnVal = "between_30_and_40";
        } else if (difference_In_Years > 40 && difference_In_Years <= 50) {
            returnVal = "between_40_and_50";
        } else if (difference_In_Years > 50 && difference_In_Years <= 60) {
            returnVal = "between_50_and_60";
        } else { //if (difference_In_Years > 60) {
            returnVal = "greater_than_60";
        }
        return returnVal;
    }
    static class FISObj implements Comparable<FISObj>{
        private String key;

        public String getKey() {
            return key;
        }

        public Sketch getValue() {
            return value;
        }

        private Sketch value;
        public FISObj(String k, Sketch v) {
            key = k;
            value = v;
        }

        @Override
        public int compareTo(FISObj o) {
            if(this.value.getEstimate() > o.value.getEstimate()) {
                return -1;
            } else if (this.value.getEstimate() == o.value.getEstimate()) {
                return 0;
            } else {
                return 1;
            }
        }
    }

    private static void sortAndCleanUpFirstLevel() {
        mapOfDiseaseToSketch.forEach((k,v) -> firstLevel.add(new FISObj(k,v)));
        Collections.sort(firstLevel);
        firstLevel.removeIf(f -> f.getValue().getEstimate() < supportLevel);
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
                //System.out.println("i=" + i + ", j=" + j);
                FISObj f2 = firstLevel.get(i);
                if (f1.getKey() != f2.getKey()) {
                    if (f2.getValue().getEstimate() >= supportLevel) {
                        Intersection intersection = SetOperation.builder().buildIntersection();
                        intersection.intersect(f1.getValue());
                        intersection.intersect(f2.getValue());
                        Sketch intersectionResult = intersection.getResult();
                        if (intersectionResult.getEstimate() > supportLevel) {
                            secondLevel.add(new FISObj(f1.getKey() + " & " + f2.getKey(), intersectionResult));
                        }
                    }
                }
            }
        }
        secondLevel.forEach(f -> {System.out.println("2," +f.getKey() + "," + f.getValue().getEstimate());});
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

        for (int j = 0; j < currentLevel.size(); j++) {
            FISObj f1 = currentLevel.get(j);
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
        nextLevel.forEach(f -> {System.out.println(level + "," + f.getKey() + "," + f.getValue().getEstimate());});
    }
}

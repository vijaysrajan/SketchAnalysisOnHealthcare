package SketchAnalysisOnHealthcare.SketchAnalysisOnHealthcare;


import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import org.apache.datasketches.memory.Memory;
import org.apache.datasketches.theta.Intersection;
import org.apache.datasketches.theta.SetOperation;
import org.apache.datasketches.theta.Sketch;
import org.apache.datasketches.theta.Sketches;

public class StaticUtils {
    public static String getAgeGroup(String dob) throws ParseException {
        String returnVal;
        SimpleDateFormat sdf
                = new SimpleDateFormat("MM/dd/yyyy");
        Date _dob = sdf.parse(dob);
        Date today = sdf.parse(sdf.format(new Date()));
        long difference_In_Time = today.getTime() - _dob.getTime();
        long difference_In_Years = (difference_In_Time / (1000L * 60 * 60 * 24 * 365));
        if (difference_In_Years <= 5 ) returnVal = "less_than_5";
        else if (difference_In_Years <= 10) returnVal = "between_5_and_10";
        else if (difference_In_Years <= 15) returnVal = "between_10_and_15";
        else if (difference_In_Years <= 20) returnVal = "between_15_and_20";
        else if (difference_In_Years <= 30) returnVal = "between_20_and_30";
        else if (difference_In_Years <= 40) returnVal = "between_30_and_40";
        else if (difference_In_Years <= 50) returnVal = "between_40_and_50";
        else if (difference_In_Years <= 60) returnVal = "between_50_and_60";
        else if (difference_In_Years <= 65) returnVal = "between_60_and_65";
        else if (difference_In_Years <= 70) returnVal = "between_65_and_70";
        else if (difference_In_Years <= 75) returnVal = "between_70_and_75";
        else if (difference_In_Years <= 80) returnVal = "between_75_and_80";
        else returnVal = "greater_than_80";
        return returnVal;
    }

    public static HashMap<String, Sketch> readFile (String fileName)
            throws IOException {
        HashMap<String, Sketch> mapOfDimValToSketch = new HashMap<>();
        // Creating an object of BufferedReader class
        File file = new File(fileName);
        BufferedReader br
                = new BufferedReader(new FileReader(file));
        String st;
        byte[] binSketch;
        Sketch sketch;
        while ((st = br.readLine()) != null) {
            String [] lineElements = st.split(",", -1);
            binSketch = Base64.getDecoder().decode(lineElements[1].getBytes(StandardCharsets.UTF_8));
            sketch = Sketches.wrapSketch(Memory.wrap(binSketch));
            mapOfDimValToSketch.put(lineElements[0], sketch);
        }
        br.close();
        return mapOfDimValToSketch;
    }

    public static void writeToFileOrStdOut(BufferedWriter bufferedWriter,
                                           int level,
                                           String rule,
                                           double metric){
        try {
            StringBuilder sb = new StringBuilder();
            sb.append(level);
            sb.append(",");
            sb.append(rule);
            sb.append(",");
            sb.append(metric);

            if (bufferedWriter != null) {
                bufferedWriter.write(sb.toString());
                bufferedWriter.newLine();
            } else {
                System.out.println(sb.toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Sketch doIntersection(Sketch a, Sketch b) {
        Intersection intersection = SetOperation.builder().buildIntersection();
        intersection.intersect(a); //, sketchB);
        intersection.intersect(b);
        return intersection.getResult();
    }

    static final StringBuilder orderedSB = new StringBuilder();
    static final ArrayList<String> orderedFISList = new ArrayList<>();
    public static String orderTheFIS (String FIS) {
        orderedSB.delete(0,orderedSB.length());
        orderedFISList.clear();
        String [] arrayFISComponents = FIS.split(" & ", -1);
        Collections.addAll(orderedFISList, arrayFISComponents);
        Collections.sort(orderedFISList);
        for (int i = 0; i < orderedFISList.size() - 1; i++) {
            orderedSB.append(orderedFISList.get(i));
            orderedSB.append(" & ");
        }
        orderedSB.append(orderedFISList.get(orderedFISList.size() -1));
        return orderedSB.toString();
    }

}

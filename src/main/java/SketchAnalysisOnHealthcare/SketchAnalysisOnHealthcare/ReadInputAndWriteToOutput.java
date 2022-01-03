package SketchAnalysisOnHealthcare.SketchAnalysisOnHealthcare;

import java.io.*;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.Base64;

import org.apache.datasketches.theta.UpdateSketch;

public class ReadInputAndWriteToOutput {
    private static final HashMap<String, UpdateSketch> mapOfDiseaseToSketch = new HashMap<>();
    private static final HashMap<String, UpdateSketch> mapOfDiseaseToSketchTopLevel = new HashMap<>();
    private static final String defaultInputFileName = "/Users/vijayrajan/healthcare/5000_Charts_Data.csv";
    private static final String defaultMainOutputFileName = "/Users/vijayrajan/healthcare/SketchFile.txt";
    private static final String defaultSimplifiedOutputFileName = "/Users/vijayrajan/healthcare/SketchFile2.txt";
    private static final int defaultBitsFrNominalEntry = 12;

    public static void main(String [] args) throws Exception {
        String inputFileName = defaultInputFileName;
        String mainOutputFileName = defaultMainOutputFileName;
        String simplifiedOutputFileName = defaultSimplifiedOutputFileName;
        int bitsForNominalEntries = defaultBitsFrNominalEntry;
        if (args.length >= 1) {
            inputFileName = args[0];
        }
        if (args.length >= 2) {
            mainOutputFileName = args[1];
        }
        if (args.length >= 3) {
            simplifiedOutputFileName = args[2];
        }
        if (args.length >= 4) {
            bitsForNominalEntries = Integer.parseInt(args[3]);
        }
        readInputFile(inputFileName, (int)Math.round(Math.pow(2,bitsForNominalEntries)));
        writeToOutputFile(mainOutputFileName, mapOfDiseaseToSketch); //"/Users/vijayrajan/healthcare/SketchFile.txt"
        writeToOutputFile(simplifiedOutputFileName, mapOfDiseaseToSketchTopLevel); //"/Users/vijayrajan/healthcare/SketchFile.txt"
        System.out.println("Number of diseases = " + mapOfDiseaseToSketch.size());
        System.out.println("Number of top level diseases = " + mapOfDiseaseToSketchTopLevel.size());
    }

    private static void writeToOutputFile(String fileName, HashMap<String,UpdateSketch> sketch) throws IOException {
        FileOutputStream out = new FileOutputStream(fileName);
        AtomicBoolean gotException = new AtomicBoolean(false);
        sketch.forEach((k,v) -> {
            try {
                out.write(k.getBytes(StandardCharsets.UTF_8));
                out.write(",".getBytes(StandardCharsets.UTF_8));
                out.write(Base64.getEncoder().encode(v.compact().toByteArray()));
                out.write("\n".getBytes(StandardCharsets.UTF_8));
            } catch (IOException e) {
                e.printStackTrace();
                gotException.set(true);
            }
        });
        out.close();
        if (gotException.get()) {
            throw new IOException("Couldn't write to file.");
        }
    }
    public static void readInputFile (String fileName, int nominalEntries) throws IOException, ParseException {

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
            String diseaseKeyTopLevel = header[3] + " = " + lineElements[3].split("\\.",-1)[0];

            updateSketchInMap("", lineElements[0], nominalEntries);
            updateSketchInMapTopLevel("",lineElements[0], nominalEntries);
            updateSketchInMap(diseaseKey, lineElements[0], nominalEntries);
            updateSketchInMapTopLevel(diseaseKeyTopLevel,lineElements[0], nominalEntries);
            updateSketchInMap(genderKey, lineElements[0], nominalEntries);
            updateSketchInMapTopLevel(genderKey,lineElements[0], nominalEntries);
            updateSketchInMap(ageGroupKey, lineElements[0], nominalEntries);
            updateSketchInMapTopLevel(ageGroupKey,lineElements[0], nominalEntries);
        }
        br.close();
    }
    private static void updateSketchInMap(String key, String patientInfo, int nominalEntry) {
        UpdateSketch updateSketch;
        if (!mapOfDiseaseToSketch.containsKey(key)) {
            updateSketch = UpdateSketch.builder().setNominalEntries(nominalEntry).build();
            mapOfDiseaseToSketch.put(key, updateSketch);
        } else updateSketch = mapOfDiseaseToSketch.get(key);
        updateSketch.update(patientInfo.hashCode());
    }
//
    private static void updateSketchInMapTopLevel(String key, String patientInfo, int nominalEntry) {
        UpdateSketch updateSketch;
        if (!mapOfDiseaseToSketchTopLevel.containsKey(key)) {
            updateSketch = UpdateSketch.builder().setNominalEntries(nominalEntry).build();
            mapOfDiseaseToSketchTopLevel.put(key, updateSketch);
        } else updateSketch = mapOfDiseaseToSketchTopLevel.get(key);
        updateSketch.update(patientInfo.hashCode());
    }
    private static String getAgeGroup(String dob) throws ParseException {
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
        else
            if (difference_In_Years <= 60) returnVal = "between_50_and_60";
            else returnVal = "greater_than_60";
        return returnVal;
    }
}

package SketchAnalysisOnHealthcare.SketchAnalysisOnHealthcare;

import java.io.*;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.Base64;
import java.util.UUID;

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
        obfuscate(inputFileName, "/Users/vijayrajan/healthcare/sample_5000_data.csv");
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

    public static void obfuscate(String inputFileName, String outputFile) throws Exception {
        File file = new File(inputFileName);
        BufferedReader br
                = new BufferedReader(new FileReader(file));
        FileOutputStream out = new FileOutputStream(outputFile);
        String st = br.readLine();
        StringBuilder sb = new StringBuilder();
        out.write(st.getBytes(StandardCharsets.UTF_8));
        out.write("\n".getBytes(StandardCharsets.UTF_8));
        String prevId = "";
        String uuid = UUID.randomUUID().toString();

        while ((st = br.readLine()) != null) {
            sb.delete(0,sb.length());
            String [] lineElements = st.split(",", -1);
            String id = lineElements[0];
            if (!id.equalsIgnoreCase(prevId)) {
                uuid = UUID.randomUUID().toString();
            }
            sb.append(uuid);
            sb.append(",");
            for (int i = 1; i < lineElements.length; i++) {
                sb.append(lineElements[i]);
                if (i != lineElements.length - 1) {
                    sb.append(",");
                }
            }
            out.write(sb.toString().getBytes(StandardCharsets.UTF_8));
            out.write("\n".getBytes(StandardCharsets.UTF_8));
            prevId = id;
        }
        out.close();
        br.close();
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
            String diseaseKey = lineElements[3];
            String genderKey = header[2] + "=" + lineElements[2];
            String ageGroupKey = "ageGroup=" + StaticUtils.getAgeGroup(lineElements[1]);
            String diseaseKeyTopLevel = lineElements[3].split("\\.",-1)[0];

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
}

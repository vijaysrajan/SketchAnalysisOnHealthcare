package SketchAnalysisOnHealthcare.SketchAnalysisOnHealthcare;

import java.io.*;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.text.ParseException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.Base64;
import java.util.UUID;

import org.apache.datasketches.theta.UpdateSketch;

public class ExplodeDriveUAggregateWithUUID {
    private static final HashMap<String, UpdateSketch> dimValToSketch = new HashMap<>();
    private static final String defaultInputFileName = "/Users/vijayrajan/IdeaProjects/SketchAnalysisOnHealthcare/DU/DUAggregateData.csv";
    private static final String defaultRawDataFileName = "/Users/vijayrajan/IdeaProjects/SketchAnalysisOnHealthcare/DU/rawDataWithUUID.csv";
    private static final String defaultDimValSketchFile = "/Users/vijayrajan/IdeaProjects/SketchAnalysisOnHealthcare/DU/DU_sketch.csv";
    private static final int defaultBitsFrNominalEntry = 12;

    public static void main(String [] args) throws Exception {
        String inputFileName = defaultInputFileName;
        String rawDataFileName = defaultRawDataFileName;
        String dimValSketchFile = defaultDimValSketchFile;
        int bitsForNominalEntries = defaultBitsFrNominalEntry;
        if (args.length >= 1) {
            inputFileName = args[0];
        }
        if (args.length >= 2) {
            rawDataFileName = args[1];
        }
        if (args.length >= 3) {
            dimValSketchFile = args[2];
        }
        if (args.length >= 4) {
            bitsForNominalEntries = Integer.parseInt(args[3]);
        }
        readInputFile(inputFileName, rawDataFileName);
        readRawInputFile(rawDataFileName, (int)Math.round(Math.pow(2,bitsForNominalEntries)));
        writeToOutputFile(dimValSketchFile, dimValToSketch); //"/Users/vijayrajan/healthcare/SketchFile.txt"
    }

    public static void readInputFile (String fileName, String rawOutputFilename) throws IOException, ParseException {

        // Creating an object of BufferedReader class
        File file = new File(fileName);
        FileOutputStream out = new FileOutputStream(rawOutputFilename);
        BufferedReader br
                = new BufferedReader(new FileReader(file));

        // Declaring a string variable
        String st = br.readLine();
        String [] header = st.split(",",-1);
        String newHeader = "tripId";
        for (int i = 0; i < 9; i++) {
            newHeader = newHeader + "," + header[i];
        }
        newHeader = newHeader + ",tripOutcome" + "\n";
        out.write(newHeader.getBytes(StandardCharsets.UTF_8));

        while ((st = br.readLine()) != null) {
            String [] lineElements = st.split(",", -1);
            String newLine = "";
            for (int i = 0; i < 9; i++) {
                newLine = newLine + lineElements[i] + ",";
            }
            String tripOutcome = "bad";
            for (int i = 0; i < Integer.parseInt(lineElements[9]); i++) {
                String realNewLine = UUID.randomUUID().toString() + "," + newLine + tripOutcome + "\n";
                out.write(realNewLine.getBytes(StandardCharsets.UTF_8) );
            }

            tripOutcome = "good";
            for (int i = 0; i < Integer.parseInt(lineElements[10]); i++) {
                String realNewLine = UUID.randomUUID().toString() + "," + newLine + tripOutcome + "\n";
                out.write(realNewLine.getBytes(StandardCharsets.UTF_8) );
            }
        }
        out.close();
        br.close();
    }

    public static void readRawInputFile (String fileName,  int nominalEntries) throws IOException, ParseException {

        // Creating an object of BufferedReader class
        File file = new File(fileName);
        BufferedReader br
                = new BufferedReader(new FileReader(file));

        // Declaring a string variable
        String st = br.readLine();
        String[] header = st.split(",", -1);

        while ((st = br.readLine()) != null) {
            String[] lineParts = st.split(",", -1);
            updateSketchInMap("", lineParts[0], nominalEntries);
            for(int i = 1; i < lineParts.length; i++) {
                String key = header[i] + "=" + lineParts[i];
                updateSketchInMap(key, lineParts[0], nominalEntries);
            }
        }
        br.close();
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

    private static void updateSketchInMap(String key, String val, int nominalEntry) {
        UpdateSketch updateSketch;
        if (!dimValToSketch.containsKey(key)) {
            updateSketch = UpdateSketch.builder().setNominalEntries(nominalEntry).build();
            dimValToSketch.put(key, updateSketch);
        } else updateSketch = dimValToSketch.get(key);
        updateSketch.update(val.hashCode());
    }
}

package SketchAnalysisOnHealthcare.SketchAnalysisOnHealthcare;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Arrays;
import java.util.Random;

import org.apache.datasketches.memory.Memory;

import org.apache.datasketches.quantiles.DoublesSketch;
import org.apache.datasketches.quantiles.DoublesUnion;
import org.apache.datasketches.quantiles.UpdateDoublesSketch;

public class ExampleQuantileSketch {

    public static double total1 = 0;
    public static double total2 = 0;

    // simplified file operations and no error handling for clarity
    // this section generates two sketches from random data and serializes them into files
    public static void main(String [] args) throws Exception {
        test1();
        test2();

        System.out.println("total1 = " + total1);
        System.out.println("total2 = " + total2);
        double t = total1 + total2;
        System.out.println("total = " + t);

    }

    public static void test1() throws Exception {
        Random rand = new Random();
        UpdateDoublesSketch sketch1 = DoublesSketch.builder().build(); // default k=128
        for (int i = 0; i < 10000; i++) {
            double n = rand.nextGaussian() + 10;
            total1 += n;
            sketch1.update(n); // mean=0, stddev=1
        }
        FileOutputStream out1 = new FileOutputStream("QuantilesDoublesSketch1.bin");
        out1.write(sketch1.toByteArray());
        out1.close();

        UpdateDoublesSketch sketch2 = DoublesSketch.builder().build(); // default k=128
        for (int i = 0; i < 10000; i++) {
            double n = rand.nextGaussian() + 100;
            total2 += n;
            sketch2.update(n); // shift the mean for the second sketch
        }
        FileOutputStream out2 = new FileOutputStream("QuantilesDoublesSketch2.bin");
        out2.write(sketch2.toByteArray());
        out2.close();
    }

    // this section deserializes the sketches, produces a union and prints some results
    public static void test2() throws Exception{
        FileInputStream in1 = new FileInputStream("QuantilesDoublesSketch1.bin");
        byte[] bytes1 = new byte[in1.available()];
        in1.read(bytes1);
        in1.close();
        DoublesSketch sketch1 = DoublesSketch.wrap(Memory.wrap(bytes1));

        FileInputStream in2 = new FileInputStream("QuantilesDoublesSketch2.bin");
        byte[] bytes2 = new byte[in2.available()];
        in2.read(bytes2);
        in2.close();
        DoublesSketch sketch2 = DoublesSketch.wrap(Memory.wrap(bytes2));

        DoublesUnion union = DoublesUnion.builder().build(); // default k=128
        union.update(sketch1);
        union.update(sketch2);
        DoublesSketch result = union.getResult();
        // Debug output from the sketch
        System.out.println(result.toString());

        System.out.println("Min, Median, Max values");
        System.out.println(Arrays.toString(result.getQuantiles(new double[]{0, 0.5, 1})));

        System.out.println("Vijay test begins -----");
        double total = 0;
        double [] bins = new double[]{-1, 0, 1};
        double bins2[] = result.getPMF(bins);

        for (double d: bins2) {
            System.out.print( d + " ");
        }
        System.out.println();
        System.out.println("Vijay test ends -----");

        System.out.println("Probability Histogram: estimated probability mass in 4 bins: (-inf, -2), [-2, 0), [0, 2), [2, +inf)");
        System.out.println(Arrays.toString(result.getPMF(new double[]{-2, 0, 2})));

        System.out.println("Frequency Histogram: estimated number of original values in the same bins");
        double[] histogram = result.getPMF(new double[]{-2, 0, 2});
        for (int i = 0; i < histogram.length; i++) {
            histogram[i] *= result.getN(); // scale the fractions by the total count of values
        }
        System.out.println(Arrays.toString(histogram));


        System.out.println("estimate of volume =" + estimateVolume(result));
    }

    private static double estimateVolume(DoublesSketch sketch) {
        double retVal = 0.0;
        int n = 100;

        double [] quantiles = sketch.getQuantiles(n);
        int totalItems = sketch.getRetainedItems();
        double [] histogramCounts = new double[n];

        //Arrays.sort(quantiles);
        histogramCounts = sketch.getPMF(quantiles);

        //compute the volume
        retVal += histogramCounts[0] * sketch.getN() * ((sketch.getMinValue() +  quantiles[0]) / 2);
        for (int i = 1; i < n-1; i++) {
            retVal += histogramCounts[i] * sketch.getN() * (( quantiles[i-1]+  quantiles[i]) / 2);
        }
        retVal += histogramCounts[n-1] * sketch.getN() * ((sketch.getMaxValue() +  quantiles[n-1]) / 2);

        return retVal;
    }

}
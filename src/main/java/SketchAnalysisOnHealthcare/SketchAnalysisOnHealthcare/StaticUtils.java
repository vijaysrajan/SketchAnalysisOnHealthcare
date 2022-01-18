package SketchAnalysisOnHealthcare.SketchAnalysisOnHealthcare;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

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
}
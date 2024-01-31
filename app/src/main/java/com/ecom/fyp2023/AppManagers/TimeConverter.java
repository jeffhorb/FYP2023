package com.ecom.fyp2023.AppManagers;

import java.time.LocalDate;

public class TimeConverter {

    public static void main(String[] args) {
        // Example values from Firestore
        String daysString = "2d";
        String weeksString = "3w";

        // Convert values to durations
        long days = convertToDays(daysString);
        long weeks = convertToDays(weeksString) * 7; // Convert weeks to days

        // Calculate end dates
        LocalDate currentDate = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            currentDate = LocalDate.now();
        }
        LocalDate endDateWithDays = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            endDateWithDays = currentDate.plusDays(days);
        }
        LocalDate endDateWithWeeks = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            endDateWithWeeks = currentDate.plusDays(weeks);
        }

        // Print results
        System.out.println("End date with days: " + endDateWithDays);
        System.out.println("End date with weeks: " + endDateWithWeeks);
    }

    public static long convertToDays(String timeValue) {
        // Extract the numeric value and the time unit
        int numericValue = Integer.parseInt(timeValue.substring(0, timeValue.length() - 1));
        String timeUnit = timeValue.substring(timeValue.length() - 1).toLowerCase();

        // Convert to days
        switch (timeUnit) {
            case "d":
                return numericValue;
            case "w":
                return numericValue * 7; // Convert weeks to days
            default:
                throw new IllegalArgumentException("Invalid time unit");
        }
    }
}

package com.ecom.fyp2023.AppManagers;

import androidx.annotation.NonNull;

import java.time.LocalDate;

public class TimeConverter {

    public static void main(String[] args) {
        // Example values from Firestore
        String daysString = "2day";
        String weeksString = "3week";

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

   public static long convertToDays(@NonNull String timeValue) {
       // Extract the numeric value and the time unit
       int numericValue = Integer.parseInt(timeValue.replaceAll("[^\\d]", ""));
       String timeUnit = timeValue.replaceAll("[\\d\\s]+", "").toLowerCase();

       // Convert to days
       switch (timeUnit) {
           case "day":
               return numericValue;
           case "week":
               return numericValue * 7; // Convert weeks to days
           default:
               throw new IllegalArgumentException("Invalid time unit: " + timeUnit);
       }
   }



}

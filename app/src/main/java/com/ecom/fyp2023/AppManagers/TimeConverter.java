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
        currentDate = LocalDate.now();
        LocalDate endDateWithDays = null;
        endDateWithDays = currentDate.plusDays(days);
        LocalDate endDateWithWeeks = null;
        endDateWithWeeks = currentDate.plusDays(weeks);

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
               return numericValue * 7L; // Convert weeks to days
           default:
               throw new IllegalArgumentException("Invalid time unit: " + timeUnit);
       }
   }
}

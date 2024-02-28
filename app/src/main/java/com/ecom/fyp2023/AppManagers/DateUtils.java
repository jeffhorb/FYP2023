package com.ecom.fyp2023.AppManagers;
import androidx.annotation.NonNull;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public class DateUtils {

    @NonNull
    public static String calculateDateDifference(Date endDate, Date startDate) {
        try {
            // Set both dates to GMT time zone
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
            dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
            endDate = dateFormat.parse(dateFormat.format(endDate));
            startDate = dateFormat.parse(dateFormat.format(startDate));

            // Calculate the difference in milliseconds
            long differenceInMillis = endDate.getTime() - startDate.getTime();

            // Calculate days using TimeUnit for more accurate results
            long daysDifference = TimeUnit.MILLISECONDS.toDays(differenceInMillis);

            return daysDifference + "day(s)";
        } catch (Exception e) {
            e.printStackTrace();
            return "Error calculating difference";
        }
    }
}

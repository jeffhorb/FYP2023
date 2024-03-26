package com.ecom.fyp2023.AppManagers;

import androidx.annotation.NonNull;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class TimeUtils {

    @NonNull
    public static String getTimeAgo(@NonNull Date timestamp) {
        long timeDifferenceMillis = Calendar.getInstance().getTimeInMillis() - timestamp.getTime();
        long seconds = TimeUnit.MILLISECONDS.toSeconds(timeDifferenceMillis);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(timeDifferenceMillis);
        long hours = TimeUnit.MILLISECONDS.toHours(timeDifferenceMillis);
        long days = TimeUnit.MILLISECONDS.toDays(timeDifferenceMillis);
        long weeks = days / 7;
        long months = days / 30;

        if (seconds < 60) {
            return seconds + " sec ago";
        } else if (minutes < 60) {
            return minutes + " min ago";
        } else if (hours < 24) {
            return hours + " h ago";
        } else if (days < 7) {
            return days + " d ago";
        } else if (weeks < 4) {
            return weeks + " w ago";
        } else {
            return months + " mo ago";
        }
    }
}

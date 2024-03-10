package com.ecom.fyp2023.AppManagers;

import androidx.annotation.NonNull;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.util.List;

public class DateValueFormatter extends ValueFormatter {
    private final List<String> dates;

    public DateValueFormatter(List<String> dates) {
        this.dates = dates;
    }

    @Override
    public String getPointLabel(@NonNull Entry entry) {
        int index = (int) entry.getX();
        if (index >= 0 && index < dates.size()) {
            return dates.get(index);
        }
        return "";
    }
    @Override
    public String getAxisLabel(float value, AxisBase axis) {
        int index = (int) value;
        if (index >= 0 && index < dates.size()) {
            return dates.get(index);
        }
        return "";
    }
}

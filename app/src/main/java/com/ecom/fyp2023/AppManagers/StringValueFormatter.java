package com.ecom.fyp2023.AppManagers;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.util.List;

public class StringValueFormatter extends ValueFormatter {

    private final List<String> taskTime;

    public StringValueFormatter(List<String> time) {
        this.taskTime = time;
    }

    @Override
    public String getAxisLabel(float value, AxisBase axis) {
        // Convert float value to integer (index) and use it to get the corresponding task title
        int index = Math.round(value);
        if (index >= 0 && index < taskTime.size()) {
            return taskTime.get(index);
        }
        return "";
    }
}


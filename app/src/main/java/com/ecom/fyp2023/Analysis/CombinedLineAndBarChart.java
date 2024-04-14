package com.ecom.fyp2023.Analysis;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.ecom.fyp2023.AppManagers.DateValueFormatter;
import com.ecom.fyp2023.ModelClasses.Projects;
import com.ecom.fyp2023.R;
import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

public class CombinedLineAndBarChart extends AppCompatActivity {

    private CombinedChart combinedChart;
    //private GestureDetector gestureDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_combined_line_and_bar_chart);

        //gestureDetector = new GestureDetector(this, new SwipeGestureListenerProjectAnalysis(this));

        combinedChart = findViewById(R.id.combinedChart);

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        TextView next = findViewById(R.id.nextAnalysis);
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CombinedLineAndBarChart.this, VelocityAnalysis.class);
                startActivity(intent);
            }
        });

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        db.collection("Projects")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        List<Projects> projectsWithDates = new ArrayList<>();
                         for (QueryDocumentSnapshot document : task.getResult()) {
                             Projects project = document.toObject(Projects.class);
                             // Check if both endDate and actualEndDate are not null
                             if (project.getEndDate() != null && project.getActualEndDate() != null) {
                                 projectsWithDates.add(project);
                             }
                         }
                         updateChart(projectsWithDates);
                    } else {
                        // Handle errors
                    }
                });
        }

    private void updateChart(@NonNull List<Projects> projects) {
        if (projects.isEmpty()) {
            // Handle the case where there are no projects
            TextView textView = findViewById(R.id.dataNotAvailable);
            textView.setText("No completed projects to display.");
            textView.setTextSize(20);
            combinedChart.setVisibility(View.GONE);
            return;
        }

        List<BarEntry> estimatedEntries = new ArrayList<>();
        List<Entry> lineEntries = new ArrayList<>();
        List<String> projectTitles = new ArrayList<>();
        Set<String> uniqueDates = new HashSet<>();

        for (int i = 0; i < projects.size(); i++) {
            Projects project = projects.get(i);
            Date estimatedDate = parseDate(project.getEndDate());
            Date actualDate = project.getActualEndDate();

            if (estimatedDate != null && actualDate != null) {
                estimatedEntries.add(new BarEntry(i, estimatedDate.getTime()));
                lineEntries.add(new Entry(i, actualDate.getTime()));
            }
            projectTitles.add(project.getTitle());
        }

        Description description = new Description();
        description.setText("Analysis of Completed Projects Estimated vs Actual end date");
        description.setPosition(1070f, 70f);
        description.setTextSize(15f);
        combinedChart.setDescription(description);

        // Create datasets
        BarDataSet barDataSet = new BarDataSet(estimatedEntries, "Estimated End Date");
        barDataSet.setColor(Color.RED);

        LineDataSet lineDataSet = new LineDataSet(lineEntries, "Actual End Date");
        lineDataSet.setColor(Color.BLUE);
        lineDataSet.setCircleColor(Color.BLUE);

        // Combine datasets
        CombinedData combinedData = new CombinedData();
        combinedData.setData(new BarData(barDataSet));
        combinedData.setData(new LineData(lineDataSet));

        List<String> uniqueDatesList = new ArrayList<>(uniqueDates);

        combinedChart.getAxisLeft().setValueFormatter(new DateValueFormatter(uniqueDatesList) {
            @Override
            public String getAxisLabel(float value, AxisBase axis) {
                return formatDateToString((long) value);
            }
        });

        // Adjust the chart's viewport and margins
        combinedChart.setExtraTopOffset(50f);
        combinedChart.setExtraBottomOffset(20f);
        combinedChart.setExtraLeftOffset(20f);
        combinedChart.setExtraRightOffset(20f);

        // Customize chart properties
        //CombinedChart barLineChart = findViewById(R.id.combinedChart);
        combinedChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(projectTitles));
        combinedChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        combinedChart.getXAxis().setAxisLineColor(Color.BLACK);
        combinedChart.getXAxis().setGranularity(1f);
        combinedChart.getXAxis().setLabelCount(projectTitles.size());

        combinedChart.setTouchEnabled(false);
        combinedChart.getAxisRight().setEnabled(false);
        combinedChart.getAxisRight().setDrawLabels(false);
        combinedChart.getAxisLeft().setAxisLineColor(Color.BLACK);

        combinedChart.setData(combinedData);
        combinedChart.invalidate();
    }


    @NonNull
    private String formatDateToString(long millis) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        Date date = new Date(millis);
        return sdf.format(date);
    }

    @Nullable
    private Date parseDate(String dateString) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
            return sdf.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

}
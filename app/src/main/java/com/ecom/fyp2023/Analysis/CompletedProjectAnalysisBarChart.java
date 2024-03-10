package com.ecom.fyp2023.Analysis;

import android.graphics.Color;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.ecom.fyp2023.AppManagers.DateValueFormatter;
import com.ecom.fyp2023.AppManagers.SwipeGestureListenerTasksAnalysis;
import com.ecom.fyp2023.ModelClasses.Projects;
import com.ecom.fyp2023.R;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
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

public class CompletedProjectAnalysisBarChart extends AppCompatActivity {

    private BarChart barChart;
    private GestureDetector gestureDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_completed_project_analysis_bar_chart);

        gestureDetector = new GestureDetector(this, new SwipeGestureListenerTasksAnalysis(this));

        barChart = findViewById(R.id.barChart);

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        db.collection("Projects")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        List<Projects> projects = new ArrayList<>();
                        List<Projects> projectsWithDates = new ArrayList<>();

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Projects project = document.toObject(Projects.class);
                            projects.add(project);
                            // Check if both endDate and actualEndDate are not null
                            if (project.getEndDate() != null && project.getActualEndDate() != null) {
                                projectsWithDates.add(project);
                            }
                        }

                        updateBarChart(projectsWithDates);
                    } else {
                        // Handle errors
                    }
                });
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return gestureDetector.onTouchEvent(event) || super.onTouchEvent(event);

    }

    private void updateBarChart(@NonNull List<Projects> projects) {

        if (projects.isEmpty()) {
            // Handle the case where there are no projects
            TextView textView = findViewById(R.id.dataNotAvailable);
            textView.setText("No Completed projects to display.");
            textView.setTextSize(20);
            barChart.setVisibility(View.GONE);
            return;
        }

        List<BarEntry> estimatedEntries = new ArrayList<>();
        List<BarEntry> actualEntries = new ArrayList<>();
        List<String> title = new ArrayList<>();
        Set<String> uniqueDates = new HashSet<>();
        List<String> e = new ArrayList<>();
        List<String> a = new ArrayList<>();

        // Sort projects based on the estimated end date
        projects.sort((project1, project2) -> {
            Date date1 = parseDate(project1.getEndDate());
            Date date2 = parseDate(project2.getEndDate());
            if (date1 != null && date2 != null) {
                return date1.compareTo(date2);
            }
            return 0;
        });


        for (int i = 0; i < projects.size(); i++) {
            Projects project = projects.get(i);

            Date estimatedEndDate= parseDate(project.getEndDate());
            Date actualEndDate = project.getActualEndDate();

            // Check if estimatedEndDate is not null and actualEndDate is a valid Date object
            if (estimatedEndDate != null && actualEndDate != null) {

                // Create separate BarEntry objects for estimated and actual end dates
                estimatedEntries.add(new BarEntry(i, estimatedEndDate.getTime()));
                actualEntries.add(new BarEntry(i, actualEndDate.getTime()));
                a.add(formatDateToString(estimatedEndDate));
                a.add(formatDateToString(estimatedEndDate));

            }
        }

        Description description = new Description();
        description.setText("Analysis of Completed Projects Estimated vs Actual end date");
        description.setPosition(1070f, 70f);
        description.setTextSize(15f);
        barChart.setDescription(description);

        BarDataSet estimatedDataSet = new BarDataSet(estimatedEntries, "Estimated End Date");
        estimatedDataSet.setColor(Color.BLUE);
        estimatedDataSet.setValueFormatter(new DateValueFormatter(e));

        BarDataSet actualDataSet = new BarDataSet(actualEntries, "Actual End Date");
        actualDataSet.setColor(Color.RED);
        estimatedDataSet.setValueFormatter(new DateValueFormatter(a));

        List<String> uniqueDatesList = new ArrayList<>(uniqueDates);
        barChart.getAxisLeft().setValueFormatter(new DateValueFormatter(uniqueDatesList) {
            @Override
            public String getAxisLabel(float value, AxisBase axis) {
                // Convert the value (millis) to formatted date
                return formatDateToString((long) value);
            }
        });
        // Set unique project titles as x-axis labels
        barChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(title));

        List<IBarDataSet> dataSets = new ArrayList<>();
        dataSets.add(estimatedDataSet);
        dataSets.add(actualDataSet);

        BarData barData = new BarData(dataSets);

        // Customize the appearance of the chart
        barChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        barChart.getXAxis().setAxisLineColor(Color.BLACK);
        barChart.getXAxis().setGranularity(1f);
        barChart.getXAxis().setLabelCount(title.size()); // Adjust label count as needed

        barChart.setTouchEnabled(false);
        //barChart.getXAxis().setLabelRotationAngle(45); title the project title
        barChart.getAxisRight().setEnabled(false); // Disable the right y-axis
        barChart.getAxisRight().setDrawLabels(false);
        barChart.getAxisLeft().setAxisLineColor(Color.BLACK);

        // Adjust the chart's viewport and margins to avoid labels being out of bounds
        barChart.setExtraTopOffset(50f);
        barChart.setExtraBottomOffset(20f);
        barChart.setExtraLeftOffset(20f);
        barChart.setExtraRightOffset(20f);

        barChart.setData(barData);
        barChart.getLegend().setEnabled(true); // Enable legend (project names)
        barChart.invalidate(); // Refresh the chart
    }
    @NonNull
    private String formatDateToString(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        return sdf.format(date);
    }

    @NonNull
    private String formatDateToString(long millis) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        Date date = new Date(millis);
        return sdf.format(date);
    }


    @Nullable
    private Date parseDate (String dateString){
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
            return sdf.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }
}

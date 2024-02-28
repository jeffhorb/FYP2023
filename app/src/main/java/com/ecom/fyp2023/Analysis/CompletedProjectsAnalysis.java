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
import com.ecom.fyp2023.AppManagers.SwipeGestureListenerProjectAnalysis;
import com.ecom.fyp2023.ModelClasses.Projects;
import com.ecom.fyp2023.R;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
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

public class CompletedProjectsAnalysis extends AppCompatActivity {

    LineChart lineChart;

    FirebaseFirestore db;

    private GestureDetector gestureDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_completed_projects_analysis);

        gestureDetector = new GestureDetector(this, new SwipeGestureListenerProjectAnalysis(this));

        lineChart = findViewById(R.id.lineChart);

        db = FirebaseFirestore.getInstance();

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

                        updateLineChart(projectsWithDates);
                    } else {
                        // Handle errors
                    }
                });
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return gestureDetector.onTouchEvent(event) || super.onTouchEvent(event);
    }

    private void updateLineChart(@NonNull List<Projects> projects) {

        if (projects.isEmpty()) {
            // Handle the case where there are no projects
            TextView textView = findViewById(R.id.dataNotAvailable);
            textView.setText("No Completed projects to display.");
            textView.setTextSize(20);
            lineChart.setVisibility(View.GONE);
        }
        List<Entry> estimatedEntries = new ArrayList<>();
        List<Entry> actualEntries = new ArrayList<>();
        List<String> title = new ArrayList<>();

        Set<String> uniqueDates = new HashSet<>();
        List<String> a = new ArrayList<>();
        List<String> e = new ArrayList<>();

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

            Date estimatedEndDate = parseDate(project.getEndDate());

            // Check if estimatedEndDate is not null and actualEndDate is a valid Date object
           if (estimatedEndDate != null && project.getActualEndDate() != null) {
                // Create separate Entry objects for estimated and actual end dates
                estimatedEntries.add(new Entry(i, estimatedEndDate.getTime()));
                actualEntries.add(new Entry(i, project.getActualEndDate().getTime()));

                // Store unique dates for y-axis labels
                e.add(formatDateToString(estimatedEndDate));
                a.add(formatDateToString(project.getActualEndDate()));
                uniqueDates.add(formatDateToString(estimatedEndDate));
                uniqueDates.add(formatDateToString(project.getActualEndDate()));

                // Store project titles for x-axis labels
                title.add(project.getTitle());
            }
        }

        Description description = new Description();
        description.setText("Analysis of Completed Projects Estimated vs Actual end date");
        description.setPosition(1070f, 70f);
        description.setTextSize(15f);
        lineChart.setDescription(description);

        LineDataSet estimatedDataSet = new LineDataSet(estimatedEntries, "Estimated End Date");
        estimatedDataSet.setColor(Color.BLUE);
        // Set custom data point formatter for estimatedDataSet
        estimatedDataSet.setValueFormatter(new DateValueFormatter(e));

        LineDataSet actualDataSet = new LineDataSet(actualEntries, "Actual End Date");
        actualDataSet.setColor(Color.RED);
        // Set custom data point formatter for actualDataSet
        actualDataSet.setValueFormatter(new DateValueFormatter(a));

        // Set unique dates as y-axis labels
        lineChart.getAxisLeft().setValueFormatter(new IndexAxisValueFormatter(uniqueDates));

        // Set unique project titles as x-axis labels
        lineChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(title));

        List<ILineDataSet> dataSets = new ArrayList<>();
        dataSets.add(estimatedDataSet);
        dataSets.add(actualDataSet);

        LineData lineData = new LineData(dataSets);

        // Customize the appearance of the chart
        lineChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        lineChart.getXAxis().setAxisLineColor(Color.BLACK);
        lineChart.getXAxis().setGranularity(1f);
        lineChart.getXAxis().setLabelCount(title.size()); // Adjust label count as needed

        lineChart.setTouchEnabled(false);

        lineChart.getAxisRight().setEnabled(false); // Disable the right y-axis
        lineChart.getAxisRight().setDrawLabels(false);
        lineChart.getAxisLeft().setAxisLineColor(Color.BLACK);

        // Adjust the chart's viewport and margins to avoid labels being out of bounds
        lineChart.setExtraTopOffset(50f);
        lineChart.setExtraBottomOffset(20f);
        lineChart.setExtraLeftOffset(20f);
        lineChart.setExtraRightOffset(20f);

        lineChart.setData(lineData);
        lineChart.getLegend().setEnabled(true); // Enable legend (project names)
        lineChart.invalidate(); // Refresh the chart
    }

    @NonNull
    private String formatDateToString(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
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
package com.ecom.fyp2023.Analysis;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.ecom.fyp2023.ModelClasses.Tasks;
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
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CompletedTasksAnalysis extends AppCompatActivity {
    LineChart lineChart;

    FirebaseFirestore db;
    String projectId;

    TextView next;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_completed_tasks_analysis);

        lineChart = findViewById(R.id.lineChart);

        db = FirebaseFirestore.getInstance();

        next = findViewById(R.id.next);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        Intent intent = getIntent();
        if (intent.hasExtra("PROID")) {
            // Retrieve the data using the key
            projectId = intent.getStringExtra("PROID");
        }

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent2 = new Intent(CompletedTasksAnalysis.this, CompletedTaskAnalysisBarChart.class);
                intent2.putExtra("PROID", projectId);
                startActivity(intent2);
            }
        });

        CollectionReference projectTasksCollection = db.collection("projectTasks");
        Query query = projectTasksCollection.whereEqualTo("projectId", projectId);
        query.addSnapshotListener((value, error) -> {
            if (error != null) {
                return;
            }

            if (value != null) {
                List<Tasks> tasksList = new ArrayList<>();
                for (QueryDocumentSnapshot document : value) {
                    String taskId = document.getString("taskId");
                    retrieveTaskDetails(taskId, tasksList);
                }
            }
        });
    }

    private void retrieveTaskDetails(String taskId, List<Tasks> tasksList) {
        CollectionReference tasksCollection = FirebaseFirestore.getInstance().collection("Tasks");
        tasksCollection.document(taskId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    Tasks taskData = document.toObject(Tasks.class);
                    assert taskData != null;
                    taskData.setTaskId(document.getId());
                    tasksList.add(taskData);
                    updateLineChart(tasksList);

                } else {
                    Log.e("Firestore", "Error fetching task details: " + task.getException());
                }
            }
        });
    }

    private void updateLineChart(@NonNull List<Tasks> tasksList) {

        if (tasksList.isEmpty()) {
            // Handle the case where there are no tasks
            TextView textView = findViewById(R.id.dataNotAvailable);
            textView.setText("No Completed Tasks to display.");
            textView.setTextSize(20);
            lineChart.setVisibility(View.GONE);
            return;  // Return early if there are no tasks
        }

        List<Entry> estimatedTimeEntries = new ArrayList<>();
        List<Entry> completedTimeEntries = new ArrayList<>();
        List<String> tasksTitle = new ArrayList<>();

        for (int i = 0; i < tasksList.size(); i++) {
            Tasks tasks = tasksList.get(i);
            String estimatedT = tasks.getEstimatedTime();
            String completedT = tasks.getCompletedTime();

            if (estimatedT != null && completedT != null) {
                // Parse the numbers from the strings
                int estimatedDays = extractDaysFromString(estimatedT);
                int completedDays = extractDaysFromString(completedT);

                // Create separate Entry objects for estimated and completed times
                estimatedTimeEntries.add(new Entry(i, estimatedDays));
                completedTimeEntries.add(new Entry(i, completedDays));

                // Store task titles for x-axis labels
                tasksTitle.add(tasks.getTaskName());
            }
        }

        Description description = new Description();
        description.setText("Completed Tasks Estimated vs Actual Completion time");
        description.setPosition(1070f, 70f);
        description.setTextSize(15f);
        lineChart.setDescription(description);

        LineDataSet estimatedDataSet = new LineDataSet(estimatedTimeEntries, "Estimated Time (In Days)");
        estimatedDataSet.setColor(Color.RED);

        LineDataSet actualDataSet = new LineDataSet(completedTimeEntries, "Completion Time(In Days)");
        actualDataSet.setColor(Color.BLUE);

        List<ILineDataSet> dataSets = new ArrayList<>();
        dataSets.add(estimatedDataSet);
        dataSets.add(actualDataSet);

        LineData lineData = new LineData(dataSets);

        // Customize the appearance of the chart
        lineChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        lineChart.getXAxis().setAxisLineColor(Color.BLACK);
        lineChart.getXAxis().setGranularity(1f);
        lineChart.getXAxis().setLabelCount(tasksTitle.size()); // Adjust label count

        lineChart.setTouchEnabled(false);

        lineChart.getAxisRight().setEnabled(false);
        lineChart.getAxisRight().setDrawLabels(false);
        lineChart.getAxisLeft().setAxisLineColor(Color.BLACK);

        // Adjust the chart's viewport and margins to avoid labels being out of bounds
        lineChart.setExtraTopOffset(50f);
        lineChart.setExtraBottomOffset(20f);
        lineChart.setExtraLeftOffset(20f);
        lineChart.setExtraRightOffset(20f);

        lineChart.setTouchEnabled(false);

        // Set task titles as x-axis labels
        lineChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(tasksTitle));

        lineChart.setData(lineData);
        lineChart.getLegend().setEnabled(true); // Enable legend (project names)
        lineChart.invalidate(); // Refresh the chart
    }

    private int extractDaysFromString(@NonNull String timeString) {
        // Extract the numeric value from the string, considering both "day(s)" and "week(s)"
        try {
            int numericValue;
            if (timeString.contains("day")) {
                numericValue = Integer.parseInt(timeString.replaceAll("[^0-9]", ""));
            } else if (timeString.contains("week")) {
                int weeks = Integer.parseInt(timeString.replaceAll("[^0-9]", ""));
                numericValue = weeks * 7; // Convert weeks to days
            } else {
                numericValue = 0;
            }

            return numericValue;
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return 0;
        }
    }
}




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
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CompletedTaskAnalysisBarChart extends AppCompatActivity {

    BarChart barChart;

    FirebaseFirestore db;

    TextView next;

    //private GestureDetector gestureDetector;

    String projectId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_completed_task_analysis_bar_chart);

        //gestureDetector = new GestureDetector(this, new SwipeGestureListenerTasksAnalysis(this));

        barChart = findViewById(R.id.barChart);
        barChart = findViewById(R.id.barChart);

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
                Intent intent2 = new Intent(CompletedTaskAnalysisBarChart.this, TasksProgressAnalysis.class);
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
                    updateBarChart(tasksList);

                } else {
                    Log.e("Firestore", "Error fetching task details: " + task.getException());
                }
            }
        });
    }

    private void updateBarChart(@NonNull List<Tasks> tasksList) {
        ArrayList<BarEntry> entries = new ArrayList<>();
        ArrayList<String> labels = new ArrayList<>();


        if (tasksList.isEmpty()) {
            // Handle the case where there are no projects
            TextView textView = findViewById(R.id.dataNotAvailable);
            textView.setText("No Completed projects to display.");
            textView.setTextSize(20);
            barChart.setVisibility(View.GONE);
            return;
        }

        for (int i = 0; i < tasksList.size(); i++) {
            Tasks task = tasksList.get(i);
            String estimatedCompletion = task.getEstimatedTime();
            String actualCompletion = task.getCompletedTime();
            int estimatedDays = extractDaysFromString(estimatedCompletion);

            // Adding estimated completion as a red bar
            entries.add(new BarEntry(i * 2, estimatedDays));
            labels.add("  " + task.getTaskName());

            // Adding actual completion as a blue bar if not null
            if (actualCompletion != null) {
                int completedDays = extractDaysFromString(actualCompletion);
                entries.add(new BarEntry(i * 2 + 1, completedDays));
                labels.add("     ");
            }
        }

        Description description = new Description();
        description.setText("Analysis of Completed Tasks Estimated vs Completion Time");
        description.setPosition(1070f, 70f);
        description.setTextSize(15f);
        barChart.setDescription(description);

        BarDataSet dataSet = new BarDataSet(entries, "Estimated Time   Completion Time");
        dataSet.setColors(Color.RED, Color.BLUE);

        BarData barData = new BarData(dataSet);
        barChart.setData(barData);

        // Customize the appearance of the chart

        barChart.getXAxis().setAxisLineColor(Color.BLACK);
        barChart.getXAxis().setGranularity(1f);
        //barChart.getXAxis().setLabelCount(tasksList.size()); // Adjust label count as needed

        barChart.setTouchEnabled(false);
        barChart.getAxisRight().setEnabled(false); // Disable the right y-axis
        barChart.getAxisRight().setDrawLabels(false);
        barChart.getAxisLeft().setAxisLineColor(Color.BLACK);

        // Adjust the chart's viewport and margins to avoid labels being out of bounds
        //barChart.setExtraTopOffset(50f);
        //barChart.setExtraBottomOffset(20f);
        //barChart.setExtraLeftOffset(20f);
        //barChart.setExtraRightOffset(20f);

        barChart.setData(barData);
        barChart.getLegend().setEnabled(true);

        barChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        barChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        barChart.getXAxis().setGranularity(1f);
        barChart.getXAxis().setGranularityEnabled(true);
        barChart.getXAxis().setLabelRotationAngle(-35);
        barChart.getAxisLeft().setAxisMinimum(0f);
        barChart.invalidate(); // Refresh chart
        }


//    private void updateBarChart(@NonNull List<Tasks> tasksList) {
//
//        if (tasksList.isEmpty()) {
//            // Handle the case where there are no projects
//            TextView textView = findViewById(R.id.dataNotAvailable);
//            textView.setText("No Completed projects to display.");
//            textView.setTextSize(20);
//            barChart.setVisibility(View.GONE);
//            return;
//        }
//
//        List<BarEntry> estimatedTimeEntries = new ArrayList<>();
//        List<BarEntry> completedTimeEntries = new ArrayList<>();
//        List<String> tasksTitle = new ArrayList<>();
//
//        List<String> e = new ArrayList<>();
//        List<String> a = new ArrayList<>();
//
//        for (int i = 0; i < tasksList.size(); i++) {
//            Tasks tasks = tasksList.get(i);
//            String estimatedT= tasks.getEstimatedTime();
//            String completedT = tasks.getCompletedTime();
//
//            // Check if estimatedT is not null
//            if (estimatedT != null) {
//                // Parse the numbers from the strings
//                int estimatedDays = extractDaysFromString(estimatedT);
//
//                // Create separate Entry object for estimated time
//                estimatedTimeEntries.add(new BarEntry(i, estimatedDays));
//
//                // Check if completedT is not null, then add completed time entry
//                if (completedT != null) {
//                    int completedDays = extractDaysFromString(completedT);
//                    completedTimeEntries.add(new BarEntry(i, completedDays));
//                    a.add(completedT);
//
//                } else {
//                    // If completedT is null, add a placeholder entry (you can customize this behavior)
//                    completedTimeEntries.add(new BarEntry(i, 0));
//                }
//                e.add(estimatedT);
//                // Store task titles for x-axis labels
//                tasksTitle.add(tasks.getTaskName());
//            }
//        }
//
//        Description description = new Description();
//        description.setText("Analysis of Completed Tasks Estimated vs Completion Time");
//        description.setPosition(1070f, 70f);
//        description.setTextSize(15f);
//        barChart.setDescription(description);
//
//        BarDataSet estimatedDataSet = new BarDataSet(estimatedTimeEntries, "Estimated Time (In Days)");
//        estimatedDataSet.setColor(Color.BLUE);
//        //estimatedDataSet.setValueFormatter(new StringValueFormatter(e));
//
//        BarDataSet actualDataSet = new BarDataSet(completedTimeEntries, "Completion Time(In Days)");
//        actualDataSet.setColor(Color.RED);
//        //estimatedDataSet.setValueFormatter(new StringValueFormatter(a));
//
//
//        estimatedDataSet.setValueFormatter(new StringValueFormatter(e));
//        actualDataSet.setValueFormatter(new StringValueFormatter(a));
//        // Set unique project titles as x-axis labels
//        barChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(tasksTitle));
//
//        List<IBarDataSet> dataSets = new ArrayList<>();
//        dataSets.add(estimatedDataSet);
//        dataSets.add(actualDataSet);
//
//        BarData barData = new BarData(dataSets);
//
//        // Customize the appearance of the chart
//        barChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
//
//        barChart.getXAxis().setAxisLineColor(Color.BLACK);
//        barChart.getXAxis().setGranularity(1f);
//        barChart.getXAxis().setLabelCount(tasksTitle.size()); // Adjust label count as needed
//
//        barChart.setTouchEnabled(false);
//        //barChart.getXAxis().setLabelRotationAngle(45); title the project title
//        barChart.getAxisRight().setEnabled(false); // Disable the right y-axis
//        barChart.getAxisRight().setDrawLabels(false);
//        barChart.getAxisLeft().setAxisLineColor(Color.BLACK);
//
//        // Adjust the chart's viewport and margins to avoid labels being out of bounds
//        barChart.setExtraTopOffset(50f);
//        barChart.setExtraBottomOffset(20f);
//        barChart.setExtraLeftOffset(20f);
//        barChart.setExtraRightOffset(20f);
//
//        barChart.setData(barData);
//        barChart.getLegend().setEnabled(true); // Enable legend (project names)
//        barChart.invalidate(); // Refresh the chart
//    }



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

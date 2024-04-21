package com.ecom.fyp2023.Analysis;

import android.content.Intent;
import android.graphics.Color;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.ecom.fyp2023.ModelClasses.Tasks;
import com.ecom.fyp2023.R;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class BurndownChart extends AppCompatActivity {
    private static final String TAG = "BurndownChartActivity";

    private FirebaseFirestore db;
    private List<Tasks> allTasks;
    private LineChart chart;

    String projectId;

    //String groupId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_burndown_chart);

        db = FirebaseFirestore.getInstance();

        TextView next = findViewById(R.id.nextAnalysis);

        if (getIntent().hasExtra("PROID")) {
            // Retrieve the data using the key
            projectId = getIntent().getStringExtra("PROID");
        }
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(BurndownChart.this, TasksProgressAnalysis.class);
                startActivity(intent);
                projectId = intent.getStringExtra("PROID");
            }
        });


        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        allTasks = new ArrayList<>();
        chart = findViewById(R.id.lineChart); // Assuming you have a LineChart view in your layout with id lineChart

        retrieveProjects();
    }

    private void retrieveProjects() {
        CollectionReference projectRef = db.collection("Projects");
        projectRef.document(projectId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot projectDocument = task.getResult();
                    if (projectDocument.exists()) {
                        Date startDate = convertToDate(projectDocument.getString("startDate"));
                        Date endDate = convertToDate(projectDocument.getString("endDate")); // Use actual end date if available, otherwise use estimated end date

                        // Retrieve task IDs associated with the project from the projectTasks collection
                        CollectionReference projectTasksRef = db.collection("projectTasks");
                        projectTasksRef.whereEqualTo("projectId", projectId).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    List<String> taskIds = new ArrayList<>();
                                    for (QueryDocumentSnapshot projectTasksDocument : task.getResult()) {
                                        String taskId = projectTasksDocument.getString("taskId");
                                        taskIds.add(taskId);
                                    }

                                    // Retrieve task details using task IDs
                                    retrieveTasks(taskIds, startDate, endDate);
                                } else {
                                    Log.d(TAG, "Error getting documents: ", task.getException());
                                }
                            }
                        });

                    } else {
                        Log.d(TAG, "Project document does not exist");
                    }
                } else {
                    Log.d(TAG, "Error getting project document: ", task.getException());
                }
            }
        });
    }


    private void retrieveTasks(@NonNull List<String> taskIds, Date startDate, Date endDate) {
        CollectionReference taskRef = db.collection("Tasks");

        for (String taskId : taskIds) {
            taskRef.document(taskId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot taskDocument = task.getResult();
                        if (taskDocument.exists()) {
                            // Parse task data and add to allTasks list
                            Tasks t = taskDocument.toObject(Tasks.class);
                            allTasks.add(t);

                            // Check if all tasks have been retrieved
                            if (allTasks.size() == taskIds.size()) {
                                // Calculate remaining work and display burndown chart
                                calculateAndDisplayBurndownChart(allTasks, startDate, endDate);
                            }
                        }
                    } else {
                        Log.d(TAG, "Error getting document: ", task.getException());
                    }
                }
            });
        }
    }

    private void calculateAndDisplayBurndownChart(@NonNull List<Tasks> allTasks, Date startDate, Date endDate) {
        // Calculate the total estimated work
        int totalEstimatedWork = 0;
        for (Tasks task : allTasks) {
            totalEstimatedWork += task.getStoryPoints();
        }

        // Calculate the duration of the project in days
        long durationInMillis = endDate.getTime() - startDate.getTime();
        long durationInDays = TimeUnit.MILLISECONDS.toDays(durationInMillis);

        // Calculate the remaining work for each day
        // Calculate the remaining work for each day
        List<Entry> entries = new ArrayList<>();
        int remainingWork = totalEstimatedWork;
        for (int day = 0; day <= durationInDays; day++) {
            // Calculate the date for the current day
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(startDate);
            calendar.add(Calendar.DAY_OF_MONTH, day);
            Date currentDate = calendar.getTime();

            // Count completed tasks for the current day
            int completedWork = 0;
            for (Tasks task : allTasks) {
                Date taskEndDate = task.getEndDate() != null ? task.getEndDate() : task.getEstimatedEndDate();
                if (taskEndDate != null && !taskEndDate.after(endDate)) {
                    // Include the task if it's completed by the project's end date or within the project's duration
                    if (taskEndDate.before(currentDate) || taskEndDate.equals(currentDate)) {
                        completedWork += task.getStoryPoints();
                    } else {
                        // Include the remaining work of the task if its actual end date is beyond the project's duration
                        completedWork += task.getStoryPoints() * (1 - (float) day / durationInDays);
                    }
                }
            }

            // Update remaining work
            remainingWork -= completedWork;

            // Add entry for the current day to the chart
            entries.add(new Entry(day, remainingWork));
        }

        // Create a LineDataSet from the entries
        LineDataSet dataSet = new LineDataSet(entries, "Remaining Work");
        dataSet.setColor(Color.BLUE);
        dataSet.setCircleColor(Color.BLUE);
        dataSet.setCircleRadius(4f);
        dataSet.setLineWidth(2f);
        dataSet.setDrawValues(false);

        // Create a LineData object with the LineDataSet
        LineData lineData = new LineData(dataSet);

        // Set up the LineChart
        chart.setData(lineData);
        chart.getDescription().setEnabled(false);
        chart.getXAxis().setLabelCount((int) (durationInDays + 1), true);
        chart.getXAxis().setValueFormatter(new IndexAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                // Format x-axis labels to display dates
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(startDate);
                calendar.add(Calendar.DAY_OF_MONTH, (int) value);
                SimpleDateFormat sdf = new SimpleDateFormat("MM/dd", Locale.getDefault());
                return sdf.format(calendar.getTime());
            }
        });
        chart.invalidate(); // Refresh the chart
    }

    // Convert string date to Date object
    private Date convertToDate(@NonNull String dateString) {
        // Parse the date string to extract day, month, and year
        String[] dateParts = dateString.split("-");
        int day = Integer.parseInt(dateParts[0]);
        int month = Integer.parseInt(dateParts[1]);
        int year = Integer.parseInt(dateParts[2]);

        // Create a Calendar instance and set the parsed date values
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month - 1, day); // Month in Calendar is zero-based

        // Return the Date object
        return calendar.getTime();
    }
}

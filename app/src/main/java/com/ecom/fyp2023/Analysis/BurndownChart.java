package com.ecom.fyp2023.Analysis;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.util.Log;

import com.ecom.fyp2023.AppManagers.TimeConverter;
import com.ecom.fyp2023.ModelClasses.Projects;
import com.ecom.fyp2023.ModelClasses.Tasks;
import com.ecom.fyp2023.R;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
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
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class BurndownChart extends AppCompatActivity {
    private static final String TAG = "BurndownChartActivity";


    private FirebaseFirestore db;
    private List<Projects> projects;
    private List<Tasks> allTasks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_burndown_chart);

        db = FirebaseFirestore.getInstance();

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());


        projects = new ArrayList<>();
        allTasks = new ArrayList<>();

        // Retrieve all projects
        CollectionReference projectRef = db.collection("Projects");
        projectRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot projectDocument : task.getResult()) {
                        String projectId = projectDocument.getId();
                        String projectName = projectDocument.getString("title");
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

                    }
                } else {
                    Log.d(TAG, "Error getting documents: ", task.getException());
                }
            }
        });
    }

    private void retrieveTasks(@NonNull List<String> taskIds, Date startDate, Date endDate) {
        CollectionReference taskRef = db.collection("Tasks");

        for (String taskId : taskIds) {
            // Query the tasks collection to find the document with the given task ID
            taskRef.document(taskId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot taskDocument = task.getResult();
                        if (taskDocument.exists()) {
                            String taskName = taskDocument.getString("taskName");
                            String progress = taskDocument.getString("progress");
                            Date taskStartDate = taskDocument.getDate("startDate");
                            Date estimatedEndDate = taskDocument.getDate("estimatedEndDate");
                            Date actualEndDate = taskDocument.getDate("EndDate");
                            String estimatedCompletionTime = taskDocument.getString("estimatedTime");
                            String actualCompletionTime = taskDocument.getString("completedTime");
                            int storyPoints = taskDocument.getLong("storyPoints").intValue();

                            // Convert estimated completion time and actual completion time to days
                            long estimatedCompletionTimeInDays = TimeConverter.convertToDays(estimatedCompletionTime);
                            long actualCompletionTimeInDays = TimeConverter.convertToDays(actualCompletionTime);

                            // Create Task object and add it to allTasks list
                        }
                    } else {
                        Log.d(TAG, "Error getting document: ", task.getException());
                    }
                }
            });
        }
    }

    @NonNull
    private Map<Date, Integer> calculateRemainingWork(@NonNull List<Tasks> sprintTasks, Date sprintStartDate, Date sprintEndDate) {
        // Calculate total work
        int totalWork = 0;
        for (Tasks task : sprintTasks) {
            totalWork += task.getStoryPoints();
        }

        // Calculate remaining work for each day during the sprint
        Map<Date, Integer> remainingWorkPerDay = new HashMap<>();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(sprintStartDate); // Start from sprint start date

        while (calendar.getTime().before(sprintEndDate) || calendar.getTime().equals(sprintEndDate)) {
            int completedWork = 0;
            for (Tasks task : sprintTasks) {
                if ((task.getEndDate() != null && task.getEndDate().before(calendar.getTime())) || (task.getEndDate() != null && task.getEndDate().equals(calendar.getTime()))) {
                    completedWork += task.getStoryPoints();
                }
            }
            int remainingWork = totalWork - completedWork;
            remainingWorkPerDay.put(calendar.getTime(), remainingWork);
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }

        return remainingWorkPerDay;
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

    private void displayBurndownChart(@NonNull Map<Date, Integer> remainingWorkPerDay) {
        LineChart chart = findViewById(R.id.lineChart); // Assuming you have a LineChart view in your layout with id chart

        // Convert Map<Date, Integer> to List<Entry> for plotting
        List<Entry> entries = new ArrayList<>();
        int dayIndex = 0;
        for (Map.Entry<Date, Integer> entry : remainingWorkPerDay.entrySet()) {
            entries.add(new Entry(dayIndex, entry.getValue()));
            dayIndex++;
        }

        // Create a dataset from the entries
        LineDataSet dataSet = new LineDataSet(entries, "Remaining Work");
        dataSet.setColor(Color.BLUE);
        dataSet.setValueTextColor(Color.BLACK);

        // Create a LineData object from the dataset
        LineData lineData = new LineData(dataSet);

        // Set the LineData to the chart
        chart.setData(lineData);

        // Customize chart appearance and behavior
        chart.getDescription().setEnabled(false);
        chart.setDrawGridBackground(false);
        chart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        chart.getXAxis().setLabelCount(entries.size(), true);
        chart.getXAxis().setValueFormatter(new IndexAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                // Convert float index back to date string for labeling X axis
                int index = (int) value;
                if (index >= 0 && index < entries.size()) {
                    Date date = remainingWorkPerDay.keySet().toArray(new Date[0])[(int) value];
                    SimpleDateFormat sdf = new SimpleDateFormat("MM/dd", Locale.getDefault());
                    return sdf.format(date);
                }
                return "";
            }
        });

        // Refresh chart
        chart.invalidate();
    }


}
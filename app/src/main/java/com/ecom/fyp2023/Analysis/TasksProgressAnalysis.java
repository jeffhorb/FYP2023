package com.ecom.fyp2023.Analysis;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.ecom.fyp2023.AppManagers.SwipeGestureListenerTasksAnalysis;
import com.ecom.fyp2023.ModelClasses.Projects;
import com.ecom.fyp2023.ModelClasses.Tasks;
import com.ecom.fyp2023.ProjectActivity;
import com.ecom.fyp2023.R;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class TasksProgressAnalysis extends AppCompatActivity {

    PieChart pieChart;
    GestureDetector gestureDetector;

    TextView next;

    String projectId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tasks_progress_analysis);

        pieChart = findViewById(R.id.pieChartTask);
        next = findViewById(R.id.next);

        gestureDetector = new GestureDetector(this, new SwipeGestureListenerTasksAnalysis(this));

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());


        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Intent intent = getIntent();
        if (intent.hasExtra("PROJECTid")) {
            // Retrieve the data using the key
            projectId = intent.getStringExtra("PROJECTid");
        }

        Intent intent1 = getIntent();
        if (intent1.hasExtra("PROJECTID")) {
            // Retrieve the data using the key
            projectId = intent1.getStringExtra("PROJECTID");
        }

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent2 = new Intent(TasksProgressAnalysis.this, CompletedTasksAnalysis.class);
                intent2.putExtra("PROID", projectId);
                startActivity(intent2);
            }
        });

        Intent intent3 = getIntent();
        if (intent3.hasExtra("PROID")) {
            // Retrieve the data using the key
            projectId = intent3.getStringExtra("PROID");
        }

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

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return gestureDetector.onTouchEvent(event) || super.onTouchEvent(event);
    }


    private void retrieveTaskDetails(String taskId, List<Tasks> tasksList) {
        CollectionReference tasksCollection = FirebaseFirestore.getInstance().collection("Tasks");
        tasksCollection.document(taskId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    Tasks taskData = document.toObject(Tasks.class);
                    taskData.setTaskId(document.getId());
                    tasksList.add(taskData);
                    updatePieChart(tasksList);

                } else {
                    Log.e("Firestore", "Error fetching task details: " + task.getException());
                }
            }
        });
    }

    private void updatePieChart(@NonNull List<Tasks> tasksList) {
        List<PieEntry> entries = new ArrayList<>();

        int completeCount = 0, inProgressCount = 0, incompleteCount = 0;

        for (Tasks tasks : tasksList) {
            switch (tasks.getProgress()) {
                case "Complete":
                    completeCount++;
                    break;
                case "In Progress":
                    inProgressCount++;
                    break;
                case "Incomplete":
                    incompleteCount++;
                    break;
            }
        }

        // Add entries only if the counts are greater than zero
        if (completeCount > 0) {
            entries.add(new PieEntry(completeCount, "Complete"));
        }
        if (inProgressCount > 0) {
            entries.add(new PieEntry(inProgressCount, "In Progress"));
        }
        if (incompleteCount > 0) {
            entries.add(new PieEntry(incompleteCount, "Incomplete"));
        }

        // If there are no entries, display a message or handle it as needed
        if (entries.isEmpty()) {
            // For example, display a message in a TextView
            TextView textView = findViewById(R.id.noData); // Use your actual TextView ID
            textView.setText("No projects to display.");
            pieChart.setVisibility(View.GONE); // Hide the PieChart
        } else {
            PieDataSet dataSet = new PieDataSet(entries, "Tasks Progress");
            dataSet.setColors(ColorTemplate.COLORFUL_COLORS);

            PieData data = new PieData(dataSet);

            pieChart.setTouchEnabled(false);

            pieChart.setData(data);
            pieChart.setVisibility(View.VISIBLE); // Show the PieChart
            pieChart.invalidate(); // Refresh the chart
        }
    }
}
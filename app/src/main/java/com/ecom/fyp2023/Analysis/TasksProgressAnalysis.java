package com.ecom.fyp2023.Analysis;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import com.ecom.fyp2023.AppManagers.SwipeGestureListenerTasksAnalysis;
import com.ecom.fyp2023.ModelClasses.Projects;
import com.ecom.fyp2023.ModelClasses.Tasks;
import com.ecom.fyp2023.R;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class TasksProgressAnalysis extends AppCompatActivity {

    PieChart pieChart;
    GestureDetector gestureDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tasks_progress_analysis);

        pieChart = findViewById(R.id.pieChartTask);

        gestureDetector = new GestureDetector(this, new SwipeGestureListenerTasksAnalysis(this));

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());


        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Tasks")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        List<Tasks> tasksList = new ArrayList<>();

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Tasks tasks = document.toObject(Tasks.class);
                            tasksList.add(tasks);
                        }

                        updatePieChart(tasksList);
                    } else {
                        // Handle errors
                    }
                });
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return gestureDetector.onTouchEvent(event) || super.onTouchEvent(event);
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
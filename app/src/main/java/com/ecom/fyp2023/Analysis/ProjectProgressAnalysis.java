package com.ecom.fyp2023.Analysis;

import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.ecom.fyp2023.AppManagers.SwipeGestureListenerProjectAnalysis;
import com.ecom.fyp2023.ModelClasses.Projects;
import com.ecom.fyp2023.R;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
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

public class ProjectProgressAnalysis extends AppCompatActivity {

    PieChart pieChart;
    FirebaseFirestore db;

    private GestureDetector gestureDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project_progress_analysis);

        gestureDetector = new GestureDetector(this, new SwipeGestureListenerProjectAnalysis(this));

        pieChart = findViewById(R.id.pieChart);

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
                        //List<Projects> projectsWithDates = new ArrayList<>();

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Projects project = document.toObject(Projects.class);
                            projects.add(project);

                            updatePieChart(projects);
                        }
                    } else {
                        // Handle errors
                    }
                });
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return gestureDetector.onTouchEvent(event) || super.onTouchEvent(event);
    }
    private void updatePieChart(@NonNull List<Projects> projects) {
        List<PieEntry> entries = new ArrayList<>();

        Description description = new Description();
        description.setText("Analysis of all project progress");
        description.setPosition(900f, 150f);
        description.setTextSize(20f);

        int completeCount = 0, inProgressCount = 0, incompleteCount = 0;

        for (Projects project : projects) {
            switch (project.getProgress()) {
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
            TextView textView = findViewById(R.id.noData); // Use your actual TextView ID
            textView.setText("No projects to display.");
            pieChart.setVisibility(View.GONE); // Hide the PieChart
        } else {
            PieDataSet dataSet = new PieDataSet(entries, "Project Progress");
            dataSet.setColors(ColorTemplate.COLORFUL_COLORS);
            pieChart.setTouchEnabled(false);


            PieData data = new PieData(dataSet);
            pieChart.setDescription(description);

            pieChart.setData(data);
            pieChart.setVisibility(View.VISIBLE); // Show the PieChart
            pieChart.invalidate(); // Refresh the chart
        }
    }

}
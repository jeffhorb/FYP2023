package com.ecom.fyp2023.Analysis;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.anychart.AnyChart;
import com.anychart.AnyChartView;
import com.anychart.chart.common.dataentry.DataEntry;
import com.anychart.chart.common.dataentry.ValueDataEntry;
import com.anychart.charts.Cartesian;
import com.ecom.fyp2023.R;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public class VelocityAnalysis extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_velocity_analysis);

        AnyChartView anyChartView = findViewById(R.id.lineChart1);
        TextView next = findViewById(R.id.next);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(VelocityAnalysis.this, ProjectProgressAnalysis.class);
                startActivity(intent);
            }
        });

        Cartesian cartesian = AnyChart.column();

        // Fetch the data from Firestore and add it to the chart
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Projects").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                List<DataEntry> data = new ArrayList<>(); // Move data initialization here
                for (QueryDocumentSnapshot sprintDoc : task.getResult()) {
                    String sprintId = sprintDoc.getId();
                    String title = sprintDoc.getString("title");
                    AtomicInteger totalStoryPoints = new AtomicInteger();
                    db.collection("projectTasks").whereEqualTo("projectId", sprintId).get().addOnCompleteListener(task1 -> {
                        if (task1.isSuccessful()) {
                            for (QueryDocumentSnapshot taskDoc : task1.getResult()) {
                                String taskId = taskDoc.getString("taskId");

                                assert taskId != null;
                                db.collection("Tasks").document(taskId).get().addOnSuccessListener(documentSnapshot -> {
                                    if (documentSnapshot.exists()) {
                                        String progress = documentSnapshot.getString("progress");
                                        if (progress != null && progress.equals("Complete")) {
                                            totalStoryPoints.addAndGet(documentSnapshot.getLong("storyPoints").intValue());
                                            // Don't update the chart here
                                            // Update the chart when all tasks for the sprint are processed
                                            data.add(new ValueDataEntry(title, totalStoryPoints));
                                            cartesian.data(data);
                                            anyChartView.setChart(cartesian);
                                        }
                                    }
                                });
                            }

                        } else {
                            Log.d("Firestore", "Error getting documents: ", task1.getException());
                        }
                    });
                }
            } else {
                Log.d("Firestore", "Error getting documents: ", task.getException());
            }
        });

        cartesian.animation(true);
        cartesian.title("Sprint Velocity");
    }
}

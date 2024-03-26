package com.ecom.fyp2023.Analysis;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.ecom.fyp2023.AppManagers.TimeConverter;
import com.ecom.fyp2023.ModelClasses.Tasks;
import com.ecom.fyp2023.R;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class TeamMemberEvaluation extends AppCompatActivity {
    String userId,userName,projectId;

    PieChart pieChart;
    FirebaseFirestore db;


    TextView next,memberName,noCompleted,noInprogress,noIncomplete,avgCompletionTime,noTasks;
    ImageView completeTasksList,incompleteTasksList,inprogressTasksList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_team_member_evaluation);
        memberName = findViewById(R.id.memberName);
        noTasks = findViewById(R.id.AssignedTasks);
        noCompleted = findViewById(R.id.completedTasks);
        noIncomplete = findViewById(R.id.incompleteTasks);
        noInprogress = findViewById(R.id.tasksInProgress);
        avgCompletionTime = findViewById(R.id.averageCompletionTime);
        completeTasksList = findViewById(R.id.expandCompleted);
        incompleteTasksList = findViewById(R.id.expandIncomplete);
        inprogressTasksList = findViewById(R.id.expandInProgress);
        pieChart = findViewById(R.id.pieChartTask);
        next = findViewById(R.id.next);
        db = FirebaseFirestore.getInstance();



        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());


        // Retrieve the intent that started this activity
        Intent intent = getIntent();
        if (intent.hasExtra("projID")){
            projectId = intent.getStringExtra("projID");

            if(intent.hasExtra("userID")&& intent.hasExtra("userName")){
                // Retrieve the data using the key you used when putting the data
                userId = intent.getStringExtra("userID");
                userName = intent.getStringExtra("userName");
                memberName.setText(userName);

            }
        }

        Intent intent2 = getIntent();
        if (intent2.hasExtra("projectId")){
            projectId = intent2.getStringExtra("projectId");

            if(intent.hasExtra("userID")&& intent.hasExtra("userName")){
                // Retrieve the data using the key you used when putting the data
                userId = intent.getStringExtra("userID");
                userName = intent.getStringExtra("userName");
                memberName.setText(userName);
            }
        }

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent2 = new Intent(TeamMemberEvaluation.this, TeamMemberTasksAnalysis.class);
                intent2.putExtra("userID", userId);
                intent2.putExtra("userName", userName);
                intent2.putExtra("projectId",projectId);
                startActivity(intent2);
            }
        });

        retrieveTasksForProjectAndUser(projectId,userId);



    }

    private void retrieveTasksForProjectAndUser(String projectId, String userId) {
        CollectionReference projectTasksCollection = FirebaseFirestore.getInstance().collection("projectTasks");
        CollectionReference userTasksCollection = FirebaseFirestore.getInstance().collection("userTasks");

        // Filter taskIds associated with the project
        Query projectQuery = projectTasksCollection.whereEqualTo("projectId", projectId);
        projectQuery.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                List<String> projectTaskIds = new ArrayList<>();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    projectTaskIds.add(document.getString("taskId"));
                }
                // Further filter taskIds associated with the user
                Query userQuery = userTasksCollection.whereEqualTo("userId", userId)
                        .whereIn("taskId", projectTaskIds);
                userQuery.get().addOnCompleteListener(userTask -> {
                    if (userTask.isSuccessful()) {
                        List<String> userTaskIds = new ArrayList<>();
                        for (QueryDocumentSnapshot document : userTask.getResult()) {
                            userTaskIds.add(document.getString("taskId"));
                        }
                        // Step 3: Retrieve task details for the filtered taskIds
                        List<Tasks> tasksList = new ArrayList<>();
                        List<String > list = new ArrayList<>();
                        for (String taskId : userTaskIds) {
                            retrieveTaskDetails(taskId, tasksList);
                            list.add(taskId);
                        }
                        noTasks.setText( String.valueOf(list.size()));

                    } else {
                        Log.e("Firestore", "Error fetching user tasks: " + userTask.getException());
                    }
                });
            } else {
                Log.e("Firestore", "Error fetching project tasks: " + task.getException());
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
                    updatePieChart(tasksList);

                    calculateAverageCompletionTime(tasksList);


                    List<String> cList = new ArrayList<>();
                    List<String> ipList = new ArrayList<>();
                    List<String> icList = new ArrayList<>();

                    // Aggregate task progress
                    for (Tasks t : tasksList) {
                        if (t.getProgress().equals("Complete")) {
                            cList.add("Task: "+t.getTaskName()+"  ."+"completionTime: "+t.getCompletedTime()+"(s)");
                        } else if (t.getProgress().equals("Incomplete")) {
                            icList.add("Task: "+t.getTaskName()+"  ."+"EstimatedTime: "+t.getEstimatedTime()+"(s)");
                        } else if (t.getProgress().equals("In Progress")) {
                            ipList.add("Task: "+t.getTaskName()+"  ."+"EstimatedTime: "+t.getEstimatedTime()+"(s)");
                        }
                    }

                    // Update UI based on aggregated progress lists
                    noCompleted.setText(String.valueOf(cList.size()));
                    noIncomplete.setText(String.valueOf(icList.size()));
                    noInprogress.setText(String.valueOf(ipList.size()));

                    // Set click listeners based on aggregated lists
                    completeTasksList.setOnClickListener(v -> showCompletedTasksDialog(cList));
                    incompleteTasksList.setOnClickListener(v -> showCompletedTasksDialog(icList));
                    inprogressTasksList.setOnClickListener(v -> showCompletedTasksDialog(ipList));
                } else {
                    Log.e("Firestore", "Error fetching task details: Document does not exist");
                }
            } else {
                Log.e("Firestore", "Error fetching task details: " + task.getException());
            }
        });
    }

    private void calculateAverageCompletionTime(@NonNull List<Tasks> tasksList) {
        int totalCompletionTimeInDays = 0;
        int totalCompletedTasks = 0;

        for (Tasks task : tasksList) {
            if (task.getProgress().equals("Complete")) {
                String completionTime = task.getCompletedTime();
                if (completionTime != null && !completionTime.isEmpty()) {
                    totalCompletionTimeInDays += TimeConverter.convertToDays(completionTime);
                    totalCompletedTasks++;
                }
            }
        }
        // Calculate average completion time
        double averageCompletionTime = totalCompletedTasks > 0 ?
                (double) totalCompletionTimeInDays / totalCompletedTasks : 0;

            avgCompletionTime.setText(String.format("%.2f days", averageCompletionTime));

    }

    private void showCompletedTasksDialog(@NonNull List<String> completedTasksNames) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Completed Tasks");

        if (completedTasksNames.isEmpty()) {
            builder.setMessage("No completed tasks found.");
        } else {
            StringBuilder message = new StringBuilder();
            for (String taskName : completedTasksNames) {
                message.append(taskName).append("\n");
            }
            builder.setMessage(message.toString());
        }

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
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
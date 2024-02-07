package com.ecom.fyp2023;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.ecom.fyp2023.AppManagers.FirestoreManager;
import com.ecom.fyp2023.Fragments.CommentListFragment;
import com.ecom.fyp2023.Fragments.UpdateTaskFragment;
import com.ecom.fyp2023.Fragments.UsersListFragment;
import com.ecom.fyp2023.ModelClasses.Tasks;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class TaskActivity extends AppCompatActivity {

    TextView tasksDetails,endDate,estTime,taskPro, commentFrag, userAssigned;
    ImageView expandMore,progressExpand;

    String projectId;
    //String username;

    FirebaseFirestore fb;

    // Interface for progress updates
    public interface OnTaskProgressUpdateListener {
        void onTaskProgressUpdated(String projectId);
    }

    private OnTaskProgressUpdateListener taskProgressUpdateListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activty_task);

        taskPro = findViewById(R.id.taskPro);
        tasksDetails = findViewById(R.id.taskDetails);
        endDate = findViewById(R.id.endDate);
        estTime = findViewById(R.id.eTime);
        commentFrag = findViewById(R.id.commentEditText);
        expandMore = findViewById(R.id.moreImageView);
        userAssigned = findViewById(R.id.userAssigned);
        progressExpand = findViewById(R.id.expandImageView);

        fb = FirebaseFirestore.getInstance();

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        expandMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPopupMenuForOption(view);
            }
        });

        progressExpand.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPopupMenu(view);
            }
        });

        Intent i1 = getIntent();
        if (i1.hasExtra("projectId1")) {
            projectId = i1.getStringExtra("projectId1");
        }

        Intent i = getIntent();
        if (i.hasExtra("projectId2")) {
            projectId = i.getStringExtra("projectId2");
        }

        Intent intent = getIntent();
        if (intent.hasExtra("selectedTask")) {
            Tasks tasks = (Tasks) intent.getSerializableExtra("selectedTask");

            assert tasks != null;
            tasksDetails.setText(tasks.getTaskDetails());
            taskPro.setText(tasks.getProgress());
            estTime.setText(tasks.getEstimatedTime());

            FirestoreManager firestoreManager = new FirestoreManager();
            firestoreManager.getDocumentId("Tasks", "taskDetails", tasks.getTaskDetails(), documentId -> {
                if (documentId != null) {

                    loadUserAssigned(documentId);

                    Bundle bundle = new Bundle();
                    bundle.putString("pId", documentId);
                    UpdateTaskFragment fragment = new UpdateTaskFragment();
                    fragment.setArguments(bundle);
                }
            });
        }

        commentFrag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CommentListFragment bottomSheetFragment = CommentListFragment.newInstance(projectId);
                bottomSheetFragment.show(getSupportFragmentManager(), bottomSheetFragment.getTag());
            }
        });
    }

   private void showPopupMenuForOption(View view) {
        PopupMenu popupMenu = new PopupMenu(this, view);
        popupMenu.inflate(R.menu.task_menu);

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {

                MenuItem unAssignTaskMenuItem = popupMenu.getMenu().findItem(R.id.UnAssignTask);

                if (menuItem.getItemId() == R.id.updateT) {

                    Intent intent = getIntent();
                    if (intent.hasExtra("selectedTask")) {
                        Tasks tasks = (Tasks) intent.getSerializableExtra("selectedTask");

                        UpdateTaskFragment updateTaskFragment = UpdateTaskFragment.newInstance();
                        Bundle bundle = new Bundle();
                        bundle.putSerializable("selectT",tasks);
                        bundle.putString("pro_key", projectId);
                        updateTaskFragment.setArguments(bundle);
                        updateTaskFragment.show(getSupportFragmentManager(), updateTaskFragment.getTag());
                    }
                }else if (menuItem.getItemId() == R.id.assignTask){

                    Intent intent = getIntent();
                    if (intent.hasExtra("selectedTask")) {
                        Tasks tasks = (Tasks) intent.getSerializableExtra("selectedTask");

                        FirestoreManager firestoreManager = new FirestoreManager();
                        assert tasks != null;
                        firestoreManager.getDocumentId("Tasks", "taskDetails", tasks.getTaskDetails(), documentId -> {
                            if (documentId != null) {

                                UsersListFragment usersListFragment = UsersListFragment.newInstance();
                                Bundle bundle = new Bundle();
                                bundle.putString("TASKID", documentId);
                                usersListFragment.setArguments(bundle);
                                usersListFragment.show(getSupportFragmentManager(), usersListFragment.getTag());
                                //try to make the unassign menu item visible only when task is assigned to user
                                //username = userAssigned.getText().toString();
                                //if (!username.equalsIgnoreCase("Unassigned")) {
                                 //   unAssignTaskMenuItem.setVisible(true);
                                //}
                            }
                        });
                    }

                }else if(menuItem.getItemId() == R.id.UnAssignTask){

                    Intent intent = getIntent();
                    if (intent.hasExtra("selectedTask")) {
                        Tasks tasks = (Tasks) intent.getSerializableExtra("selectedTask");
                        FirestoreManager firestoreManager = new FirestoreManager();
                        firestoreManager.getDocumentId("Tasks", "taskDetails", tasks.getTaskDetails(), documentId -> {
                            if (documentId != null) {
                                unAssignTask(documentId);
                                userAssigned.setText("Unassigned");
                                //unAssignTaskMenuItem.setVisible(false);
                            }
                        });
                    }
                }
                return true;
            }
        });
        // Show the popup menu
        popupMenu.show();
    }

    private void showPopupMenu(View view) {
        PopupMenu popupMenu = new PopupMenu(this, view);
        popupMenu.inflate(R.menu.task_progress_menu);
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {

                if (menuItem.getItemId() == R.id.taskIncomplete) {
                    updateTaskProgress("Incomplete");

                } else if (menuItem.getItemId() == R.id.taskInprogress) {
                    updateTaskProgress("In Progress");
                } else if (menuItem.getItemId() == R.id.taskComplete) {
                    updateTaskProgress("Complete");
                }
                return true;
            }
        });
        // Show the popup menu
        popupMenu.show();
    }
    private void updateTaskProgress(String progress) {
        Intent intent = getIntent();
        if (intent.hasExtra("selectedTask")) {
            Tasks tasks = (Tasks) intent.getSerializableExtra("selectedTask");

            FirestoreManager firestoreManager = new FirestoreManager();
            assert tasks != null;
            firestoreManager.getDocumentId("Tasks", "taskDetails", tasks.getTaskDetails(), documentId -> {
                if (documentId != null) {
                    FirebaseFirestore.getInstance().collection("Tasks")
                            .document(documentId)
                            .update("progress", progress)
                            .addOnSuccessListener(aVoid -> {
                                // Update successful
                                Toast.makeText(TaskActivity.this, "Progress updated to " + progress, Toast.LENGTH_SHORT).show();
                                taskPro.setText(progress);
                                tasks.setProgress(progress);

                                // Automatically update project progress based on task progress
                                updateProjectProgressAuto(projectId, "Incomplete");

                            })
                            .addOnFailureListener(e -> {
                                // Error updating progress
                                Log.e("updateTaskProgress", "Error updating progress", e);
                                Toast.makeText(TaskActivity.this, "Error updating progress", Toast.LENGTH_SHORT).show();
                            });
                }
            });
        }
    }

    private void updateProjectProgressAuto(String projectId, String progress) {
        // Query the projectTasks collection to check the progress of associated tasks
        FirebaseFirestore.getInstance().collection("projectTasks")
                .whereEqualTo("projectId", projectId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        AtomicBoolean anyTaskInProgress = new AtomicBoolean(false);
                        AtomicBoolean allTasksComplete = new AtomicBoolean(true);
                        // List to store task IDs
                        List<String> taskIds = new ArrayList<>();

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String taskId = document.getString("taskId");
                            taskIds.add(taskId);

                            // Fetch the task document to get the progress
                            FirebaseFirestore.getInstance().collection("Tasks")
                                    .document(taskId)
                                    .get()
                                    .addOnCompleteListener(taskDocument -> {
                                        if (taskDocument.isSuccessful()) {
                                            DocumentSnapshot taskSnapshot = taskDocument.getResult();

                                            // Check if the task document exists and contains the "progress" field
                                            if (taskSnapshot.exists() && taskSnapshot.contains("progress")) {
                                                String taskProgress = taskSnapshot.getString("progress");
                                                // Check if any task is "In Progress"
                                                if (taskProgress != null && taskProgress.equalsIgnoreCase("In Progress")) {
                                                    anyTaskInProgress.set(true);
                                                }
                                                // Check if taskProgress is null or not equal to "Complete"
                                                if (taskProgress == null || !taskProgress.equalsIgnoreCase("Complete")) {
                                                    allTasksComplete.set(false);
                                                }
                                            }
                                        }
                                        // Check the flags after each task document fetch
                                        if (anyTaskInProgress.get()) {
                                            // If any task is "In Progress," update the project progress to "In Progress"
                                            updateProjectProgressInFirestore(projectId, "In Progress");
                                        } else if (allTasksComplete.get()) {
                                            // If all tasks are complete, update the project progress to "Complete"
                                            updateProjectProgressInFirestore(projectId, "Complete");
                                        } else {
                                            // If none of the tasks are complete, update the project progress to "Incomplete"
                                            updateProjectProgressInFirestore(projectId, "Incomplete");
                                        }
                                    });
                        }
                    } else {
                        Log.e("Firestore", "Error getting tasks: " + task.getException());
                    }
                });
    }

    private void updateProjectProgressInFirestore(String projectId, String progress) {
        // Update the 'progress' field in the 'Projects' collection
        FirebaseFirestore.getInstance().collection("Projects")
                .document(projectId)
                .update("progress", progress)
                .addOnSuccessListener(aVoid -> {
                    Log.d("Firestore", "Progress updated successfully");

                    //Toast.makeText(ProjectActivity.this, "Project progress updated", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Failed to update progress: " + e.getMessage());
                });
    }


    private void loadUserAssigned(String taskId) {
        FirebaseFirestore.getInstance().collection("userTasks")
                .whereEqualTo("taskId", taskId)
                .limit(1)
                .addSnapshotListener(this, (value, error) -> {
                    if (error != null) {
                        Log.e("loadUserAssigned", "Error getting user assignment", error);
                        return;
                    }
                    if (value != null && !value.isEmpty()) {
                        String userId = value.getDocuments().get(0).getString("userId");
                        loadUserDetails(userId);
                    } else {
                        userAssigned.setText("Unassigned");
                    }
                });
    }

    private void loadUserDetails(String userId) {
        FirebaseFirestore.getInstance().collection("Users")
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        userAssigned.setText(documentSnapshot.getString("userName"));
                    } else {
                        userAssigned.setText("Unassigned");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("loadUserDetails", "Error loading user details", e);
                    userAssigned.setText("Error");
                });
    }

    private void unAssignTask(String taskId) {
        FirebaseFirestore.getInstance().collection("userTasks")
                .whereEqualTo("taskId", taskId)
                .limit(1)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        // Document found, delete it
                        String documentId = task.getResult().getDocuments().get(0).getId();
                        FirebaseFirestore.getInstance().collection("userTasks")
                                .document(documentId)
                                .delete()
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(TaskActivity.this, "Task Unassigned", Toast.LENGTH_SHORT).show();
                                    // Update the UI or perform any other actions
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("unAssignTask", "Error deleting document", e);
                                    Toast.makeText(TaskActivity.this, "Error Unassigning Task", Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        // Document not found
                        Toast.makeText(TaskActivity.this, "Task already Unassigned", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
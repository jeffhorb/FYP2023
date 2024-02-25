package com.ecom.fyp2023;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.ecom.fyp2023.AppManagers.FirestoreManager;
import com.ecom.fyp2023.AppManagers.SharedPreferenceManager;
import com.ecom.fyp2023.Fragments.CommentListFragment;
import com.ecom.fyp2023.Fragments.NotesFragment;
import com.ecom.fyp2023.Fragments.UpdateTaskFragment;
import com.ecom.fyp2023.Fragments.UsersListFragment;
import com.ecom.fyp2023.ModelClasses.Notes;
import com.ecom.fyp2023.ModelClasses.Tasks;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class TaskActivity extends AppCompatActivity {

    TextView tasksDetails, endDate, estTime, taskPro, commentFrag, userAssigned,taskName;
    ImageView expandMore, progressExpand, prerequisiteList;

    EditText notes;
    String projectId,notesId;
    Button save, clear,view;

    FirebaseFirestore fb;

    SharedPreferenceManager sharedPreferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activty_task);

        taskPro = findViewById(R.id.taskPro);
        taskName = findViewById(R.id.taskName);
        tasksDetails = findViewById(R.id.taskDetails);
        endDate = findViewById(R.id.endDate);
        estTime = findViewById(R.id.eTime);
        commentFrag = findViewById(R.id.commentEditText);
        expandMore = findViewById(R.id.moreImageView);
        userAssigned = findViewById(R.id.userAssigned);
        progressExpand = findViewById(R.id.expandImageView);
        prerequisiteList = findViewById(R.id.prerequisiteList);
        notes = findViewById(R.id.notesEditText);
        save = findViewById(R.id.saveNote);
        clear = findViewById(R.id.clearNote);
        view = findViewById(R.id.viewNote);

        fb = FirebaseFirestore.getInstance();

        sharedPreferenceManager = new SharedPreferenceManager(this);

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

        // Set a TextWatcher to update shared preferences in real-time
        notes.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int before, int count) {
                // Not needed for this case
            }
            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                // Save the note to SharedPreferences as the text changes
                String note = charSequence.toString();
                saveNoteToSharedPreferences(note);
            }
            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        Intent intent = getIntent();
        if (intent.hasExtra("selectedTask")) {
            Tasks tasks = (Tasks) intent.getSerializableExtra("selectedTask");

            prerequisiteList.setOnClickListener(v -> {
                // Show the prerequisites dialog
                showPrerequisitesDialog(tasks.getPrerequisites());
            });

            assert tasks != null;
            taskName.setText(tasks.getTaskName());
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

                // Set an OnClickListener for the save notes button
                save.setOnClickListener(v -> {
                    // Get the text from the EditText
                    String note = notes.getText().toString();

                    if (TextUtils.isEmpty(note)){
                        notes.setError("Add notes");
                    }else {
                        saveNotes(note, documentId);
                    }
                });

                view.setOnClickListener(v -> {
                    Bundle bundle = new Bundle();
                    bundle.putString("taskID", documentId);
                    NotesFragment fragment = new NotesFragment();
                    fragment.setArguments(bundle);
                    fragment.show(getSupportFragmentManager(), fragment.getTag());

                });
                // Retrieve and set the stored note in EditText when the activity starts
                String storedNote = sharedPreferenceManager.getStoredNoteForTask(documentId);
                notes.setText(storedNote);
            });
        }

        //open comments fragment
        commentFrag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CommentListFragment bottomSheetFragment = CommentListFragment.newInstance(projectId);
                bottomSheetFragment.show(getSupportFragmentManager(), bottomSheetFragment.getTag());
            }
        });


        // Set OnClickListener for the clear notes button
        clear.setOnClickListener(v -> {

            sharedPreferenceManager.clearStoredNote();
            notes.setText("");
        });
    }

    private void showPopupMenuForOption(View view) {
        PopupMenu popupMenu = new PopupMenu(this, view);
        popupMenu.inflate(R.menu.task_menu);

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {

                //MenuItem unAssignTaskMenuItem = popupMenu.getMenu().findItem(R.id.UnAssignTask);

                if (menuItem.getItemId() == R.id.updateT) {

                    Intent intent = getIntent();
                    if (intent.hasExtra("selectedTask")) {
                        Tasks tasks = (Tasks) intent.getSerializableExtra("selectedTask");

                        FirestoreManager firestoreManager = new FirestoreManager();
                        assert tasks != null;
                        firestoreManager.getDocumentId("Tasks", "taskDetails", tasks.getTaskDetails(), documentId -> {
                            if (documentId != null) {

                                tasks.setTaskId(documentId);

                                UpdateTaskFragment updateTaskFragment = UpdateTaskFragment.newInstance();
                                updateTaskFragment.setOnTaskUpdateListener((UpdateTaskFragment.OnTaskUpdateListener) TaskActivity.this);
                                Bundle bundle = new Bundle();
                                bundle.putSerializable("selectT", tasks);
                                bundle.putString("pro_key", projectId);
                                updateTaskFragment.setArguments(bundle);
                                updateTaskFragment.show(getSupportFragmentManager(), updateTaskFragment.getTag());

                            }
                        });
                    }
                } else if (menuItem.getItemId() == R.id.assignTask) {

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

                } else if (menuItem.getItemId() == R.id.UnAssignTask) {

                    Intent intent = getIntent();
                    if (intent.hasExtra("selectedTask")) {
                        Tasks tasks = (Tasks) intent.getSerializableExtra("selectedTask");
                        FirestoreManager firestoreManager = new FirestoreManager();
                        assert tasks != null;
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
                    //updateTaskProgress("Complete");
                    updateTaskProgressToComplete("Complete");
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
                                if ("Complete".equals(progress)) {
                                    // Record timestamp when progress is set to Complete
                                    updateProjectProgressAuto(projectId, "Complete");
                                } else {
                                    // Handle the case when progress is changed from Complete
                                    updateProjectProgressAuto(projectId,progress);
                                }
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

    private void updateTaskProgressToComplete(String progress) {
        Intent intent = getIntent();
        if (intent.hasExtra("selectedTask")) {
            Tasks tasks = (Tasks) intent.getSerializableExtra("selectedTask");

            FirestoreManager firestoreManager = new FirestoreManager();
            assert tasks != null;
            firestoreManager.getDocumentId("Tasks", "taskDetails", tasks.getTaskDetails(), documentId -> {
                if (documentId != null) {
                    // Fetch the list of prerequisites for the current task
                    List<String> prerequisites = tasks.getPrerequisites();

                    // Check if all prerequisites are complete
                    arePrerequisitesComplete(prerequisites, arePrerequisitesComplete -> {
                        if (arePrerequisitesComplete) {
                            FirebaseFirestore.getInstance().collection("Tasks")
                                    .document(documentId)
                                    .update("progress", progress)
                                    .addOnSuccessListener(aVoid -> {
                                        // Update successful
                                        Toast.makeText(TaskActivity.this, "Progress updated to " + progress, Toast.LENGTH_SHORT).show();
                                        taskPro.setText(progress);
                                        tasks.setProgress(progress);

                                        // Automatically update project progress based on task progress
                                        updateProjectProgressAuto(projectId,"Complete");

                                    })
                                    .addOnFailureListener(e -> {
                                        // Error updating progress
                                        Log.e("updateTaskProgress", "Error updating progress", e);
                                        Toast.makeText(TaskActivity.this, "Error updating progress", Toast.LENGTH_SHORT).show();
                                    });
                        } else {
                            // Display a warning that prerequisites are not complete
                            Toast.makeText(TaskActivity.this, "Prerequisites are not complete. " + tasks.getPrerequisites(), Toast.LENGTH_LONG).show();
                        }
                    });
                }
            });
        }
    }

    private void arePrerequisitesComplete(List<String> prerequisites, OnPrerequisitesCheckComplete callback) {
        if (prerequisites != null && !prerequisites.isEmpty()) {
            AtomicInteger count = new AtomicInteger(prerequisites.size());

            for (String taskId : prerequisites) {
                isTaskComplete(taskId, isComplete -> {
                    if (!isComplete) {
                        callback.onPrerequisitesCheckComplete(false);
                    } else {
                        if (count.decrementAndGet() == 0) {
                            callback.onPrerequisitesCheckComplete(true);
                        }
                    }
                });
            }
        } else {
            // No prerequisites, consider them complete
            callback.onPrerequisitesCheckComplete(true);
        }
    }

    private interface OnPrerequisitesCheckComplete {
        void onPrerequisitesCheckComplete(boolean arePrerequisitesComplete);
    }

    private void isTaskComplete(String taskId, OnTaskCompleteCallback callback) {
        getTaskProgress(taskId, new OnTaskProgressCallback() {
            @Override
            public void onTaskProgressComplete(String progress) {
                callback.onTaskComplete("Complete".equals(progress));
            }

            @Override
            public void onTaskProgressError() {
                // Handle error (you may consider treating it as incomplete)
                callback.onTaskComplete(false);
            }
        });
    }

    private interface OnTaskCompleteCallback {
        void onTaskComplete(boolean isComplete);
    }

    private void getTaskProgress(String taskId, OnTaskProgressCallback callback) {
        // Fetch the task progress from Firestore based on taskId
        FirebaseFirestore.getInstance().collection("Tasks")
                .document(taskId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        String progress = task.getResult().getString("progress");
                        callback.onTaskProgressComplete(progress);
                    } else {
                        // Handle errors or non-existent task
                        callback.onTaskProgressError();
                    }
                });
    }

    private interface OnTaskProgressCallback {
        void onTaskProgressComplete(String progress);

        void onTaskProgressError();
    }
    private void updateProjectProgressAuto(String projectId, String progress) {
        FirebaseFirestore.getInstance().collection("projectTasks")
                .whereEqualTo("projectId", projectId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        AtomicBoolean anyTaskInProgress = new AtomicBoolean(false);
                        AtomicBoolean allTasksComplete = new AtomicBoolean(true);

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String taskId = document.getString("taskId");

                            assert taskId != null;
                            FirebaseFirestore.getInstance().collection("Tasks")
                                    .document(taskId)
                                    .get()
                                    .addOnCompleteListener(taskDocument -> {
                                        if (taskDocument.isSuccessful()) {
                                            DocumentSnapshot taskSnapshot = taskDocument.getResult();

                                            if (taskSnapshot.exists() && taskSnapshot.contains("progress")) {
                                                String taskProgress = taskSnapshot.getString("progress");
                                                if (taskProgress != null && taskProgress.equalsIgnoreCase("In Progress")) {
                                                    anyTaskInProgress.set(true);
                                                }
                                                if (taskProgress == null || !taskProgress.equalsIgnoreCase("Complete")) {
                                                    allTasksComplete.set(false);
                                                }
                                            }
                                        }

                                        if (anyTaskInProgress.get()) {
                                            updateProjectProgressInFirestore(projectId, "In Progress");

                                            // Set actualEndDate to null if progress is In Progress
                                            if ("In Progress".equals(progress)) {
                                                updateActualEndDateInFirestoreToNull(projectId);
                                            }
                                        } else if (allTasksComplete.get()) {
                                            updateProjectProgressInFirestore(projectId, "Complete");

                                            // Record timestamp when project progress is set to Complete
                                            if ("Complete".equals(progress)) {
                                                updateActualEndDateInFirestore(projectId);
                                            } else {
                                                // Handle the case when progress is changed from Complete
                                                updateActualEndDateInFirestoreToNull(projectId);
                                            }
                                        } else {
                                            // If any task is "In Progress" or some tasks are "Complete," set the project progress to "Incomplete"
                                            updateProjectProgressInFirestore(projectId, "Incomplete");

                                            // Set actualEndDate to null if progress is In Progress or Incomplete
                                            if (!"Complete".equals(progress)) {
                                                updateActualEndDateInFirestoreToNull(projectId);
                                            }
                                        }
                                    });
                        }
                    } else {
                        Log.e("Firestore", "Error getting tasks: " + task.getException());
                    }
                });
    }

    private void updateActualEndDateInFirestore(String projectId) {
        // Record timestamp in the 'actualEndDate' field in the 'Projects' collection
        Date currentDate = new Date();
        FirebaseFirestore.getInstance().collection("Projects")
                .document(projectId)
                .update("actualEndDate", currentDate)
                .addOnSuccessListener(aVoid -> {
                    Log.d("Firestore", "Actual End Date updated successfully");

                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Failed to update Actual End Date: " + e.getMessage());
                });
    }

    private void updateActualEndDateInFirestoreToNull(String projectId) {
        // Set 'actualEndDate' field to null in the 'Projects' collection
        FirebaseFirestore.getInstance().collection("Projects")
                .document(projectId)
                .update("actualEndDate", null) // Set to null
                .addOnSuccessListener(aVoid -> {
                    Log.d("Firestore", "Actual End Date set to null successfully");

                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Failed to set Actual End Date to null: " + e.getMessage());
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

    private void showPrerequisitesDialog(List<String> prerequisites) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Prerequisites");

        StringBuilder prerequisitesText = new StringBuilder();

        if (prerequisites != null && !prerequisites.isEmpty()) {
            for (String prerequisiteId : prerequisites) {
                // Fetch the task details based on the prerequisite ID
                getTaskName(prerequisiteId, taskName -> {
                    if (taskName != null) {
                        prerequisitesText.append("- ").append(taskName).append("\n");

                        // If this is the last prerequisite, set the message and show the dialog
                        if (prerequisiteId.equals(prerequisites.get(prerequisites.size() - 1))) {
                            builder.setMessage(prerequisitesText.toString());
                            builder.setPositiveButton("OK", (dialog, which) -> dialog.dismiss());
                            AlertDialog dialog = builder.create();
                            dialog.show();
                        }
                    }
                    builder.setMessage("No prerequisites");
                    builder.setPositiveButton("OK", (dialog, which) -> dialog.dismiss());
                    AlertDialog dialog = builder.create();
                    dialog.show();
                });
            }
        } else {
            builder.setMessage("No prerequisites");
            builder.setPositiveButton("OK", (dialog, which) -> dialog.dismiss());
            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }

    private void getTaskName(String taskId, @NonNull TaskDetailsCallback callback) {
        FirebaseFirestore.getInstance().collection("Tasks")
                .document(taskId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        // Assuming "taskDetails" is the field storing task details
                        String taskName = task.getResult().getString("taskName");
                        callback.onCallback(taskName);
                    } else {
                        callback.onCallback(null);
                    }
                });
    }

    // Callback interface to handle asynchronous result
    public interface TaskDetailsCallback {
        void onCallback(String taskDetails);
    }

    public void saveNotes(String note, String taskId) {
        sharedPreferenceManager.saveNoteToSharedPreferencesForTask(note, taskId);

        CollectionReference dbTasks = fb.collection("Notes");

        Notes notes = new Notes(note);
        dbTasks.add(notes).addOnSuccessListener(documentReference -> {
            Toast.makeText(TaskActivity.this, "Notes saved", Toast.LENGTH_SHORT).show();
            notesId = documentReference.getId();
            addTaskNotes(taskId, notesId);
        }).addOnFailureListener(e -> Toast.makeText(TaskActivity.this, "Failed \n" + e, Toast.LENGTH_SHORT).show());
    }

    private void addTaskNotes(String taskId, String noteId) {
        // Creates a new projectTask document with an automatically generated ID
        Map<String, Object> taskNotes = new HashMap<>();

        taskNotes.put("taskId", taskId);
        taskNotes.put("noteId", noteId);
        taskNotes.put("timestamp", com.google.firebase.Timestamp.now());

        fb.collection("taskNotes").add(taskNotes).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.d("TaskNotes", "taskNotes added with ID: " + task.getResult().getId());
            } else {
                Log.e("TaskNote", "Error adding userProject", task.getException());
            }
        });
    }

    private void saveNoteToSharedPreferences(String note) {

        Intent intent = getIntent();
        if (intent.hasExtra("selectedTask")) {
            Tasks tasks = (Tasks) intent.getSerializableExtra("selectedTask");

            FirestoreManager firestoreManager = new FirestoreManager();
            firestoreManager.getDocumentId("Tasks", "taskDetails", tasks.getTaskDetails(), documentId -> {
                if (documentId != null) {
                    sharedPreferenceManager.saveNoteToSharedPreferencesForTask(note, documentId);
                }
            });
        }
    }
}
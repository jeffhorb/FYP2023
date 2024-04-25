package com.ecom.fyp2023;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.ecom.fyp2023.AppManagers.DateUtils;
import com.ecom.fyp2023.AppManagers.FirestoreManager;
import com.ecom.fyp2023.AppManagers.SharedPreferenceManager;
import com.ecom.fyp2023.Fragments.CommentListFragment;
import com.ecom.fyp2023.Fragments.NotesFragment;
import com.ecom.fyp2023.Fragments.UpdateTaskFragment;
import com.ecom.fyp2023.Fragments.UsersListFragment;
import com.ecom.fyp2023.ModelClasses.Notes;
import com.ecom.fyp2023.ModelClasses.Tasks;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class TaskActivity extends AppCompatActivity {

    TextView tasksDetails, endDate,endDateTitle, estTime, taskPro, commentFrag, userAssigned,taskName,startDate,completedTime,completedTimeTitle,difficulty
            ,storyPints;
    ImageView expandMore, progressExpand, prerequisiteList,help;

    EditText notes;
    String projectId;
    Button save, clear,view;

    String nId,notesContent;

    FirebaseFirestore fb;
    TextView assignedTitle;

    //TODO: PASS GROUPID
    private String groupId;

    SharedPreferenceManager sharedPreferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activty_task);

        taskPro = findViewById(R.id.taskPro);
        taskName = findViewById(R.id.taskName);
        tasksDetails = findViewById(R.id.taskDetails);
        endDate = findViewById(R.id.endDate);
        startDate = findViewById(R.id.startDate);
        estTime = findViewById(R.id.eTime);
        difficulty = findViewById(R.id.diff);
        commentFrag = findViewById(R.id.commentEditText);
        expandMore = findViewById(R.id.moreImageView);
        userAssigned = findViewById(R.id.userAssigned);
        progressExpand = findViewById(R.id.expandImageView);
        prerequisiteList = findViewById(R.id.prerequisiteList);
        notes = findViewById(R.id.notesEditText);
        save = findViewById(R.id.saveNote);
        clear = findViewById(R.id.clearNote);
        view = findViewById(R.id.viewNote);
        endDateTitle = findViewById(R.id.endDateTextView);
        completedTimeTitle = findViewById(R.id.cTimeTitle);
        completedTime = findViewById(R.id.cTime);
        storyPints =   findViewById(R.id.storyPoint);
        help = findViewById(R.id.help);
        assignedTitle = findViewById(R.id.assignTitle);


        fb = FirebaseFirestore.getInstance();

        taskName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (taskName.getMaxLines() == 1) {
                    taskName.setMaxLines(Integer.MAX_VALUE);
                } else {
                    taskName.setMaxLines(1);
                }
            }
        });
        tasksDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (tasksDetails.getMaxLines() == 3) {
                    taskName.setMaxLines(Integer.MAX_VALUE);
                } else {
                    tasksDetails.setMaxLines(3);
                }
            }
        });

        sharedPreferenceManager = new SharedPreferenceManager(this);

        String groupId = sharedPreferenceManager.getGroupId();

        if (groupId == null){

            commentFrag.setVisibility(View.GONE);
            assignedTitle.setVisibility(View.GONE);
            userAssigned.setVisibility(View.GONE);
        }

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

        help.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(TaskActivity.this);
                builder.setTitle("Story Point Guide");

                String message = "1-3 days:\n" +
                        "  - Low difficulty: 1\n" +
                        "  - Medium difficulty: 2\n" +
                        "  - High difficulty: 3\n\n" +
                        "4-7 days:\n" +
                        "  - Low difficulty: 4\n" +
                        "  - Medium difficulty: 5\n" +
                        "  - High difficulty: 6\n\n" +
                        "8-18 days:\n" +
                        "  - Low difficulty: 7\n" +
                        "  - Medium difficulty: 8\n" +
                        "  - High difficulty: 9\n\n" +
                        "19-28 days:\n" +
                        "  - Low difficulty: 10\n" +
                        "  - Medium difficulty: 11\n" +
                        "  - High difficulty: 12\n\n" +
                        "Above 28 days:\n" +
                        "  - Low difficulty: 13\n" +
                        "  - Medium difficulty: 14\n" +
                        "  - High difficulty: 15";

                builder.setMessage(message);
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                // Create and show the dialog
                AlertDialog dialog = builder.create();
                dialog.show();
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
                assert tasks != null;
                showPrerequisitesDialog(tasks.getPrerequisites());
            });

            assert tasks != null;
            taskName.setText(tasks.getTaskName());
            tasksDetails.setText(tasks.getTaskDetails());
            taskPro.setText(tasks.getProgress());
            difficulty.setText(tasks.getDifficulty());
            estTime.setText(tasks.getEstimatedTime()+"(s)");
            storyPints.setText(tasks.getStoryPoints() + " point(s)");

            Date sDate = tasks.getStartDate();
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
            String formattedStartDate = sdf.format(sDate);
            startDate.setText(formattedStartDate);

            //make tasks end date and it completion time visible if project is completed
            String comTime = tasks.getCompletedTime();
            Date eDate = tasks.getEndDate();
            if (eDate != null&&comTime != null) {
                SimpleDateFormat sdf1 = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
                String formattedEndDate = sdf1.format(eDate);
                endDate.setText(formattedEndDate);
                endDateTitle.setVisibility(View.VISIBLE);
                endDate.setVisibility(View.VISIBLE);
                completedTime.setText(comTime +"(s)");
                completedTimeTitle.setVisibility(View.VISIBLE);
                completedTime.setVisibility(View.VISIBLE);
            }

            FirestoreManager firestoreManager = new FirestoreManager();
            firestoreManager.getDocumentId("Tasks", "taskDetails", tasks.getTaskDetails(), documentId -> {
                if (documentId != null) {

                    loadUserAssigned(documentId);

                    Bundle bundle = new Bundle();
                    bundle.putString("pId", documentId);
                    UpdateTaskFragment fragment = new UpdateTaskFragment();
                    fragment.setArguments(bundle);

                    //update project progress textview in real time tasks progress is updated in TaskActivty.
                    fb.collection("Tasks")
                            .document(documentId)
                            .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                                @Override
                                public void onEvent(@Nullable DocumentSnapshot snapshot, @Nullable FirebaseFirestoreException e) {
                                    if (e != null) {
                                        Log.e("FirestoreListener", "Error getting project updates", e);
                                        return;
                                    }
                                    if (snapshot != null && snapshot.exists()) {
                                        // Update the progress TextView in real-time
                                        String updatedName = snapshot.getString("taskName");
                                        taskName.setText(updatedName);
                                        String updatedDetails = snapshot.getString("taskDetails");
                                        tasksDetails.setText(updatedDetails);
                                        String updatedEstimatedT = snapshot.getString("estimatedTime");
                                        estTime.setText(updatedEstimatedT);
                                        Long storyPointLong = snapshot.getLong("storyPoints");
                                        int updatedStoryP = storyPointLong != null ? storyPointLong.intValue() : 0;
                                        storyPints.setText(String.valueOf(updatedStoryP+ " point(s)"));
                                        String updatedDiff = snapshot.getString("difficulty");
                                        difficulty.setText(updatedDiff);
                                        String updatedProgress = snapshot.getString("progress");
                                        Date updatedStartDate = snapshot.getDate("startDate");
                                        SimpleDateFormat sdf2 = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
                                        assert updatedStartDate != null;
                                        String formattedStartDate = sdf2.format(updatedStartDate);
                                        startDate.setText(formattedStartDate);

                                        Date updatedEndDate = snapshot.getDate("endDate");
                                        String updatedCompletionTime = snapshot.getString("completedTime");
                                        if (updatedEndDate != null&&updatedCompletionTime != null) {
                                            SimpleDateFormat sdf1 = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
                                            String formattedEndDate = sdf1.format(updatedEndDate);
                                            endDate.setText(formattedEndDate);
                                            endDate.setVisibility(View.VISIBLE);
                                            endDateTitle.setVisibility(View.VISIBLE);
                                            completedTime.setText(updatedCompletionTime+"(s)");
                                            completedTime.setVisibility(View.VISIBLE);
                                            completedTimeTitle.setVisibility(View.VISIBLE);
                                        }else{
                                            endDate.setVisibility(View.INVISIBLE);
                                            endDate.setVisibility(View.INVISIBLE);
                                            completedTimeTitle.setVisibility(View.GONE);
                                            completedTime.setVisibility(View.GONE);
                                        }
                                        if (updatedProgress != null) {
                                            taskPro.setText(updatedProgress);
                                        }
                                    }
                                }
                            });
                }
                // Set an OnClickListener for the save notes button
                save.setOnClickListener(v -> {

                    String note = notes.getText().toString();

                    if (TextUtils.isEmpty(note)){
                        notes.setError("Add notes");
                    }else if (nId != null && !nId.isEmpty()) {
                        updateNote(nId,note);
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

        Menu menu = popupMenu.getMenu();

        // Check if groupId is null and set the visibility of teamAnalysis accordingly
        String groupId = sharedPreferenceManager.getGroupId();
        MenuItem assignTask = menu.findItem(R.id.assignTask);
        MenuItem unassignTask = menu.findItem(R.id.UnAssignTask);
        assignTask.setVisible(groupId != null);
        unassignTask.setVisible(groupId!= null);

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
                                Bundle bundle = new Bundle();
                                bundle.putSerializable("selectT", tasks);
                                bundle.putString("pro_key", projectId);
                                updateTaskFragment.setArguments(bundle);
                                FragmentManager fragmentManager = TaskActivity.this.getSupportFragmentManager();
                                FragmentTransaction transaction = fragmentManager.beginTransaction();
                                transaction.replace(R.id.updateTaskfra, updateTaskFragment);
                                transaction.addToBackStack(null);
                                transaction.commit();
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
                    endDateTitle.setVisibility(View.GONE);
                    endDate.setVisibility(View.GONE);
                    completedTime.setVisibility(View.GONE);
                    completedTimeTitle.setVisibility(View.GONE);
                } else if (menuItem.getItemId() == R.id.taskInprogress) {
                    updateTaskProgress("In Progress");
                    endDateTitle.setVisibility(View.GONE);
                    endDate.setVisibility(View.GONE);
                    completedTime.setVisibility(View.GONE);
                    completedTimeTitle.setVisibility(View.GONE);
                } else if (menuItem.getItemId() == R.id.taskComplete) {
                    //updateTaskProgress("Complete");
                    updateTaskProgressToComplete("Complete");

                    // Update the progress TextView in real-time
                    Intent intent = getIntent();
                    if (intent.hasExtra("selectedTask")) {
                        Tasks tasks = (Tasks) intent.getSerializableExtra("selectedTask");

                        FirestoreManager firestoreManager = new FirestoreManager();
                        assert tasks != null;
                        firestoreManager.getDocumentId("Tasks", "taskDetails", tasks.getTaskDetails(), documentId -> {
                            if (documentId != null) {

                                fb.collection("Tasks").document(documentId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                                    @Override
                                    public void onEvent(@Nullable DocumentSnapshot snapshot, @Nullable FirebaseFirestoreException e) {
                                        if (e != null) {
                                            Log.e("FirestoreListener", "Error getting project updates", e);
                                            return;
                                        }
                                        if (snapshot != null && snapshot.exists()) {
                                            String cTime = snapshot.getString("completedTime");

                                            Date eDate = snapshot.getDate("endDate");
                                            if (eDate != null&&cTime !=null) {
                                                SimpleDateFormat sdf1 = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
                                                String formattedEndDate = sdf1.format(eDate);
                                                endDate.setText(formattedEndDate);
                                                endDateTitle.setVisibility(View.VISIBLE);
                                                endDate.setVisibility(View.VISIBLE);
                                                completedTime.setText(cTime+"(s)");
                                                completedTime.setVisibility(View.VISIBLE);
                                                completedTimeTitle.setVisibility(View.VISIBLE);
                                            }
                                        }
                                    }
                                });
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

    //update task progress to In progress or Incomplete and set the completedTime to null
    private void updateTaskProgress(String progress) {
        Intent intent = getIntent();
        if (intent.hasExtra("selectedTask")) {
            Tasks tasks = (Tasks) intent.getSerializableExtra("selectedTask");

            FirestoreManager firestoreManager = new FirestoreManager();
            assert tasks != null;
            firestoreManager.getDocumentId("Tasks", "taskDetails", tasks.getTaskDetails(), documentId -> {
                if (documentId != null) {
                    // Create a Map to store the fields to be updated
                    Map<String, Object> updateFields = new HashMap<>();
                    updateFields.put("progress", progress);

                    // Check if progress is "Complete" to handle completedTime
                    if ("Complete".equals(progress)) {
                        updateTaskEndDateInFirestoreToNull(documentId);
                        updateFields.put("completedTime", null);

                        // Automatically update project progress based on task progress
                        updateProjectProgressAuto(projectId, "Complete");
                    } else {
                        // Handle the case when progress is changed from Complete, set the completedTime to null
                        updateTaskEndDateInFirestoreToNull(documentId);
                        updateFields.put("completedTime", null);
                        updateProjectProgressAuto(projectId, progress);
                    }

                    FirebaseFirestore.getInstance().collection("Tasks")
                            .document(documentId)
                            .update(updateFields)
                            .addOnSuccessListener(aVoid -> {
                                // Update successful
                                Toast.makeText(TaskActivity.this, "Progress updated to " + progress, Toast.LENGTH_SHORT).show();
                                taskPro.setText(progress);
                                tasks.setProgress(progress);
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
                                    .update("progress", progress, "endDate", new Date())
                                    .addOnSuccessListener(aVoid -> {
                                        // Update successful
                                        Toast.makeText(TaskActivity.this, "Progress updated to " + progress, Toast.LENGTH_SHORT).show();
                                        taskPro.setText(progress);
                                        tasks.setProgress(progress);

                                        // Update completedTime field
                                        if ("Complete".equals(progress)) {
                                            updateCompletedTime(documentId, new Date(), tasks.getStartDate());
                                        }
                                        // Automatically update project progress based on task progress
                                        updateProjectProgressAuto(projectId, "Complete");
                                    })
                                    .addOnFailureListener(e -> {
                                        // Error updating progress and endDate
                                        Log.e("updateTaskProgress", "Error updating progress and endDate", e);
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

    //update completedtime when task is completed
    private void updateCompletedTime(String documentId, Date endDate, Date startDate) {
        String completedTime = DateUtils.calculateDateDifference(endDate, startDate);
        FirebaseFirestore.getInstance().collection("Tasks")
                .document(documentId)
                .update("completedTime", completedTime)
                .addOnSuccessListener(aVoid -> {
                    Log.d("UpdateCompletedTime", "Update successful");
                })
                .addOnFailureListener(e -> {
                    Log.e("UpdateCompletedTime", "Error updating completedTime", e);
                });
    }

    //check if all prerequisites are completed
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

    //update project progress automatically when it tasks progress is updated
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

    private void updateTaskEndDateInFirestoreToNull(String taskId) {
        // Set 'actualEndDate' field to null in the 'Projects' collection
        FirebaseFirestore.getInstance().collection("Tasks")
                .document(taskId)
                .update("endDate", null) // Set to null
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
            int prerequisitesCount = prerequisites.size();
            AtomicInteger prerequisitesProcessed = new AtomicInteger();

            for (String prerequisiteId : prerequisites) {
                // Fetch the task details based on the prerequisite ID
                getTaskName(prerequisiteId, taskName -> {
                    if (taskName != null && !taskName.isEmpty()) {
                        prerequisitesText.append("- ").append(taskName).append("\n");
                    }

                    // Increment the counter for processed prerequisites
                    prerequisitesProcessed.getAndIncrement();

                    // If all prerequisites have been processed, set the message and show the dialog
                    if (prerequisitesProcessed.get() == prerequisitesCount) {
                        if (prerequisitesText.length() > 0) {
                            builder.setMessage(prerequisitesText.toString());
                        } else {
                            builder.setMessage("No prerequisites");
                        }

                        builder.setPositiveButton("OK", (dialog, which) -> dialog.dismiss());
                        AlertDialog dialog = builder.create();
                        dialog.show();
                    }
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

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String userAuthId = currentUser.getUid();
        Notes notes = new Notes(note, groupId,userAuthId);
        dbTasks.add(notes).addOnSuccessListener(documentReference -> {
            Toast.makeText(TaskActivity.this, "Notes saved", Toast.LENGTH_SHORT).show();
            String notesId = documentReference.getId();
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
            assert tasks != null;
            firestoreManager.getDocumentId("Tasks", "taskDetails", tasks.getTaskDetails(), documentId -> {
                if (documentId != null) {
                    sharedPreferenceManager.saveNoteToSharedPreferencesForTask(note, documentId);
                }
            });
        }
    }

    // Move the updateNote method outside the save method
    private void updateNote(String noteId, String note) {
        // Create the updated note
        Map<String, Object> noteData = new HashMap<>();
        noteData.put("note", note);

        fb.collection("Notes")
                .document(noteId)
                .update(noteData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(TaskActivity.this, "Note has been updated..", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Log.e("UpdateNote", "Error updating note", e);
                });
    }

    //intent from notesRV
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        // Now getIntent() should always return the most recent
        if (intent.hasExtra("noteID") && intent.hasExtra("notes")) {
            nId = intent.getStringExtra("noteID");
            notesContent = intent.getStringExtra("notes");
            notes.setText(notesContent);
        }
    }
}


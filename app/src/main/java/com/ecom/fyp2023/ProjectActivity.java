package com.ecom.fyp2023;

import static com.ecom.fyp2023.Fragments.BottomSheetDialogAddProject.MESSAGE_KEY;

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
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ecom.fyp2023.Adapters.TasksRVAdapter;
import com.ecom.fyp2023.AppManagers.FirestoreManager;
import com.ecom.fyp2023.Fragments.BottomSheetFragmentAddTask;
import com.ecom.fyp2023.Fragments.CommentListFragment;
import com.ecom.fyp2023.Fragments.UpdateTaskFragment;
import com.ecom.fyp2023.ModelClasses.Projects;
import com.ecom.fyp2023.ModelClasses.Tasks;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

public class ProjectActivity extends AppCompatActivity implements
        TasksRVAdapter.OnTaskProgressUpdateListener,
        TasksRVAdapter.OnEndDateUpdateListener,
        BottomSheetFragmentAddTask.OnEndDateUpdateListener {


    private RecyclerView recyclerView;
    private ArrayList<Tasks> tasksArrayList;
    private TasksRVAdapter tasksRVAdapter;
    private FirebaseFirestore db;

    TextView commentFrag;
    private TextView proDes, progress, proEndDate;
    ImageView progressExpand;

    String projId;

    public static final String projectId_key = "proId";
    public static final String p_key = "p_key";

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
        super.onPointerCaptureChanged(hasCapture);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project);

        TextView proTitle = findViewById(R.id.titleTextview);
        proDes = findViewById(R.id.descriptionTextView);
        TextView proStartDate = findViewById(R.id.startDateTextView);
        proEndDate = findViewById(R.id.endDateTextView);
        TextView proPriority = findViewById(R.id.priorityTextView);
        ImageView addTask = findViewById(R.id.addTasksImageView);
        progressExpand = findViewById(R.id.expandImageView);
        progress = findViewById(R.id.progressTextview);
        recyclerView = findViewById(R.id.tasksRecyclerView);
        commentFrag = findViewById(R.id.commentEditText);

        //expand Max line
        proDes.setMaxLines(3);

        // Set an onClickListener for the TextView
        proDes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (proDes.getMaxLines() == 3) {
                    proDes.setMaxLines(Integer.MAX_VALUE);
                } else {
                    proDes.setMaxLines(3);
                }
            }
        });

        db = FirebaseFirestore.getInstance();


        tasksArrayList = new ArrayList<>();

        LinearLayoutManager layoutManager = new LinearLayoutManager(ProjectActivity.this, RecyclerView.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);

        tasksRVAdapter = new TasksRVAdapter(tasksArrayList, ProjectActivity.this,this,this);
        recyclerView.setAdapter(tasksRVAdapter);



        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        //onclick for project in recyclerview
        Intent intent = getIntent();
        if (intent.hasExtra("selectedProject")) {
            Projects projects = (Projects) intent.getSerializableExtra("selectedProject");

            assert projects != null;
            proTitle.setText(projects.getTitle());
            proDes.setText(projects.getDescription());
            progress.setText(projects.getProgress());
            proStartDate.setText(projects.getStartDate());
            proEndDate.setText(projects.getEndDate());
            proPriority.setText(projects.getPriority());


            FirestoreManager firestoreManager = new FirestoreManager();
            firestoreManager.getDocumentId("Projects", "title", projects.getTitle(), documentId -> {
                if (documentId != null) {

                    tasksRVAdapter.setSelectedProject(documentId);
                    fetchAndDisplayTasks(documentId);

                    projId = documentId;

                    CommentListFragment bottomSheetFragment = CommentListFragment.newInstance(documentId);
                    bottomSheetFragment.show(getSupportFragmentManager(), bottomSheetFragment.getTag());

                    Bundle bundle = new Bundle();
                    bundle.putString("pId", documentId);
                    UpdateTaskFragment fragment = new UpdateTaskFragment();
                    fragment.setArguments(bundle);


                    db.collection("projectTasks")
                            .whereEqualTo("projectId", documentId)  // Replace with the ID of the current project
                            .addSnapshotListener(new EventListener<QuerySnapshot>() {
                                @Override
                                public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                                    if (error != null) {
                                        Log.e("FirestoreListener", "Error getting tasks updates", error);
                                        return;
                                    }

                                    if (value != null && !value.isEmpty()) {
                                        // Convert QuerySnapshot to a list of Tasks and update the adapter
                                        List<Tasks> updatedTasks = new ArrayList<>();
                                        for (QueryDocumentSnapshot document : value) {
                                            String taskId = document.getString("taskId");
                                            retrieveTaskDetails(taskId, updatedTasks);
                                        }

                                        // Update the RecyclerView adapter with the new task list
                                        tasksRVAdapter.updateList(updatedTasks);
                                    }
                                }
                            });

                }
            });
        }

        //receives intent for onclick add task button
        Intent i = getIntent();
        if (i.hasExtra(MESSAGE_KEY)) {
            String receivedProId = i.getStringExtra(MESSAGE_KEY);

            projId = receivedProId;
            tasksRVAdapter.setSelectedProject(receivedProId);

            BottomSheetFragmentAddTask taskFragment = BottomSheetFragmentAddTask.newInstance();
            taskFragment.setOnEndDateUpdateListener(ProjectActivity.this);  // Set the listener
            Bundle bundle = new Bundle();
            bundle.putString(projectId_key, receivedProId);
            taskFragment.setArguments(bundle);
            taskFragment.show(getSupportFragmentManager(), taskFragment.getTag());

            fetchAndDisplayTasks(receivedProId);

            String receivedProTitle = getIntent().getStringExtra("proTitle");
            proTitle.setText(receivedProTitle);
            String receivedProDesc = getIntent().getStringExtra("proDesc");
            proDes.setText(receivedProDesc);
            String receivedProgress = getIntent().getStringExtra("progres");
            progress.setText(receivedProgress);
            String receivedStartDT = getIntent().getStringExtra("startDt");
            proStartDate.setText(receivedStartDT);
            String receivedEndDt = getIntent().getStringExtra("endDt");
            proEndDate.setText(receivedEndDt);
            String receivedPriority = getIntent().getStringExtra("priority");
            proPriority.setText(receivedPriority);
        }

        addTask.setOnClickListener(v -> {

            Intent i1 = getIntent();
            if (i1.hasExtra(MESSAGE_KEY)) {
                String receivedProId = i1.getStringExtra(MESSAGE_KEY);

                BottomSheetFragmentAddTask taskFragment = BottomSheetFragmentAddTask.newInstance();
                taskFragment.setOnEndDateUpdateListener(ProjectActivity.this);  // Set the listener
                Bundle bundle = new Bundle();
                bundle.putString(projectId_key, receivedProId);
                taskFragment.setArguments(bundle);
                taskFragment.show(getSupportFragmentManager(), taskFragment.getTag());

            }

            Intent intent1 = getIntent();
            if (intent1.hasExtra("selectedProject")) {
                Projects projects = (Projects) intent1.getSerializableExtra("selectedProject");

                FirestoreManager firestoreManager = new FirestoreManager();
                assert projects != null;
                firestoreManager.getDocumentId("Projects", "title", projects.getTitle(), documentId -> {
                    if (documentId != null) {

                        fetchAndDisplayTasks(documentId);

                        BottomSheetFragmentAddTask taskFragment = BottomSheetFragmentAddTask.newInstance();
                        taskFragment.setOnEndDateUpdateListener(ProjectActivity.this);  // Set the listener
                        Bundle bundle = new Bundle();
                        bundle.putString(p_key, documentId);
                        taskFragment.setArguments(bundle);
                        taskFragment.show(getSupportFragmentManager(), taskFragment.getTag());

                    }
                });
            }
        });

        commentFrag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent i = getIntent();
                if (i.hasExtra(MESSAGE_KEY)) {
                    String receivedProId = i.getStringExtra(MESSAGE_KEY);

                    CommentListFragment bottomSheetFragment = CommentListFragment.newInstance(receivedProId);
                    bottomSheetFragment.show(getSupportFragmentManager(), bottomSheetFragment.getTag());

                }

                Intent intent = getIntent();
                if (intent.hasExtra("selectedProject")) {
                    Projects projects = (Projects) intent.getSerializableExtra("selectedProject");

                    FirestoreManager firestoreManager = new FirestoreManager();
                    assert projects != null;
                    firestoreManager.getDocumentId("Projects", "title", projects.getTitle(), documentId -> {
                        if (documentId != null) {

                            fetchAndDisplayTasks(documentId);

                            CommentListFragment bottomSheetFragment = CommentListFragment.newInstance(documentId);
                            bottomSheetFragment.show(getSupportFragmentManager(), bottomSheetFragment.getTag());

                        }  // Handle the case where the document ID couldn't be retrieved

                    });
                }
            }
        });

        progressExpand.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPopupMenu(view);
            }
        });
    }

    @Override
    public void onTaskProgressUpdated() {
        // Trigger updateProjectProgress here
        // Call your updateProjectProgress method here
        //updateProjectProgress(projectId, progress);
        Intent i = getIntent();
        if (i.hasExtra(MESSAGE_KEY)) {

            String receivedProId = i.getStringExtra(MESSAGE_KEY);
            updateProjectProgressAuto(receivedProId, "Incomplete");
            Toast.makeText(ProjectActivity.this, "Project progress updated", Toast.LENGTH_SHORT).show();

            assert receivedProId != null;
            FirebaseFirestore.getInstance().collection("Projects")
                    .document(receivedProId)
                    .addSnapshotListener((documentSnapshot, error) -> {
                        if (error != null) {
                            Log.e("Firestore", "Error listening for document changes: " + error.getMessage());
                            return;
                        }

                        if (documentSnapshot != null && documentSnapshot.exists()) {
                            // Update the 'progress' TextView with the new value
                            String newProgress = documentSnapshot.getString("progress");
                            if (newProgress != null) {
                                progress.setText(newProgress);
                            }
                        }
                    });
        }

        Intent intent = getIntent();
        if (intent.hasExtra("selectedProject")) {
            Projects projects = (Projects) intent.getSerializableExtra("selectedProject");
            Toast.makeText(ProjectActivity.this, "Project progress updated", Toast.LENGTH_SHORT).show();

            FirestoreManager firestoreManager = new FirestoreManager();
            assert projects != null;
            firestoreManager.getDocumentId("Projects", "title", projects.getTitle(), documentId -> {
                if (documentId != null) {

                    updateProjectProgressAuto(documentId, "Incomplete");

                    FirebaseFirestore.getInstance().collection("Projects")
                            .document(documentId)
                            .addSnapshotListener((documentSnapshot, error) -> {
                                if (error != null) {
                                    Log.e("Firestore", "Error listening for document changes: " + error.getMessage());
                                    return;
                                }

                                if (documentSnapshot != null && documentSnapshot.exists()) {
                                    // Update the 'progress' TextView with the new value
                                    String newProgress = documentSnapshot.getString("progress");
                                    if (newProgress != null) {
                                        progress.setText(newProgress);
                                    }
                                }
                            });
                }
            });
        }
    }

    private void fetchAndDisplayTasks(String projectId) {
        CollectionReference projectTasksCollection = db.collection("projectTasks");
        Query query = projectTasksCollection
                .whereEqualTo("projectId", projectId)
                .orderBy("timestamp", Query.Direction.ASCENDING);

        query.addSnapshotListener((value, error) -> {
            if (error != null) {
                Toast.makeText(ProjectActivity.this, "Error getting tasks: " + error.getMessage(), Toast.LENGTH_SHORT).show();
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

    private void retrieveTaskDetails(String taskId, List<Tasks> tasksList) {
        CollectionReference tasksCollection = db.collection("Tasks");
        tasksCollection.document(taskId).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            Tasks taskData = document.toObject(Tasks.class);
                            tasksList.add(taskData);

                            tasksRVAdapter.updateList(tasksList);
                            taskData.setTaskId(document.getId());

                        } else {
                            Log.e("Firestore", "Error fetching task details: " + task.getException());
                            //Toast.makeText(ProjectActivity.this, "Task not found", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(ProjectActivity.this, "Error fetching task details", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showPopupMenu(View view) {
        PopupMenu popupMenu = new PopupMenu(this, view);
        popupMenu.inflate(R.menu.progress_option);
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {

                    if (menuItem.getItemId() == R.id.projectInprogress) {

                    Intent i = getIntent();
                    if (i.hasExtra(MESSAGE_KEY)) {
                        String receivedProId = i.getStringExtra(MESSAGE_KEY);

                        updateProjectProgress(receivedProId, "In Progress");


                        FirebaseFirestore.getInstance().collection("Projects")
                                .document(receivedProId)
                                .addSnapshotListener((documentSnapshot, error) -> {
                                    if (error != null) {
                                        Log.e("Firestore", "Error listening for document changes: " + error.getMessage());
                                        return;
                                    }

                                    if (documentSnapshot != null && documentSnapshot.exists()) {
                                        // Update the 'progress' TextView with the new value
                                        String newProgress = documentSnapshot.getString("progress");
                                        if (newProgress != null) {
                                            progress.setText(newProgress);
                                        }
                                    }
                                });
                    }

                    Intent intent = getIntent();
                    if (intent.hasExtra("selectedProject")) {
                        Projects projects = (Projects) intent.getSerializableExtra("selectedProject");

                        FirestoreManager firestoreManager = new FirestoreManager();
                        assert projects != null;
                        firestoreManager.getDocumentId("Projects", "title", projects.getTitle(), documentId -> {
                            if (documentId != null) {

                                updateProjectProgress(documentId, "In Progress");


                                FirebaseFirestore.getInstance().collection("Projects")
                                        .document(documentId)
                                        .addSnapshotListener((documentSnapshot, error) -> {
                                            if (error != null) {
                                                Log.e("Firestore", "Error listening for document changes: " + error.getMessage());
                                                return;
                                            }

                                            if (documentSnapshot != null && documentSnapshot.exists()) {
                                                // Update the 'progress' TextView with the new value
                                                String newProgress = documentSnapshot.getString("progress");
                                                if (newProgress != null) {
                                                    progress.setText(newProgress);
                                                }
                                            }
                                        });
                            }  // Handle the case where the document ID couldn't be retrieved
                        });
                    }

                }
                return true;
            }
        });
        // Show the popup menu
        popupMenu.show();
    }



    @NonNull
    private String formatDateFromTimestamp(Timestamp timestamp) {
        if (timestamp != null) {
            Date date = timestamp.toDate();
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
            return sdf.format(date);
        } else {
            return ""; // Handle the case where Timestamp is null
        }
    }

        private void updateProjectProgress(String projectId, String progress) {
            // Query the projectTasks collection to check the progress of associated tasks
            FirebaseFirestore.getInstance().collection("projectTasks")
                    .whereEqualTo("projectId", projectId)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            AtomicBoolean allTasksComplete = new AtomicBoolean(true);

                            // List to store task IDs
                            List<String> taskIds = new ArrayList<>();
                            if (allTasksComplete.get()) {
                                // If any task is incomplete, notify the user or add additional logic
                                Toast.makeText(ProjectActivity.this, "All tasks is complete for this project", Toast.LENGTH_SHORT).show();}

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

                                                    // Check if taskProgress is null or not equal to "Complete"
                                                    if (taskProgress == null || !taskProgress.equalsIgnoreCase("Complete")) {
                                                        allTasksComplete.set(false);
                                                    }
                                                }
                                            }

                                            // Check the flag after each task document fetch
                                            //if (allTasksComplete.get()) {
                                                // If any task is incomplete, notify the user or add additional logic
                                              //  Toast.makeText(ProjectActivity.this, "All tasks is complete for this project", Toast.LENGTH_SHORT).show();
                                            //} else
                                                if (!allTasksComplete.get()) {
                                                // If all tasks are complete, update the project progress
                                                // Update the 'progress' field in the 'Projects' collection
                                                FirebaseFirestore.getInstance().collection("Projects")
                                                        .document(projectId)
                                                        .update("progress", progress)
                                                        .addOnSuccessListener(aVoid -> {
                                                            Log.d("Firestore", "Progress updated successfully");
                                                        })
                                                        .addOnFailureListener(e -> {
                                                            Log.e("Firestore", "Failed to update progress: " + e.getMessage());
                                                            // Handle failure, if necessary
                                                        });
                                            }
                                        });
                            }

                        } else {
                            Log.e("Firestore", "Error getting tasks: " + task.getException());
                        }
                    });
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
                    // You can add any additional actions you want to perform on success
                    //Toast.makeText(ProjectActivity.this, "Project progress updated", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Failed to update progress: " + e.getMessage());
                    // Handle failure, if necessary
                });
    }
    @Override
    public void onEndDateUpdated(String updatedEndDate) {
        // Update your TextView with the new end date
        proEndDate.setText(updatedEndDate);
    }




}
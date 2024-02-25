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

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ecom.fyp2023.Adapters.TasksRVAdapter;
import com.ecom.fyp2023.Analysis.TasksProgressAnalysis;
import com.ecom.fyp2023.AppManagers.FirestoreManager;
import com.ecom.fyp2023.Fragments.BottomSheetFragmentAddTask;
import com.ecom.fyp2023.Fragments.CommentListFragment;
import com.ecom.fyp2023.Fragments.UpdateProject;
import com.ecom.fyp2023.Fragments.UpdateTaskFragment;
import com.ecom.fyp2023.ModelClasses.Projects;
import com.ecom.fyp2023.ModelClasses.Tasks;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ProjectActivity extends AppCompatActivity implements
        TasksRVAdapter.OnEndDateUpdateListener,
        BottomSheetFragmentAddTask.OnEndDateUpdateListener, UpdateTaskFragment.OnTaskUpdateListener,UpdateTaskFragment.OnEndDateUpdateListener {

    private RecyclerView recyclerView;
    private ArrayList<Tasks> tasksArrayList;
    private TasksRVAdapter tasksRVAdapter;
    private FirebaseFirestore db;
    String projectId;

    TextView commentFrag;
    private TextView proDes, progress, proEndDate;

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
        progress = findViewById(R.id.progressTextview);
        recyclerView = findViewById(R.id.tasksRecyclerView);
        commentFrag = findViewById(R.id.commentEditText);
        ImageView expandMore = findViewById(R.id.moreImageView);

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

        tasksRVAdapter = new TasksRVAdapter(tasksArrayList, ProjectActivity.this,this);
        recyclerView.setAdapter(tasksRVAdapter);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        expandMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPopupMenu(view);
            }
        });

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

                    projectId = documentId;

                    //update project progress textview in real time tasks progress is updated in TaskActivty.
                    db.collection("Projects")
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
                                        String updatedProgress = snapshot.getString("progress");
                                        if (updatedProgress != null) {
                                            progress.setText(updatedProgress);
                                        }
                                    }
                                }
                            });

                    tasksRVAdapter.setSelectedProject(documentId);
                    fetchAndDisplayTasks(documentId);

                    CommentListFragment bottomSheetFragment = CommentListFragment.newInstance(documentId);
                    bottomSheetFragment.show(getSupportFragmentManager(), bottomSheetFragment.getTag());

                    UpdateTaskFragment fragment = UpdateTaskFragment.newInstance();

                    Bundle bundle = new Bundle();
                    bundle.putString("pId", documentId);
                    fragment.setOnEndDateUpdateListener(ProjectActivity.this);
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
            projectId = receivedProId;

            //update project progress textview in real time tasks progress is updated in TaskActivty.
            db.collection("Projects")
                    .document(receivedProId)
                    .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                        @Override
                        public void onEvent(@Nullable DocumentSnapshot snapshot, @Nullable FirebaseFirestoreException e) {
                            if (e != null) {
                                Log.e("FirestoreListener", "Error getting project updates", e);
                                return;
                            }

                            if (snapshot != null && snapshot.exists()) {
                                // Update the progress TextView in real-time
                                String updatedProgress = snapshot.getString("progress");
                                if (updatedProgress != null) {
                                    progress.setText(updatedProgress);
                                }
                            }
                        }
                    });

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
    }

    private void showPopupMenu(View view) {
        PopupMenu popupMenu = new PopupMenu(this, view);
        popupMenu.inflate(R.menu.project_option2);
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {

                if (menuItem.getItemId() == R.id.tasksProgressAnalysis) {
                    Intent intent = new Intent(ProjectActivity.this, TasksProgressAnalysis.class);
                    startActivity(intent);
                    finish();

                }//update project in the projectActivity activity
                else if (menuItem.getItemId() == R.id.UpdateProject) {

                    /*Intent i1 = getIntent();
                    if (i1.hasExtra("proJT")) {
                        Projects project = (Projects) i1.getSerializableExtra("proJT");

                        FirestoreManager firestoreManager = new FirestoreManager();

                        firestoreManager.getDocumentId("Projects", "title", project.getTitle(), documentId -> {
                            if (documentId != null) {
                                project.setProjectId(documentId);


                                // Create an instance of your UpdateFragment.
                                UpdateProject updateFragment = new UpdateProject();

                                // Pass data to the fragment using Bundle.
                                Bundle bundle = new Bundle();
                                bundle.putSerializable("proT", project);
                                updateFragment.setArguments(bundle);

                                // Replace the existing fragment with the new fragment.
                                FragmentManager fragmentManager = ProjectActivity.this.getSupportFragmentManager();
                                FragmentTransaction transaction = fragmentManager.beginTransaction();
                                transaction.replace(R.id.updatePfra, updateFragment);
                                transaction.addToBackStack(null);
                                transaction.commit();
                            }
                            });*/

                    //}


                    Intent intent1 = getIntent();
                    if (intent1.hasExtra("selectedProject")) {
                        Projects projects = (Projects) intent1.getSerializableExtra("selectedProject");

                    FirestoreManager firestoreManager = new FirestoreManager();
                    assert projects != null;
                    firestoreManager.getDocumentId("Projects", "title", projects.getTitle(), documentId -> {
                        if (documentId != null) {
                            projects.setProjectId(documentId);


                            // Create an instance of your UpdateFragment.
                            UpdateProject updateFragment = new UpdateProject();

                            // Pass data to the fragment using Bundle.
                            Bundle bundle = new Bundle();
                            bundle.putSerializable("proJ",  projects);
                            updateFragment.setArguments(bundle);

                            // Replace the existing fragment with the new fragment.
                            FragmentManager fragmentManager =ProjectActivity.this.getSupportFragmentManager();
                            FragmentTransaction transaction = fragmentManager.beginTransaction();
                            transaction.replace(R.id.updatePfra, updateFragment);
                            transaction.addToBackStack(null);
                            transaction.commit();


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
    @Override
    public void onTaskUpdated() {
        // Implement the logic to refresh the task data in the RecyclerView
        // Fetch and display tasks again to update the RecyclerView
        fetchAndDisplayTasks(projectId);
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

                            taskData.setTaskId(document.getId());
                            tasksList.add(taskData);

                            tasksRVAdapter.updateList(tasksList);

                        } else {
                            Log.e("Firestore", "Error fetching task details: " + task.getException());
                            //Toast.makeText(ProjectActivity.this, "Task not found", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(ProjectActivity.this, "Error fetching task details", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onEndDateUpdated(String updatedEndDate) {
        // Update your TextView with the new end date
        proEndDate.setText(updatedEndDate);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Fetch and display tasks again to update the RecyclerView
        if (projectId != null) {
            fetchAndDisplayTasks(projectId);
        }
    }
}
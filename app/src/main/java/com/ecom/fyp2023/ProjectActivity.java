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
import com.ecom.fyp2023.Fragments.UsersListFragment;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class ProjectActivity extends AppCompatActivity implements UpdateTaskFragment.OnTaskUpdateListener {

    private TasksRVAdapter tasksRVAdapter;
    private FirebaseFirestore db;
    String projectId;

    TextView commentFrag,proTitle;
    private TextView proDes, progress, proEndDate,projectCompletionDate,completionTitle;

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

        proTitle = findViewById(R.id.titleTextview);
        proDes = findViewById(R.id.descriptionTextView);
        TextView proStartDate = findViewById(R.id.startDateTextView);
        proEndDate = findViewById(R.id.endDateTextView);
        projectCompletionDate = findViewById(R.id.completionDateTextView);
        completionTitle = findViewById(R.id.completionDateTitle);
        TextView proPriority = findViewById(R.id.priorityTextView);
        ImageView addTask = findViewById(R.id.addTasksImageView);
        progress = findViewById(R.id.progressTextview);
        RecyclerView recyclerView = findViewById(R.id.tasksRecyclerView);
        commentFrag = findViewById(R.id.commentEditText);
        ImageView expandMore = findViewById(R.id.moreImageView);

        //proDes.setMaxLines(3);

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
        proTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (proTitle.getMaxLines() == 1) {
                    proDes.setMaxLines(Integer.MAX_VALUE);
                } else {
                    proDes.setMaxLines(1);
                }
            }
        });

        db = FirebaseFirestore.getInstance();

        ArrayList<Tasks> tasksArrayList = new ArrayList<>();

        LinearLayoutManager layoutManager = new LinearLayoutManager(ProjectActivity.this, RecyclerView.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);

        tasksRVAdapter = new TasksRVAdapter(tasksArrayList, ProjectActivity.this);
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
                                        String updatedTitle = snapshot.getString("title");
                                        proTitle.setText(updatedTitle);
                                        String updatedDescription = snapshot.getString("description");
                                        proDes.setText(updatedDescription);
                                        String updatedStartDt = snapshot.getString("startDate");
                                        proStartDate.setText(updatedStartDt);
                                        String updatedEndDate = snapshot.getString("endDate");
                                        proEndDate.setText(updatedEndDate);
                                        String updatedPriority = snapshot.getString("priority");
                                        proPriority.setText(updatedPriority);
                                        String updatedProgress = snapshot.getString("progress");
                                        Date completionDate = snapshot.getDate("actualEndDate");
                                        if (completionDate != null) {
                                            SimpleDateFormat sdf1 = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
                                            String formattedEndDate = sdf1.format(completionDate);
                                            projectCompletionDate.setText(formattedEndDate);
                                            completionTitle.setVisibility(View.VISIBLE);
                                            projectCompletionDate.setVisibility(View.VISIBLE);
                                        }else{
                                            completionTitle.setVisibility(View.GONE);
                                            projectCompletionDate.setVisibility(View.GONE);
                                        }
                                        if (updatedProgress != null) {
                                            progress.setText(updatedProgress);
                                        }
                                    }
                                }
                            });

                    tasksRVAdapter.setSelectedProject(documentId);
                    fetchAndDisplayTasks(documentId);

                    //CommentListFragment bottomSheetFragment = CommentListFragment.newInstance(documentId);
                    //bottomSheetFragment.show(getSupportFragmentManager(), bottomSheetFragment.getTag());
                    if (!isFinishing()) {
                        CommentListFragment bottomSheetFragment = CommentListFragment.newInstance(documentId);
                        bottomSheetFragment.show(getSupportFragmentManager(), bottomSheetFragment.getTag());
                    }
                    UpdateTaskFragment fragment = UpdateTaskFragment.newInstance();
                    Bundle bundle = new Bundle();
                    bundle.putString("pId", documentId);
                    fragment.setArguments(bundle);

                    db.collection("projectTasks")
                            .whereEqualTo("projectId", documentId)
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


            //update project progress textview in real time tasks progress is updated in ProjectActivity.
            assert receivedProId != null;
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
                                // Update the TextViews in real-time
                                String updatedTitle = snapshot.getString("title");
                                proTitle.setText(updatedTitle);
                                String updatedDescription = snapshot.getString("description");
                                proDes.setText(updatedDescription);
                                String updatedStartDt = snapshot.getString("startDate");
                                proStartDate.setText(updatedStartDt);
                                String updatedEndDate = snapshot.getString("endDate");
                                proEndDate.setText(updatedEndDate);
                                String updatedPriority = snapshot.getString("priority");
                                proPriority.setText(updatedPriority);
                                String updatedProgress = snapshot.getString("progress");
                                Date completionDate = snapshot.getDate("actualEndDate");
                                if (completionDate != null) {
                                    SimpleDateFormat sdf1 = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
                                    String formattedEndDate = sdf1.format(completionDate);
                                    projectCompletionDate.setText(formattedEndDate);
                                    completionTitle.setVisibility(View.VISIBLE);
                                    projectCompletionDate.setVisibility(View.VISIBLE);
                                }else{
                                    completionTitle.setVisibility(View.GONE);
                                    projectCompletionDate.setVisibility(View.GONE);
                                }
                                if (updatedProgress != null) {
                                    progress.setText(updatedProgress);
                                }
                            }
                        }
                    });

            tasksRVAdapter.setSelectedProject(receivedProId);

            BottomSheetFragmentAddTask taskFragment = BottomSheetFragmentAddTask.newInstance();
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


                    Intent i =getIntent();
                    if (i.hasExtra(MESSAGE_KEY)) {
                        String receivedProId = i.getStringExtra(MESSAGE_KEY);
                        Intent intent = new Intent(ProjectActivity.this, TasksProgressAnalysis.class);
                        intent.putExtra("PROJECTid", receivedProId);
                        startActivity(intent);
                    }

                  else  if (getIntent().hasExtra("selectedProject")) {
                        Projects projects = (Projects) getIntent().getSerializableExtra("selectedProject");
                        FirestoreManager firestoreManager = new FirestoreManager();
                        assert projects != null;
                        firestoreManager.getDocumentId("Projects", "title", projects.getTitle(), documentId -> {
                            if (documentId != null) {
                                Intent intent = new Intent(ProjectActivity.this, TasksProgressAnalysis.class);
                                intent.putExtra("PROJECTID", documentId);
                                startActivity(intent);
                            }
                        });
                    }

                }//update project in the projectActivity activity
                else if (menuItem.getItemId() == R.id.UpdateProject) {

                    Intent i1 = getIntent();
                    if (i1.hasExtra(MESSAGE_KEY)) {
                        String receivedProId = i1.getStringExtra(MESSAGE_KEY);
                        // Create an instance of your UpdateFragment.
                        UpdateProject updateFragment = new UpdateProject();
                        // Pass data to the fragment using Bundle.
                        Bundle bundle = new Bundle();
                        bundle.putString("proTid", receivedProId);
                        updateFragment.setArguments(bundle);
                        // Replace the existing fragment with the new fragment.
                        FragmentManager fragmentManager = ProjectActivity.this.getSupportFragmentManager();
                        FragmentTransaction transaction = fragmentManager.beginTransaction();
                        transaction.replace(R.id.updatePfra, updateFragment);
                        transaction.addToBackStack(null);
                        transaction.commit();
                    }

                    Intent intent1 = getIntent();
                    if (intent1.hasExtra("selectedProject")) {
                        Projects projects = (Projects) intent1.getSerializableExtra("selectedProject");
                        FirestoreManager firestoreManager = new FirestoreManager();
                        assert projects != null;
                        firestoreManager.getDocumentId("Projects", "title", projects.getDescription(), documentId -> {
                            if (documentId != null) {
                                projects.setProjectId(documentId);
                                // Create an instance of your UpdateFragment.
                                UpdateProject updateFragment = new UpdateProject();
                                // Pass data to the fragment using Bundle.
                                Bundle bundle = new Bundle();
                                bundle.putSerializable("proJ", projects);
                                updateFragment.setArguments(bundle);
                                // Replace the existing fragment with the new fragment.
                                FragmentManager fragmentManager = ProjectActivity.this.getSupportFragmentManager();
                                FragmentTransaction transaction = fragmentManager.beginTransaction();
                                transaction.replace(R.id.updatePfra, updateFragment);
                                transaction.addToBackStack(null);
                                transaction.commit();
                            }  // Handle the case where the document ID couldn't be retrieved
                        });
                    }

                }else if (menuItem.getItemId() == R.id.teamAnalysis){

                    UsersListFragment usersListFragment = UsersListFragment.newInstance();
                    Bundle bundle1 = new Bundle();
                    usersListFragment.setArguments(bundle1);
                    bundle1.putString("proTid", projectId);
                    FragmentManager fragmentManager = ProjectActivity.this.getSupportFragmentManager();
                    FragmentTransaction transaction = fragmentManager.beginTransaction();
                    transaction.replace(R.id.userFragment, usersListFragment);
                    transaction.addToBackStack(null);
                    transaction.commit();
                }

                return true;
            }
        });
        // Show the popup menu
        popupMenu.show();
    }
    @Override
    public void onTaskUpdated() {
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

                            assert taskData != null;
                            taskData.setTaskId(document.getId());
                            tasksList.add(taskData);
                            tasksList.sort((o1, o2) -> o2.getStartDate().compareTo(o1.getStartDate()));
                            tasksRVAdapter.updateList(tasksList);
                        } else {
                            Log.e("Firestore", "Error fetching task details: " + task.getException());
                        }
                    } else {
                        Toast.makeText(ProjectActivity.this, "Error fetching task details", Toast.LENGTH_SHORT).show();
                    }
                });
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
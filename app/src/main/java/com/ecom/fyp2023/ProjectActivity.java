package com.ecom.fyp2023;

import static com.ecom.fyp2023.Fragments.BottomSheetDialogAddProject.MESSAGE_KEY;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ecom.fyp2023.Adapters.TasksRVAdapter;
import com.ecom.fyp2023.AppManagers.FirestoreManager;
import com.ecom.fyp2023.Fragments.BottomSheetFragmentAddTask;
import com.ecom.fyp2023.Fragments.CommentListFragment;
import com.ecom.fyp2023.ModelClasses.Projects;
import com.ecom.fyp2023.ModelClasses.Tasks;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class ProjectActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ArrayList<Tasks> tasksArrayList;
    private TasksRVAdapter tasksRVAdapter;
    private FirebaseFirestore db;
    TextView commentFrag;
    //String receivedProId;

    private TextView proDes;

    public static final String projectId_key="proId";
    public static final String p_key="p_key";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project);

        TextView proTitle = findViewById(R.id.titleTextveiw);
        proDes = findViewById(R.id.descriptionTextView);
        TextView proStartDate = findViewById(R.id.startDateTextView);
        TextView proEndDate = findViewById(R.id.endDateTextView);
        TextView proPriority = findViewById(R.id.priorityTextView);
        ImageView addTask = findViewById(R.id.addTasksImageView);
        recyclerView = findViewById(R.id.tasksRecyclerView);
        commentFrag = findViewById(R.id.commentEditText);


        db = FirebaseFirestore.getInstance();


        tasksArrayList = new ArrayList<>();

        LinearLayoutManager layoutManager = new LinearLayoutManager(ProjectActivity.this, RecyclerView.VERTICAL,false);
        recyclerView.setLayoutManager(layoutManager);

        tasksRVAdapter = new TasksRVAdapter(tasksArrayList);
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
            proStartDate.setText(projects.getStartDate());
            proEndDate.setText(projects.getEndDate());
            proPriority.setText(projects.getPriority());

            FirestoreManager firestoreManager = new FirestoreManager();
            firestoreManager.getDocumentId("Projects", "title", projects.getTitle(), documentId -> {
                if (documentId != null) {

                    fetchAndDisplayTasks(documentId);

                    CommentListFragment bottomSheetFragment = CommentListFragment.newInstance(documentId);
                    bottomSheetFragment.show(getSupportFragmentManager(), bottomSheetFragment.getTag());

                }
            });

        }

        //receives intent for onclick add task button
        Intent i = getIntent();
        if (i.hasExtra(MESSAGE_KEY)) {
            String receivedProId = i.getStringExtra(MESSAGE_KEY);

            BottomSheetFragmentAddTask taskFragment = BottomSheetFragmentAddTask.newInstance();
            Bundle bundle = new Bundle();
            bundle.putString(projectId_key, receivedProId);
            taskFragment.setArguments(bundle);
            taskFragment.show(getSupportFragmentManager(), taskFragment.getTag());

            String receivedProTitle = getIntent().getStringExtra("proTitle");
            proTitle.setText(receivedProTitle);
            String receivedProDesc = getIntent().getStringExtra("proDesc");
            proDes.setText(receivedProDesc);
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

    private void fetchAndDisplayTasks(String projectId) {
        tasksArrayList.clear();

        CollectionReference projectTasksCollection = db.collection("projectTasks");
        Query query = projectTasksCollection
                .whereEqualTo("projectId", projectId)
                .orderBy("timestamp", Query.Direction.ASCENDING);
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        String taskId = document.getString("taskId");
                        retrieveTaskDetails(taskId);
                    }
                    Log.d("RecyclerView", "Before update - Size: " + tasksArrayList.size());
                    Log.d("RecyclerView", "After update - Size: " + tasksArrayList.size());
                    tasksRVAdapter.notifyDataSetChanged();

                } else {
                    Toast.makeText(ProjectActivity.this, "Error fetching tasks", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void retrieveTaskDetails(String taskId) {
        CollectionReference tasksCollection = db.collection("Tasks");
        tasksCollection.document(taskId).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            Tasks taskData = document.toObject(Tasks.class);
                            tasksArrayList.add(taskData);

                            Log.d("RecyclerView", "Before update - Size: " + tasksArrayList.size());
                            Log.d("RecyclerView", "After update - Size: " + tasksArrayList.size());
                            tasksRVAdapter.notifyDataSetChanged();
                            recyclerView.requestLayout();


                        } else {
                            Toast.makeText(ProjectActivity.this, "Task details not found", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(ProjectActivity.this, "Error fetching task details", Toast.LENGTH_SHORT).show();
                    }
                });

    }

    public void onDescriptionClick(View view) {
        if (proDes.getMaxLines() == Integer.MAX_VALUE) {
            proDes.setMaxLines(3);
        } else {
            proDes.setMaxLines(Integer.MAX_VALUE);
        }
    }

    private String formatDateFromTimestamp(Timestamp timestamp) {
        if (timestamp != null) {
            Date date = timestamp.toDate();
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
            return sdf.format(date);
        } else {
            return ""; // Handle the case where Timestamp is null
        }
    }


}
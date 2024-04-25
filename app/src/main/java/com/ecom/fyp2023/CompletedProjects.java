package com.ecom.fyp2023;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ecom.fyp2023.Adapters.CompletedProjectsAdapter;
import com.ecom.fyp2023.AppManagers.SharedPreferenceManager;
import com.ecom.fyp2023.ModelClasses.Projects;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

public class CompletedProjects extends AppCompatActivity {

    //String groupId = GroupIdGlobalVariable.getInstance().getGlobalData();;


    SharedPreferenceManager sharedPrefManager;
    String groupId;
    CompletedProjectsAdapter recyclerAdapter;

    private ArrayList<Projects> projectsArrayList;
    RecyclerView recyclerView;
    FirebaseFirestore db;
    String savedGroupId,savedAuthId;


    private SearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.projects_completed);

        db = FirebaseFirestore.getInstance();

        sharedPrefManager = new SharedPreferenceManager(this);


        projectsArrayList = new ArrayList<>();
        recyclerView = findViewById(R.id.CompletedRecyclerView);
        searchView = findViewById(R.id.CompletedSearchView);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        LinearLayoutManager layoutManager = new LinearLayoutManager(CompletedProjects.this, RecyclerView.VERTICAL,false);
        recyclerView.setLayoutManager(layoutManager);

        setupSearchView();

        recyclerAdapter = new CompletedProjectsAdapter(projectsArrayList,CompletedProjects.this);
        recyclerView.setAdapter(recyclerAdapter);

        savedAuthId = sharedPrefManager.getUserAuthId();

        savedGroupId = sharedPrefManager.getGroupId();
        retrievePersonalData();

        if(savedAuthId != null){

            retrievePersonalData();

        } else if (savedGroupId != null) {
            retrieveGroupData();

        }
    }

    public void retrieveGroupData(){

        db.collection("Projects")
                .whereEqualTo("groupId", savedGroupId) // Filter projects by groupId
                .whereIn("progress", Arrays.asList("Complete"))
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Toast.makeText(CompletedProjects.this, "Error getting data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (value != null) {
                        projectsArrayList.clear();
                        for (DocumentSnapshot document : value) {
                            Projects project = document.toObject(Projects.class);
                            if (project != null) {
                                project.setProjectId(document.getId());
                                projectsArrayList.add(project);
                            }else {

                            }
                        }
                        recyclerAdapter.notifyDataSetChanged();
                    }
                });
    }


    public void retrievePersonalData(){
        db.collection("Projects")
                .whereEqualTo("userAuthId", savedAuthId)
                .whereIn("progress", Arrays.asList("Complete"))
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Toast.makeText(CompletedProjects.this, "Error getting data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (value != null) {
                        projectsArrayList.clear();
                        for (DocumentSnapshot document : value) {
                            Projects project = document.toObject(Projects.class);
                            if (project != null) {
                                project.setProjectId(document.getId());
                                projectsArrayList.add(project);
                                recyclerAdapter.notifyDataSetChanged();
                            }
                        }

                    }
                });
    }


//    private void fetchCompletedFromFirestore() {
//        CollectionReference filesCollection = db.collection("Projects");
//
//        Query query;
//        if (groupId != null) {
//            // Retrieve files belonging to the provided groupId
//            query = filesCollection.whereEqualTo("groupId", groupId);
//        } else {
//            // Retrieve files belonging to the provided userAuthId
//            String userAuthId = FirebaseAuth.getInstance().getCurrentUser().getUid();
//            query = filesCollection.whereEqualTo("userAuthId", userAuthId);
//        }
//
//        query.addSnapshotListener((value, error) -> {
//            if (error != null) {
//                // Handle errors
//                Toast.makeText(getApplicationContext(), "Failed to fetch files from Firestore: " + error.getMessage(), Toast.LENGTH_SHORT).show();
//                return;
//            }
//
//            if (value != null) {
//                projectsArrayList.clear(); // Clear the existing list
//                for (QueryDocumentSnapshot document : value) {
//                    Projects projects = document.toObject(Projects.class);
//                    projectsArrayList.add(projects);
//                }
//                recyclerAdapter.updateList(projectsArrayList);
//            }
//        });
//    }
//
//    public void retrieveGroupData(){
//
//        db.collection("Projects")
//                .whereEqualTo("groupId", savedGroupId) // Filter projects by groupId
//                .whereIn("progress", Arrays.asList("In Progress", "Incomplete"))
//                .addSnapshotListener((value, error) -> {
//                    if (error != null) {
//                        return;
//                    }
//
//                    if (value != null) {
//                        projectsArrayList.clear();
//                        for (DocumentSnapshot document : value) {
//                            Projects project = document.toObject(Projects.class);
//                            if (project != null) {
//                                project.setProjectId(document.getId());
//                                projectsArrayList.add(project);
//                            }else {
//
//
//
//                            }
//                        }
//                        recyclerAdapter.notifyDataSetChanged();
//                    }
//                });
//
//    }


    private void setupSearchView() {
        searchView.setOnQueryTextListener(new androidx.appcompat.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterData(newText);
                return true;
            }
        });
    }

    private void filterData(String query) {
        ArrayList<Projects> filteredList = new ArrayList<>();

        for (Projects item : projectsArrayList) {
            if (item.getTitle().toLowerCase().contains(query.toLowerCase())||item.getPriority().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(item);
            }
        }

        recyclerAdapter.updateList(filteredList);
    }

}
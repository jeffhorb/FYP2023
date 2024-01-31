package com.ecom.fyp2023;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ecom.fyp2023.Adapters.CompletedProjectsAdapter;
import com.ecom.fyp2023.ModelClasses.Projects;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Objects;

public class CompletedProjects extends AppCompatActivity {

    CompletedProjectsAdapter recyclerAdapter;

    private ArrayList<Projects> projectsArrayList;
    RecyclerView recyclerView;
    FirebaseFirestore db;

    private SearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.projects_completed);

        db = FirebaseFirestore.getInstance();

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

        db.collection("Projects")
                .whereEqualTo("progress", "Complete")
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
                            }
                        }
                        recyclerAdapter.notifyDataSetChanged();
                    }
                });


    }

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
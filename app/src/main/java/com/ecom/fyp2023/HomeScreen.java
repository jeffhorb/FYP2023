package com.ecom.fyp2023;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ecom.fyp2023.Adapters.ProjectsRVAdapter;
import com.ecom.fyp2023.AppManagers.SharedPreferenceManager;
import com.ecom.fyp2023.Fragments.BottomSheetDialogAddProject;
import com.ecom.fyp2023.ModelClasses.Projects;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HomeScreen extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    public DrawerLayout drawerLayout;
    public ActionBarDrawerToggle actionBarDrawerToggle;

    ProjectsRVAdapter recyclerAdapter;

    private ArrayList<Projects> projectsArrayList;
    RecyclerView recyclerView;
    FirebaseFirestore db;
    NavigationView navigationView;

    SharedPreferenceManager sharedPrefManager;

    private SearchView searchView;

    public interface DataUpdateCallback {
        void onDataUpdated(String newData);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        db = FirebaseFirestore.getInstance();

        projectsArrayList = new ArrayList<>();


        recyclerView = findViewById(R.id.recyclerView);

        searchView = findViewById(R.id.searchView);

        LinearLayoutManager layoutManager = new LinearLayoutManager(HomeScreen.this, RecyclerView.VERTICAL,false);
        recyclerView.setLayoutManager(layoutManager);

        sharedPrefManager = new SharedPreferenceManager(this);

        //search method
        setupSearchView();

        recyclerAdapter = new ProjectsRVAdapter(projectsArrayList,HomeScreen.this);
        recyclerView.setAdapter(recyclerAdapter);

        db.collection("Projects")
                .whereIn("progress", Arrays.asList("In Progress", "Incomplete"))
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Toast.makeText(HomeScreen.this, "Error getting data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
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



        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer);

        navigationView = findViewById(R.id.nav_view);

        navigationView.setNavigationItemSelectedListener(this);
        navigationView.bringToFront();


        actionBarDrawerToggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.nav_open, R.string.nav_close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
            actionBarDrawerToggle.syncState();


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //return super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == R.id.completedP) {
           Intent intent = new Intent(HomeScreen.this, CompletedProjects.class);
           startActivity(intent);
            return true;
        } else if (itemId == R.id.imageNotification) {
            // Handle the action for menu option 2
            Toast.makeText(this, "Menu Option 2 clicked", Toast.LENGTH_SHORT).show();
            // Add your custom logic here
            return true;
        }
        // Add more if statements for additional menu options if needed

        return super.onOptionsItemSelected(item);
    }



    //bottom navigation
    public void addPro(MenuItem menuitem){

        BottomSheetDialogAddProject bottomSheetDialogFragment = BottomSheetDialogAddProject.newInstance();
        bottomSheetDialogFragment.show(getSupportFragmentManager(), bottomSheetDialogFragment.getTag());

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


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == R.id.nav_home) {
            Toast.makeText(this, "Home clicked", Toast.LENGTH_SHORT).show();
        } else if (itemId == R.id.nav_settings) {
            Toast.makeText(HomeScreen.this, "Settings clicked", Toast.LENGTH_SHORT).show();
        } else if (itemId == R.id.nav_about) {
            Toast.makeText(this, "About Us clicked", Toast.LENGTH_SHORT).show();
        } else if (itemId == R.id.nav_logout) {

            sharedPrefManager.clearSession();
            FirebaseAuth.getInstance().signOut();

            Intent intent = new Intent(HomeScreen.this, Login_activity.class);
            startActivity(intent);
            finish();

        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

}

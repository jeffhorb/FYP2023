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

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class HomeScreen extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    public DrawerLayout drawerLayout;
    public ActionBarDrawerToggle actionBarDrawerToggle;



    ProjectsRVAdapter recyclerAdapter;

    ArrayList<Projects> projectsArrayList;
    RecyclerView recyclerView;
    FirebaseFirestore db;
    NavigationView navigationView;

    SharedPreferenceManager sharedPrefManager;

    private SearchView searchView;

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

        db.collection("Projects").get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                        if (!queryDocumentSnapshots.isEmpty()) {

                            List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();
                            for (DocumentSnapshot d : list) {

                                Projects project = d.toObject(Projects.class);

                                project.setProjectId(d.getId());

                                projectsArrayList.add(project);
                            }

                            recyclerAdapter.notifyDataSetChanged();
                        } else {
                            // if the snapshot is empty we are displaying a toast message.
                            Toast.makeText(HomeScreen.this, "No data found in Database", Toast.LENGTH_SHORT).show();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        Toast.makeText(HomeScreen.this, "Fail to get the data.", Toast.LENGTH_SHORT).show();
                    }
                });


        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer);

        navigationView = findViewById(R.id.nav_view);

        navigationView.setNavigationItemSelectedListener(this);


        actionBarDrawerToggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.nav_open, R.string.nav_close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
            actionBarDrawerToggle.syncState();
    }


    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    //bottom navigation
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

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

        int id = item.getItemId();

        if (id == R.id.nav_logout) {

            sharedPrefManager.clearSession();
            FirebaseAuth.getInstance().signOut();

            Intent intent = new Intent(HomeScreen.this, Login_activity.class);
            startActivity(intent);
            finish();
            return true;

        } else if (id == R.id.nav_settings) {
            // Intent intent = new Intent(HomeScreen.this, SettingsActivity.class);
            // startActivity(intent);
            //finish();
            return true;
        }

        return false;
    }
}

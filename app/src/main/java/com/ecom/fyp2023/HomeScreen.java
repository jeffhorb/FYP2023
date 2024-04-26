package com.ecom.fyp2023;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ecom.fyp2023.Adapters.ProjectsRVAdapter;
import com.ecom.fyp2023.Analysis.ProjectProgressAnalysis;
import com.ecom.fyp2023.AppManagers.FirestoreManager;
import com.ecom.fyp2023.AppManagers.SharedPreferenceManager;
import com.ecom.fyp2023.AuthenticationClasses.Login_activity;
import com.ecom.fyp2023.Fragments.BottomSheetDialogAddProject;
import com.ecom.fyp2023.Fragments.UsersListFragment;
import com.ecom.fyp2023.TeamManagementClasses.AdminChecker;
import com.ecom.fyp2023.TeamManagementClasses.CreateGroupActivity;
import com.ecom.fyp2023.TeamManagementClasses.GroupMembers;
import com.ecom.fyp2023.TeamManagementClasses.PendingGroupInvitations;
import com.ecom.fyp2023.FastbaordSDK.CollabrativeWhiteboard;
import com.ecom.fyp2023.ModelClasses.Projects;
import com.ecom.fyp2023.SentimentAnalysis.SentimentAnalysisActivity;
import com.ecom.fyp2023.VersionControlClasses.DocumentActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HomeScreen extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    public DrawerLayout drawerLayout;
    public ActionBarDrawerToggle actionBarDrawerToggle;

    ProjectsRVAdapter recyclerAdapter;

    private ArrayList<Projects> projectsArrayList;
    RecyclerView recyclerView;
    FirebaseFirestore db;
    NavigationView navigationView;

    String savedGroupId,savedAuthId;

    SharedPreferenceManager sharedPrefManager;

    TextView groupN,options;

    private boolean manageAccountClicked = false;

    private SearchView searchView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        groupN = findViewById(R.id.groupName);
        options = findViewById(R.id.options);

        db = FirebaseFirestore.getInstance();

        projectsArrayList = new ArrayList<>();

        sharedPrefManager = new SharedPreferenceManager(this);

        recyclerView = findViewById(R.id.recyclerView);

        searchView = findViewById(R.id.searchView);

        LinearLayoutManager layoutManager = new LinearLayoutManager(HomeScreen.this, RecyclerView.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);

        savedAuthId = sharedPrefManager.getUserAuthId();

        savedGroupId = sharedPrefManager.getGroupId();


        if(savedAuthId != null){

            retrievePersonalData();

       } else if (savedGroupId != null) {
            isUserInGroup(savedGroupId, new FirebaseCallback() {
                @Override
                public void onCallback(boolean isUserInGroup) {
                    if(isUserInGroup){
                        retrieveGroupData();
                        String groupName = sharedPrefManager.getGroupName();
                        String groupDes = sharedPrefManager.getGroupDescription();
                        groupN.setText(groupName);
                        options.setVisibility(View.VISIBLE);
                        options.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                PopupMenu popupMenu = new PopupMenu(HomeScreen.this, v);
                                popupMenu.getMenu().add("Group Members");
                                popupMenu.getMenu().add("");
                                popupMenu.getMenu().add("Send Invite");
                                popupMenu.getMenu().add("Pending Invitations");

                                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                                    @Override
                                    public boolean onMenuItemClick(MenuItem item) {
                                        String selectedItem = item.getTitle().toString();
                                        if (selectedItem.equals("Pending Invitations")) {
                                            // Handle Pending Invitations option
                                            Intent intent = new Intent(HomeScreen.this, PendingGroupInvitations.class);
                                            startActivity(intent);
                                        } else if (selectedItem.equals("Group Members")) {
                                            // Handle Group Members option
                                            Intent intent = new Intent(HomeScreen.this, GroupMembers.class);
                                            startActivity(intent);
                                        } else if (selectedItem.equals("Send Invite")) {
                                            UsersListFragment usersListFragment = UsersListFragment.newInstance();
                                            Bundle bundle = new Bundle();
                                            bundle.putString("groupId", savedGroupId);
                                            bundle.putString("groupName", groupName);
                                            bundle.putString("groupDes",groupDes);
                                            usersListFragment.setArguments(bundle);
                                            usersListFragment.show(getSupportFragmentManager(), usersListFragment.getTag());
                                        }
                                        return true;
                                    }
                                });

                                popupMenu.show();
                            }
                        });
                    } else {
                        retrievePersonalData();
                        sharedPrefManager.clearGroupId();

                    }
                }
            });

        }else {
            retrievePersonalData();
            sharedPrefManager.saveUserAuthId(FirebaseAuth.getInstance().getCurrentUser().getUid());

        }

        //search method
        setupSearchView();

        setUserDetailsInNavHeader();

        recyclerAdapter = new ProjectsRVAdapter(projectsArrayList, HomeScreen.this);
        recyclerView.setAdapter(recyclerAdapter);

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

    public interface FirebaseCallback {
        void onCallback(boolean isUserInGroup);
    }

    public void isUserInGroup(String groupId, final FirebaseCallback firebaseCallback) {
        db.collection("groups").document(groupId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        List<String> members = (List<String>) document.get("members");
                        firebaseCallback.onCallback(members.contains(FirebaseAuth.getInstance().getCurrentUser().getUid()));
                    } else {
                        Log.d("TAG", "No such document");
                    }
                } else {
                    Log.d("TAG", "get failed with ", task.getException());
                }
            }
        });
    }


    public void retrieveGroupData(){

        db.collection("Projects")
                .whereEqualTo("groupId", savedGroupId) // Filter projects by groupId
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
                            }else {

                            }
                        }
                        recyclerAdapter.notifyDataSetChanged();
                    }
                });
    }


    public void retrievePersonalData(){
        db.collection("Projects")
                .whereEqualTo("userAuthId", FirebaseAuth.getInstance().getCurrentUser().getUid())
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
                                recyclerAdapter.notifyDataSetChanged();
                            }
                        }
                    }
                });
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
            //TODO: intent.putExtra("groupId",groupId);
            startActivity(intent);
            return true;
        } else if (itemId == R.id.whiteboard) {
            String groupName = sharedPrefManager.getGroupName();
            //createWhiteboard();
            Intent intent = new Intent(HomeScreen.this, CollabrativeWhiteboard.class);
            intent.putExtra("groupName", groupName);
            //intent.putExtra("userName",userName);
            startActivity(intent);

            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //bottom navigation to add project
    public void addPro(MenuItem menuitem) {
        BottomSheetDialogAddProject bottomSheetDialogFragment = BottomSheetDialogAddProject.newInstance();
        bottomSheetDialogFragment.show(getSupportFragmentManager(), bottomSheetDialogFragment.getTag());

    }

    //bottom nav to open project analysis
    public void analysePro(MenuItem menuitem) {
        Intent intent = new Intent(HomeScreen.this, ProjectProgressAnalysis.class);
        startActivity(intent);
    }

    public void openDocument(MenuItem menuitem) {
        Intent intent = new Intent(HomeScreen.this, DocumentActivity.class);
        startActivity(intent);
    }

    public void Sentiment(MenuItem menuitem) {
        Intent intent = new Intent(HomeScreen.this, SentimentAnalysisActivity.class);
        startActivity(intent);
    }

    public void Group(MenuItem menuitem) {
        Intent intent = new Intent(HomeScreen.this, CreateGroupActivity.class);
        startActivity(intent);
    }

    //hide sentiment analysis for private space
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // Hide the sentimentMenuItem if the groupId is null
        String currentGroupId = sharedPrefManager.getGroupId();
        boolean isGroupIdNotNull = currentGroupId != null;
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        // Find the sentiment item in the bottom navigation menu
        MenuItem sentimentMenuItem = bottomNavigationView.getMenu().findItem(R.id.sentiment);
        // Set the visibility of the sentimentMenuItem
        if (sentimentMenuItem != null) {
            sentimentMenuItem.setVisible(isGroupIdNotNull);
        }
        // Return false to allow normal menu processing to proceed
        return false;
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
            if (item.getTitle().toLowerCase().contains(query.toLowerCase()) || item.getPriority().toLowerCase().contains(query.toLowerCase())) {
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

        } else if (itemId == R.id.manageAccount) {
            // Declare a boolean variable to track whether the "Manage Account" option is clicked
            manageAccountClicked = true;
            // Update the user details in the navigation header
            setUserDetailsInNavHeader();

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

    private void setUserDetailsInNavHeader() {
        NavigationView navigationView = findViewById(R.id.nav_view);
        View headerView = navigationView.getHeaderView(0);

        // Find TextViews in the header layout
        TextView name = headerView.findViewById(R.id.uN);
        TextView email = headerView.findViewById(R.id.uE);
        TextView skill = headerView.findViewById(R.id.skill);
        TextView adminStatus = headerView.findViewById(R.id.adminsStatus);

        // Get the current user's ID
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();

            OnCompleteListener<QuerySnapshot> onCompleteListener = new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        if (querySnapshot != null && !querySnapshot.isEmpty()) {
                            // Customer document exists, retrieve its details
                            DocumentSnapshot document = querySnapshot.getDocuments().get(0);
                            String userName = document.getString("userName");
                            String userEmail = document.getString("userEmail");
                            String skl = document.getString("occupation");

                            // Set user details in the TextViews
                            name.setText(userName);
                            email.setText(userEmail);

                            if (skill != null){
                                skill.setText(skl);
                            }
                            if (manageAccountClicked) {
                                // If "Manage Account" is clicked, show the update account dialog
                                showUpdateAccountDialog(document.getId());
                                // Reset manageAccountClicked back to false
                                manageAccountClicked = false;
                            }

                            // Check if the user is an admin
                            AdminChecker.checkIfAdmin(new AdminChecker.AdminCheckCallback() {
                                @Override
                                public void onResult(boolean isAdmin) {
                                    if (isAdmin) {
                                        // Set admin status to "ADMIN"
                                        adminStatus.setText("ADMIN");
                                    } else {
                                        // User is not an admin
                                        adminStatus.setText("");
                                    }
                                }
                            }, userId, sharedPrefManager.getGroupId());
                        } else {
                            // Customer document does not exist
                            Log.d("HomePage", "No such document");
                        }
                    } else {
                        // Error occurred while retrieving customer document
                        Log.d("HomePage", "Error getting customer document", task.getException());
                    }
                }
            };

            // Call getCustomerDocumentId with onCompleteListener
            getCustomerDocumentId(userId, onCompleteListener);
        } else {
            // Current user is null
            Log.d("HomePage", "No user signed in");
        }
    }

    private void showUpdateAccountDialog(String id) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.update_account_dialog, null);
        builder.setView(dialogView);

        // Find EditTexts in the dialog layout
        EditText newNameEditText = dialogView.findViewById(R.id.newNameEditText);
        EditText newEmailEditText = dialogView.findViewById(R.id.newEmailEditText);
        EditText newRole = dialogView.findViewById(R.id.newRoleEditText);


        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            getCustomerDocumentId(userId, new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        if (querySnapshot != null && !querySnapshot.isEmpty()) {
                            // Customer document exists, retrieve its details
                            DocumentSnapshot document = querySnapshot.getDocuments().get(0);
                            String userName = document.getString("userName");
                            String userEmail = document.getString("userEmail");
                            String userRole = document.getString("occupation");

                            // Set current details in EditText fields
                            newNameEditText.setText(userName);
                            newEmailEditText.setText(userEmail);
                            if (userRole !=null){
                                newRole.setText(userRole);
                            }

                        } else {
                            // Document does not exist
                            Log.d("HomePage", "No such document");
                        }
                    } else {
                        // Error getting document
                        Log.d("HomePage", "Error getting document", task.getException());
                    }
                }
            });
        }

        FirestoreManager firestoreManager = new FirestoreManager();

        builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Update user's details in Firestore
                Map<String, Object> updatedData = new HashMap<>();
                updatedData.put("userName", newNameEditText.getText().toString());
                updatedData.put("userEmail", newEmailEditText.getText().toString());
                updatedData.put("occupation",newRole.getText().toString());

                firestoreManager.updateDocument("Users", id, updatedData, new FirestoreManager.OnUpdateCompleteListener() {
                    @Override
                    public void onUpdateComplete(boolean success) {
                        if (success) {
                            Toast.makeText(HomeScreen.this, "User details updated successfully", Toast.LENGTH_SHORT).show();

                        } else {
                            Toast.makeText(HomeScreen.this, "Failed to update user details", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void getCustomerDocumentId(String userId, OnCompleteListener<QuerySnapshot> onCompleteListener) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference customersRef = db.collection("Users");

        customersRef.whereEqualTo("userId", userId)
                .limit(1)
                .get()
                .addOnCompleteListener(onCompleteListener);
    }
}

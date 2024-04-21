package com.ecom.fyp2023.InvitationClass;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.ecom.fyp2023.AppManagers.SharedPreferenceManager;
import com.ecom.fyp2023.HomeScreen;
import com.ecom.fyp2023.ModelClasses.Group;
import com.ecom.fyp2023.R;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CreateGroupActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private Button createGroupButton,pendingInvites;
    private RecyclerView recyclerView;
    private GroupAdapter groupAdapter;
    private List<Group> groupList;

    SharedPreferenceManager sharedPreferenceManager;

    TextView mySpace;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        sharedPreferenceManager = new SharedPreferenceManager(this);


        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        pendingInvites = findViewById(R.id.seePendingInvites);
        createGroupButton = findViewById(R.id.create_group_button);
        recyclerView = findViewById(R.id.recycler_view_groups);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        mySpace = findViewById(R.id.mySpace);

        groupList = new ArrayList<>();
        groupAdapter = new GroupAdapter(this, groupList);
        recyclerView.setAdapter(groupAdapter);

        pendingInvites.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CreateGroupActivity.this, PendingInvitesActivity.class);
                startActivity(intent);
            }
        });

        createGroupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCreateGroupDialog();
            }
        });

        mySpace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                String userAuthId = currentUser.getUid();

                sharedPreferenceManager.clearSavedIds();
                Intent intent = new Intent(CreateGroupActivity.this, HomeScreen.class);
                sharedPreferenceManager.saveUserAuthId(userAuthId);
                startActivity(intent);

            }
        });

        // Load user's groups
        loadUserGroups();
    }

    private void showCreateGroupDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_create_group, null);
        dialogBuilder.setView(dialogView);

        final EditText groupNameEditText = dialogView.findViewById(R.id.edit_text_group_name);
        final EditText groupDescriptionEditText = dialogView.findViewById(R.id.edit_text_group_description);
        Button createButton = dialogView.findViewById(R.id.button_create_group);

        final AlertDialog dialog = dialogBuilder.create();

        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String groupName = groupNameEditText.getText().toString().trim();
                String groupDescription = groupDescriptionEditText.getText().toString().trim();

                if (TextUtils.isEmpty(groupName)) {
                    groupNameEditText.setError("Group name is required");
                    return;
                }

                if (TextUtils.isEmpty(groupDescription)) {
                    groupNameEditText.setError("Group Description is required");
                    return;
                }
                // Create group
                createGroup(groupName, groupDescription);
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void createGroup(String groupName, String groupDescription) {
        // Get current user ID
        String userId = mAuth.getCurrentUser().getUid();

        // Create group object
        Group group = new Group();
        group.setGroupName(groupName);
        group.setDescription(groupDescription);
        group.addMember(userId); // Add current user as a member

        // Add group to Firestore
        CollectionReference groupsRef = db.collection("groups");
        groupsRef.add(group)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Toast.makeText(CreateGroupActivity.this, "Group created successfully", Toast.LENGTH_SHORT).show();
                        loadUserGroups(); // Reload groups after creating a new one
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(CreateGroupActivity.this, "Failed to create group: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadUserGroups() {
        // Get current user ID
        String userId = mAuth.getCurrentUser().getUid();

        // Query groups where the current user is a member
        db.collection("groups")
                .whereArrayContains("members", userId)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Toast.makeText(CreateGroupActivity.this, "Failed to load groups: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            return;
                        }

                        if (queryDocumentSnapshots != null) {
                            groupList.clear();
                            for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                                Group group = documentSnapshot.toObject(Group.class);
                                groupList.add(group);
                            }
                            groupAdapter.notifyDataSetChanged();
                        }
                    }
                });
    }

//    private void loadUserGroups() {
//        // Get current user ID
//        String userId = mAuth.getCurrentUser().getUid();
//
//        // Query groups where the current user is a member
//        db.collection("groups")
//                .whereArrayContains("members", userId)
//                .get()
//                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
//                    @Override
//                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
//                        groupList.clear();
//                        for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
//                            Group group = documentSnapshot.toObject(Group.class);
//                            groupList.add(group);
//                        }
//                        groupAdapter.notifyDataSetChanged();
//                    }
//                })
//                .addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//                        Toast.makeText(CreateGroupActivity.this, "Failed to load groups: " + e.getMessage(), Toast.LENGTH_SHORT).show();
//                    }
//                });
//    }
}
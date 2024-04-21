package com.ecom.fyp2023.InvitationClass;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.ecom.fyp2023.AppManagers.SharedPreferenceManager;
import com.ecom.fyp2023.ModelClasses.Group;
import com.ecom.fyp2023.ModelClasses.Invitation;
import com.ecom.fyp2023.ModelClasses.Users;
import com.ecom.fyp2023.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class GroupMembers extends AppCompatActivity {

    RecyclerView recyclerView;

    GroupMembersAdapter adapter;

    List<Users> usersList;

    SharedPreferenceManager sharedPreferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_members);

        recyclerView = findViewById(R.id.recyclerView);

        usersList = new ArrayList<>();
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        adapter = new GroupMembersAdapter(usersList, this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        sharedPreferenceManager = new SharedPreferenceManager(this);

        String groupId = sharedPreferenceManager.getGroupId();

        if (groupId != null) {
            retrieveGroupMembers(groupId);
        }

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void retrieveGroupMembers(String groupId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Query the groups collection to get the member list for the provided groupId
        db.collection("groups")
                .document(groupId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            List<String> memberIds = (List<String>) documentSnapshot.get("members");
                            if (memberIds != null && !memberIds.isEmpty()) {
                                // Retrieve users from the Users collection based on the memberIds
                                for (String memberId : memberIds) {
                                    db.collection("Users")
                                            .whereEqualTo("userId", memberId)
                                            .get()
                                            .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                                @Override
                                                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                                    for (QueryDocumentSnapshot userSnapshot : queryDocumentSnapshots) {
                                                        Users user = userSnapshot.toObject(Users.class);
                                                        usersList.add(user);
                                                    }
                                                    adapter.notifyDataSetChanged();
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Log.e("GroupMembers", "Failed to retrieve user: " + e.getMessage());
                                                }
                                            });
                                }
                            }
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("GroupMembers", "Failed to retrieve group members: " + e.getMessage());
                    }
                });
    }
}
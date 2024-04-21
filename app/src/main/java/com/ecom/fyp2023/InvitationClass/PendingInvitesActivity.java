package com.ecom.fyp2023.InvitationClass;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;

import com.ecom.fyp2023.HomeScreen;
import com.ecom.fyp2023.ModelClasses.Invitation;
import com.ecom.fyp2023.R;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
public class PendingInvitesActivity extends AppCompatActivity {

    RecyclerView pendingInvites;
    PendingInvitesAdapter adapter;
    List<Invitation> invitationList;

    String groupId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pending_invites);

        pendingInvites = findViewById(R.id.recyclerView);
        invitationList = new ArrayList<>();
        adapter = new PendingInvitesAdapter(invitationList, this);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        pendingInvites.setLayoutManager(layoutManager);
        pendingInvites.setAdapter(adapter);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        // Retrieve the groupId from the notification intent
        groupId = getIntent().getStringExtra("groupId");
        adapter.setPendingInviteGroupId(groupId);

        // Retrieve pending invites for the user
        retrievePendingInvites();
    }


    private void retrievePendingInvites() {
        // Initialize Firestore
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Get the current user's authentication ID
        String userAuthId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Query the Users collection to get all document IDs
        db.collection("Users")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<String> userDocIds = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        // Add each document ID to the list
                        String currentUser = document.getString("userId");

                        assert currentUser != null;
                        if(currentUser.equals(userAuthId)){
                            userDocIds.add(document.getId());
                        }
                    }
                    // Now query the invitations collection for invites received by the user
                    queryInvitationsForUser(userDocIds);
                })
                .addOnFailureListener(e -> {
                    // Handle any errors
                    Log.e("PendingInvites", "Failed to retrieve user document IDs: " + e.getMessage());
                });
    }

    private void queryInvitationsForUser(@NonNull List<String> userDocIds) {
        // Initialize Firestore
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Clear the existing invitation list
        invitationList.clear();

        // Create a query to listen for changes in the invitations collection
        for (String userDocId : userDocIds) {
            db.collection("invitations")
                    .whereEqualTo("userId", userDocId)
                    .addSnapshotListener((queryDocumentSnapshots, e) -> {
                        if (e != null) {
                            // Handle any errors
                            Log.e("PendingInvites", "Failed to retrieve pending invites: " + e.getMessage());
                            return;
                        }

                        // Clear the existing invitation list
                        invitationList.clear();

                        // Iterate through the documents and add non-rejected invitations to the list
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            // Convert each document to an Invitation object
                            Invitation invitation = document.toObject(Invitation.class);
                            // Check if the invitation status is "Rejected"
                            if (!invitation.getStatus().equals("Rejected")) {
                                // Add the invitation to the list
                                invitationList.add(invitation);
                            }
                        }
                        // Notify the adapter that the data set has changed
                        adapter.notifyDataSetChanged();
                    });
        }
    }


//    private void queryInvitationsForUser(@NonNull List<String> userDocIds) {
//        // Initialize Firestore
//        FirebaseFirestore db = FirebaseFirestore.getInstance();
//
//        // Initialize list to store invitations
//        invitationList.clear();
//
//        // Query the invitations collection for invites received by the user
//        for (String userDocId : userDocIds) {
//            db.collection("invitations")
//                    .whereEqualTo("userId", userDocId)
//                    .get()
//                    .addOnSuccessListener(queryDocumentSnapshots -> {
//                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
//                            // Convert each document to an Invitation object
//                            Invitation invitation = document.toObject(Invitation.class);
//                            invitationList.add(invitation);
//                        }
//                        // Notify the adapter that the data set has changed
//                        adapter.notifyDataSetChanged();
//                    })
//                    .addOnFailureListener(e -> {
//                        // Handle any errors
//                        Log.e("PendingInvites", "Failed to retrieve pending invites: " + e.getMessage());
//                    });
//        }
//    }

}
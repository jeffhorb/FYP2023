package com.ecom.fyp2023.TeamManagementClasses;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.ecom.fyp2023.AppManagers.SharedPreferenceManager;
import com.ecom.fyp2023.ModelClasses.Invitation;
import com.ecom.fyp2023.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class PendingGroupInvitations extends AppCompatActivity {

    RecyclerView recyclerView;
    PendingGroupInvitesAdapter adapter;

    List<Invitation> invitationList;

    SharedPreferenceManager sharedPreferenceManager;


    TextView noInvites;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pending_group_invitations);

        recyclerView = findViewById(R.id.recyclerView);

        invitationList = new ArrayList<>();
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        adapter = new PendingGroupInvitesAdapter(invitationList, this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        sharedPreferenceManager = new SharedPreferenceManager(this);

        String groupId = sharedPreferenceManager.getGroupId();

        noInvites = findViewById(R.id.noInvites);

        if (groupId != null) {
            retrieveInvites(groupId);
        }

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void retrieveInvites(String groupId) {

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        // Query invitations where groupId is equal to the provided groupId
        db.collection("invitations")
                .whereEqualTo("groupId", groupId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        invitationList.clear();
                        for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                            Invitation invitation = documentSnapshot.toObject(Invitation.class);
                            invitationList.add(invitation);
                        }
                        adapter.notifyDataSetChanged();

                        // Set visibility of views based on invitationList size
                        if (invitationList.isEmpty()) {
                            recyclerView.setVisibility(View.GONE);
                            noInvites.setVisibility(View.VISIBLE);
                        } else {
                            recyclerView.setVisibility(View.VISIBLE);
                            noInvites.setVisibility(View.GONE);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(PendingGroupInvitations.this, "Failed to retrieve invitations: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}

//    private void retrieveInvites(String groupId) {
//
//        FirebaseFirestore db = FirebaseFirestore.getInstance();
//        // Query invitations where groupId is equal to the provided groupId
//        db.collection("invitations")
//                .whereEqualTo("groupId", groupId)
//                .get()
//                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
//                    @Override
//                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
//                        invitationList.clear();
//                        for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
//                            Invitation invitation = documentSnapshot.toObject(Invitation.class);
//                            invitationList.add(invitation);
//                        }
//                        adapter.notifyDataSetChanged();
//                    }
//                })
//                .addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//                        Toast.makeText(PendingGroupInvitations.this, "Failed to retrieve invitations: " + e.getMessage(), Toast.LENGTH_SHORT).show();
//                    }
//                });
//    }



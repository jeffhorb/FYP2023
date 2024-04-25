package com.ecom.fyp2023.InvitationClass;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.ecom.fyp2023.Analysis.TeamMemberEvaluation;
import com.ecom.fyp2023.AppManagers.FirestoreManager;
import com.ecom.fyp2023.AppManagers.SharedPreferenceManager;
import com.ecom.fyp2023.ModelClasses.Projects;
import com.ecom.fyp2023.ModelClasses.Users;
import com.ecom.fyp2023.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class GroupMembersAdapter extends RecyclerView.Adapter<GroupMembersAdapter.ViewHolder> {

    private List<Users> userList;
    private List<String> adminList; // List to store admin userIds
    private Context context;
    SharedPreferenceManager sharedPreferenceManager;

    String userName;

    String groupId;

    public GroupMembersAdapter(List<Users> userList, List<String> adminList, Context context) {
        this.userList = userList;
        this.adminList = adminList; // Initialize the adminList
        this.context = context;
        sharedPreferenceManager = new SharedPreferenceManager(context);
        groupId = sharedPreferenceManager.getGroupId();
    }

    public void updateAdminList(List<String> adminList) {
        this.adminList = adminList;
        notifyDataSetChanged(); // Notify the adapter to refresh the views
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.group_member_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Users user = userList.get(position);
        holder.userNameTextView.setText(user.getUserName());
        holder.userMailTextView.setText(user.getUserEmail());
        holder.roleTextView.setText(user.getOccupation());


        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String currentUserUid = currentUser.getUid();

//        // Check if user is an admin
//        if (adminList.contains(user.getUserId())) {
//            holder.adminStatus.setVisibility(View.VISIBLE);
//            holder.adminStatus.setText("Admin");
//            holder.makeAdmin.setVisibility(View.GONE); // Hide makeAdmin button if the user is already an admin
//            holder.unMakeAdmin.setVisibility(View.GONE); // Show unMakeAdmin button
//        } else {
//            holder.makeAdmin.setVisibility(View.VISIBLE); // Show makeAdmin button
//            ///holder.unMakeAdmin.setVisibility(View.GONE); // Hide unMakeAdmin button
//            holder.removeButton.setVisibility(View.GONE);
//        }

        boolean isAdmin = adminList.contains(user.getUserId());

        // Set admin status text
        if (isAdmin) {
            holder.adminStatus.setVisibility(View.VISIBLE);
            holder.adminStatus.setText("Admin");
        } else {
            holder.adminStatus.setVisibility(View.GONE); // Hide admin status for non-admin users
        }

        // Show/hide remove button based on user's admin status and whether it's the current user
        if (isAdmin || currentUserUid.equals(user.getUserId())) {
            holder.removeButton.setVisibility(View.VISIBLE);
        } else {
            holder.removeButton.setVisibility(View.GONE);
        }

        // Show/hide makeAdmin and unMakeAdmin buttons based on whether the current user is an admin
        if (isAdmin) {
            holder.makeAdmin.setVisibility(View.VISIBLE);
            holder.unMakeAdmin.setVisibility(View.VISIBLE);
        } else {
            holder.makeAdmin.setVisibility(View.GONE);
            holder.unMakeAdmin.setVisibility(View.GONE);
        }


//        if(currentUserUid.equals(user.getUserId())){
//            holder.removeButton.setVisibility(View.GONE);
//        }
        holder.removeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Confirmation");
                builder.setMessage("Are you sure you want to remove this user from the group?");
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        groupId = sharedPreferenceManager.getGroupId();
                        String userAuthId = user.getUserId();
                        removeMember(groupId, userAuthId);
                        userList.remove(holder.getAdapterPosition());
                        notifyDataSetChanged();
                    }
                });
                builder.setNegativeButton("No", null);
                builder.show();
            }
        });

        holder.makeAdmin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String selectedUserId = userList.get(holder.getAdapterPosition()).getUserId();

                String groupId = sharedPreferenceManager.getGroupId();

                // Update the "admins" field in the Firestore database
                addAdminToGroup(groupId, selectedUserId);

                holder.unMakeAdmin.setVisibility(View.VISIBLE);
                holder.makeAdmin.setVisibility(View.GONE);
            }
        });

        holder.unMakeAdmin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String selectedUserId = userList.get(holder.getAdapterPosition()).getUserId();

                String groupId = sharedPreferenceManager.getGroupId();

                // Update the "admins" field in the Firestore database
                removeAdminFromGroup(groupId, selectedUserId);

                // Optionally, update the UI to reflect the change
                holder.unMakeAdmin.setVisibility(View.GONE);
                holder.makeAdmin.setVisibility(View.VISIBLE);
            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO: move the user evalustion here. find a way to save the evaluation of removed users

                userName = user.getUserName();
                groupId = sharedPreferenceManager.getGroupId();
                FirestoreManager firestoreManager = new FirestoreManager();
                firestoreManager.getDocumentId("Users", "userEmail", user.getUserEmail(), documentId -> {
                    if (documentId != null) {
                      fetchProjectsForGroup(groupId,documentId);
                    }
                });
            }
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView userNameTextView, userMailTextView, roleTextView;
        TextView removeButton,makeAdmin,unMakeAdmin,adminStatus;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            userNameTextView = itemView.findViewById(R.id.userName);
            userMailTextView = itemView.findViewById(R.id.userMail);
            roleTextView = itemView.findViewById(R.id.occupation);
            removeButton = itemView.findViewById(R.id.remove);
            makeAdmin = itemView.findViewById(R.id.makeAdmin);
            unMakeAdmin = itemView.findViewById(R.id.unmakeAdmin);
            adminStatus = itemView.findViewById(R.id.adminStat);

        }
    }

    private void isAdmin(String userId, AdminCheckCallback callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference groupRef = db.collection("groups").document(groupId);
        groupRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                List<String> adminIds = (List<String>) documentSnapshot.get("admins");
                boolean isAdmin = adminIds != null && adminIds.contains(userId);
                callback.onResult(isAdmin);
            } else {
                callback.onResult(false);
            }
        }).addOnFailureListener(e -> {
            Log.e("GroupMembersAdapter", "Failed to fetch admin data: " + e.getMessage());
            callback.onResult(false);
        });
    }

    interface AdminCheckCallback {
        void onResult(boolean isAdmin);
    }

    // Method to remove a user from the group
    private void removeMember(String groupId, String userAuthId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference groupRef = db.collection("groups").document(groupId);

        groupRef.update("members", FieldValue.arrayRemove(userAuthId))
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(context, "User removed from the group", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context, "Failed to remove user from the group: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    //added selected user as an admin
    private void addAdminToGroup(String groupId, String userId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference groupRef = db.collection("groups").document(groupId);

        // Update the "admins" field by adding the new user ID
        groupRef.update("admins", FieldValue.arrayUnion(userId))
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(context, "User added as admin", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context, "Failed to add user as admin: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void removeAdminFromGroup(String groupId, String userId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference groupRef = db.collection("groups").document(groupId);

        // Update the "admins" field by removing the user ID
        groupRef.update("admins", FieldValue.arrayRemove(userId))
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(context, "User removed from admins", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context, "Failed to remove user from admins: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Method to fetch projects for a group
    private void fetchProjectsForGroup(String groupId,String userId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference projectsRef = db.collection("Projects");

        Query query = projectsRef.whereEqualTo("groupId", groupId);
        query.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                List<Projects> projects = new ArrayList<>();
                for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                    Projects project = documentSnapshot.toObject(Projects.class);

                    String projectId = documentSnapshot.getId(); // Get the document ID
                    project.setProjectId(projectId);
                    projects.add(project);
                }
                showProjectsDialog(projects,userId);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context, "Failed to fetch projects for group: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showProjectsDialog(@NonNull List<Projects> projects, String userId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Projects");

        if (projects.isEmpty()) {
            builder.setMessage("No projects found for this group.");
        } else {
            // Inflate custom layout for each project
            for (int i = 0; i < projects.size(); i++) {
                Projects project = projects.get(i);
                View customView = LayoutInflater.from(context).inflate(R.layout.custom_project_item_dialog, null);

                TextView projectNameTextView = customView.findViewById(R.id.projectNameTextView);
                TextView descriptionTextView = customView.findViewById(R.id.descriptionTextView);
                TextView progressTextView = customView.findViewById(R.id.progressTextView);

                projectNameTextView.setText(project.getTitle());
                descriptionTextView.setText("Description: " + project.getDescription());
                progressTextView.setText("Progress: " + project.getProgress());

                // Add custom view to dialog
                builder.setView(customView);

                // Set click listener for each project
                customView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Handle click event
                        String projectId = project.getProjectId();

                        // Create an Intent
                        Intent intent = new Intent(context, TeamMemberEvaluation.class);
                        intent.putExtra("userID", userId);
                        intent.putExtra("userName", userName);
                        intent.putExtra("projID", projectId);
                        context.startActivity(intent);
                    }
                });
            }
        }

        builder.setPositiveButton("OK", null);
        builder.show();
    }

}


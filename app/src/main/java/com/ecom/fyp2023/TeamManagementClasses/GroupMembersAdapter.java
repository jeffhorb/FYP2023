package com.ecom.fyp2023.TeamManagementClasses;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
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
import com.google.firebase.firestore.DocumentSnapshot;
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
        boolean isAdmin = adminList.contains(currentUserUid);
        boolean isAdmins = adminList.contains(user.getUserId());

        //affects admins only
        if(isAdmins){
            holder.adminStatus.setVisibility(View.VISIBLE);
            holder.adminStatus.setText("ADMIN");
            holder.makeAdmin.setVisibility(View.GONE);
            holder.unMakeAdmin.setVisibility(View.VISIBLE);
        }
        //affects all users w
        else if (isAdmin) {
            holder.makeAdmin.setVisibility(View.VISIBLE);
            holder.removeButton.setVisibility(View.VISIBLE);

        } else {
            holder.adminStatus.setVisibility(View.GONE);
            holder.unMakeAdmin.setVisibility(View.GONE);
            holder.removeButton.setVisibility(View.GONE);
        }

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

                    holder.adminStatus.setVisibility(View.VISIBLE);
                    holder.makeAdmin.setVisibility(View.GONE);
                    holder.unMakeAdmin.setVisibility(View.VISIBLE);

                    notifyDataSetChanged();

                    }
            });

        holder.unMakeAdmin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String selectedUserId = user.getUserId();
                String groupId = sharedPreferenceManager.getGroupId();

                // Get the current user's ID
                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                if (currentUser != null) {
                    String currentUserId = currentUser.getUid();

                    // Check admin access using AdminChecker
                    AdminChecker.checkIfAdmin(new AdminChecker.AdminCheckCallback() {
                        @Override
                        public void onResult(boolean isAdmin) {
                            if (isAdmin) {
                                // Current user is admin, proceed with the action
                                countAdmins(groupId, new CountAdminsCallback() {
                                    @Override
                                    public void onCounted(int adminCount) {
                                        if (adminCount > 1) {
                                            // More than one admin, allow removal
                                            removeAdminFromGroup(groupId, selectedUserId);
                                            holder.unMakeAdmin.setVisibility(View.GONE);
                                            holder.makeAdmin.setVisibility(View.VISIBLE);
                                            holder.adminStatus.setVisibility(View.GONE);
                                            holder.removeButton.setVisibility(View.VISIBLE);
                                        } else {
                                            // Show dialog indicating that there must be at least one admin
                                            showLastAdminDialog();
                                        }
                                    }
                                });
                            } else {
                                // Current user is not admin, show restricted access dialog
                                showRestrictActionDialog();
                            }
                        }
                    }, currentUserId, groupId);
                }
            }
        });

        //affects admins only
        if(isAdmins){
            holder.adminStatus.setVisibility(View.VISIBLE);
            holder.adminStatus.setText("ADMIN");
            holder.makeAdmin.setVisibility(View.GONE);
            holder.unMakeAdmin.setVisibility(View.VISIBLE);
        }
        //affects all users w
        else if (isAdmin) {
            holder.makeAdmin.setVisibility(View.VISIBLE);
            holder.removeButton.setVisibility(View.VISIBLE);

        } else {
            holder.adminStatus.setVisibility(View.GONE);
            holder.unMakeAdmin.setVisibility(View.GONE);
            holder.removeButton.setVisibility(View.GONE);
        }



//        holder.unMakeAdmin.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (isAdmin) {
//                    String selectedUserId = user.getUserId();
//                    String groupId = sharedPreferenceManager.getGroupId();
//
//                    countAdmins(groupId, new CountAdminsCallback() {
//                        @Override
//                        public void onCounted(int adminCount) {
//                            if (adminCount > 1) {
//                                // More than one admin, allow removal
//                                removeAdminFromGroup(groupId, selectedUserId);
//                                holder.unMakeAdmin.setVisibility(View.GONE);
//                                holder.makeAdmin.setVisibility(View.VISIBLE);
//                                holder.adminStatus.setVisibility(View.GONE);
//                                holder.removeButton.setVisibility(View.VISIBLE);
//                            } else {
//                                // Show dialog indicating that there must be at least one admin
//                                showLastAdminDialog();
//                            }
//                        }
//                    });
//                } else {
//                    showRestrictActionDialog();
//                }
//            }
//        });


        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userName = user.getUserName();
                groupId = sharedPreferenceManager.getGroupId();
                FirestoreManager firestoreManager = new FirestoreManager();
                firestoreManager.getDocumentId("Users", "userEmail", user.getUserEmail(), documentId -> {
                    if (documentId != null) {
                      fetchProjectsForGroup(groupId,documentId,userName);
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

    private void showRestrictActionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Restricted Action");
        builder.setMessage("This action can only be performed by an admin.");
        builder.setPositiveButton("OK", null);
        builder.show();
    }

    // Method to show dialog indicating that there must be at least one admin
    private void showLastAdminDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Cannot Remove Last Admin");
        builder.setMessage("Team must have at least one Admin.");
        builder.setPositiveButton("OK", null);
        builder.show();
    }

    // Method to count the number of admins in the group make sure admin lsit is not empty given
    private void countAdmins(String groupId, CountAdminsCallback callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference groupRef = db.collection("groups").document(groupId);

        groupRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    List<String> admins = (List<String>) documentSnapshot.get("admins");
                    if (admins != null) {
                        int adminCount = admins.size();
                        callback.onCounted(adminCount);
                    } else {
                        // Admins field is empty
                        callback.onCounted(0);
                    }
                } else {
                    // Document does not exist
                    callback.onCounted(0);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // Error occurred while fetching document
                callback.onCounted(0);
            }
        });
    }

    // Callback interface for countAdmins method
    interface CountAdminsCallback {
        void onCounted(int adminCount);
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
    private void fetchProjectsForGroup(String groupId,String userId, String userName) {
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
                showProjectsDialog(projects,userId, userName);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context, "Failed to fetch projects for group: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

//    private void showProjectsDialog(@NonNull List<Projects> projects, String userId) {
//        AlertDialog.Builder builder = new AlertDialog.Builder(context);
//        builder.setTitle("PROJECT LIST");
//
//        // Create a LinearLayout to hold the custom views
//        LinearLayout layout = new LinearLayout(context);
//        layout.setOrientation(LinearLayout.VERTICAL);
//
//        for (Projects project : projects) {
//            View customView = LayoutInflater.from(context).inflate(R.layout.custom_project_item_dialog, null);
//            TextView projectNameTextView = customView.findViewById(R.id.projectNameTextView);
//            TextView descriptionTextView = customView.findViewById(R.id.descriptionTextView);
//            TextView progressTextView = customView.findViewById(R.id.progressTextView);
//            projectNameTextView.setText(project.getTitle());
//            descriptionTextView.setText(project.getDescription());
//            progressTextView.setText(project.getProgress());
//
//            // Set the click listener for each custom view
//            customView.setOnClickListener(v -> navigateToTeamMemberEvaluation(project, userId));
//
//            // Add the custom view to the layout
//            layout.addView(customView);
//        }
//
//        // If there are no projects, show a message
//        if (projects.isEmpty()) {
//            builder.setMessage("No projects found for this group.");
//        } else {
//            // Set the custom layout as the dialog view
//            builder.setView(layout);
//        }
//
//        builder.setPositiveButton("OK", null);
//        builder.show();
//    }


    private void showProjectsDialog(@NonNull List<Projects> projects, String userId, String userName ) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("PROJECT LIST");

        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_custom_layout, null);
        RecyclerView recyclerView = dialogView.findViewById(R.id.projectsRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        GroupMemberProjectAdapter adapter = new GroupMemberProjectAdapter(context, projects, userId, userName);
        recyclerView.setAdapter(adapter);

        builder.setView(dialogView);
        builder.setPositiveButton("OK", null);
        builder.show();
    }

//    private void navigateToTeamMemberEvaluation(@NonNull Projects project, String userId) {
//        String projectId = project.getProjectId();
//        Intent intent = new Intent(context, TeamMemberEvaluation.class);
//        intent.putExtra("userID", userId);
//        intent.putExtra("userName", userName);
//        intent.putExtra("projID", projectId);
//        context.startActivity(intent);
//    }


//    private void showProjectsDialog(@NonNull List<Projects> projects, String userId) {
//        AlertDialog.Builder builder = new AlertDialog.Builder(context);
//        builder.setTitle("PROJECT LIST");
//        CharSequence[] projectTitles = new CharSequence[projects.size()];
//        for (int i = 0; i < projects.size(); i++) {
//            projectTitles[i] = projects.get(i).getTitle();
//        }
//        builder.setItems(projectTitles, new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                Projects selectedProject = projects.get(which);
//                navigateToTeamMemberEvaluation(selectedProject, userId);
//            }
//        });
//        builder.setPositiveButton("OK", null);
//        builder.show();
//    }
//
//    private void navigateToTeamMemberEvaluation(Projects project, String userId) {
//        String projectId = project.getProjectId();
//        Intent intent = new Intent(context, TeamMemberEvaluation.class);
//        intent.putExtra("userID", userId);
//        intent.putExtra("userName", userName);
//        intent.putExtra("projID", projectId);
//        context.startActivity(intent);
//    }


//    private void showProjectsDialog(@NonNull List<Projects> projects, String userId) {
//        AlertDialog.Builder builder = new AlertDialog.Builder(context);
//        builder.setTitle("PROJECT LIST");
//
//        LinearLayout layout = new LinearLayout(context);
//        layout.setOrientation(LinearLayout.VERTICAL);
//
//        for (int i = 0; i < projects.size(); i++) {
//            final Projects project = projects.get(i);
//            View customView = LayoutInflater.from(context).inflate(R.layout.custom_project_item_dialog, null);
//            TextView projectNameTextView = customView.findViewById(R.id.projectNameTextView);
//            TextView descriptionTextView = customView.findViewById(R.id.descriptionTextView);
//            TextView progressTextView = customView.findViewById(R.id.progressTextView);
//            projectNameTextView.setText(project.getTitle());
//            descriptionTextView.setText(project.getDescription());
//            progressTextView.setText(project.getProgress());
//
//            customView.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    String projectId = project.getProjectId();
//                    Intent intent = new Intent(context, TeamMemberEvaluation.class);
//                    intent.putExtra("userID", userId);
//                    intent.putExtra("userName", userName);
//                    intent.putExtra("projID", projectId);
//                    context.startActivity(intent);
//                }
//            });
//
//            layout.addView(customView);
//        }
//
//        builder.setView(layout);
//
//    //}
//        builder.setPositiveButton("OK", null);
//        builder.show();
//    }
//
}


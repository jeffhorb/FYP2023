package com.ecom.fyp2023.Adapters;

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
import com.ecom.fyp2023.Fragments.UsersListFragment;
import com.ecom.fyp2023.ModelClasses.Invitation;
import com.ecom.fyp2023.ModelClasses.Users;
import com.ecom.fyp2023.R;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.auth.User;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserRVAdapter extends RecyclerView.Adapter<UserRVAdapter.ViewHolder> {

    private final List<Users> userList;
    private final Context context;


    private final UsersListFragment fragment;
    private String taskId,projectId,groupId,groupName,groupDescription;

    public void setSelectedTaskId(String selectedTaskId) {
        this.taskId = selectedTaskId;
    }

    public void setSelectedProjectId(String proId) {
        this.projectId = proId;
    }

    public UserRVAdapter(List<Users> userList, Context context, UsersListFragment fragment) {
        this.userList = userList;
        this.context = context;
        this.fragment = fragment;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public void setGroupDescription(String groupDescription) {
        this.groupDescription = groupDescription;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Users user = userList.get(position);
        holder.userEmail.setText(user.getUserEmail());
        holder.username.setText(user.getUserName());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle the item click event, you might want to open a detailed view or perform some action
                FirestoreManager firestoreManager = new FirestoreManager();
                firestoreManager.getDocumentId("Users", "userEmail", user.getUserEmail(), documentId -> {
                    if (documentId != null) {
                        if (taskId != null){
                            AssignUserTask(documentId,taskId);
                            fragment.dismiss();

                        }else if(groupId != null&& groupName != null) {

                            //SendInvitationToUser(documentId,groupId,groupName,groupDescription, user.getUserName(), user.getUserEmail());
                            showConfirmationDialog(user);

                        }else {
                                // Create an Intent
                                Intent intent = new Intent(context, TeamMemberEvaluation.class);
                                intent.putExtra("userID", documentId);
                                intent.putExtra("userName", user.getUserName());
                                intent.putExtra("projID",projectId);
                                context.startActivity(intent);
                                fragment.dismiss();


                        }
                    }  // Handle the case where the document ID couldn't be retrieved
                });

            }
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView username,userEmail;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            username = itemView.findViewById(R.id.userName);
            userEmail = itemView.findViewById(R.id.userEmail);
        }
    }

    //show confirmation dialog before sending invitation
    private void showConfirmationDialog(@NonNull Users user) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Send Invitation");
        builder.setMessage("Are you sure you want to send an invitation to " + user.getUserName() + "?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Send invitation
                FirestoreManager firestoreManager = new FirestoreManager();
                firestoreManager.getDocumentId("Users", "userEmail", user.getUserEmail(), documentId -> {
                    if (documentId != null) {
                        SendInvitationToUser(documentId, groupId, groupName, groupDescription, user.getUserName(), user.getUserEmail());
                        fragment.dismiss();

                    }
                });
            }
        });
        builder.setNegativeButton("No", null);
        builder.show();
    }

    public void SendInvitationToUser(String userId,String groupId,String groupName,String groupDescription,String userName,String userEmail) {

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Get the current timestamp
        Date timestamp = new Date(System.currentTimeMillis());

        Invitation invitation = new Invitation(groupId, userId, timestamp,groupName,groupDescription,"Pending",userName,userEmail);

        // Add the invitation to Firestore
        db.collection("invitations")
                .add(invitation)
                .addOnSuccessListener(documentReference -> {
                    // Invitation added successfully
                    Toast.makeText(context, "Invitation sent to user!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    // Failed to add invitation
                    Toast.makeText(context, "Failed to send invitation: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }


    //assign user to task
    private void AssignUserTask(String userId, String taskId) {
        FirebaseFirestore fb = FirebaseFirestore.getInstance();

        // Check if the task is already assigned to a user
        fb.collection("userTasks")
                .whereEqualTo("taskId", taskId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (task.getResult() != null && !task.getResult().isEmpty()) {
                            Toast.makeText(context, "Task is already assigned to a user", Toast.LENGTH_SHORT).show();
                        } else {
                            Map<String, Object> userTasks = new HashMap<>();
                            userTasks.put("userId", userId);
                            userTasks.put("taskId", taskId);

                            fb.collection("userTasks").add(userTasks).addOnCompleteListener(addTaskToUserTask -> {
                                if (addTaskToUserTask.isSuccessful()) {
                                    Toast.makeText(context, "Task Assigned to user", Toast.LENGTH_SHORT).show();
                                } else {
                                    Log.e("userTasks", "Error adding userTask", addTaskToUserTask.getException());
                                }
                            });
                        }
                    } else {
                        Log.e("userTasks", "Error checking if task is already assigned", task.getException());
                    }
                });
    }
}


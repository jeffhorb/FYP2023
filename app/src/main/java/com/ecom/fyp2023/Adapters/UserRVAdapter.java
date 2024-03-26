package com.ecom.fyp2023.Adapters;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ecom.fyp2023.Analysis.TeamMemberEvaluation;
import com.ecom.fyp2023.AppManagers.FirestoreManager;
import com.ecom.fyp2023.ModelClasses.Users;
import com.ecom.fyp2023.R;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserRVAdapter extends RecyclerView.Adapter<UserRVAdapter.ViewHolder> {

    private final List<Users> userList;
    private final Context context;

    private String taskId,projectId;

    public void setSelectedTaskId(String selectedTaskId) {
        this.taskId = selectedTaskId;
    }

    public void setSelectedProjectId(String proId) {
        this.projectId = proId;
    }

    public UserRVAdapter(List<Users> userList, Context context) {
        this.userList = userList;
        this.context = context;
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
                        }else {
                            // Create an Intent
                            Intent intent = new Intent(context, TeamMemberEvaluation.class);
                            intent.putExtra("userID", documentId);
                            intent.putExtra("userName", user.getUserName());
                            intent.putExtra("projID",projectId);
                            context.startActivity(intent);
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


package com.ecom.fyp2023.InvitationClass;

import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.ecom.fyp2023.AppManagers.SharedPreferenceManager;
import com.ecom.fyp2023.ModelClasses.Users;
import com.ecom.fyp2023.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class GroupMembersAdapter extends RecyclerView.Adapter<GroupMembersAdapter.ViewHolder> {

    private List<Users> userList;
    private Context context;
    SharedPreferenceManager sharedPreferenceManager;

    public GroupMembersAdapter(List<Users> userList, Context context) {
        this.userList = userList;
        this.context = context;
        sharedPreferenceManager = new SharedPreferenceManager(context);
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
        holder.roleTextView.setText(user.getRole());
        // Set click listener for remove button if needed

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String currentUserUid = currentUser.getUid();

        if(currentUserUid.equals(user.getUserId())){
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
                        String groupId = sharedPreferenceManager.getGroupId();
                        String userAuthId = user.getUserId();
                        removeMember(groupId, userAuthId);
                        userList.remove(holder.getAdapterPosition());
                        notifyDataSetChanged(); // Notify adapter about the change
                    }
                });
                builder.setNegativeButton("No", null);
                builder.show();
            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO: move the user evalustion here. find a way to save the evaluation of removed users
            }
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView userNameTextView, userMailTextView, roleTextView;
        Button removeButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            userNameTextView = itemView.findViewById(R.id.userName);
            userMailTextView = itemView.findViewById(R.id.userMail);
            roleTextView = itemView.findViewById(R.id.role);
            removeButton = itemView.findViewById(R.id.remove);
            // Set click listener for remove button if needed
        }
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
}


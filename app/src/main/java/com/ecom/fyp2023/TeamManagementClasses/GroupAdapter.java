package com.ecom.fyp2023.TeamManagementClasses;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ecom.fyp2023.AppManagers.FirestoreManager;
import com.ecom.fyp2023.AppManagers.SharedPreferenceManager;
import com.ecom.fyp2023.Fragments.BottomSheetFragmentAddTask;
import com.ecom.fyp2023.HomeScreen;
import com.ecom.fyp2023.ModelClasses.Group;
import com.ecom.fyp2023.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class GroupAdapter extends RecyclerView.Adapter<GroupAdapter.GroupViewHolder> {

    private Context context;
    private List<Group> groupList;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    SharedPreferenceManager sharedPreferenceManager;

    public GroupAdapter(Context context, List<Group> groupList) {
        this.context = context;
        this.groupList = groupList;
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        sharedPreferenceManager = new SharedPreferenceManager(context);
    }

    @NonNull
    @Override
    public GroupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_group, parent, false);
        return new GroupViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final GroupViewHolder holder, int position) {
        final Group group = groupList.get(position);
        holder.groupNameTextView.setText(group.getGroupName());
        holder.groupDescriptionTextView.setText(group.getDescription());


        holder.leaveGroupTextview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(context);
                builder.setTitle("Confirmation");
                builder.setMessage("Are you sure you want to Leave this group?");
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        FirestoreManager firestoreManager = new FirestoreManager();
                        firestoreManager.getDocumentId("groups", "groupName", group.getGroupName(), documentId -> {
                            if (documentId != null) {
                                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                                String currentUserUid = currentUser.getUid();
                                removeMember(documentId, currentUserUid);
                                groupList.remove(holder.getAdapterPosition());

                            }
                        });

                        notifyDataSetChanged();
                    }
                });
                builder.setNegativeButton("No", null);
                builder.show();
            }
        });
        holder.showMembersButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirestoreManager firestoreManager = new FirestoreManager();
                firestoreManager.getDocumentId("groups", "groupName", group.getGroupName(), documentId -> {
                    if (documentId != null) {

                        showGroupMembers(documentId);

                    }
                });

            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                sharedPreferenceManager.clearSavedIds();

                FirestoreManager firestoreManager = new FirestoreManager();
                firestoreManager.getDocumentId("groups", "groupName", group.getGroupName(), documentId -> {
                    if (documentId != null) {

                        sharedPreferenceManager.saveGroupId(documentId);
                        sharedPreferenceManager.saveGroupName(group.getGroupName());
                        sharedPreferenceManager.saveGroupDescription(group.getDescription());
                        Intent intent = new Intent(context, HomeScreen.class);
                        context.startActivity(intent);

                    }
                });

            }
        });
    }

    @Override
    public int getItemCount() {
        return groupList.size();
    }

    static class GroupViewHolder extends RecyclerView.ViewHolder {
        TextView groupNameTextView;
        TextView groupDescriptionTextView;
        TextView showMembersButton;

        TextView leaveGroupTextview;

        GroupViewHolder(@NonNull View itemView) {
            super(itemView);
            groupNameTextView = itemView.findViewById(R.id.text_view_group_name);
            groupDescriptionTextView = itemView.findViewById(R.id.text_view_group_description);
            showMembersButton = itemView.findViewById(R.id.button_show_members);
            leaveGroupTextview = itemView.findViewById(R.id.leaveGroup);
        }
    }

    private void showGroupMembers(String groupId) {
        DocumentReference groupRef = db.collection("groups").document(groupId);

        groupRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot groupDoc = task.getResult();
                    if (groupDoc.exists()) {
                        List<String> memberIds = (List<String>) groupDoc.get("members");
                        if (memberIds != null && !memberIds.isEmpty()) {
                            StringBuilder membersBuilder = new StringBuilder();
                            AtomicInteger membersFetched = new AtomicInteger(0); // Counter for fetched members

                            // Query users collection to retrieve user names
                            for (String memberId : memberIds) {
                                db.collection("Users")
                                        .whereEqualTo("userId", memberId)
                                        .get()
                                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                if (task.isSuccessful()) {
                                                    for (DocumentSnapshot userDoc : task.getResult()) {
                                                        String userName = userDoc.getString("userName");
                                                        String userRole = userDoc.getString("occupation");
                                                        membersBuilder.append(userName).append(" (").append(userRole).append(")\n");
                                                    }
                                                }
                                                // Increment the counter
                                                int fetched = membersFetched.incrementAndGet();

                                                // Show dialog only when all members are fetched
                                                if (fetched == memberIds.size() && membersBuilder.length() > 0) {
                                                    showMembersDialog(membersBuilder.toString());
                                                }
                                            }
                                        });
                            }
                        } else {
                            Toast.makeText(context, "No members found", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(context, "Group document does not exist", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(context, "Failed to fetch group details", Toast.LENGTH_SHORT).show();
                }
            }
        });
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


    private void showMembersDialog(String members) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Group Members");
        builder.setMessage(members);
        builder.setPositiveButton("OK", null);
        builder.create().show();
    }
}

package com.ecom.fyp2023.InvitationClass;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ecom.fyp2023.Analysis.TeamMemberEvaluation;
import com.ecom.fyp2023.AppManagers.FirestoreManager;
import com.ecom.fyp2023.AppManagers.SharedPreferenceManager;
import com.ecom.fyp2023.HomeScreen;
import com.ecom.fyp2023.ModelClasses.Group;
import com.ecom.fyp2023.R;
import com.ecom.fyp2023.TaskActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
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


        holder.showMembersButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirestoreManager firestoreManager = new FirestoreManager();
                firestoreManager.getDocumentId("groups", "groupName", group.getGroupName(), documentId -> {
                    if (documentId != null) {

                        showGroupMembers(documentId);

                    }  // Handle the case where the document ID couldn't be retrieved
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

                    }  // Handle the case where the document ID couldn't be retrieved
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

        GroupViewHolder(@NonNull View itemView) {
            super(itemView);
            groupNameTextView = itemView.findViewById(R.id.text_view_group_name);
            groupDescriptionTextView = itemView.findViewById(R.id.text_view_group_description);
            showMembersButton = itemView.findViewById(R.id.button_show_members);
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
                                                        String userRole = userDoc.getString("role");
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


    //    private void showGroupMembers(String groupId) {
//        DocumentReference groupRef = db.collection("groups").document(groupId);
//
//        groupRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
//            @Override
//            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
//                if (task.isSuccessful()) {
//                    DocumentSnapshot groupDoc = task.getResult();
//                    if (groupDoc.exists()) {
//                        List<String> memberIds = (List<String>) groupDoc.get("members");
//                        if (memberIds != null && !memberIds.isEmpty()) {
//                            StringBuilder membersBuilder = new StringBuilder();
//                            // Query users collection to retrieve user names
//                            for (String memberId : memberIds) {
//                                db.collection("Users")
//                                        .whereEqualTo("userId", memberId)
//                                        .get()
//                                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
//                                            @Override
//                                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
//                                                if (task.isSuccessful()) {
//                                                    for (DocumentSnapshot userDoc : task.getResult()) {
//                                                        String userName = userDoc.getString("userName");
//                                                        String userRole = userDoc.getString("role");
//                                                        membersBuilder.append(userName).append(" (").append(userRole).append(")\n");
//                                                    }
//                                                }
//                                                // Show dialog only when all members are fetched
//                                                if (membersBuilder.length() > 0) {
//                                                    showMembersDialog(membersBuilder.toString());
//                                                }
//                                            }
//                                        });
//                            }
//                        } else {
//                            Toast.makeText(context, "No members found", Toast.LENGTH_SHORT).show();
//                        }
//                    } else {
//                        Toast.makeText(context, "Group document does not exist", Toast.LENGTH_SHORT).show();
//                    }
//                } else {
//                    Toast.makeText(context, "Failed to fetch group details", Toast.LENGTH_SHORT).show();
//                }
//            }
//        });
//    }
    private void showMembersDialog(String members) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Group Members");
        builder.setMessage(members);
        builder.setPositiveButton("OK", null);
        builder.create().show();
    }
}

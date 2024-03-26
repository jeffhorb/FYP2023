package com.ecom.fyp2023.Adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ecom.fyp2023.AppManagers.FirestoreManager;
import com.ecom.fyp2023.AppManagers.TimeUtils;
import com.ecom.fyp2023.ModelClasses.Comment;
import com.ecom.fyp2023.ModelClasses.Tasks;
import com.ecom.fyp2023.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CommentRVAdapter extends RecyclerView.Adapter<CommentRVAdapter.ViewHolder> {

    private List<Comment> commentList;

    private final Context context;

    public CommentRVAdapter(List<Comment> commentList, Context context) {
        this.commentList = commentList;
        this.context = context;
    }

    @NonNull
    @Override
    public CommentRVAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.comment_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();


        if (currentUser != null) {
            String currentUserId = currentUser.getUid();

            Comment comment = commentList.get(position);
            holder.userEmail.setText(comment.getUserName());
            holder.comment.setText(comment.getComment());

//            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM", Locale.getDefault());
//            String formattedDate = sdf.format(comment.getTimestamp());
//            holder.timeAdded.setText(formattedDate);
            Date timestamp = comment.getTimestamp(); // Assuming comment.getTimestamp() returns a Date object
            String timeAgo = TimeUtils.getTimeAgo(timestamp);
            holder.timeAdded.setText(timeAgo);


            // Compare the current user's ID with the user ID associated with the comment
            if (comment.getCurrentUserId().equals(currentUserId)) {
                holder.deleteComment.setVisibility(View.VISIBLE); // Show delete button
            } else {
                holder.deleteComment.setVisibility(View.GONE); // Hide delete button
            }

            holder.deleteComment.setOnClickListener(v -> {
                // Call a method to delete the item from Firestore
                removeComment(position);
            });
              /*holder.itemView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                // Handle the item click event, you might want to open a detailed view or perform some action
//            }
//        });*/
        }
    }

    @Override
    public int getItemCount() {
        return commentList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView userEmail, comment,timeAdded;

        ImageView deleteComment;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            userEmail = itemView.findViewById(R.id.commentUserName);
            comment = itemView.findViewById(R.id.commentText);
            deleteComment = itemView.findViewById(R.id.deleteComment);
            timeAdded = itemView.findViewById(R.id.timeAdded);
        }
    }
    public void updateList(@NonNull List<Comment> itemList) {
        this.commentList = itemList;
        notifyDataSetChanged();

    }

    private void removeComment(int position) {

        if (position >= 0 && position < commentList.size()) {
            Comment removedComment = commentList.remove(position);
            notifyItemRemoved(position);


            FirestoreManager firestoreManager = new FirestoreManager();
            firestoreManager.getDocumentId("Comments", "comment", removedComment.getComment(), documentId -> {
                if (documentId != null) {
                    removeItemFromFirestore(documentId);
                    removeCommentFromProjectComment(documentId);
                } else {
                    // Handle the case where the document ID couldn't be retrieved
                    Toast.makeText(context, "Document does not exist", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void removeItemFromFirestore(String documentId) {
        FirebaseFirestore.getInstance().collection("Comments").document(documentId).delete().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(context, "comment deleted", Toast.LENGTH_SHORT).show();

            } else {
                Toast.makeText(context, "Error occurred", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void removeCommentFromProjectComment(String commentId) {
        FirebaseFirestore.getInstance().collection("projectComments")
                .whereEqualTo("commentId", commentId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (DocumentSnapshot document : task.getResult()) {
                            String projectCommentId = document.getId();
                            removeItemFromProjectComments(projectCommentId);
                        }
                    } else {
                        // Handle failure in fetching projectTasks
                        Log.e("RemoveProjectTask", "Error fetching projectTasks", task.getException());
                    }
                });
    }

    private void removeItemFromProjectComments(String projectCommentId) {
        FirebaseFirestore.getInstance().collection("projectComments").document(projectCommentId)
                .delete()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d("removefromprojectCommment", "comment removed from projectComments successfully");
                    } else {
                        Log.e("removefromprojectCommment", "Error removing comment from projectComments", task.getException());
                    }
                });
    }
}

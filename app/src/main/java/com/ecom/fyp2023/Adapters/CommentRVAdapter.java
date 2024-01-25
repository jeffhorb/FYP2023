package com.ecom.fyp2023.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ecom.fyp2023.AppManagers.FirestoreManager;
import com.ecom.fyp2023.ModelClasses.Comment;
import com.ecom.fyp2023.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class CommentRVAdapter extends RecyclerView.Adapter<CommentRVAdapter.ViewHolder> {

    private final List<Comment> commentList;

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
            String userEmail = currentUser.getEmail();
            holder.userEmail.setText(userEmail);
        }

        Comment comment = commentList.get(position);

        holder.comment.setText(comment.getComment());

        holder.deleteComment.setOnClickListener(v -> {
            // Call a method to delete the item from Firestore
            removeComment(holder.getAdapterPosition());
        });

        /*holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle the item click event, you might want to open a detailed view or perform some action
            }
        });*/


       /* //will used to code the done vector asset
        holder.done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Call a method to delete the item from Firestore
                removeProject(holder.getAdapterPosition());
            }
        });*/
    }

    @Override
    public int getItemCount() {
        return commentList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView userEmail, comment;

        ImageView deleteComment;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            userEmail = itemView.findViewById(R.id.commentUserName);
            comment = itemView.findViewById(R.id.commentText);
            deleteComment = itemView.findViewById(R.id.deleteComment);
        }
    }

    private void removeComment(int position) {
        Comment removeComment = commentList.remove(position);
        notifyItemRemoved(position);

        FirestoreManager firestoreManager = new FirestoreManager();
        firestoreManager.getDocumentId("Comments", "comment", removeComment.getComment(), documentId -> {
            if (documentId != null) {
                removeItemFromFirestore(documentId);
            } else {
                // Handle the case where the document ID couldn't be retrieved
                Toast.makeText(context, "Document does not exist", Toast.LENGTH_SHORT).show();
            }
        });
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
}

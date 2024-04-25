package com.ecom.fyp2023.TeamManagementClasses;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.ecom.fyp2023.ModelClasses.Invitation;
import com.ecom.fyp2023.R;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.List;

public class PendingGroupInvitesAdapter extends RecyclerView.Adapter<PendingGroupInvitesAdapter.ViewHolder> {

    private List<Invitation> pendingInvitations;

    Context context;

    public PendingGroupInvitesAdapter(List<Invitation> pendingInvitations, Context context) {
        this.pendingInvitations = pendingInvitations;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.group_pending_invites_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Invitation invitation = pendingInvitations.get(position);
        holder.userNameTextView.setText(invitation.getUserName());
        holder.userMailTextView.setText(invitation.getUserEmail());
        holder.statusTextView.setText(invitation.getStatus());

        holder.rescindButton.setTag(invitation);
        holder.rescindButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Invitation invite = (Invitation) v.getTag();
                showConfirmationDialog(invite);
            }
        });
    }

    @Override
    public int getItemCount() {
        return pendingInvitations.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView userNameTextView, userMailTextView, statusTextView;
        Button rescindButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            userNameTextView = itemView.findViewById(R.id.userName);
            userMailTextView = itemView.findViewById(R.id.userMail);
            statusTextView = itemView.findViewById(R.id.status);
            rescindButton = itemView.findViewById(R.id.RescendInvite);
            // Set click listener for rescind button if needed
        }
    }

    private void showConfirmationDialog(Invitation invitation) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Rescind Invitation");
        builder.setMessage("Are you sure you want to Rescind this invitation?, Invitation Request will be Deleted");
        builder.setPositiveButton("Yes", (dialog, which) -> {
            // User confirmed, proceed to accept invitation
            deleteInvitation(invitation);
        });
        builder.setNegativeButton("No", (dialog, which) -> {
            // User canceled, do nothing
        });
        builder.show();
    }
    private void deleteInvitation(@NonNull Invitation invitationId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Query the invitations collection for the invitation to delete
        db.collection("invitations")
                .whereEqualTo("userId", invitationId.getUserId())
                .whereEqualTo("groupId", invitationId.getGroupId())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        // Delete each invitation document
                        db.collection("invitations").document(document.getId())
                                .delete()
                                .addOnSuccessListener(aVoid -> {
                                    // Invitation deleted successfully
                                    Toast.makeText(context, "Invitation Rescinded", Toast.LENGTH_SHORT).show();
                                    // Remove the invitation from the list
                                    int index = pendingInvitations.indexOf(invitationId);
                                    if (index != -1) {
                                        pendingInvitations.remove(index);
                                        notifyItemRemoved(index);
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    // Handle failure to delete invitation
                                    Toast.makeText(context, "Failed to rescind invitation: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    // Handle failure to query invitations
                    Toast.makeText(context, "Failed to query invitations: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }


//    private void deleteInvitation(@NonNull Invitation invitationId) {
//        FirebaseFirestore db = FirebaseFirestore.getInstance();
//
//        // Query the invitations collection for the invitation to delete
//        db.collection("invitations")
//                .whereEqualTo("userId", invitationId.getUserId())
//                .whereEqualTo("groupId", invitationId.getGroupId())
//                .get()
//                .addOnSuccessListener(queryDocumentSnapshots -> {
//                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
//                        // Delete each invitation document
//                        db.collection("invitations").document(document.getId())
//                                .delete()
//                                .addOnSuccessListener(aVoid -> {
//                                    // Invitation deleted successfully
//                                    Toast.makeText(context, "Invitation Rescinded", Toast.LENGTH_SHORT).show();
//                                })
//                                .addOnFailureListener(e -> {
//                                    // Handle failure to delete invitation
//                                    Toast.makeText(context, "Failed to rescind invitation: " + e.getMessage(), Toast.LENGTH_SHORT).show();
//                                });
//                    }
//                })
//                .addOnFailureListener(e -> {
//                    // Handle failure to query invitations
//                    Toast.makeText(context, "Failed to query invitations: " + e.getMessage(), Toast.LENGTH_SHORT).show();
//                });
//    }

}

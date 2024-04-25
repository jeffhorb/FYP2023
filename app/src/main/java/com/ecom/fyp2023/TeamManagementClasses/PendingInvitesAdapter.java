package com.ecom.fyp2023.TeamManagementClasses;

import android.content.Context;
import android.util.Log;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.List;

public class PendingInvitesAdapter extends RecyclerView.Adapter<PendingInvitesAdapter.ViewHolder> {

    private final List<Invitation> invitationList;
    private final Context context;

    String pendingInvitesGroupId;

    public void setPendingInviteGroupId(String groupId) {

        this.pendingInvitesGroupId = groupId;

    }

    public PendingInvitesAdapter(List<Invitation> invitationList, Context context) {
        this.invitationList = invitationList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.pending_invite_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Invitation invitation = invitationList.get(position);
        holder.groupName.setText(invitation.getGroupName());
        holder.description.setText(invitation.getGroupDescription());

       // holder.acceptButton.setTag(invitation.getGroupId());
        holder.acceptButton.setTag(invitation);
        holder.rejectButton.setTag(invitation);


        holder.acceptButton.setOnClickListener(v -> {
            // Retrieve groupId associated with this invitation
           // String groupId = (String) v.getTag();
            // Show a confirmation dialog
            //showConfirmationDialog(groupId);

            Invitation invite = (Invitation) v.getTag();
            showConfirmationDialog(invite);

        });
        holder.rejectButton.setOnClickListener(v -> {
            Invitation invite = (Invitation) v.getTag();
            showRejectConfirmationDialog(invite);
        });
    }

    @Override
    public int getItemCount() {
        return invitationList.size();
    }



    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView groupName, description;
        Button acceptButton, rejectButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            groupName = itemView.findViewById(R.id.groupNameTextView);
            description = itemView.findViewById(R.id.descriptionId);
            acceptButton = itemView.findViewById(R.id.acceptButton);
            rejectButton = itemView.findViewById(R.id.rejectButton);
        }
    }

    private void showConfirmationDialog(Invitation invitation) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Accept Invitation");
        builder.setMessage("Are you sure you want to accept this invitation and join the group?");
        builder.setPositiveButton("Yes", (dialog, which) -> {
            // User confirmed, proceed to accept invitation
            acceptInvitation(invitation);
        });
        builder.setNegativeButton("No", (dialog, which) -> {
            // User canceled, do nothing
        });
        builder.show();
    }

    private void acceptInvitation(@NonNull Invitation invitation) {
        // Initialize Firestore
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Get the current user's authentication ID
        String userAuthId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Add the user's authentication ID to the group members
        db.collection("groups").document(invitation.getGroupId())
                .update("members", FieldValue.arrayUnion(userAuthId))
                .addOnSuccessListener(aVoid -> {
                    // Member added successfully
                    Toast.makeText(context, "You have joined the group", Toast.LENGTH_LONG).show();

                    // Now delete the invitation document
                    deleteInvitation(invitation);
                })
                .addOnFailureListener(e -> {
                    // Handle failure to add member
                    Toast.makeText(context, "Failed to join the group: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
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
                                    Toast.makeText(context, "Invitation accepted", Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> {
                                    // Handle failure to delete invitation
                                    Toast.makeText(context, "Failed to delete invitation: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    // Handle failure to query invitations
                    Toast.makeText(context, "Failed to query invitations: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void showRejectConfirmationDialog(Invitation invitationId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Reject Invitation");
        builder.setMessage("Are you sure you want to reject this invitation?");
        builder.setPositiveButton("Reject", (dialog, which) -> {
            // Handle reject invitation
            rejectInvitation(invitationId);
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void rejectInvitation(@NonNull Invitation invitation) {
        // Update status field of invitation document to "rejected"
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("invitations").whereEqualTo("groupId", invitation.getGroupId())
                .whereEqualTo("userId", invitation.getUserId())
                .whereEqualTo("status", "Pending") // Assuming status field is "pending" for pending invitations
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String invitationId = document.getId();
                        db.collection("invitations").document(invitationId)
                                .update("status", "Rejected")
                                .addOnSuccessListener(aVoid -> {
                                    // Invitation status updated successfully
                                    // Update UI as needed
                                    Toast.makeText(context, "Invitation rejected", Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> {
                                    // Handle failure
                                    Log.e("RejectInvitation", "Error rejecting invitation: " + e.getMessage());
                                    Toast.makeText(context, "Failed to reject invitation", Toast.LENGTH_SHORT).show();
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    // Handle failure
                    Log.e("RejectInvitation", "Error finding invitation: " + e.getMessage());
                    Toast.makeText(context, "Failed to find invitation", Toast.LENGTH_SHORT).show();
                });
    }
}

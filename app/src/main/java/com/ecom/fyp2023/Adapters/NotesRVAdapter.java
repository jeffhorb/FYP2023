package com.ecom.fyp2023.Adapters;

import android.app.AlertDialog;
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
import com.ecom.fyp2023.ModelClasses.Notes;
import com.ecom.fyp2023.R;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class NotesRVAdapter extends RecyclerView.Adapter<NotesRVAdapter.ViewHolder> {

    private List<Notes> notesList;

    private final Context context;

    public NotesRVAdapter(List<Notes> notesList, Context context) {
        this.notesList = notesList;
        this.context = context;
    }

    @NonNull
    @Override
    public NotesRVAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.note_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        Notes notes = notesList.get(position);
        holder.notes.setText(notes.getNote());

        holder.deleteNotes.setOnClickListener(v -> {
            // Call a method to delete the item from Firestore
            showRemoveConfirmationDialog(position);
        });
    }

    @Override
    public int getItemCount() {
        return notesList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView notes;

        ImageView deleteNotes,updateNotes;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            notes = itemView.findViewById(R.id.notesEditText);
            deleteNotes = itemView.findViewById(R.id.deleteNote);
            updateNotes = itemView.findViewById(R.id.updateNote);
        }
    }
    public void updateList(@NonNull List<Notes> itemList) {
        this.notesList = itemList;
        notifyDataSetChanged();

    }

    private void showRemoveConfirmationDialog(final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage("Are you sure you want to remove this project and its tasks?")
                .setPositiveButton("Yes", (dialogInterface, i) -> removeNotes(position))
                .setNegativeButton("No", (dialogInterface, i) -> {
                });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void removeNotes(int position) {
        Notes removeNotes = notesList.remove(position);
        notifyItemRemoved(position);

        FirestoreManager firestoreManager = new FirestoreManager();
        firestoreManager.getDocumentId("Notes", "note", removeNotes.getNote(), documentId -> {
            if (documentId != null) {
                removeNotesFromTaskNotes(documentId);
                removeItemFromFirestore(documentId);
            } else {
                // Handle the case where the document ID couldn't be retrieved
                Toast.makeText(context, "Document does not exist", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void removeItemFromFirestore(String documentId) {
        FirebaseFirestore.getInstance().collection("Notes").document(documentId).delete().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(context, "Notes deleted", Toast.LENGTH_SHORT).show();

            } else {
                Toast.makeText(context, "Error occurred", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void removeNotesFromTaskNotes(String noteId) {
        FirebaseFirestore.getInstance().collection("taskNotes")
                .whereEqualTo("noteId", noteId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (DocumentSnapshot document : task.getResult()) {
                            String taskNotesId = document.getId();
                            removeItemFromTaskNotes(taskNotesId);
                        }
                    } else {
                        // Handle failure in fetching projectTasks
                        Log.e("RemoveProjectTask", "Error fetching projectTasks", task.getException());
                    }
                });
    }

    private void removeItemFromTaskNotes(String taskNotesId) {
        FirebaseFirestore.getInstance().collection("taskNotes").document(taskNotesId)
                .delete()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d("removefromtaskNotes", "Task removed from projectTasks successfully");
                    } else {
                        Log.e("removedfrontasknotes", "Error removing task from projectTasks", task.getException());
                    }
                });
    }

}

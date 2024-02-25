package com.ecom.fyp2023.Fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ecom.fyp2023.Adapters.NotesRVAdapter;
import com.ecom.fyp2023.ModelClasses.Notes;
import com.ecom.fyp2023.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class NotesFragment extends BottomSheetDialogFragment {

    RecyclerView recyclerView;
    NotesRVAdapter notesRVAdapter;

    FirebaseFirestore fb;

    String taskId;
    List<Notes> notesList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notes, container, false);


        recyclerView = view.findViewById(R.id.notesRecyclerview);
        fb = FirebaseFirestore.getInstance();

        notesList = new ArrayList<>();
        LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);

        notesRVAdapter = new NotesRVAdapter(notesList, requireContext());
        recyclerView.setAdapter(notesRVAdapter);

        Bundle args = getArguments();
        if (args != null && args.containsKey("taskID")) {
            taskId = args.getString("taskID");
            displayNotes(taskId);
        }

        ImageView closeImageView = view.findViewById(R.id.closeImageView);
        // Set an OnClickListener to handle the close action
        closeImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                dismiss();
            }
        });
        return view;
    }

    private void displayNotes(String taskId) {
        CollectionReference projectCommentsCollection = fb.collection("taskNotes");
        Query query = projectCommentsCollection
                .whereEqualTo("taskId", taskId)
                .orderBy("timestamp", Query.Direction.DESCENDING);

        query.addSnapshotListener((value, error) -> {
            if (error != null) {
                Log.e("Firestore", "Error getting notes: " + error.getMessage());
                Toast.makeText(getActivity(), "Error getting notes: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                return;
            }
            if (value != null && !value.isEmpty()) {
                List<Notes> notes = new ArrayList<>();
                for (QueryDocumentSnapshot document : value) {
                    String noteId = document.getString("noteId");
                    retrieveTaskNotes(noteId, notes);
                }
                // Update the adapter with the tasks list after all tasks are retrieved
                Log.d("Firestore", "Comment List size: " + notes.size());
                //notesRVAdapter.updateList(notes);
            }
            Log.d("Firestore", "Fetching notes for taskId: " + taskId);
        });
    }

    private void retrieveTaskNotes(String notesId, List<Notes> notes) {
        CollectionReference notesCollection = fb.collection("Notes");
        notesCollection.document(notesId).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            Notes notesData = document.toObject(Notes.class);
                            notes.add(notesData);

                            Log.d("Firestore", "Retrieved Notes Data: " + notesData);

                            notesRVAdapter.updateList(notes);
                        } else {
                            Log.e("Firestore", "Document does not exist for notesid: " + notesId);
                        }
                    } else {
                        Log.e("Firestore", "Error fetching notes: " + task.getException());
                        Toast.makeText(getActivity(), "Error fetching notes", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}

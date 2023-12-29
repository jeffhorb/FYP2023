package com.ecom.fyp2023;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class FirestoreManager {

    public interface OnDocumentDeletedListener {
        void onDocumentDeleted(boolean success);
    }

    private FirebaseFirestore firestore;

    public FirestoreManager() {
        this.firestore = FirebaseFirestore.getInstance();
    }

    // Method to get the document ID from Firestore
    public void getDocumentId(String collectionPath, String fieldName, String value, final OnDocumentIdRetrievedListener listener) {
        firestore.collection(collectionPath)
                .whereEqualTo(fieldName, value)
                .limit(1)  // Assuming there's only one document with the given field value
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(Task<QuerySnapshot> task) {
                        if (task.isSuccessful() && task.getResult() != null && !task.getResult().isEmpty()) {
                            DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
                            String documentId = documentSnapshot.getId();
                            if (listener != null) {
                                listener.onDocumentIdRetrieved(documentId);
                            }
                        } else {
                            if (listener != null) {
                                listener.onDocumentIdRetrieved(null);
                            }
                        }
                    }
                });
    }

    public interface OnDocumentIdRetrievedListener {
        void onDocumentIdRetrieved(String documentId);
    }

    public void deleteProjectAndTasks(String projectId, final OnDeleteListener listener) {
        // Step 1: Retrieve the document reference for the project
        DocumentReference projectRef = firestore.collection("Projects").document(projectId);

        // Step 2: Delete the project document
        projectRef.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(Task<Void> task) {
                if (task.isSuccessful()) {
                    // Step 3: Retrieve the references to the tasks associated with the project
                    retrieveTaskReferences(projectId, listener);
                } else {
                    if (listener != null) {
                        listener.onDeleteComplete(false);
                    }
                }
            }
        });
    }

    private void retrieveTaskReferences(String projectId, final OnDeleteListener listener) {
        firestore.collection("Tasks")
                .whereEqualTo("projectId", projectId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            // Step 4: Delete each task document
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String taskId = document.getId();
                                DocumentReference taskRef = firestore.collection("Tasks").document(taskId);
                                taskRef.delete();
                            }

                            if (listener != null) {
                                listener.onDeleteComplete(true);
                            }
                        } else {
                            if (listener != null) {
                                listener.onDeleteComplete(false);
                            }
                        }
                    }
                });
    }

    public interface OnDeleteListener {
        void onDeleteComplete(boolean success);
    }
}

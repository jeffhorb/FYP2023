package com.ecom.fyp2023.AppManagers;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

public class FirestoreManager {

    private final FirebaseFirestore firestore;

    public FirestoreManager() {
        this.firestore = FirebaseFirestore.getInstance();
    }

    // Method to get the document ID from Firestore
    public void getDocumentId(String collectionPath, String fieldName, String value, final OnDocumentIdRetrievedListener listener) {
        firestore.collection(collectionPath)
                .whereEqualTo(fieldName, value)
                .limit(1)  // Assuming there's only one document with the given field value
                .get()
                .addOnCompleteListener(task -> {
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
                });
    }

    public interface OnDocumentIdRetrievedListener {
        void onDocumentIdRetrieved(String documentId);
    }

}

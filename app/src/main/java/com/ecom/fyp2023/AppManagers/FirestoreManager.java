package com.ecom.fyp2023.AppManagers;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Map;

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

    // Method to update document in Firestore
    public void updateDocument(String collectionPath, String documentId, Map<String, Object> data, OnUpdateCompleteListener listener) {
        firestore.collection(collectionPath)
                .document(documentId)
                .update(data)
                .addOnCompleteListener(task -> {
                    if (listener != null) {
                        if (task.isSuccessful()) {
                            listener.onUpdateComplete(true);
                        } else {
                            listener.onUpdateComplete(false);
                        }
                    }
                });
    }


    public interface OnUpdateCompleteListener {
        void onUpdateComplete(boolean success);
    }

    public interface OnDocumentIdRetrievedListener {
        void onDocumentIdRetrieved(String documentId);
    }

}

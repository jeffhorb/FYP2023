package com.ecom.fyp2023.TeamManagementClasses;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class AdminChecker {
    public static void checkIfAdmin(AdminCheckCallback callback, String userId, String groupId) {
        if (groupId != null) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            // Query the groups collection to fetch the group document
            DocumentReference groupRef = db.collection("groups").document(groupId);

            // Get the group document
            groupRef.get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                if (document.exists()) {
                                    // Check if the admins field exists and contains the user's ID
                                    List<String> admins = (List<String>) document.get("admins");
                                    boolean isAdmin = admins != null && admins.contains(userId);
                                    // Callback with the result
                                    callback.onResult(isAdmin);
                                } else {
                                    // Group document does not exist
                                    Log.e("AdminChecker", "Group document does not exist");
                                    // Callback with the result indicating the user is not an admin
                                    callback.onResult(false);
                                }
                            } else {
                                // Error occurred while fetching group document
                                Log.e("AdminChecker", "Error getting group document: ", task.getException());
                                // Callback with the result indicating the user is not an admin
                                callback.onResult(false);
                            }
                        }
                    });
        } else {
            // Group ID is null, cannot check admin status
            Log.e("AdminChecker", "Group ID is null");
            // Callback with the result indicating the user is not an admin
            callback.onResult(false);
        }
    }


    public interface AdminCheckCallback {
        void onResult(boolean isAdmin);
    }
}



package com.ecom.fyp2023;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ecom.fyp2023.Adapters.FilesAdapter;
import com.ecom.fyp2023.ModelClasses.FileModel;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class DocumentActivity extends AppCompatActivity {
    private static final int FILE_PICKER_REQUEST_CODE = 123 ;
    RecyclerView recyclerView;
    List<FileModel> fileList;
    FilesAdapter filesAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_document);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        recyclerView = findViewById(R.id.recycler_files);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        fileList = new ArrayList<>();
        filesAdapter = new FilesAdapter(fileList, DocumentActivity.this);
        recyclerView.setAdapter(filesAdapter);

        // Fetch files from Firestore
        fetchFilesFromFirestore();

    }

    private void fetchFilesFromFirestore() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference filesCollection = db.collection("files");

        filesCollection.get().addOnSuccessListener(queryDocumentSnapshots -> {
            // Clear the existing list
            fileList.clear();

            // Loop through the documents and add them to fileList
            for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                FileModel file = documentSnapshot.toObject(FileModel.class);
                fileList.add(file);
            }

            // Notify the adapter that the data set has changed
            filesAdapter.notifyDataSetChanged();
        }).addOnFailureListener(e -> {
            // Handle errors
            Toast.makeText(getApplicationContext(), "Failed to fetch files from Firestore: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.document_menu, menu);
        return true;
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FILE_PICKER_REQUEST_CODE && resultCode == RESULT_OK) {
            // Get the URI of the selected file
            Uri fileUri = data.getData();
            // Now, you can proceed with uploading this file to Firebase Storage
            assert fileUri != null;
            uploadFileToFirebaseStorage(fileUri);
        }
    }

    private void uploadFileToFirebaseStorage(@NonNull Uri fileUri) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();

        // Extract the original file name
        String fileName = getFileName(fileUri);

        // Get a reference to a file in Firebase Storage
        StorageReference fileRef = storageRef.child("files/" + fileName);

        // Upload file to Firebase Storage
        UploadTask uploadTask = fileRef.putFile(fileUri);
        uploadTask.addOnSuccessListener(taskSnapshot -> {
            // File uploaded successfully, get the download URL
            fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                // Store file metadata in Firestore
                //String fileName = fileUri.getLastPathSegment();
                String downloadUrl = uri.toString();
                storeFileMetadataInFirestore(fileName, downloadUrl, fileRef.getPath());
            }).addOnFailureListener(exception -> {
                Toast.makeText(getApplicationContext(), "Failed to get download URL: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
            });
            Toast.makeText(getApplicationContext(), "File Uploaded", Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(exception -> {
            Toast.makeText(getApplicationContext(), "Upload Failed: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

//    private void storeFileMetadataInFirestore(String fileName, String downloadUrl, String filePath) {
//        // Get reference to Firestore collection where you want to store file metadata
//        FirebaseFirestore db = FirebaseFirestore.getInstance();
//        CollectionReference filesCollection = db.collection("files");
//
//        // Create a new FileModel object with file metadata
//        FileModel file = new FileModel(fileName, downloadUrl,filePath);
//
//        // Add the file metadata to Firestore
//        filesCollection.add(file)
//                .addOnSuccessListener(documentReference -> {
//                    Toast.makeText(getApplicationContext(), "File metadata added to Firestore", Toast.LENGTH_SHORT).show();
//                })
//                .addOnFailureListener(e -> {
//                    Toast.makeText(getApplicationContext(), "Failed to add file metadata to Firestore: " + e.getMessage(), Toast.LENGTH_SHORT).show();
//                });
//    }

    private void storeFileMetadataInFirestore(String fileName, String downloadUrl, String filePath) {
        // Get reference to Firestore collection where you want to store file metadata
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference filesCollection = db.collection("files");

        // Create a new FileModel object with file metadata
        FileModel file = new FileModel(fileName, downloadUrl, filePath);

        // Add the file metadata to Firestore
        filesCollection.add(file)
                .addOnSuccessListener(documentReference -> {
                    // File metadata added successfully, now add the version to the versions sub-collection
                    addFileVersionToSubcollection(documentReference.getId(), file);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getApplicationContext(), "Failed to add file metadata to Firestore: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void addFileVersionToSubcollection(String fileId, FileModel file) {
        // Get reference to Firestore collection for file versions
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference versionsCollection = db.collection("files").document(fileId).collection("versions");

        // Create a new version document with metadata
        Map<String, Object> versionData = new HashMap<>();
        versionData.put("userId", FirebaseAuth.getInstance().getCurrentUser().getUid()); // Assuming you're using Firebase Authentication
        versionData.put("timestamp", FieldValue.serverTimestamp());
        versionData.put("content", "Initial content"); // You might want to modify this based on your use case

        // Add the version document to Firestore
        versionsCollection.add(versionData)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(getApplicationContext(), "File version added to Firestore", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getApplicationContext(), "Failed to add file version to Firestore: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    // Helper method to get the file name from the Uri
    @SuppressLint("Range")
    private String getFileName(@NonNull Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == R.id.upload) {
            // Launch file picker intent
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            //intent.setType("application/pdf|application/msword|application/vnd.ms-excel|application/vnd.ms-powerpoint|text/plain");

            intent.setType("*/*"); // Allow all file types
            startActivityForResult(intent, FILE_PICKER_REQUEST_CODE);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
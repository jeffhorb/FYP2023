package com.ecom.fyp2023;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.text.InputType;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ecom.fyp2023.Adapters.FilesAdapter;
import com.ecom.fyp2023.ModelClasses.FileModel;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DocumentActivity extends AppCompatActivity {
    private static final int FILE_PICKER_REQUEST_CODE = 123 ;
    RecyclerView recyclerView;
    List<FileModel> fileList;
    FilesAdapter filesAdapter;

    String id;
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

        filesCollection.addSnapshotListener((value, error) -> {
            if (error != null) {
                // Handle errors
                Toast.makeText(getApplicationContext(), "Failed to fetch files from Firestore: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                return;
            }

            if (value != null) {
                fileList.clear(); // Clear the existing list
                for (QueryDocumentSnapshot document : value) {
                    FileModel file = document.toObject(FileModel.class);
                    fileList.add(file);
                }
                filesAdapter.updateList(fileList);
            }
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
                    //addFileVersionToSubcollection(documentReference.getId(), file);

                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getApplicationContext(), "Failed to add file metadata to Firestore: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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
            intent.setType("*/*"); // Allow all file types
            startActivityForResult(intent, FILE_PICKER_REQUEST_CODE);
            return true;
        } else if (itemId == R.id.createNewFile) {
            // Show dialog to get file name from user
            showCreateFileDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showCreateFileDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Create New File");

        // Set up the input
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("Create", (dialog, which) -> {
            String fileName = input.getText().toString().trim();
            if (!TextUtils.isEmpty(fileName)) {
                // Create a temporary file
                File newFile = createTempFile(fileName, "");

                // Upload the file to Firebase Storage
                if (newFile != null) {
                    Uri fileUri = Uri.fromFile(newFile);
                    uploadFileToFirebaseStorage(fileUri);
                } else {
                    Toast.makeText(getApplicationContext(), "Failed to create a new file.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getApplicationContext(), "File name cannot be empty.", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    //create file
    @Nullable
    private File createTempFile(String fileName, String fileContent) {
        try {
            // Create a temporary file
            File tempFile = File.createTempFile("temp_", ".txt", getCacheDir());
            // Write content to the file
            FileWriter writer = new FileWriter(tempFile);
            writer.write(fileContent);
            writer.close();
            // Rename the temporary file to the desired file name with .txt extension
            File newFile = new File(tempFile.getParent(), fileName + ".txt");
            tempFile.renameTo(newFile);
            return newFile;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }


}
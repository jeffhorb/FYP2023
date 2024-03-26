package com.ecom.fyp2023.TextEditor;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.ecom.fyp2023.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import jp.wasabeef.richeditor.RichEditor;

public class RichEditorFileEditor extends AppCompatActivity {

    RichEditor mEditor;
    StorageReference storageReference;
    String filePath;
    String originalContent;

    private TextView mPreview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rich_editor_file_editor);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        // Initialize Firebase Storage reference
        storageReference = FirebaseStorage.getInstance().getReference();

        mEditor = findViewById(R.id.rich_editor);
        mEditor.setBold();
        mEditor.setItalic();
        mEditor.setUnderline();
        // Other formatting options

        if (getIntent().hasExtra("filePath")) {
            filePath = getIntent().getStringExtra("filePath");
            loadFileContent(filePath);
        }

        mPreview = findViewById(R.id.preview);
        mEditor.setOnTextChangeListener(new RichEditor.OnTextChangeListener() {
            @Override
            public void onTextChange(String text) {
                mPreview.setText(text);
            }
        });

        // Load original content when activity starts
        originalContent = mEditor.getHtml();
    }

    // Method to load file content from Firebase Storage
    private void loadFileContent(String filePath) {
        StorageReference fileRef = storageReference.child(filePath);
        fileRef.getBytes(Long.MAX_VALUE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                String content = new String(bytes);
                mEditor.setHtml(content);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle any errors
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.file_editor_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == R.id.saveFile) {
            saveChanges();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void saveChanges() {
        String currentContent = mEditor.getHtml();
        if (!currentContent.equals(originalContent)) {
            // Content has changed, save the changes
            updateFileVersion(currentContent);
        } else {
            // Content hasn't changed
            Toast.makeText(this, "No changes to save", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateFileVersion(String newContent) {
        // Get reference to Firestore collection for file versions
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference versionsCollection = db.collection("files").document(filePath).collection("versions");

        // Create a new version document with metadata
        Map<String, Object> versionData = new HashMap<>();
        versionData.put("userId", FirebaseAuth.getInstance().getCurrentUser().getUid()); // Assuming you're using Firebase Authentication
        versionData.put("timestamp", FieldValue.serverTimestamp());
        versionData.put("content", newContent);

        // Add the version document to Firestore
        versionsCollection.add(versionData)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(getApplicationContext(), "File version updated in Firestore", Toast.LENGTH_SHORT).show();
                    // Update original content to current content after successful ssave
                    originalContent = newContent;
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getApplicationContext(), "Failed to update file version in Firestore: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}

package com.ecom.fyp2023.VersionControlClasses;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.ecom.fyp2023.AppManagers.SharedPreferenceManager;
import com.ecom.fyp2023.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import jp.wasabeef.richeditor.RichEditor;

public class RichEditorFileEditor extends AppCompatActivity {

    RichEditor mEditor;
    StorageReference storageReference;
    String filePath,fileDocId;
    String originalContent, enteredMessage;

    //String groupId = GroupIdGlobalVariable.getInstance().getGlobalData();

    private MenuItem revertMenuItem;

    Date time;

    String groupId;

    private SharedPreferenceManager sharedPreferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rich_editor_file_editor);

        sharedPreferenceManager = new SharedPreferenceManager(this);
        mEditor = findViewById(R.id.rich_editor);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        storageReference = FirebaseStorage.getInstance().getReference();

        groupId = sharedPreferenceManager.getGroupId();

        if (getIntent().hasExtra("filePath") && getIntent().hasExtra("filesDocId")) {
            filePath = getIntent().getStringExtra("filePath");
            fileDocId = getIntent().getStringExtra("filesDocId");

            // Check if SharedPreferences is empty
            String savedText = sharedPreferenceManager.getRichEditorText(fileDocId);
            if (savedText.isEmpty()) {
                // Load latest file version
                loadLatestFileVersion();
            } else {
                // Set the text in RichEditor from SharedPreferences
                mEditor.setHtml(savedText);
            }
        }

        mEditor.setOnTextChangeListener(new RichEditor.OnTextChangeListener() {
            @Override
            public void onTextChange(String text) {
                // Save the text in RichEditor to SharedPreferences
                sharedPreferenceManager.saveRichEditorText(fileDocId, text);
                if (revertMenuItem != null) {
                    revertMenuItem.setVisible(false);
                }
            }
        });

        findViewById(R.id.action_undo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.undo();
            }
        });

        findViewById(R.id.action_redo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.redo();
            }
        });

        findViewById(R.id.action_bold).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.setBold();
            }
        });

        findViewById(R.id.action_italic).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.setItalic();
            }
        });

        findViewById(R.id.action_subscript).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.setSubscript();
            }
        });

        findViewById(R.id.action_superscript).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.setSuperscript();
            }
        });

        findViewById(R.id.action_strikethrough).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.setStrikeThrough();
            }
        });

        findViewById(R.id.action_underline).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.setUnderline();
            }
        });

        findViewById(R.id.action_heading1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.setHeading(1);
            }
        });

        findViewById(R.id.action_heading2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.setHeading(2);
            }
        });

        findViewById(R.id.action_heading3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.setHeading(3);
            }
        });

        findViewById(R.id.action_heading4).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.setHeading(4);
            }
        });

        findViewById(R.id.action_heading5).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.setHeading(5);
            }
        });

        findViewById(R.id.action_heading6).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.setHeading(6);
            }
        });

        findViewById(R.id.action_txt_color).setOnClickListener(new View.OnClickListener() {
            private boolean isChanged;

            @Override
            public void onClick(View v) {
                mEditor.setTextColor(isChanged ? Color.BLACK : Color.RED);
                isChanged = !isChanged;
            }
        });

        findViewById(R.id.action_indent).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.setIndent();
            }
        });

        findViewById(R.id.action_outdent).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.setOutdent();
            }
        });

        findViewById(R.id.action_align_left).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.setAlignLeft();
            }
        });

        findViewById(R.id.action_align_center).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.setAlignCenter();
            }
        });

        findViewById(R.id.action_align_right).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.setAlignRight();
            }
        });

        findViewById(R.id.action_blockquote).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.setBlockquote();
            }
        });

        findViewById(R.id.action_insert_bullets).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.setBullets();
            }
        });

        findViewById(R.id.action_insert_numbers).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.setNumbers();
            }
        });

        findViewById(R.id.action_insert_checkbox).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.insertTodo();
            }
        });

        // Load original content when activity starts
        originalContent = mEditor.getHtml();


    }

    // Method to load the latest version of the file content from Firestore
//    private void loadLatestFileVersion() {
//        FirebaseFirestore db = FirebaseFirestore.getInstance();
//        CollectionReference versionsCollection = db.collection("files").document(fileDocId).collection("versions");
//
//        versionsCollection.get().addOnSuccessListener(queryDocumentSnapshots -> {
//            if (!queryDocumentSnapshots.isEmpty()) {
//                DocumentSnapshot latestVersion = queryDocumentSnapshots.getDocuments().get(0);
//                String content = latestVersion.getString("content");
//                time = latestVersion.getDate("timestamp");
//                if (content != null) {
//                    // Set the content of the latest version to the RichEditor
//                    mEditor.setHtml(content);
//
//                    if (revertMenuItem != null) {
//                        revertMenuItem.setVisible(true);
//                    }
//
//                } else {
//                    // Handle case where content is null
//                    Toast.makeText(getApplicationContext(), "Latest version content is null", Toast.LENGTH_SHORT).show();
//                }
//            } else {
//                // Handle case where no versions are found for the group or user
//                loadFileContent(filePath);
//            }
//        }).addOnFailureListener(e -> {
//            // Handle any errors
//            Toast.makeText(getApplicationContext(), "Failed to load latest version: " + e.getMessage(), Toast.LENGTH_SHORT).show();
//        });
//    }

     //Method to load the latest version of the file content from Firestore

    private void loadLatestFileVersion() {
        // Get reference to Firestore collection for file versions
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference versionsCollection = db.collection("files").document(fileDocId).collection("versions");

        // Query to get the latest version based on timestamp
        versionsCollection.orderBy("timestamp", Query.Direction.DESCENDING).limit(1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        // Retrieve the latest version document
                        DocumentSnapshot latestVersion = queryDocumentSnapshots.getDocuments().get(0);
                        String content = latestVersion.getString("content");
                        time = latestVersion.getDate("timestamp");
                        if (content != null) {
                            // Set the content of the latest version to the RichEditor
                            mEditor.setHtml(content);

                            if (revertMenuItem != null) {
                                revertMenuItem.setVisible(true);
                            }

                        } else {
                            // Handle case where content is null
                            Toast.makeText(getApplicationContext(), "Latest version content is null", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        // Handle case where no versions are found
                        loadFileContent(filePath);
                    }
                })
                .addOnFailureListener(e -> {
                    // Handle any errors
                    Toast.makeText(getApplicationContext(), "Failed to load latest version: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
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

    // Method to show confirmation dialog
    private void showConfirmationDialog(String title, String message, DialogInterface.OnClickListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title)
                .setMessage(message)
                .setPositiveButton("Yes", listener)
                .setNegativeButton("No", null)
                .show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.file_editor_menu, menu);
        // Initialize the revert menu item reference
        revertMenuItem = menu.findItem(R.id.revert);
        revertMenuItem.setVisible(false); // Set it initially to not visible
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == R.id.saveFile) {
            // Save the text in RichEditor to SharedPreferences
            String currentContent = mEditor.getHtml();
            sharedPreferenceManager.saveRichEditorText(fileDocId,currentContent);
            return true;
        } else if (itemId == R.id.saveChanges) {
            // Show confirmation dialog before saving changes
            showConfirmationDialog("Save Changes", "This will create a new version of the file", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    // Show dialog for entering message
                    showEnterMessageDialog(new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            // Call saveChanges with entered message
                            saveChanges(enteredMessage);
                            String currentContent = mEditor.getHtml();
                        }
                    });
                }
            });
            return true;
        }
        else if (itemId == R.id.latestVersion) {
            // Show confirmation dialog before loading the latest version
            showConfirmationDialog("Load Latest Version", "Are you sure you want to load the latest version?", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    loadLatestFileVersion();
                    String currentContent = mEditor.getHtml();
                    sharedPreferenceManager.saveRichEditorText(fileDocId,currentContent);
                }
            });
            return true;
        } else if (itemId == R.id.updateOriginalFile) {
            // Show confirmation dialog before updating the original file
            showConfirmationDialog("Update Original File", "Are you sure you want to update the original file with the latest version?",
                    new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    updateOriginalFileWithLatestVersion();
                }
            });
            return true;
        } else if (itemId == R.id.openVersions) {
            Intent intent = new Intent(RichEditorFileEditor.this, VersionHistoryActivity.class);
            intent.putExtra("filePath", filePath);
            intent.putExtra("filesId", fileDocId);
            startActivity(intent);
            return true;
        }else if (itemId == R.id.revert){

            // Show confirmation dialog before reverting to the previous version
            showConfirmationDialog("Revert", "Are you sure you want to revert to the previous version?", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                    checkContentInVersions();
                }
            });

            return  true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Method to show dialog for entering message
    private void showEnterMessageDialog(DialogInterface.OnClickListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogLayout = inflater.inflate(R.layout.dialog_layout, null);
        EditText messageEditText = dialogLayout.findViewById(R.id.messageEditText);

        builder.setTitle("Enter Message")
                .setMessage("Please enter a message:")
                .setView(dialogLayout)
                .setPositiveButton("OK", (dialogInterface, i) -> {
                    // Get the entered message
                    enteredMessage = messageEditText.getText().toString();
                    // Call listener with entered message
                    listener.onClick(dialogInterface, i);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // Method to save changes with message
    private void saveChanges(String message) {
        String currentContent = mEditor.getHtml();
        if (!currentContent.equals(originalContent) && !currentContent.isEmpty()) {
            // Content has changed, save the changes
            updateFileVersion(currentContent, message);
            loadLatestFileVersion();
        } else {
            // Content hasn't changed
            Toast.makeText(this, "No changes to made, Please make changes", Toast.LENGTH_LONG).show();
        }
    }

    // Method to update file version with message
    private void updateFileVersion(String newContent, String message) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference versionsCollection = db.collection("files").document(fileDocId).collection("versions");

        // Add groupId or userAuthId field to the version data
        String userAuthId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        Map<String, Object> versionData = new HashMap<>();
        versionData.put("userId", userAuthId);
        versionData.put("timestamp", FieldValue.serverTimestamp());
        versionData.put("content", newContent);
        versionData.put("message", message);
//        if (groupId != null) {
//            versionData.put("groupId", groupId);
//        } else {
//            versionData.put("userAuthId", userAuthId);
//        }

        // Add the version document to Firestore
        versionsCollection.add(versionData).addOnSuccessListener(documentReference -> {
            Toast.makeText(getApplicationContext(), "File version updated in Firestore", Toast.LENGTH_SHORT).show();
            // Update original content to current content after successful save
            originalContent = newContent;
        }).addOnFailureListener(e -> {
            Toast.makeText(getApplicationContext(), "Failed to update file version in Firestore: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }
//    private void updateFileVersion(String newContent, String message) {
//        // Get reference to Firestore collection for file versions
//        FirebaseFirestore db = FirebaseFirestore.getInstance();
//        CollectionReference versionsCollection = db.collection("files").document(fileDocId).collection("versions");
//
//        // Create a new version document with metadata
//        Map<String, Object> versionData = new HashMap<>();
//        versionData.put("userId", FirebaseAuth.getInstance().getCurrentUser().getUid()); // Assuming you're using Firebase Authentication
//        versionData.put("timestamp", FieldValue.serverTimestamp());
//        versionData.put("content", newContent);
//        versionData.put("message", message);
//        versionData.put("groupId", groupId);
//
//        // Add the version document to Firestore
//        versionsCollection.add(versionData)
//                .addOnSuccessListener(documentReference -> {
//                    Toast.makeText(getApplicationContext(), "File version updated in Firestore", Toast.LENGTH_SHORT).show();
//                    // Update original content to current content after successful save
//                    originalContent = newContent;
//
//                })
//                .addOnFailureListener(e -> {
//                    Toast.makeText(getApplicationContext(), "Failed to update file version in Firestore: " + e.getMessage(), Toast.LENGTH_SHORT).show();
//                });
//    }

    // Method to update the original file with the newest version
//    private void updateOriginalFileWithLatestVersion() {
//        // Get reference to Firestore collection for file versions
//        FirebaseFirestore db = FirebaseFirestore.getInstance();
//        CollectionReference versionsCollection = db.collection("files").document(fileDocId).collection("versions");
//
//        // Query to get the latest version based on timestamp
//        versionsCollection.orderBy("timestamp", Query.Direction.DESCENDING).limit(1)
//                .get()
//                .addOnSuccessListener(queryDocumentSnapshots -> {
//                    if (!queryDocumentSnapshots.isEmpty()) {
//                        // Retrieve the latest version document
//                        DocumentSnapshot latestVersion = queryDocumentSnapshots.getDocuments().get(0);
//                        String content = latestVersion.getString("content");
//                        if (content != null) {
//                            // Update original file document in Firestore with the latest version content and metadata
//                            Map<String, Object> fileData = new HashMap<>();
//                            fileData.put("content", content);
//                            fileData.put("timestamp", latestVersion.getTimestamp("timestamp")); // You may need to adjust this based on your data model
//
//                            FirebaseFirestore.getInstance().collection("files").document(fileDocId)
//                                    .set(fileData, SetOptions.merge()) // Merge with existing data
//                                    .addOnSuccessListener(aVoid -> {
//                                        // Original file updated successfully in Firestore
//                                        Toast.makeText(getApplicationContext(), "Original file updated in Firestore with latest version", Toast.LENGTH_SHORT).show();
//
//                                        // Now update the file in Firebase Storage with the latest version content
//                                        updateFileInStorage(content);
//                                    })
//                                    .addOnFailureListener(e -> {
//                                        // Handle failure to update original file in Firestore
//                                        Toast.makeText(getApplicationContext(), "Failed to update original file in Firestore: " + e.getMessage(), Toast.LENGTH_SHORT).show();
//                                    });
//                        } else {
//                            // Handle case where content is null
//                            Toast.makeText(getApplicationContext(), "Latest version content is null", Toast.LENGTH_SHORT).show();
//                        }
//                    } else {
//                        // Handle case where no versions are found
//                        Toast.makeText(getApplicationContext(), "No versions found for this file", Toast.LENGTH_SHORT).show();
//                    }
//                })
//                .addOnFailureListener(e -> {
//                    // Handle any errors
//                    Toast.makeText(getApplicationContext(), "Failed to load latest version: " + e.getMessage(), Toast.LENGTH_SHORT).show();
//                });
//    }

    // Method to update the original file with the newest version
    private void updateOriginalFileWithLatestVersion() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference versionsCollection = db.collection("files").document(fileDocId).collection("versions");

        // Query to get the latest version based on timestamp and group id or userAuthId
        Query query;
        if (groupId != null) {
            // Load files belonging to the group
            query = versionsCollection.whereEqualTo("groupId", groupId).orderBy("timestamp", Query.Direction.DESCENDING).limit(1);
        } else {
            // Load files belonging to the user's private space
            String userAuthId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            query = versionsCollection.whereEqualTo("userAuthId", userAuthId).orderBy("timestamp", Query.Direction.DESCENDING).limit(1);
        }

        query.get().addOnSuccessListener(queryDocumentSnapshots -> {
            if (!queryDocumentSnapshots.isEmpty()) {
                DocumentSnapshot latestVersion = queryDocumentSnapshots.getDocuments().get(0);
                String content = latestVersion.getString("content");
                if (content != null) {
                    // Update original file document in Firestore with the latest version content and metadata
                    Map<String, Object> fileData = new HashMap<>();
                    fileData.put("content", content);
                    fileData.put("timestamp", latestVersion.getTimestamp("timestamp"));

                    FirebaseFirestore.getInstance().collection("files").document(fileDocId)
                            .set(fileData, SetOptions.merge()) // Merge with existing data
                            .addOnSuccessListener(aVoid -> {
                                // Original file updated successfully in Firestore
                                Toast.makeText(getApplicationContext(), "Original file updated in Firestore with latest version", Toast.LENGTH_SHORT).show();

                                // Now update the file in Firebase Storage with the latest version content
                                updateFileInStorage(content);
                            })
                            .addOnFailureListener(e -> {
                                // Handle failure to update original file in Firestore
                                Toast.makeText(getApplicationContext(), "Failed to update original file in Firestore: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                } else {
                    // Handle case where content is null
                    Toast.makeText(getApplicationContext(), "Latest version content is null", Toast.LENGTH_SHORT).show();
                }
            } else {
                // Handle case where no versions are found for the group or user
                Toast.makeText(getApplicationContext(), "No versions found for this file", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            // Handle any errors
            Toast.makeText(getApplicationContext(), "Failed to load latest version: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    // Method to update the file in Firebase Storage with the latest version content
    private void updateFileInStorage(@NonNull String newContent) {
        // Get reference to the file in Firebase Storage
        StorageReference fileRef = storageReference.child(filePath);

        // Update the content of the file in Firebase Storage
        fileRef.putBytes(newContent.getBytes())
                .addOnSuccessListener(taskSnapshot -> {
                    // File in Firebase Storage updated successfully
                    Toast.makeText(getApplicationContext(), "File updated in Firebase Storage with latest version", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    // Handle failure to update file in Firebase Storage
                    Toast.makeText(getApplicationContext(), "Failed to update file in Firebase Storage: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
    // Method to check if the content in RichEditor can be found in versions documents
    private void checkContentInVersions() {
        // Get the current content from the RichEditor
        String currentContent = mEditor.getHtml();

        // Get reference to Firestore collection for file versions
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference versionsCollection = db.collection("files").document(fileDocId).collection("versions");

        // Query to find all versions that match the current content
        versionsCollection.whereEqualTo("content", currentContent)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        // Content found in at least one version, rollback to previous version
                        rollbackToPreviousVersions();
                    } else {
                        // Content not found in any version
                        Toast.makeText(getApplicationContext(), "Content not found in any version", Toast.LENGTH_SHORT).show();
                        rollbackToPreviousVersion();

                    }
                })
                .addOnFailureListener(e -> {
                    // Handle any errors
                    Toast.makeText(getApplicationContext(), "Failed to check content in versions: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    // Method to rollback to previous versions
    private void rollbackToPreviousVersions() {
        // Get the current content from the RichEditor
        String currentContent = mEditor.getHtml();

        // Get reference to Firestore collection for file versions
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference versionsCollection = db.collection("files").document(fileDocId).collection("versions");

        // Query to find the version that matches the current content
        Query query = versionsCollection.whereEqualTo("content", currentContent).limit(1);

        // Execute the query to find the version with the same content
        query.get().addOnSuccessListener(queryDocumentSnapshots -> {
            if (!queryDocumentSnapshots.isEmpty()) {
                // Retrieve the document with the same content
                DocumentSnapshot currentVersion = queryDocumentSnapshots.getDocuments().get(0);
                Date currentTimestamp = currentVersion.getDate("timestamp");

                // Query to find the previous version based on timestamp
                assert currentTimestamp != null;
                versionsCollection.whereLessThan("timestamp", currentTimestamp)
                        .orderBy("timestamp", Query.Direction.DESCENDING).limit(1)
                        .get()
                        .addOnSuccessListener(previousVersionSnapshots -> {
                            if (!previousVersionSnapshots.isEmpty()) {
                                // Retrieve the previous version document
                                DocumentSnapshot previousVersion = previousVersionSnapshots.getDocuments().get(0);
                                String content = previousVersion.getString("content");
                                if (content != null) {
                                    // Set the content of the previous version to the RichEditor
                                    mEditor.setHtml(content);
                                } else {
                                    // Handle case where content is null
                                    Toast.makeText(getApplicationContext(), "Previous version content is null", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                // Handle case where there are not enough versions
                                Toast.makeText(getApplicationContext(), "No more versions to revert to.  Reverted to original file", Toast.LENGTH_LONG).show();
                                loadFileContent(filePath);
                            }
                        })
                        .addOnFailureListener(e -> {
                            // Handle any errors
                            Toast.makeText(getApplicationContext(), "Failed to revert to previous version: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            } else {
                // Handle case where content is not found
                Toast.makeText(getApplicationContext(), "Content not found in any version", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            // Handle any errors
            Toast.makeText(getApplicationContext(), "Failed to find content in versions: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }


    private void rollbackToPreviousVersion() {
        // Get reference to Firestore collection for file versions
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference versionsCollection = db.collection("files").document(fileDocId).collection("versions");

        // Query to get the previous version based on timestamp
        versionsCollection.whereLessThan("timestamp", time)
                .orderBy("timestamp", Query.Direction.DESCENDING).limit(1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        // Retrieve the previous version document
                        DocumentSnapshot previousVersion = queryDocumentSnapshots.getDocuments().get(0);
                        String content = previousVersion.getString("content");
                        if (content != null) {
                            // Set the content of the previous version to the RichEditor
                            mEditor.setHtml(content);
                        } else {
                            // Handle case where content is null
                            Toast.makeText(getApplicationContext(), "Previous version content is null", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        // Handle case where there is no previous version
                        Toast.makeText(getApplicationContext(), "There is no previous version to revert to", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    // Handle any errors
                    Toast.makeText(getApplicationContext(), "Failed to revert to previous version: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

}

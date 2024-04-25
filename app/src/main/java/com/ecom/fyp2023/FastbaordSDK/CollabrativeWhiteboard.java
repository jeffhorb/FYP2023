package com.ecom.fyp2023.FastbaordSDK;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.ecom.fyp2023.R;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Objects;

import io.agora.board.fast.FastRoom;
import io.agora.board.fast.FastboardView;
import io.agora.board.fast.extension.FastResult;
import io.agora.board.fast.model.FastInsertDocParams;
import io.agora.board.fast.model.FastRegion;
import io.agora.board.fast.model.FastRoomOptions;

public class CollabrativeWhiteboard extends AppCompatActivity {

    private FastRoom fastRoom;
    LinearLayout document, image;
    // Request codes for file selection

    TextView groupN,userName;
    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int PICK_VIDEO_REQUEST = 2;
    private static final int PICK_DOCUMENT_REQUEST = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_board);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setupFastboard();

        document = findViewById(R.id.file);
        image = findViewById(R.id.image);
        groupN = findViewById(R.id.groupName);
        userName = findViewById(R.id.userName);

        if (getIntent().hasExtra("groupName")){

            String groupName = getIntent().getStringExtra("groupName");
            //String userName = getIntent().getStringExtra("userName");
            if (groupName != null){
                groupN.setText(groupName);
            }
            else {
                groupN.setVisibility(View.GONE);
            }
        }
        image.setOnClickListener(v -> pickFileFromDevice("image/*", PICK_IMAGE_REQUEST));
        document.setOnClickListener(v -> pickFileFromDevice("*/*", PICK_DOCUMENT_REQUEST, new String[]{"application/pdf", "application/msword", "application/vnd.ms-powerpoint", "text/plain"}));
    }

    private void setupFastboard() {
        FastboardView fastboardView = findViewById(R.id.fastboard_view);
        fastRoom = fastboardView.getFastboard().createFastRoom(getRoomOptions());
        fastRoom.join();
    }

    @NonNull
    private FastRoomOptions getRoomOptions() {
        String appIdentifier = "WtuiYPshEe6D_PXT5qj0Fg/DuTZLQ54PfeZTA";
        String roomUUID = "87cad3f0fc1a11ee8f6b69560a95c9aa";
        String roomToken = "NETLESSROOM_YWs9cmF0cjlYU09nQ1hQU2tfUyZub25jZT1lMTRlZDg0MC1mYzJlLTExZWUtYTljMS1iOTkwZWNkNGI4YTkmcm9sZT0wJnNpZz01OGUxYjE3MWM3MDM1N2E2MzhkOThhNjkxMThiYzg4YzMwZGVmMzc0ODBlOGViN2Q5N2U4OTMxNDI4NzJkODUyJnV1aWQ9ODdjYWQzZjBmYzFhMTFlZThmNmI2OTU2MGE5NWM5YWE";
        String userId = "uidr";
        FastRegion region = FastRegion.US_SV;
        return new FastRoomOptions(appIdentifier, roomUUID, roomToken, userId, region);
    }

    // This method will be invoked after the user picks an image
    private void pickFileFromDevice(String type, int requestCode) {
        pickFileFromDevice(type, requestCode, new String[]{type});
    }

    private void pickFileFromDevice(String type, int requestCode, String[] mimeTypes) {
        Intent intent = new Intent();
        intent.setType(type);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        if (mimeTypes != null) {
            intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
        }
        startActivityForResult(Intent.createChooser(intent, "Select File"), requestCode);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri selectedFileUri = data.getData();

            if (requestCode == PICK_IMAGE_REQUEST) {
                // Upload image to Firestore Storage and get URL
                uploadImageToFirestore(selectedFileUri);
            } else if (requestCode == PICK_DOCUMENT_REQUEST) {
                //TODO: Create FastInsertDocParams and FastResult objects
                // addDocumentToBoard(params, result);
            }
        }
    }

    private void uploadImageToFirestore(Uri imageUri) {
        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        String imageName = "image_" + System.currentTimeMillis() + ".jpg";
        StorageReference imageRef = storageRef.child("images/" + imageName);

        imageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    // Image upload successful, get the URL of the uploaded image
                    imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        // Log the obtained URL
                        Log.d("Image URL", uri.toString());
                        // Use the obtained URL to insert the image into the Fastboard
                        addImageToBoard(uri, 500, 500); // example dimensions
                    }).addOnFailureListener(e -> {
                        // Handle failure to get the download URL
                        Toast.makeText(CollabrativeWhiteboard.this, "Failed to get image URL", Toast.LENGTH_SHORT).show();
                    });
                })
                .addOnFailureListener(e -> {
                    // Handle image upload failure
                    Toast.makeText(CollabrativeWhiteboard.this, "Failed to upload image", Toast.LENGTH_SHORT).show();
                    Log.e("Upload Error", e.getMessage()); // Log the error message
                });
    }


//    private void uploadImageToFirestore(Uri imageUri) {
//        // Obtain a reference to the Firebase Storage instance
//        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
//
//        // Create a reference to the image file to be uploaded
//        String imageName = "image_" + System.currentTimeMillis() + ".jpg";
//        StorageReference imageRef = storageRef.child("images/" + imageName);
//
//        // Upload the image file to Firebase Cloud Storage
//        imageRef.putFile(imageUri)
//                .addOnSuccessListener(taskSnapshot -> {
//                    // Image upload successful, get the URL of the uploaded image
//                    imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
//                        // Use the obtained URL to insert the image into the Fastboard
//                        addImageToBoard(uri, 500, 500); // example dimensions
//                    }).addOnFailureListener(e -> {
//                        // Handle failure to get the download URL
//                        Toast.makeText(CollabrativeWhiteboard.this, "Failed to get image URL", Toast.LENGTH_SHORT).show();
//                    });
//                })
//                .addOnFailureListener(e -> {
//                    // Handle image upload failure
//                    Toast.makeText(CollabrativeWhiteboard.this, "Failed to upload image", Toast.LENGTH_SHORT).show();
//                });
//    }

    private void addImageToBoard(@NonNull Uri imageUrl, int width, int height) {
        fastRoom.insertImage(imageUrl.toString(), width, height);
    }
    private void addDocumentToBoard(FastInsertDocParams params, FastResult<String> result) {
        fastRoom.insertDocs(params, result);
    }
}
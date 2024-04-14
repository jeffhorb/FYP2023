package com.ecom.fyp2023.VersionControlClasses;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.widget.Toast;

import com.ecom.fyp2023.Adapters.VersionAdapter;
import com.ecom.fyp2023.ModelClasses.VersionModel;
import com.ecom.fyp2023.R;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class VersionHistoryActivity extends AppCompatActivity {

    private List<VersionModel> versionList;
    private VersionAdapter versionAdapter;

    String filePath,fileId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_version_history);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        if (getIntent().hasExtra("filePath") && getIntent().hasExtra("filesId")) {
            filePath = getIntent().getStringExtra("filePath");
            fileId = getIntent().getStringExtra("filesId");
        }

        RecyclerView recyclerView = findViewById(R.id.recycler_version);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        versionList = new ArrayList<>();
        versionAdapter = new VersionAdapter(versionList, this, filePath, fileId);
        recyclerView.setAdapter(versionAdapter);

        // Fetch version history from Firestore
        fetchVersionHistory();
    }

    private void fetchVersionHistory() {

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference versionsCollection = db.collection("files").document(fileId).collection("versions");

        versionsCollection.orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    versionList.clear();
                    for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        VersionModel version = documentSnapshot.toObject(VersionModel.class);
                        versionList.add(version);
                    }
                    versionAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getApplicationContext(), "Failed to fetch version history: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}

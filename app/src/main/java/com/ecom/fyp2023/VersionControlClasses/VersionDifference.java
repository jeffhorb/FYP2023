package com.ecom.fyp2023.VersionControlClasses;

import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.BackgroundColorSpan;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.text.HtmlCompat;

import com.ecom.fyp2023.AppManagers.DiffComputation;
import com.ecom.fyp2023.AppManagers.SharedPreferenceManager;
import com.ecom.fyp2023.ModelClasses.Diff;
import com.ecom.fyp2023.R;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import org.xml.sax.XMLReader;

import java.util.Date;
import java.util.LinkedList;
import java.util.Objects;


public class VersionDifference extends AppCompatActivity {

    private TextView previousVersionTextView, currentVersionTextView,diffTextView;

    String currentContent, fileId;
    Date currentTimestamp;

    private FirebaseFirestore db;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diff_utils);

        previousVersionTextView = findViewById(R.id.previous_version_text_view);
        currentVersionTextView = findViewById(R.id.current_version_text_view);
        diffTextView = findViewById(R.id.diff_text_view);

        db = FirebaseFirestore.getInstance();

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());


        if (getIntent().hasExtra("content") && getIntent().hasExtra("timestamp") && getIntent().hasExtra("fileId")) {

            currentContent = getIntent().getStringExtra("content");
            fileId = getIntent().getStringExtra("fileId");
            long currentTimestampMillis = getIntent().getLongExtra("timestamp", 0);
            currentTimestamp = new Date(currentTimestampMillis);

            // Fetch content of the previous version
            fetchPreviousVersionContent();
        }
    }


    private void fetchPreviousVersionContent() {
        db.collection("files").document(fileId).collection("versions")
                .whereLessThan("timestamp", currentTimestamp)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot documentSnapshot = queryDocumentSnapshots.getDocuments().get(0);
                        String previousContent = documentSnapshot.getString("content");
                        previousVersionTextView.setText(previousContent);
                        currentVersionTextView.setText(currentContent);
                        assert previousContent != null;
                        previousVersionTextView.setText(HtmlCompat.fromHtml(previousContent, HtmlCompat.FROM_HTML_MODE_COMPACT));
                        currentVersionTextView.setText(HtmlCompat.fromHtml(currentContent, HtmlCompat.FROM_HTML_MODE_COMPACT));
                        // Show differences between previous and current content
                        assert previousContent != null;
                        compareVersions(previousContent,currentContent);
                    } else {
                        // No previous version found, handle this case
                        currentVersionTextView.setText(HtmlCompat.fromHtml(currentContent, HtmlCompat.FROM_HTML_MODE_COMPACT));
                        // Clear the previous version TextView
                        previousVersionTextView.setText("");
                    }
                })
                .addOnFailureListener(e -> {
                    // Handle errors
                });
    }

    private void compareVersions(String previousContent, String currentContent) {
        DiffComputation diffComputation = new DiffComputation();
        LinkedList<Diff> diffs = diffComputation.diff_main(previousContent, currentContent);

        // Perform a semantic cleanup to increase human readability
        diffComputation.diff_cleanupSemantic(diffs);
        // Initialize SpannableStringBuilder for the diffs
        SpannableStringBuilder diffSpannable = new SpannableStringBuilder();
        // Iterate over the diffs and apply formatting
        for (Diff diff : diffs) {
            int start = diffSpannable.length();
            diffSpannable.append(HtmlCompat.fromHtml(diff.text, HtmlCompat.FROM_HTML_MODE_COMPACT));
            int end = diffSpannable.length();

            if (diff.operation == DiffComputation.Operation.INSERT) {
                diffSpannable.setSpan(new BackgroundColorSpan(getResources().getColor(R.color.blue)), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            } else if (diff.operation == DiffComputation.Operation.DELETE) {
                diffSpannable.setSpan(new BackgroundColorSpan(getResources().getColor(R.color.red)), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
        // Update the TextViews with the respective contents
        runOnUiThread(() -> {
            // Use HtmlCompat.fromHtml with FROM_HTML_MODE_COMPACT
            previousVersionTextView.setText(HtmlCompat.fromHtml(previousContent, HtmlCompat.FROM_HTML_MODE_COMPACT));
            currentVersionTextView.setText(HtmlCompat.fromHtml(currentContent, HtmlCompat.FROM_HTML_MODE_COMPACT));
            diffTextView.setText(diffSpannable);
        });
    }
}

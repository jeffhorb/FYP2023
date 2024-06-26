package com.ecom.fyp2023.SentimentAnalysis;

import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ecom.fyp2023.Adapters.SentimentRvAdapter;
import com.ecom.fyp2023.AppManagers.SharedPreferenceManager;
import com.ecom.fyp2023.ModelClasses.Sentiments;
import com.ecom.fyp2023.R;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.common.reflect.TypeToken;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.gson.Gson;

import org.tensorflow.lite.Interpreter;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class SentimentAnalysisActivity extends AppCompatActivity {

    private TextView sentimentResult, seeHistory, clearHistory;

    ImageView positive,negative;
    RecyclerView historyRecycler;
    private Interpreter tflite;
    private FirebaseFirestore db;


    //TODO: pass groupid
    //String groupId = GroupIdGlobalVariable.getInstance().getGlobalData();
    SentimentRvAdapter adapter;

    SharedPreferenceManager sharedPreferenceManager;

    ArrayList<Sentiments> sentimentsArrayList;

    private HashMap<String, Integer> vocabulary;

    String groupId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sentiment_analysis);


        sentimentResult = findViewById(R.id.textview);
        seeHistory = findViewById(R.id.history);
        clearHistory = findViewById(R.id.clearHistory);
        historyRecycler = findViewById(R.id.historyRecyclerview);
        negative = findViewById(R.id.negetive);
        positive = findViewById(R.id.positive);

        historyRecycler.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        sentimentsArrayList = new ArrayList<>();

        adapter = new SentimentRvAdapter(sentimentsArrayList, this);
        historyRecycler.setAdapter(adapter);


        sharedPreferenceManager = new SharedPreferenceManager(this);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        groupId = sharedPreferenceManager.getGroupId();

        try {
            loadModel(); // Load the TensorFlow Lite model during activity creation
            db = FirebaseFirestore.getInstance();
            vocabulary = loadVocabulary();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        db.collection("Comments").whereEqualTo("groupId", groupId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<String> allComments = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String comment = document.getString("comment");
                            allComments.add(comment);
                        }
                        loadModel();
                        // Perform sentiment analysis on all comments collectively
                        analyzeComments(allComments);
                    } else {
                        System.out.println("Error getting documents: " + task.getException());
                    }
                });

        clearHistory.setOnClickListener(v -> {
            clearHistory();
            historyRecycler.setVisibility(View.INVISIBLE);
            adapter.update(sentimentsArrayList);
        });

        seeHistory.setOnClickListener(v -> {
            historyRecycler.setVisibility(View.VISIBLE);
            fetchSentimentsFromFirestore();
        });
    }

    // Method to load the TensorFlow Lite model
    private void loadModel() {
        try {
            tflite = new Interpreter(loadModelFile());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        // Release TensorFlow Lite interpreter resources
        if (tflite != null) {
            tflite.close();
            tflite = null;
        }
        super.onDestroy();
    }

    // Method to load the TensorFlow Lite model file
    private MappedByteBuffer loadModelFile() throws IOException {
        AssetFileDescriptor fileDescriptor = this.getAssets().openFd("model.tflite");
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }
    // Method to read the vocabulary JSON file and parse it into a HashMap
    private HashMap<String, Integer> loadVocabulary() throws IOException {
        AssetManager assetManager = getAssets();
        InputStream inputStream = assetManager.open("vocabulary.json");
        int size = inputStream.available();
        byte[] buffer = new byte[size];
        inputStream.read(buffer);
        inputStream.close();
        String json = new String(buffer, StandardCharsets.UTF_8);
        Gson gson = new Gson();
        Type type = new TypeToken<HashMap<String, Integer>>() {}.getType();
        return gson.fromJson(json, type);
    }

    // Method to preprocess comments for sentiment analysis
    @NonNull
    private float[] preprocessComment(String comment) {
        // Preprocess the comment similar to your Python code
        comment = comment.replaceAll("http\\S+|\\d+|[^A-Za-z\\s]", "");
        comment = comment.toLowerCase();
        // Handle contractions here
        comment = handleContractions(comment);
        // Tokenize text
        String[] tokens = comment.split("\\s+");
        // Remove stopwords
        List<String> stopWords = Arrays.asList("a", "an", "the", "and", "or", "but", "is", "are", "am", "it", "this", "that", "of",
                "from", "in", "on", "at", "to", "with", "by", "for", "about", "into", "over", "after", "below", "under", "above", "out", "in", "off", "on");
        StringBuilder processedComment = new StringBuilder();
        for (String token : tokens) {
            if (!stopWords.contains(token)) {
                processedComment.append(token).append(" ");
            }
        }
        // Convert processedComment to float array and return
        return convertToFloatArray(processedComment.toString());
    }

    @NonNull
    private String handleContractions(String text) {
        text = text.replace("can't", "can not");
        text = text.replace("won't", "will not");
        text = text.replace("n't", " not");
        text = text.replace("'re", " are");
        text = text.replace("'s", " is");
        text = text.replace("'d", " would");
        text = text.replace("'ll", " will");
        text = text.replace("'t", " not");
        text = text.replace("'ve", " have");
        text = text.replace("'m", " am");
        text = text.replace("'ain't", " am not");
        text = text.replace("'shan't", " shall not");
        text = text.replace("'sha'n't", " shall not");
        text = text.replace("'ma'am", " madam");
        text = text.replace("'o'clock", " of the clock");
        text = text.replace("'ne'er", " never");
        text = text.replace("'o'er", " over");
        text = text.replace("'ol'", " old");
        return text;
    }

    // Method to convert processed comment to a float array
    @NonNull
    private float[] convertToFloatArray(@NonNull String text) {
        String[] words = text.split("\\s+");
        float[] oneHot = new float[1500];
        for (String word : words) {
            int index = getWordIndex(word);
            if (index != -1) {
                oneHot[index] = 1;
            }
        }
        return oneHot;
    }

    // Method to get the index of the word in the vocabulary
    private int getWordIndex(String word) {
        // Return -1 if the word is not in the vocabulary
        if (vocabulary.containsKey(word)) {
            return vocabulary.get(word);
        } else {
            return -1;
        }
    }

    private void analyzeComments(@NonNull List<String> allComments) {
        if (allComments.isEmpty()) {
            // No comments found for sentiment analysis
            sentimentResult.setText("No comments for Sentiment analysis.");
            return;
        }

        boolean hasRecognizedWords = false; // Flag to track if any recognized words are found
        boolean isPositive = false; // Flag to track if sentiment is positive

        float[] preprocessedComment = preprocessComment(allComments.toString());
        //seeHistory.setText(allComments.toString());
        boolean containsRecognizedWords = checkRecognizedWords(preprocessedComment);

        if (containsRecognizedWords) {
            hasRecognizedWords = true; // Set the flag to true if recognized words are found

            float[][] outputVal = new float[1][1];
            tflite.run(preprocessedComment, outputVal);
            int predictedSentiment = (outputVal[0][0] > 0.5) ? 1 : 0;
            isPositive = predictedSentiment == 0;

            String sentiment = isPositive ? "Positive" : "Negative";
            sentimentResult.setText(sentiment);
            saveSentimentAndTimestamp(sentiment);
        }

        // Toggle visibility of image views based on sentiment
        positive.setVisibility(isPositive ? View.VISIBLE : View.GONE);
        negative.setVisibility(isPositive ? View.GONE : View.VISIBLE);

        // If no recognized words are found in any comment, set sentiment to neutral
        if (!hasRecognizedWords) {
            sentimentResult.setText("Neutral or Comment(s) cannot be recognized");
        }
    }


    //    // Method to analyze comments for sentiment
//    private void analyzeComments(@NonNull List<String> allComments) {
//        if (allComments.isEmpty()) {
//            // No comments found for sentiment analysis
//            sentimentResult.setText("No comments for Sentiment analysis.");
//            return;
//        }
//        boolean hasRecognizedWords = false; // Flag to track if any recognized words are found
//        //for (String comment : allComments) {
//        // Perform sentiment analysis on each comment
//        float[] preprocessedComment = preprocessComment(allComments.toString());
//        seeHistory.setText(allComments.toString());
//        boolean containsRecognizedWords = checkRecognizedWords(preprocessedComment);
//        if (containsRecognizedWords) {
//            hasRecognizedWords = true; // Set the flag to true if recognized words are found
//            float[][] outputVal = new float[1][1];
//            tflite.run(preprocessedComment, outputVal);
//            int predictedSentiment = (outputVal[0][0] > 0.5) ? 1 : 0;
//            String sentiment = predictedSentiment == 0 ? "Positive" : "Negative";
//            sentimentResult.setText( sentiment);
//            saveSentimentAndTimestamp(sentiment);
//        }
//        // If no recognized words are found in any comment, set sentiment to neutral
//        if (!hasRecognizedWords) {
//            sentimentResult.setText("Neutral or Comment(s) can not be recognised");
//        }
//    }
    // Method to check if the preprocessed comment contains recognized words
    private boolean checkRecognizedWords(@NonNull float[] preprocessedComment) {
        for (float word : preprocessedComment) {
            if (word == 1) {
                // If at least one word is recognized (indicated by 1 in oneHot encoding), return true
                return true;
            }
        }
        // No recognized words are found
        return false;
    }

    // Method to clear history
    private void clearHistory() {
        // Get all documents from Sentiments collection
        db.collection("Sentiments").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Loop through each document and delete it
                for (QueryDocumentSnapshot document : task.getResult()) {
                    document.getReference().delete();
                }
            } else {
                Log.d("TAG", "Error getting documents: ", task.getException());
            }
        });
    }

    // Method to fetch sentiments from Firestore
    private void fetchSentimentsFromFirestore() {
        Query query = db.collection("Sentiments").whereEqualTo("groupId", groupId)
                .orderBy("timestamp", Query.Direction.DESCENDING);

        query.addSnapshotListener((value, error) -> {
            if (error != null) {
                Log.e("Firestore", "Error getting notes: " + error.getMessage());
                return;
            }
            if (value != null && !value.isEmpty()) {
                for (QueryDocumentSnapshot document : value) {
                    String sentiment = document.getString("sentiment");
                    Date timestamp = document.getDate("timestamp");

                    Sentiments sentiments = document.toObject(Sentiments.class);
                    if (sentiments != null) {
                        sentimentsArrayList.add(sentiments);
                        adapter.notifyDataSetChanged();
                    }

                    if (timestamp != null) {
                        clearHistory.setVisibility(View.VISIBLE);
                    }
                }
            }
            adapter.notifyDataSetChanged();
        });
    }

    // Method to save sentiment and timestamp in Firestore
    private void saveSentimentAndTimestamp(String sentiment) {
        // Get the current timestamp
        Date currentTimestamp = new Date();

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String userAuthId = currentUser != null ? currentUser.getUid() : "";

        // Create a new document in the "Sentiments" collection with sentiment and timestamp
        Map<String, Object> sentimentData = new HashMap<>();
        sentimentData.put("sentiment", sentiment);
        sentimentData.put("timestamp", currentTimestamp);
        sentimentData.put("groupId", groupId);

        db.collection("Sentiments")
                .add(sentimentData)
                .addOnSuccessListener(documentReference -> {
                    Log.d("SentimentHistory", "Sentiment document added with ID: " + documentReference.getId());
                })
                .addOnFailureListener(e -> {
                    Log.w("SentimentHistory", "Error adding sentiment document", e);
                });
    }
}
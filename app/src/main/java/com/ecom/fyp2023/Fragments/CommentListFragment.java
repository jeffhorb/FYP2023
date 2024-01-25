package com.ecom.fyp2023.Fragments;

import static com.ecom.fyp2023.ProjectActivity.projectId_key;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ecom.fyp2023.Adapters.CommentRVAdapter;
import com.ecom.fyp2023.ModelClasses.Comment;
import com.ecom.fyp2023.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class CommentListFragment extends BottomSheetDialogFragment {

    private static final String ARG_DATA_KEY = "data_key";

    ImageView send;
    EditText commentText;

    CommentRVAdapter commentRVAdapter;

    RecyclerView recyclerView;
    FirebaseFirestore fb;

    ArrayList<Comment> commentList;

    String commentId, projectId ,userId;
    private FirebaseAuth mAuth;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_comment_list, container, false);

        send = view.findViewById(R.id.sendImageView);
        commentText = view.findViewById(R.id.commentEditText);
        recyclerView = view.findViewById(R.id.commentsRecyclerView);

        fb = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        commentList = new ArrayList<>();

        LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext(), RecyclerView.VERTICAL,false);
        recyclerView.setLayoutManager(layoutManager);

        commentRVAdapter = new CommentRVAdapter(commentList,requireContext());
        recyclerView.setAdapter(commentRVAdapter);

        Bundle args = getArguments();
        if (args != null && args.containsKey(projectId_key)) {
            projectId = args.getString(projectId_key);
        }

        Bundle arguments = getArguments();
        if (arguments != null && arguments.containsKey(ARG_DATA_KEY)) {
            projectId = arguments.getString(ARG_DATA_KEY);
            fetchAndDisplayComments(projectId);

        }

        // Assuming 'send' is your ImageView for the send button
        send.setVisibility(View.INVISIBLE);

        commentText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {
                // No action needed before text changes
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                // Check if the comment text is empty
                if (TextUtils.isEmpty(charSequence)) {
                    send.setVisibility(View.INVISIBLE);
                } else {
                    send.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                // No action needed after text changes
            }
        });

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String comment = commentText.getText().toString();
                if (TextUtils.isEmpty(comment)) {
                    commentText.setError("Comment cannot be empty");
                    return;
                }

                AddComment(comment);
                commentText.setText(null);
            }
        });


        ImageView closeImageView = view.findViewById(R.id.closeImageView);
        // Set an OnClickListener to handle the close action
        closeImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        return view;
    }

    public void AddComment(String c) {



        CollectionReference dbComment = fb.collection("Comments");

        Comment comment = new Comment(c, com.google.firebase.Timestamp.now());
        dbComment.add(comment).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
            @Override
            public void onSuccess(DocumentReference documentReference) {

                userId = mAuth.getCurrentUser().getUid();
                commentId = documentReference.getId();

                addProjectComment(projectId, commentId, userId);

                //Toast.makeText(getActivity(), "Sent ", Toast.LENGTH_SHORT).show();
                //commentRVAdapter.notifyDataSetChanged();
            }
        }).addOnFailureListener(e -> Toast.makeText(getActivity(), "Failed \n" + e, Toast.LENGTH_SHORT).show());
    }

    private void addProjectComment(String projectId, String commentId, String userId) {
        // Creates a new userProjects document with an automatically generated ID
        Map<String, Object> proComment = new HashMap<>();

        proComment.put("projectId", projectId);
        proComment.put("commentId", commentId);
        proComment.put("userid", userId);
        proComment.put("timestamp", com.google.firebase.Timestamp.now());

        fb.collection("ProjectComments").add(proComment).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
            @Override
            public void onComplete(@NonNull Task<DocumentReference> task) {
                if (task.isSuccessful()) {
                    Log.d("projectComment", "projectComment added with ID: " + task.getResult().getId());
                } else {
                    Log.e("projectTasks", "Error adding projectComment", task.getException());
                }
            }
        });
    }
    @NonNull
    public static CommentListFragment newInstance(String data) {
        CommentListFragment fragment = new CommentListFragment();
        Bundle args = new Bundle();
        args.putString(ARG_DATA_KEY, data);
        fragment.setArguments(args);
        return fragment;
    }

    private void fetchAndDisplayComments(String projectId) {
        commentList.clear(); // Clear existing data

        CollectionReference projectCommentsCollection = fb.collection("ProjectComments");
        Query query = projectCommentsCollection
                .whereEqualTo("projectId", projectId)
                .orderBy("timestamp", Query.Direction.DESCENDING);

        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        String commentId = document.getString("commentId");
                        retrieveCommentDetails(commentId);

                    }
                    commentRVAdapter.notifyDataSetChanged();

                } else {
                    Log.e("CommentListFragment", "Error fetching comments", task.getException());
                    Toast.makeText(getActivity(), "Error fetching comment", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    private void retrieveCommentDetails(String commentId) {
        CollectionReference commentsCollection = fb.collection("Comments");
        commentsCollection.document(commentId).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                Comment commentData = document.toObject(Comment.class);
                                commentList.add(commentData);

                                commentRVAdapter.notifyDataSetChanged();

                            }  // Toast.makeText(getActivity(), "no comments", Toast.LENGTH_SHORT).show();

                        } else {
                            Toast.makeText(getContext(), "Error fetching the comments ", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

}
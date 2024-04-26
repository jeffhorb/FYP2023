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
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ecom.fyp2023.Adapters.CommentRVAdapter;
import com.ecom.fyp2023.AppManagers.SharedPreferenceManager;
import com.ecom.fyp2023.ModelClasses.Comment;
import com.ecom.fyp2023.ModelClasses.Users;
import com.ecom.fyp2023.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommentListFragment extends BottomSheetDialogFragment {

    private static final String ARG_DATA_KEY = "data_key";

    ImageView send;
    EditText commentText;

    CommentRVAdapter commentRVAdapter;

    RecyclerView recyclerView;
    FirebaseFirestore fb;

    ArrayList<Comment> commentList;

    String commentId, projectId, userId;
    private FirebaseAuth mAuth;

    //String groupId = GroupIdGlobalVariable.getInstance().getGlobalData();

    SharedPreferenceManager sharedPrefManager;

    private String groupId;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_comment_list, container, false);

        send = view.findViewById(R.id.sendImageView);
        commentText = view.findViewById(R.id.commentEditText);
        recyclerView = view.findViewById(R.id.commentsRecyclerView);

        fb = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        commentList = new ArrayList<>();

        sharedPrefManager = new SharedPreferenceManager(requireContext());

        LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);

        commentRVAdapter = new CommentRVAdapter(commentList, requireContext());
        recyclerView.setAdapter(commentRVAdapter);

        groupId = sharedPrefManager.getGroupId();

        Bundle args = getArguments();
        if (args != null && args.containsKey(projectId_key)) {
            projectId = args.getString(projectId_key);
            fetchAndDisplayComments(projectId);
        }

        Bundle arguments = getArguments();
        if (arguments != null && arguments.containsKey(ARG_DATA_KEY)) {
            projectId = arguments.getString(ARG_DATA_KEY);
            fetchAndDisplayComments(projectId);

        }

        // Assuming 'send' is your ImageView for the send button
        send.setVisibility(View.INVISIBLE);

        // TextWatcher to detect '@' character at the beginning
        commentText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (start == 0 && count == 1 && s.charAt(0) == '@') {
                    showUserSelectionDialog();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        commentText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {
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
                //TODO: PASS GroupId into the variable
                String comment = commentText.getText().toString();
                userId = mAuth.getCurrentUser().getUid();
                String userEmail = mAuth.getCurrentUser().getEmail();
                AddComment(comment,userId,userEmail,groupId);
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

    public void AddComment(String c,String currentUserId,String userEmail,String groupId) {

        String cUserId = mAuth.getCurrentUser().getUid();

        FirebaseFirestore.getInstance().collection("Users")
                .whereEqualTo("userId", cUserId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        // User document found with matching auth ID
                        DocumentSnapshot userDocument = queryDocumentSnapshots.getDocuments().get(0);
                        String userName = userDocument.getString("userName");

                        CollectionReference dbComment = fb.collection("Comments");

                        Comment comment = new Comment(c, Calendar.getInstance().getTime(), currentUserId,userEmail,userName,groupId,null);
                        dbComment.add(comment).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                            @Override
                            public void onSuccess(DocumentReference documentReference) {

                                userId = mAuth.getCurrentUser().getUid();
                                commentId = documentReference.getId();
                                addProjectComment(projectId, commentId, userId);
                                addUserComment(userId,commentId);
                            }
                        }).addOnFailureListener(e -> Toast.makeText(getActivity(), "Failed \n" + e, Toast.LENGTH_SHORT).show());
                        // Now you can use the userName as needed
                        Log.d("UserName", "User name: " + userName);

                    } else {
                        // No user document found with matching auth ID
                        Log.d("UserName", "User document not found");
                    }
                })
                .addOnFailureListener(e -> {
                    // Handle failure in fetching user document
                    Log.e("FetchUserName", "Error fetching user document", e);
                });

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

    private void addUserComment(String userId, String commentId) {
        // Creates a new userProjects document with an automatically generated ID
        Map<String, Object> userComment = new HashMap<>();
        userComment.put("commentId", commentId);
        userComment.put("userid", userId);

        fb.collection("userComments").add(userComment).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
            @Override
            public void onComplete(@NonNull Task<DocumentReference> task) {
                if (task.isSuccessful()) {
                    Log.d("userComment", "userComment added with ID: " + task.getResult().getId());
                } else {
                    Log.e("userComment", "Error adding projectComment", task.getException());
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
    private void showUserSelectionDialog() {
        // Get the current user ID from authentication
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String currentUserId = currentUser != null ? currentUser.getUid() : null;

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        if (groupId == null) {
            Log.e("FetchUsers", "groupId is null");
            // Handle the case where groupId is null, such as displaying an error message
            return;
        }

        // Query the Groups collection to get the list of members for the specified group
        db.collection("groups")
                .document(groupId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        List<String> memberIds = (List<String>) documentSnapshot.get("members");
                        List<Users> userList = new ArrayList<>();

                        if (memberIds != null && !memberIds.isEmpty()) {
                            // Fetch user data for each member ID
                            for (String memberId : memberIds) {
                                db.collection("Users")
                                        .whereEqualTo("userId", memberId)
                                        .get()
                                        .addOnSuccessListener(querySnapshot -> {
                                            for (QueryDocumentSnapshot userDocument : querySnapshot) {
                                                Users user = userDocument.toObject(Users.class);

                                                // Check if the user is not the current user
                                                if (!user.getUserId().equals(currentUserId)) {
                                                    userList.add(user);
                                                }
                                            }

                                            // Create a string array to store user emails for dialog selection
                                            String[] userNames = new String[userList.size()];
                                            for (int i = 0; i < userList.size(); i++) {
                                                userNames[i] = userList.get(i).getUserName();
                                            }

                                            // Display the list of user emails in a dialog for selection
                                            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
                                            builder.setTitle("Select User");
                                            builder.setItems(userNames, (dialog, which) -> {
                                                // Append the selected user's email to the comment text
                                                String selectedUserName = userNames[which];
                                                String currentText = commentText.getText().toString();
                                                if (currentText.startsWith("@")) {
                                                    // If the text starts with '@', append the selected user's name directly
                                                    commentText.setText("@" + selectedUserName + " " + currentText.substring(1));
                                                } else {
                                                    // If no '@' symbol found, append the selected user's name with '@'
                                                    commentText.setText("@" + selectedUserName + " " + currentText);
                                                }
                                                // Move the cursor to the end of the text
                                                commentText.setSelection(commentText.getText().length());
                                            });
                                            builder.show();
                                        })
                                        .addOnFailureListener(e -> {
                                            // Handle failure to fetch user data
                                            Log.e("FetchUsers", "Failed to fetch user data: " + e.getMessage());
                                        });
                            }
                        }
                    } else {
                        // Handle case where document does not exist
                        Log.e("FetchUsers", "Group document does not exist");
                    }
                })
                .addOnFailureListener(e -> {
                    // Handle failure in fetching groups
                    Log.e("FetchUsers", "Error fetching group: " + e.getMessage());
                    Toast.makeText(requireContext(), "Error fetching group", Toast.LENGTH_SHORT).show();
                });
    }

        private void fetchAndDisplayComments(String projectId) {
        CollectionReference projectCommentsCollection = fb.collection("ProjectComments");
        Query query = projectCommentsCollection
                .whereEqualTo("projectId", projectId)
                .orderBy("timestamp", Query.Direction.DESCENDING);

        query.addSnapshotListener((value, error) -> {
            if (error != null) {
                Log.e("Firestore", "Error getting comments: " + error.getMessage());
                Toast.makeText(getActivity(), "Error getting comments: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                return;
            }

            if (value != null) {
                List<Comment> commentList = new ArrayList<>();
                for (QueryDocumentSnapshot document : value) {
                    String commentId = document.getString("commentId");
                    retrieveProjectCommentDetails(commentId, commentList);
                }
            }

            Log.d("Firestore", "Fetching comment for project: " + projectId);
        });
    }

    private void retrieveProjectCommentDetails(String commentId, List<Comment> commentList) {
        CollectionReference commentsCollection = fb.collection("Comments");
        commentsCollection.document(commentId).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            Comment commentData = document.toObject(Comment.class);
                            commentList.add(commentData);

                            Log.d("Firestore", "Retrieved Comment Data: " + commentData);

                            commentList.sort((o1, o2) -> o2.getTimestamp().compareTo(o1.getTimestamp()));

                            // Update the adapter with the comment list after all comments are retrieved
                            commentRVAdapter.updateList(commentList);
                        } else {
                            Log.e("Firestore", "Document does not exist for commentId: " + commentId);
                        }
                    } else {
                        Log.e("Firestore", "Error fetching Comments: " + task.getException());
                        Toast.makeText(getActivity(), "Error fetching Comments", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
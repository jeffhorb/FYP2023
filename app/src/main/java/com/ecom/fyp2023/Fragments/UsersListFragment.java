package com.ecom.fyp2023.Fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ecom.fyp2023.Adapters.UserRVAdapter;
import com.ecom.fyp2023.AppManagers.SharedPreferenceManager;
import com.ecom.fyp2023.ModelClasses.Users;
import com.ecom.fyp2023.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.jetbrains.annotations.Contract;

import java.util.ArrayList;
import java.util.List;

public class UsersListFragment extends BottomSheetDialogFragment {

    @NonNull
    @Contract(" -> new")
    public static UsersListFragment newInstance() {
        return new UsersListFragment();
    }

    private UserRVAdapter userAdapter;
    private List<Users> userList;

    String proId,taskId;
    SharedPreferenceManager sharedPrefManager;

    String groupId;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_users_list, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.userRecyclerview);
        userList = new ArrayList<>();

        LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);

        userAdapter = new UserRVAdapter(userList, requireContext(),this);
        recyclerView.setAdapter(userAdapter);

        sharedPrefManager = new SharedPreferenceManager(requireContext());

        groupId = sharedPrefManager.getGroupId();

        ImageView closeImageView = view.findViewById(R.id.closeImageView);

        // Set an OnClickListener to handle the close action
        closeImageView.setOnClickListener(v -> {
            // Close the BottomSheetDialogFragment when the close icon is clicked
            dismiss();
        });

        //intent from ProjectActivity
        Bundle argument1 = getArguments();
        if (argument1 != null && argument1.containsKey("proTid")) {
            proId = argument1.getString("proTid");
            userAdapter.setSelectedProjectId(proId);
            fetchGroupMember(groupId);
        }

        Bundle bundle = getArguments();
        if (bundle != null && bundle.containsKey("TASKID")) {
            taskId = bundle.getString("TASKID");
            userAdapter.setSelectedTaskId(taskId);
            fetchGroupMember(groupId);
        }

        if(getArguments() != null && getArguments().containsKey("groupId")&& getArguments().containsKey("groupName")&&getArguments().containsKey("groupDes")){
            //String groupId = getArguments().getString("groupId");
            SharedPreferenceManager sharedPreferenceManager = new SharedPreferenceManager(getContext());
            String groupId = sharedPreferenceManager.getGroupId();
            String groupName = getArguments().getString("groupName");
            String groupDescription = getArguments().getString("groupDes");
            userAdapter.setGroupId(groupId);
            userAdapter.setGroupName(groupName);
            userAdapter.setGroupDescription(groupDescription);
            fetchUsers(groupId);
        }


    return view;
    }



    private void fetchGroupMember(String groupId) {
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

                        if (memberIds != null && !memberIds.isEmpty()) {
                            // Fetch user data for each member ID
                            for (String memberId : memberIds) {
                                db.collection("Users")
                                        .whereEqualTo("userId", memberId)
                                        .get()
                                        .addOnSuccessListener(querySnapshot -> {
                                            for (QueryDocumentSnapshot userDocument : querySnapshot) {
                                                Users user = userDocument.toObject(Users.class);
                                                userList.add(user);
                                            }
                                            userAdapter.notifyDataSetChanged();
                                        })
                                        .addOnFailureListener(e -> {
                                            // Handle failure to fetch user data
                                            Log.e("userList", "Failed to fetch user data: " + e.getMessage());
                                        });
                            }
                        } else {
                            //case where the group has no members
                        }
                    } else {
                        // Handle case where the group document does not exist
                    }
                })
                .addOnFailureListener(e -> {
                    // Handle failure to fetch group data
                    Log.e("userList", "Failed to fetch group data: " + e.getMessage());
                });
    }



    private void fetchUsers(String groupId) {
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

                        // Fetch all users from the "Users" collection
                        db.collection("Users")
                                .get()
                                .addOnSuccessListener(querySnapshot -> {
                                    for (QueryDocumentSnapshot userDocument : querySnapshot) {
                                        Users user = userDocument.toObject(Users.class);
                                        String userId = userDocument.getString("userId"); // Assuming the document ID is the user ID

                                        // Check if the user ID is not in the list of member IDs
                                        assert memberIds != null;
                                        if (!memberIds.contains(userId)) {
                                            userList.add(user);
                                        }
                                    }
                                    userAdapter.notifyDataSetChanged();
                                })
                                .addOnFailureListener(e -> {
                                    // Handle failure to fetch user data
                                    Log.e("userList", "Failed to fetch user data: " + e.getMessage());
                                });
                    } else {
                        // Handle case where the group document does not exist
                    }
                })
                .addOnFailureListener(e -> {
                    // Handle failure to fetch group data
                    Log.e("userList", "Failed to fetch group data: " + e.getMessage());
                });
    }



//    private void fetchUsers(String groupId) {
//        FirebaseFirestore db = FirebaseFirestore.getInstance();
//
//        if (groupId == null) {
//            Log.e("FetchUsers", "groupId is null");
//            // Handle the case where groupId is null, such as displaying an error message
//            return;
//        }
//
//        // Query the Groups collection to get the list of members for the specified group
//        db.collection("groups")
//                .document(groupId)
//                .get()
//                .addOnSuccessListener(documentSnapshot -> {
//                    if (documentSnapshot.exists()) {
//                        List<String> memberIds = (List<String>) documentSnapshot.get("members");
//
//                        if (memberIds != null && !memberIds.isEmpty()) {
//                            // Fetch user data for each member ID
//                            for (String memberId : memberIds) {
//                                db.collection("Users")
//                                        .get()
//                                        .addOnSuccessListener(querySnapshot -> {
//                                            for (QueryDocumentSnapshot userDocument : querySnapshot) {
//                                                Users user = userDocument.toObject(Users.class);
//                                                String userId = userDocument.getString("userId");
//
//                                                if (!memberId.equals(userId)){
//                                                    userList.add(user);
//                                                }
//
//                                            }
//                                            userAdapter.notifyDataSetChanged();
//                                        })
//                                        .addOnFailureListener(e -> {
//                                            // Handle failure to fetch user data
//                                            Log.e("userList", "Failed to fetch user data: " + e.getMessage());
//                                        });
//                            }
//                        } else {
//                            //case where the group has no members
//                        }
//                    } else {
//                        // Handle case where the group document does not exist
//                    }
//                })
//                .addOnFailureListener(e -> {
//                    // Handle failure to fetch group data
//                    Log.e("userList", "Failed to fetch group data: " + e.getMessage());
//                });
//    }


//    private void fetchUserData() {
//        FirebaseFirestore db = FirebaseFirestore.getInstance();
//
//        db.collection("Users")
//                .get()
//                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
//                    @Override
//                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
//                        if (task.isSuccessful()) {
//                            for (QueryDocumentSnapshot document : task.getResult()) {
//                                Users user = document.toObject(Users.class);
//                                userList.add(user);
//                            }
//                            userAdapter.notifyDataSetChanged();
//                        }  // Handle errors
//
//                    }
//                });
//    }
}
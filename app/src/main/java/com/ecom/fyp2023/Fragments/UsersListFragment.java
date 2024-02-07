package com.ecom.fyp2023.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ecom.fyp2023.Adapters.UserRVAdapter;
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

    private RecyclerView recyclerView;
    private UserRVAdapter userAdapter;
    private List<Users> userList;

    String proId,taskId;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_users_list, container, false);

        recyclerView = view.findViewById(R.id.userRecyclerview);
        userList = new ArrayList<>();

        LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);

        userAdapter = new UserRVAdapter(userList, requireContext());
        recyclerView.setAdapter(userAdapter);

        // Fetch user data from Firestore
        fetchUserData();

        Bundle arguments = getArguments();
        if (arguments != null && arguments.containsKey("proID")) {
            proId = arguments.getString("proID");
            //userAdapter.setSelectedProjectId(proId);
        }

        Bundle bundle = getArguments();
        if (bundle != null && bundle.containsKey("TASKID")) {
            taskId = bundle.getString("TASKID");
            userAdapter.setSelectedTaskId(taskId);
        }


    return view;
    }
    private void fetchUserData() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("Users")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Users user = document.toObject(Users.class);
                                userList.add(user);
                            }
                            userAdapter.notifyDataSetChanged();
                        } else {
                            // Handle errors
                        }
                    }
                });
    }
}
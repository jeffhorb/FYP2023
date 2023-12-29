package com.ecom.fyp2023;

import static com.ecom.fyp2023.BottomSheetDialogAddProject.MESSAGE_KEY;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class ProjectActivity extends AppCompatActivity {
    private RecyclerView projectsRv;
    private ArrayList<Tasks> projectsArrayList;
    //private TasksRvAdapter projectsRVAdapter;
    private FirebaseFirestore db;
    public static final String projectId_key="proId";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project);

        String receivedProId = getIntent().getStringExtra(MESSAGE_KEY);

        BottomSheetFragmentAddTask taskFragment = BottomSheetFragmentAddTask.newInstance();
        Bundle bundle = new Bundle();
        bundle.putString(projectId_key, receivedProId); // Replace "key" with a unique identifier
        taskFragment.setArguments(bundle);
        taskFragment.show(getSupportFragmentManager(), taskFragment.getTag());

        // Handle the received data as needed
        Log.d("ProjectActivity", "Received data from fragment: " + receivedProId);

    }

}
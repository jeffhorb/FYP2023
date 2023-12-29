package com.ecom.fyp2023;

import static com.ecom.fyp2023.ProjectActivity.projectId_key;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;


public class BottomSheetFragmentAddTask extends BottomSheetDialogFragment {

    EditText progress;
    EditText estimatedTime;
    EditText details;
    EditText difficulty;
    Button save;
    String taskId, projectId;
    FirebaseFirestore fb;

    public static BottomSheetFragmentAddTask newInstance() {
        return new BottomSheetFragmentAddTask();
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bottom_sheet_add_task, container, false);

        details = view.findViewById(R.id.taskDetails);
        estimatedTime = view.findViewById(R.id.estimatedTime);
        difficulty = view.findViewById(R.id.taskDif);
        progress = view.findViewById(R.id.taskProgress);
        save = view.findViewById(R.id.saveBtn);

        fb = FirebaseFirestore.getInstance();

        Bundle arguments = getArguments();
        if (arguments != null) {

           projectId = arguments.getString(projectId_key);

        }

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String detls = details.getText().toString();
                String estTime = estimatedTime.getText().toString();
                String diff = difficulty.getText().toString();
                String progrs = progress.getText().toString();


               /* if (TextUtils.isEmpty(detls)) {
                    details.setError("Field required");
                }
                else if (TextUtils.isEmpty(estTime)) {
                    estimatedTime.setError("Field required");
                }
                else if (TextUtils.isEmpty(progrs)) {
                    progress.setError("Field required");
                }
                else if ( !diff.equalsIgnoreCase("low")|| !diff.equalsIgnoreCase("medium")||!!diff.equalsIgnoreCase("high")) {
                    difficulty.setError("Difficulty can only be high, medium or low");
                }
                else {*/
                    saveTasks(detls, diff, progrs, estTime);
                //}

                details.setText(null);
                estimatedTime.setText(null);
                difficulty.setText(null);
                progress.setText(null);

            }
        });
        return view;

    }


    public void saveTasks(String d, String diff, String prog, String estT) {


        CollectionReference dbTasks = fb.collection("Tasks");

        Tasks tasks = new Tasks(d, diff, prog, estT);
        dbTasks.add(tasks).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
            @Override
            public void onSuccess(DocumentReference documentReference) {


                Toast.makeText(getActivity(), "tasks saved", Toast.LENGTH_SHORT).show();
                taskId = documentReference.getId();
                addProjectTask(projectId, taskId);

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                Toast.makeText(getActivity(), "Failed \n" + e, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addProjectTask(String projectId, String taskId) {
        // Creates a new userProjects document with an automatically generated ID
        Map<String, Object> projectTasks = new HashMap<>();

        projectTasks.put("projectId", projectId);
        projectTasks.put("taskId", taskId);
        fb.collection("projectTasks").add(projectTasks).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
            @Override
            public void onComplete(@NonNull Task<DocumentReference> task) {
                if (task.isSuccessful()) {
                    Log.d("projectTasks", "projectTasks added with ID: " + task.getResult().getId());
                } else {
                    Log.e("projectTasks", "Error adding userProject", task.getException());
                }
            }
        });
    }

}
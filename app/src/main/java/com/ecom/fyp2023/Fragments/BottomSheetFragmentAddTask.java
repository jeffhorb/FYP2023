package com.ecom.fyp2023.Fragments;

import static com.ecom.fyp2023.ProjectActivity.p_key;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.ecom.fyp2023.ModelClasses.Tasks;
import com.ecom.fyp2023.ProjectActivity;
import com.ecom.fyp2023.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.jetbrains.annotations.Contract;

import java.util.HashMap;
import java.util.Map;


public class BottomSheetFragmentAddTask extends BottomSheetDialogFragment {

    Spinner progress,difficulty;
    EditText estimatedTime,details;
    Button save;
    String taskId, projectId;
    FirebaseFirestore fb;

    @NonNull
    @Contract(" -> new")
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

        Bundle args = getArguments();
        if (args != null && args.containsKey(ProjectActivity.projectId_key)) {
            projectId = args.getString(ProjectActivity.projectId_key);
        }

        Bundle arguments = getArguments();
        if (arguments != null && arguments.containsKey(p_key)) {
            projectId = arguments.getString(p_key);

        }

        ImageView closeImageView = view.findViewById(R.id.closeImageView);

        // Set an OnClickListener to handle the close action
        closeImageView.setOnClickListener(v -> {
            // Close the BottomSheetDialogFragment when the close icon is clicked
            dismiss();
        });

        save.setOnClickListener(v -> {

            String detls = details.getText().toString();
            String estTime = estimatedTime.getText().toString();
            String diff = difficulty.getSelectedItem().toString();
            String progrs = progress.getSelectedItem().toString();


            if (TextUtils.isEmpty(detls)) {
                details.setError("Field required");
            }
            else if (TextUtils.isEmpty(estTime)) {
                estimatedTime.setError("Field required");
            }
            else {
                saveTasks(detls, diff, progrs, estTime);
            }
            details.setText(null);
            estimatedTime.setText(null);
        });
        return view;

    }

    public void saveTasks(String d, String diff, String prog, String estT) {

        CollectionReference dbTasks = fb.collection("Tasks");

        Tasks tasks = new Tasks(d, diff, prog, estT);
        dbTasks.add(tasks).addOnSuccessListener(documentReference -> {


            Toast.makeText(getActivity(), "tasks saved", Toast.LENGTH_SHORT).show();
            taskId = documentReference.getId();
            addProjectTask(projectId, taskId);

        }).addOnFailureListener(e -> Toast.makeText(getActivity(), "Failed \n" + e, Toast.LENGTH_SHORT).show());
    }

    private void addProjectTask(String projectId, String taskId) {
        // Creates a new userProjects document with an automatically generated ID
        Map<String, Object> projectTasks = new HashMap<>();

        projectTasks.put("projectId", projectId);
        projectTasks.put("taskId", taskId);
        projectTasks.put("timestamp", com.google.firebase.Timestamp.now());

        fb.collection("projectTasks").add(projectTasks).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.d("projectTasks", "projectTasks added with ID: " + task.getResult().getId());
            } else {
                Log.e("projectTasks", "Error adding userProject", task.getException());
            }
        });
    }

}
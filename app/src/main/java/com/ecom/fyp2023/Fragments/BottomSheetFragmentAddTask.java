package com.ecom.fyp2023.Fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
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

import com.ecom.fyp2023.AppManagers.TimeConverter;
import com.ecom.fyp2023.ModelClasses.Tasks;
import com.ecom.fyp2023.ProjectActivity;
import com.ecom.fyp2023.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.jetbrains.annotations.Contract;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public class BottomSheetFragmentAddTask extends BottomSheetDialogFragment {

    Spinner progress, difficulty;
    EditText  details;
    Button save;
    String taskId, projectId;
    FirebaseFirestore fb;

    @NonNull
    @Contract(" -> new")
    public static BottomSheetFragmentAddTask newInstance() {
        return new BottomSheetFragmentAddTask();
    }

    private OnEndDateUpdateListener endDateUpdateListener;

    public interface OnEndDateUpdateListener {
        void onEndDateUpdated(String updatedEndDate);

    }

    public void setOnEndDateUpdateListener(OnEndDateUpdateListener listener) {
        this.endDateUpdateListener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bottom_sheet_add_task, container, false);

        details = view.findViewById(R.id.taskDetails);
        //estimatedTime = view.findViewById(R.id.estimatedTime);
        TextInputEditText estimatedTime = (TextInputEditText) view.findViewById(R.id.estimatedTime);
        difficulty = view.findViewById(R.id.taskDif);
        progress = view.findViewById(R.id.taskProgress);
        save = view.findViewById(R.id.saveBtn);


        fb = FirebaseFirestore.getInstance();

        Bundle args = getArguments();
        if (args != null && args.containsKey(ProjectActivity.projectId_key)) {
            projectId = args.getString(ProjectActivity.projectId_key);
        }

        Bundle arguments = getArguments();
        if (arguments != null && arguments.containsKey(ProjectActivity.p_key)) {
            projectId = arguments.getString(ProjectActivity.p_key);

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
            } else if (TextUtils.isEmpty(estTime)) {
                estimatedTime.setError("Field required");
            } else if (!isValidEstimationFormat(estTime)) {
                estimatedTime.setError("Invalid format. Use a number followed by 'd' or 'w'.");
            } else {
                saveTasks(detls, diff, progrs, estTime);
                // Clear the input fields after successful save

                details.setText(null);
                estimatedTime.setText(null);
            }
        });
        return view;
    }

    // Function to check if estTime has a valid format
    private boolean isValidEstimationFormat(@NonNull String estTime) {
        String regex = "\\d+[dw]";
        return estTime.matches(regex);
    }

    public void saveTasks(String d, String diff, String prog, String estT) {

        CollectionReference dbTasks = fb.collection("Tasks");

        Tasks tasks = new Tasks(d, diff, prog, estT);
        dbTasks.add(tasks).addOnSuccessListener(documentReference -> {

            Toast.makeText(getActivity(), "Task saved", Toast.LENGTH_SHORT).show();
            taskId = documentReference.getId();
            addProjectTask(projectId, taskId);

            tasks.setTaskId(taskId);

            // Calculate total estimated time for associated tasks
            calculateTotalEstimatedTimeAndEndDate(projectId);

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

    private void calculateTotalEstimatedTimeAndEndDate(String projectId) {
        fb.collection("projectTasks")
                .whereEqualTo("projectId", projectId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        AtomicLong totalEstimatedTime = new AtomicLong();

                        for (DocumentSnapshot document : task.getResult()) {
                            String taskId = document.getString("taskId");

                            fb.collection("Tasks")
                                    .document(taskId)
                                    .get()
                                    .addOnCompleteListener(taskDocumentTask -> {
                                        if (taskDocumentTask.isSuccessful()) {
                                            DocumentSnapshot taskDocument = taskDocumentTask.getResult();

                                            if (taskDocument != null && taskDocument.exists()) {
                                                String estimatedTime = taskDocument.getString("estimatedTime");

                                                if (estimatedTime != null) {
                                                    long taskDays = TimeConverter.convertToDays(estimatedTime);
                                                    totalEstimatedTime.addAndGet(taskDays);

                                                    Log.d("CalculateTime", "Task processed. Total estimated time so far: " + totalEstimatedTime);

                                                    // Update the end date of the project based on the total estimated time
                                                    updateProjectEndDate(projectId, totalEstimatedTime.get());
                                                } else {
                                                    Log.e("CalculateTime", "Estimated time is null for task: " + taskId);
                                                }
                                            } else {
                                                Log.e("CalculateTime", "Task document is null or doesn't exist for taskId: " + taskId);
                                            }
                                        } else {
                                            // Handle failure in fetching task document
                                            Log.e("CalculateTime", "Error fetching task document", taskDocumentTask.getException());
                                        }
                                    });
                        }
                    } else {
                        // Handle failure in fetching project tasks
                        Log.e("CalculateTime", "Error fetching project tasks", task.getException());
                    }
                });
    }

    private void updateProjectEndDate(String projectId, long totalEstimatedTime) {
        fb.collection("Projects")
                .document(projectId)
                .get()
                .addOnCompleteListener(projectDocumentTask -> {
                    if (projectDocumentTask.isSuccessful()) {
                        DocumentSnapshot projectDocument = projectDocumentTask.getResult();
                        if (projectDocument.exists()) {
                            String startDate = projectDocument.getString("startDate");
                            String updatedEndDate = calculateEndDate(startDate, totalEstimatedTime);

                            fb.collection("Projects")
                                    .document(projectId)
                                    .update("endDate", updatedEndDate)
                                    .addOnCompleteListener(updateTask -> {
                                        if (updateTask.isSuccessful()) {
                                            Log.d("UpdateProject", "Project end date updated successfully");
                                            endDateUpdateListener.onEndDateUpdated(updatedEndDate);
                                        } else {
                                            Log.e("UpdateProject", "Error updating project end date", updateTask.getException());
                                        }
                                    });
                        }
                    } else {
                        // Handle failure in fetching project document
                        Log.e("UpdateProject", "Error fetching project document", projectDocumentTask.getException());
                    }
                });
    }

    @Nullable
    private String calculateEndDate(String startDate, long totalEstimatedTime) {
        try {
            // Parse the start date
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
            Date parsedStartDate = sdf.parse(startDate);

            // Calculate end date based on start date and total estimated time
            if (parsedStartDate != null) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(parsedStartDate);
                calendar.add(Calendar.DAY_OF_YEAR, (int) totalEstimatedTime);

                // Format the updated end date
                return sdf.format(calendar.getTime());
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return null;
    }
}

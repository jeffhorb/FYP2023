package com.ecom.fyp2023.Fragments;

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
import androidx.core.content.ContextCompat;

import com.ecom.fyp2023.Adapters.TasksRVAdapter;
import com.ecom.fyp2023.AppManagers.TimeConverter;
import com.ecom.fyp2023.ModelClasses.Tasks;
import com.ecom.fyp2023.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.jetbrains.annotations.Contract;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class UpdateTaskFragment extends BottomSheetDialogFragment implements BottomSheetFragmentAddTask.OnEndDateUpdateListener {

    EditText taskDetails;

    Spinner taskDifficulty,duration;
    String  progress,proId;
    private FirebaseFirestore fb;
    Tasks tasks;

    @NonNull
    @Contract(" -> new")
    public static UpdateTaskFragment newInstance() {

        return new UpdateTaskFragment();
    }


    private BottomSheetFragmentAddTask.OnEndDateUpdateListener endDateUpdateListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_update_task, container, false);
        view.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white));

        endDateUpdateListener = this;

        fb = FirebaseFirestore.getInstance();

        taskDetails = view.findViewById(R.id.updateTaskDetails);
        TextInputEditText estimatedTime = (TextInputEditText) view.findViewById(R.id.updateEstimatedTime);
        taskDifficulty = view.findViewById(R.id.updateTaskDif);
        duration = view.findViewById(R.id.upTaskDuration);

        Button updateTaskBtn = view.findViewById(R.id.updateBtn);

        Bundle args = getArguments();
        if (args != null && args.containsKey("pId2")) {
            proId = args.getString("pId2");
        }

        Bundle arguments = getArguments();
        if (arguments != null && arguments.containsKey("pId")) {
            proId = arguments.getString("pId");
        }

        Bundle arguments1 = getArguments();
        if (arguments1 != null && arguments1.containsKey("pro_key")) {
            proId = arguments1.getString("pro_key");
        }

        Bundle bundle = getArguments();
        if (bundle != null && bundle.containsKey("tasks")) {
        tasks = (Tasks) bundle.getSerializable("tasks");
        }

        //from TaskActivity
        Bundle bundle1 = getArguments();
        if (bundle1 != null && bundle1.containsKey("selectT")) {
            tasks = (Tasks) bundle.getSerializable("selectT");
        }

        assert tasks != null;
        taskDetails.setText(tasks.getTaskDetails());
        //estimatedTime.setText(tasks.getEstimatedTime());

        String diffVal = tasks.getDifficulty();
        String[] diffSpinnerItems = getResources().getStringArray(R.array.tasksDifficulty);
        int position = Arrays.asList(diffSpinnerItems).indexOf(diffVal);
        taskDifficulty.setSelection(position);

        ImageView closeImageView = view.findViewById(R.id.closeImageView);

        // Set an OnClickListener to handle the close action
        closeImageView.setOnClickListener(v -> {
            // Close the BottomSheetDialogFragment when the close icon is clicked
            dismiss();
        });

        updateTaskBtn.setOnClickListener(v -> {
            String detls = taskDetails.getText().toString();
            String estTime = estimatedTime.getText().toString();
            String diff = taskDifficulty.getSelectedItem().toString();
            String selectedText = duration.getSelectedItem().toString();
            String newText = estTime + selectedText;

            estimatedTime.setText(newText);
            estimatedTime.setSelection(estimatedTime.length());

            if (TextUtils.isEmpty(detls)) {
                taskDetails.setError("Field required");
            } else if (!isValidEstimationFormat(newText)) {
                estimatedTime.setError("Invalid format. Use a number followed by duration");
            } else {
                // Call the updateTask method
                updateTask(tasks, detls, diff, progress, newText);
                dismiss();
            }
        });
        return view;
    }

    private boolean isValidEstimationFormat(@NonNull String estTime) {
        String regex = "\\d+(day|week)";
        return estTime.matches(regex);
    }

    private void updateTask(@NonNull Tasks task, String taskDetails, String tasksDiff, String progres, String taskEtime) {

        String existingProgress = task.getProgress();

        // Create the updated task with the existing progress value
        Tasks updateTasks = new Tasks(taskDetails, tasksDiff, existingProgress, taskEtime);

        fb.collection("Tasks")
                .document(task.getTaskId())
                .set(updateTasks)
                .addOnSuccessListener(aVoid -> {
                    Log.d("projectid", "proid " +proId);

                    calculateTotalEstimatedTimeAndEndDate(proId);
                    Toast.makeText(requireContext(), "Task has been updated.", Toast.LENGTH_SHORT).show();

                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), "Failed to update the task.", Toast.LENGTH_SHORT).show();
                    Log.e("UpdateTask", "Error updating task", e);
                });
    }

    private void calculateTotalEstimatedTimeAndEndDate(String projectId) {
        fb.collection("projectTasks")
                .whereEqualTo("projectId", projectId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        AtomicLong highestEstimatedTime = new AtomicLong();
                        AtomicInteger tasksProcessed = new AtomicInteger(0);
                        int totalTasks = task.getResult().size();

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

                                                    // Update the highest estimated time
                                                    highestEstimatedTime.updateAndGet(currentValue ->
                                                            Math.max(currentValue, taskDays)
                                                    );

                                                    Log.d("CalculateTime", "Task processed. Highest estimated time so far: " + highestEstimatedTime);
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

                                        // Increment the counter for processed tasks
                                        tasksProcessed.incrementAndGet();

                                        // Check if all tasks have been processed
                                        if (tasksProcessed.get() == totalTasks) {
                                            // Update the project's end date based on the highest estimated time
                                            updateProjectEndDate(projectId, highestEstimatedTime.get());
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

    @Override
    public void onEndDateUpdated(String updatedEndDate) {

    }
}

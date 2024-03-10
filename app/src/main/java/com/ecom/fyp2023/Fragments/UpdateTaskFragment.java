package com.ecom.fyp2023.Fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.ecom.fyp2023.AppManagers.CustomArrayAdapter;
import com.ecom.fyp2023.AppManagers.TimeConverter;
import com.ecom.fyp2023.ModelClasses.Tasks;
import com.ecom.fyp2023.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import org.jetbrains.annotations.Contract;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class UpdateTaskFragment extends BottomSheetDialogFragment implements CustomArrayAdapter.TaskIdProvider {

    TextInputEditText taskDetails,taskName;

    Spinner taskDifficulty,duration;

    String  progress,projectId,completedTime;
    private FirebaseFirestore fb;
    Tasks tasks;
    Date startDate,endDate;

    ArrayAdapter<String> prerequisitesAdapter;
    private final List<String> selectedPrerequisites = new ArrayList<>();

    @Override
    public void provideTaskId(String taskName, BottomSheetFragmentAddTask.OnTaskIdFetchedListener listener) {
        getTaskIdFromName(taskName, listener);
    }

    public interface OnTaskUpdateListener {
        void onTaskUpdated();
    }
    private OnTaskUpdateListener taskUpdateListener;

    public void setOnTaskUpdateListener(OnTaskUpdateListener listener) {
        this.taskUpdateListener = listener;
    }

    @NonNull
    @Contract(" -> new")
    public static UpdateTaskFragment newInstance() {
        return new UpdateTaskFragment();
    }

    //private OnEndDateUpdateListener endDateUpdateListener;

   // public interface OnEndDateUpdateListener {
      //  void onEndDateUpdated(String updatedEndDate);
   // }

    //public void setOnEndDateUpdateListener(OnEndDateUpdateListener listener) {
    //    this.endDateUpdateListener = listener;
   // }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_update_task, container, false);
        view.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white));

        // Set the listener
        //setOnEndDateUpdateListener((UpdateTaskFragment.OnEndDateUpdateListener) requireActivity());

        fb = FirebaseFirestore.getInstance();

        taskDetails = view.findViewById(R.id.updateTaskDetails);
        taskName = view.findViewById(R.id.taskName);
        TextInputEditText estimatedTime = view.findViewById(R.id.updateEstimatedTime);
        taskDifficulty = view.findViewById(R.id.updateTaskDif);
        duration = view.findViewById(R.id.upTaskDuration);

        Button updateTaskBtn = view.findViewById(R.id.updateBtn);

        Spinner spinnerPrerequisite = view.findViewById(R.id.spinnerPrerequisite);
        List<String> taskNames = getTaskNamesFromFirestore();
        prerequisitesAdapter = new CustomArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, taskNames, selectedPrerequisites, this);
        prerequisitesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPrerequisite.setAdapter(prerequisitesAdapter);

        Bundle args = getArguments();
        if (args != null && args.containsKey("pId2")) {
            projectId = args.getString("pId2");
        }

        Bundle arguments = getArguments();
        if (arguments != null && arguments.containsKey("tId")) {
            projectId = arguments.getString("pId");
        }

        Bundle arguments1 = getArguments();
        if (arguments1 != null && arguments1.containsKey("pro_key")) {
            projectId = arguments1.getString("pro_key");
        }

        Bundle bundle = getArguments();
        if (bundle != null && bundle.containsKey("tasks")) {
            tasks = (Tasks) bundle.getSerializable("tasks");
            tasks.getTaskId();
        }

        //from TaskActivity
        Bundle bundle1 = getArguments();
        if (bundle1 != null && bundle1.containsKey("selectT")) {
            tasks = (Tasks) bundle.getSerializable("selectT");
            tasks.getTaskId();
        }

        assert tasks != null;
        taskDetails.setText(tasks.getTaskDetails());
        taskName.setText(tasks.getTaskName());
        estimatedTime.setText(tasks.getEstimatedTime());

        String diffVal = tasks.getDifficulty();
        String[] diffSpinnerItems = getResources().getStringArray(R.array.tasksDifficulty);
        int position = Arrays.asList(diffSpinnerItems).indexOf(diffVal);
        taskDifficulty.setSelection(position);

        ImageView closeImageView = view.findViewById(R.id.closeImageView);

        // Set an OnClickListener to handle the close action
        closeImageView.setOnClickListener(v -> {
            dismiss();
        });

        spinnerPrerequisite.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                String selectedTaskName = (String) parentView.getItemAtPosition(position);

                getTaskIdFromName(selectedTaskName, new BottomSheetFragmentAddTask.OnTaskIdFetchedListener() {
                    @Override
                    public void onTaskIdFetched(String taskId) {
                        highlightSelectedItems(spinnerPrerequisite, selectedPrerequisites);

                        if (taskId != null) {
                            // Toggle the selected task ID in the list of prerequisites
                            if (selectedPrerequisites.contains(taskId)) {
                                // Unselect the task
                                selectedPrerequisites.remove(taskId);
                            } else {
                                // Select the task
                                selectedPrerequisites.add(taskId);
                            }
                            // Highlight selected items in the Spinner
                            highlightSelectedItems(spinnerPrerequisite, selectedPrerequisites);
                        } else {
                            // Handle the case where the task ID is not found
                            // (e.g., display an error message or take appropriate action)
                        }
                    }
                });
            }
            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }
        });

        updateTaskBtn.setOnClickListener(v -> {
            String detls = taskDetails.getText().toString();
            String taskN = taskName.getText().toString();
            String estTime = estimatedTime.getText().toString();
            String diff = taskDifficulty.getSelectedItem().toString();
            String selectedText = duration.getSelectedItem().toString();
            String timeEstimate = estTime + selectedText;


            estimatedTime.setText(timeEstimate);
            estimatedTime.setSelection(estimatedTime.length());

            if (TextUtils.isEmpty(detls)) {
                taskDetails.setError("Field required");
            } else if (TextUtils.isEmpty(taskN)) {
                taskName.setError("Field require");
            } else if (TextUtils.isEmpty(diff)) {
                Toast.makeText(getActivity(), "Select difficulty", Toast.LENGTH_LONG).show();
            }else if (!isValidEstimationFormat(timeEstimate)) {
                estimatedTime.setError("Invalid format. Use a number followed by duration");
            } else {
                // Call the updateTask method
                updateTask(tasks, taskN, detls, diff, progress, timeEstimate,selectedPrerequisites,completedTime,startDate,endDate);
                dismiss();
            }
        });
        return view;
    }

    private boolean isValidEstimationFormat(@NonNull String estTime) {
        String regex = "\\d+(day|week)";
        return estTime.matches(regex);
    }

    private void updateTask(@NonNull Tasks task, String taskN, String taskDetails, String tasksDiff, String progres, String taskEtime,List<String> prerequisite,String completeT,Date sD,Date eD) {

        String existingProgress = task.getProgress();
        String existingCompletedTime = task.getCompletedTime();
        Date existingStartDate = task.getStartDate();
        Date existingEndDate = task.getEndDate();

        // Create the updated task with the existing progress value
        Tasks updateTasks = new Tasks(taskN,taskDetails, tasksDiff, existingProgress, taskEtime,prerequisite,existingCompletedTime,existingStartDate,existingEndDate);

        fb.collection("Tasks")
                .document(task.getTaskId())
                .set(updateTasks)
                .addOnSuccessListener(aVoid -> {
                    Log.d("proID", "proID " +projectId);
                    calculateTotalEstimatedTimeAndEndDate(projectId);
                    Toast.makeText(requireContext(), "Task has been updated.", Toast.LENGTH_SHORT).show();
                    if (taskUpdateListener != null) {
                        taskUpdateListener.onTaskUpdated();
                   }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), "Failed to update the task.", Toast.LENGTH_SHORT).show();
                    Log.e("UpdateTask", "Error updating task", e);
                });
    }

    //update end date of project automatically when startdate is updated. this does not account for prerequisites take out when satisfied
    /*private void calculateTotalEstimatedTimeAndEndDate(String projectId) {
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
    }*/

    private void calculateTotalEstimatedTimeAndEndDate(String projectId) {
        fb.collection("projectTasks")
                .whereEqualTo("projectId", projectId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        AtomicLong highestEstimatedTime = new AtomicLong();
                        AtomicInteger tasksProcessed = new AtomicInteger(0);
                        int totalTasks = task.getResult().size();

                        // Set to keep track of already considered tasks for end date calculation
                        Set<String> consideredTasks = new HashSet<>();

                        for (DocumentSnapshot document : task.getResult()) {
                            String taskId = document.getString("taskId");

                            assert taskId != null;
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

                                                    // Consider prerequisites if any
                                                    List<String> prerequisites = (List<String>) taskDocument.get("prerequisites");
                                                    if (prerequisites != null && !prerequisites.isEmpty()) {
                                                        for (String prerequisiteTaskId : prerequisites) {
                                                            // Check if the prerequisite task is already considered
                                                            if (!consideredTasks.contains(prerequisiteTaskId)) {
                                                                // Fetch and calculate estimated time for the prerequisite task
                                                                fb.collection("Tasks")
                                                                        .document(prerequisiteTaskId)
                                                                        .get()
                                                                        .addOnCompleteListener(prerequisiteTaskDocumentTask -> {
                                                                            if (prerequisiteTaskDocumentTask.isSuccessful()) {
                                                                                DocumentSnapshot prerequisiteTaskDocument = prerequisiteTaskDocumentTask.getResult();

                                                                                if (prerequisiteTaskDocument != null && prerequisiteTaskDocument.exists()) {
                                                                                    String prerequisiteEstimatedTime = prerequisiteTaskDocument.getString("estimatedTime");

                                                                                    if (prerequisiteEstimatedTime != null) {
                                                                                        long prerequisiteTaskDays = TimeConverter.convertToDays(prerequisiteEstimatedTime);

                                                                                        // Update the highest estimated time
                                                                                        highestEstimatedTime.updateAndGet(currentValue ->
                                                                                                Math.max(currentValue, taskDays + prerequisiteTaskDays)
                                                                                        );

                                                                                        // Mark the prerequisite task as considered
                                                                                        consideredTasks.add(prerequisiteTaskId);

                                                                                        Log.d("CalculateTime", "Prerequisite task processed. Highest estimated time so far: " + highestEstimatedTime);

                                                                                        // Check if all tasks have been processed
                                                                                        if (tasksProcessed.incrementAndGet() == totalTasks) {
                                                                                            // Update the project's end date based on the highest estimated time
                                                                                            updateProjectEndDate(projectId, highestEstimatedTime.get());
                                                                                        }
                                                                                    } else {
                                                                                        Log.e("CalculateTime", "Prerequisite task's estimated time is null for task: " + prerequisiteTaskId);
                                                                                    }
                                                                                } else {
                                                                                    Log.e("CalculateTime", "Prerequisite task document is null or doesn't exist for taskId: " + prerequisiteTaskId);
                                                                                }
                                                                            } else {
                                                                                // Handle failure in fetching prerequisite task document
                                                                                Log.e("CalculateTime", "Error fetching prerequisite task document", prerequisiteTaskDocumentTask.getException());
                                                                            }
                                                                        });
                                                            }
                                                        }
                                                    } else {
                                                        // Update the highest estimated time if no prerequisites
                                                        highestEstimatedTime.updateAndGet(currentValue ->
                                                                Math.max(currentValue, taskDays)
                                                        );

                                                        // Check if all tasks have been processed
                                                        if (tasksProcessed.incrementAndGet() == totalTasks) {
                                                            // Update the project's end date based on the highest estimated time
                                                            updateProjectEndDate(projectId, highestEstimatedTime.get());
                                                        }
                                                    }
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
                                            //endDateUpdateListener.onEndDateUpdated(updatedEndDate);
                                        } else {
                                            Log.e("UpdateProject", "Error updating project end date", updateTask.getException());
                                        }
                                    });
                        }
                    } else {
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

    // Fetch task names from Firestore
    @NonNull
    private List<String> getTaskNamesFromFirestore() {
        List<String> taskNames = new ArrayList<>();
        taskNames.add("     ");
        // Replace "Tasks" with the actual name of your collection
        FirebaseFirestore.getInstance().collection("Tasks")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            // Assuming you have a "taskDetails" field in your document
                            String taskName = document.getString("taskName");
                            if(!tasks.getTaskDetails().equalsIgnoreCase(taskName)){
                                taskNames.add(taskName);
                            }
                        }
                        // Notify the adapter that the data set has changed
                        prerequisitesAdapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(getActivity(), "Failed to fetch task names", Toast.LENGTH_SHORT).show();
                    }
                });
        return taskNames;
    }

    // Fetch task ID from Firestore based on task name
    private void getTaskIdFromName(String selectedTaskName, BottomSheetFragmentAddTask.OnTaskIdFetchedListener listener) {
        FirebaseFirestore.getInstance().collection("Tasks")
                .whereEqualTo("taskName", selectedTaskName) // Assuming "taskDetails" is the field containing the task name
                .limit(1) // Limit to 1 document (assuming task names are unique)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null && !task.getResult().isEmpty()) {
                        // Retrieve the task ID
                        String taskId = task.getResult().getDocuments().get(0).getId();
                        listener.onTaskIdFetched(taskId);
                    } else {
                        // Handle errors or non-existent task name
                        listener.onTaskIdFetched(null);
                    }
                });
    }

    private void highlightSelectedItems(@NonNull Spinner spinner, List<String> selectedPrerequisites) {
        for (int i = 0; i < spinner.getCount(); i++) {
            View view = spinner.getChildAt(i);
            if (view != null) {
                TextView textView = view.findViewById(android.R.id.text1);
                String taskName = (String) spinner.getItemAtPosition(i);
                getTaskIdFromName(taskName, new BottomSheetFragmentAddTask.OnTaskIdFetchedListener() {
                    @Override
                    public void onTaskIdFetched(String taskId) {
                        if (taskId != null && selectedPrerequisites.contains(taskId)) {
                            // Highlight the selected item
                            textView.setTextColor(Color.BLUE);
                        } else {
                            // Reset the color for unselected items
                            textView.setTextColor(Color.BLACK);
                        }
                    }
                });
            }
        }
    }
}

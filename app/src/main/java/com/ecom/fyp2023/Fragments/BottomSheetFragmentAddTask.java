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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.ecom.fyp2023.AppManagers.CustomArrayAdapter;
import com.ecom.fyp2023.AppManagers.TimeConverter;
import com.ecom.fyp2023.ModelClasses.Tasks;
import com.ecom.fyp2023.ProjectActivity;
import com.ecom.fyp2023.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import org.jetbrains.annotations.Contract;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class BottomSheetFragmentAddTask extends BottomSheetDialogFragment implements CustomArrayAdapter.TaskIdProvider{

    Spinner progress, difficulty, duration;
    TextInputEditText details;
    Button save;
    String taskId, projectId;
    FirebaseFirestore fb;
    ArrayAdapter<String> prerequisitesAdapter;
    private List<String> selectedPrerequisites = new ArrayList<>();

    @NonNull
    @Contract(" -> new")
    public static BottomSheetFragmentAddTask newInstance() {
        return new BottomSheetFragmentAddTask();
    }

    private OnEndDateUpdateListener endDateUpdateListener;

    @Override
    public void provideTaskId(String taskName, OnTaskIdFetchedListener listener) {
        getTaskIdFromName(taskName, listener);

    }

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
        TextInputEditText estimatedTime = (TextInputEditText) view.findViewById(R.id.estimatedTime);
        difficulty = view.findViewById(R.id.taskDif);
        progress = view.findViewById(R.id.taskProgress);
        save = view.findViewById(R.id.saveBtn);
        duration = view.findViewById(R.id.taskDuration);

        Spinner spinnerPrerequisite = view.findViewById(R.id.spinnerPrerequisite);
        List<String> taskNames = getTaskNamesFromFirestore();
        prerequisitesAdapter = new CustomArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, taskNames, selectedPrerequisites, this);
        prerequisitesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPrerequisite.setAdapter(prerequisitesAdapter);

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

        //new stuff
        spinnerPrerequisite.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                String selectedTaskName = (String) parentView.getItemAtPosition(position);
                getTaskIdFromName(selectedTaskName, new OnTaskIdFetchedListener() {
                    @Override
                    public void onTaskIdFetched(String taskId) {

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
                // Do nothing when nothing is selected
            }
        });


        save.setOnClickListener(v -> {
            String detls = details.getText().toString();
            String estTime = estimatedTime.getText().toString();
            String diff = difficulty.getSelectedItem().toString();
            String progrs = progress.getSelectedItem().toString();
            String selectedText = duration.getSelectedItem().toString();
            String newText = estTime + selectedText;

            estimatedTime.setText(newText);
            estimatedTime.setSelection(estimatedTime.length());

            if (TextUtils.isEmpty(detls)) {
                details.setError("Field required");
                estimatedTime.setText(null);
            } else if (TextUtils.isEmpty(estTime)) {
                estimatedTime.setError("Field required");
            } else if (!isValidEstimationFormat(newText)) {
                estimatedTime.setText(null);
                estimatedTime.setError("Invalid format. Use a number followed by duration");
            } else {

                saveTasks(detls, diff, progrs, newText, selectedPrerequisites);
                selectedPrerequisites.clear();

                details.setText(null);
                estimatedTime.setText(null);
            }
        });
        return view;
    }

    // Function to check if estTime has a valid format
    private boolean isValidEstimationFormat(@NonNull String estTime) {
        String regex = "\\d+(day|week)";
        return estTime.matches(regex);
    }

    public void saveTasks(String d, String diff, String prog, String estT, List<String> prerequisites) {

        CollectionReference dbTasks = fb.collection("Tasks");

        Tasks tasks = new Tasks(d, diff, prog, estT, prerequisites);
        dbTasks.add(tasks).addOnSuccessListener(documentReference -> {

            Toast.makeText(getActivity(), "Task saved", Toast.LENGTH_SHORT).show();
            taskId = documentReference.getId();
            addProjectTask(projectId, taskId);

            tasks.setTaskId(taskId);
            tasks.setPrerequisites(selectedPrerequisites);

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
                        AtomicLong highestEstimatedTime = new AtomicLong();
                        AtomicInteger tasksProcessed = new AtomicInteger(0);
                        int totalTasks = task.getResult().size();

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
                            //String endDate = projectDocument.getString("endDate");

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

    // Fetch task names from Firestore
    @NonNull
    private List<String> getTaskNamesFromFirestore() {
        List<String> taskNames = new ArrayList<>();

        taskNames.add("");

        // Replace "Tasks" with the actual name of your collection
        FirebaseFirestore.getInstance().collection("Tasks")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            // Assuming you have a "taskDetails" field in your document
                            String taskName = document.getString("taskDetails");
                            taskNames.add(taskName);
                        }

                        // Notify the adapter that the data set has changed
                        prerequisitesAdapter.notifyDataSetChanged();
                    } else {
                        // Handle errors
                        Toast.makeText(getActivity(), "Failed to fetch task names", Toast.LENGTH_SHORT).show();
                    }
                });

        return taskNames;
    }

    // Fetch task ID from Firestore based on task name
    public void getTaskIdFromName(String selectedTaskName, OnTaskIdFetchedListener listener) {
        FirebaseFirestore.getInstance().collection("Tasks")
                .whereEqualTo("taskDetails", selectedTaskName) // Assuming "taskDetails" is the field containing the task name
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

    // Define an interface for the callback
    public interface OnTaskIdFetchedListener {
        void onTaskIdFetched(String taskId);
    }

    // New method to highlight selected items in the Spinner
    // Updated highlightSelectedItems method
    private void highlightSelectedItems(@NonNull Spinner spinner, List<String> selectedPrerequisites) {
        for (int i = 0; i < spinner.getCount(); i++) {
            View view = spinner.getChildAt(i);
            if (view != null) {
                TextView textView = view.findViewById(android.R.id.text1);
                String taskName = (String) spinner.getItemAtPosition(i);
                getTaskIdFromName(taskName, new OnTaskIdFetchedListener() {
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
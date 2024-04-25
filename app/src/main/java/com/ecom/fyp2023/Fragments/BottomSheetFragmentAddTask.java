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

import com.ecom.fyp2023.AppManagers.CustomArrayAdapter;
import com.ecom.fyp2023.AppManagers.SharedPreferenceManager;
import com.ecom.fyp2023.AppManagers.TimeConverter;
import com.ecom.fyp2023.ModelClasses.Tasks;
import com.ecom.fyp2023.ProjectActivity;
import com.ecom.fyp2023.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class BottomSheetFragmentAddTask extends BottomSheetDialogFragment implements CustomArrayAdapter.TaskIdProvider{

    Spinner progress, difficulty, duration;
    TextInputEditText details,taskName,estimatedTime;
    Button save;
    String taskId, projectId,timeEstimate;
    Date estimatedEDate;


    int StoryPoint;
    FirebaseFirestore fb;
    ArrayAdapter<String> prerequisitesAdapter;

    SharedPreferenceManager sharedPreferenceManager;
    private final List<String> selectedPrerequisites = new ArrayList<>();

    @NonNull
    @Contract(" -> new")
    public static BottomSheetFragmentAddTask newInstance() {
        return new BottomSheetFragmentAddTask();
    }

    @Override
    public void provideTaskId(String taskName, OnTaskIdFetchedListener listener) {
        getTaskIdFromName(taskName, listener);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bottom_sheet_add_task, container, false);

        details = view.findViewById(R.id.taskDetails);
        taskName = view.findViewById(R.id.taskName);
        estimatedTime = view.findViewById(R.id.estimatedTime);
        difficulty = view.findViewById(R.id.taskDif);
        progress = view.findViewById(R.id.taskProgress);
        save = view.findViewById(R.id.saveBtn);
        duration = view.findViewById(R.id.taskDuration);

        sharedPreferenceManager = new SharedPreferenceManager(getContext());





        fb = FirebaseFirestore.getInstance();

        Bundle args = getArguments();
        if (args != null && args.containsKey(ProjectActivity.projectId_key)) {
            projectId = args.getString(ProjectActivity.projectId_key);
        }

        Bundle arguments = getArguments();
        if (arguments != null && arguments.containsKey(ProjectActivity.p_key)) {
            projectId = arguments.getString(ProjectActivity.p_key);
        }

        Spinner spinnerPrerequisite = view.findViewById(R.id.spinnerPrerequisite);
        List<String> taskNames = getTaskNamesFromFirestore(projectId);
        prerequisitesAdapter = new CustomArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, taskNames, selectedPrerequisites, this);
        prerequisitesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPrerequisite.setAdapter(prerequisitesAdapter);

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
                        }  // Handle the case where the task ID is not found
                    }
                });
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }
        });
        
        save.setOnClickListener(v -> {
            String detls = details.getText().toString();
            String taskN = taskName.getText().toString();
            String estTime = estimatedTime.getText().toString();
            String diff = difficulty.getSelectedItem().toString();
            String progrs = progress.getSelectedItem().toString();
            String selectedText = duration.getSelectedItem().toString();
            timeEstimate = estTime + selectedText;
            Calendar calendar = Calendar.getInstance();
            Date startDate = calendar.getTime();

            if (TextUtils.isEmpty(detls)) {
                details.setError("Field required");
            } else if (TextUtils.isEmpty(estTime)) {
                estimatedTime.setError("Field required");
            }else if(TextUtils.isEmpty(taskN)){
                taskName.setError("Field required");
            } else if (!isValidEstimationFormat(timeEstimate)) {
                estimatedTime.setError("Invalid format. Use a number followed by duration");
            } else if (TextUtils.isEmpty(diff)) {
                Toast.makeText(getActivity(), "Select difficulty", Toast.LENGTH_LONG).show();
            } else {
                // Call the listener setup method for the prerequisite spinner
                setupPrerequisitesListener();

                checkTaskNameExists(taskN, detls, diff, progrs, timeEstimate, selectedPrerequisites, null, startDate, null, estimatedEDate, StoryPoint);
            }
        });
        return view;
    }

    // Function to check if estTime has a valid format
    private boolean isValidEstimationFormat(@NonNull String estTime) {
        String regex = "\\d+(day|week)";
        return estTime.matches(regex);
    }

    private void checkTaskNameExists(String taskName1, String details1, String difficulty, String progress,
                                     String estimatedTime1, List<String> prerequisites, String completedTime,
                                     Date startDate, Date endDate, Date estimatedEndDate, int storyPoint) {
        // Query Tasks collection to check if a task with the same name exists
        FirebaseFirestore.getInstance().collection("Tasks")
                .whereEqualTo("taskName", taskName1)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (!task.getResult().isEmpty()) {
                            // Task name already exists
                            taskName.setError("Task name already exists");
                        } else {
                            // Task name is unique, proceed with saving
                            saveTasks(taskName1, details1, difficulty, progress, estimatedTime1, prerequisites,
                                    completedTime, startDate, endDate, estimatedEndDate, storyPoint);
                            selectedPrerequisites.clear();

                            estimatedTime.setText(timeEstimate);
                            estimatedTime.setSelection(estimatedTime.length());

                            details.setText(null);
                            estimatedTime.setText(null);
                            taskName.setText(null);
                        }
                    } else {
                        // Handle errors
                        Toast.makeText(getActivity(), "Failed to check task name", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    public void saveTasks(String tN, String d, String diff, String prog, String estT, List<String> prerequisites, String completedTime, Date startDate, Date endDate, Date estEndDate, int sP) {
        CollectionReference dbTasks = fb.collection("Tasks");

        // Convert estimated time to days using the TimeConverter class
        long estimatedDays = TimeConverter.convertToDays(estT);

        // Calculate estimated end date
        Date estimatedEndDate = calculateEstimatedEndDate(startDate, estimatedDays);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String userAuthId = currentUser.getUid();

        String groupId = sharedPreferenceManager.getGroupId();
        // Calculate StoryPoint based on difficulty level and estimated time
        int storyPoint = calculateStoryPoint(diff, estimatedDays);

        Tasks tasks = new Tasks(tN, d, diff, prog, estT, prerequisites, completedTime, startDate, endDate, estimatedEndDate, storyPoint,userAuthId,groupId);
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


    private int calculateStoryPoint(String difficulty, long estimatedDays) {
        int storyPoint = 0;
        if (estimatedDays >= 1 && estimatedDays <= 3) {
            if (difficulty.equalsIgnoreCase("low")) {
                storyPoint = 1;
            } else if (difficulty.equalsIgnoreCase("medium")) {
                storyPoint = 2;
            } else if (difficulty.equalsIgnoreCase("high")) {
                storyPoint = 3;
            }
        } else if (estimatedDays >= 4 && estimatedDays <= 7) {
            if (difficulty.equalsIgnoreCase("low")) {
                storyPoint = 4;
            } else if (difficulty.equalsIgnoreCase("medium")) {
                storyPoint = 5;
            } else if (difficulty.equalsIgnoreCase("high")) {
                storyPoint = 6;
            }
        } else if (estimatedDays >= 8 && estimatedDays <= 18) {
            if (difficulty.equalsIgnoreCase("low")) {
                storyPoint = 7;
            } else if (difficulty.equalsIgnoreCase("medium")) {
                storyPoint = 8;
            } else if (difficulty.equalsIgnoreCase("high")) {
                storyPoint = 9;
            }
        } else if (estimatedDays >= 19 && estimatedDays <= 28) {
            if (difficulty.equalsIgnoreCase("low")) {
                storyPoint = 10;
            } else if (difficulty.equalsIgnoreCase("medium")) {
                storyPoint = 11;
            } else if (difficulty.equalsIgnoreCase("high")) {
                storyPoint = 12;
            }
        } else if (estimatedDays > 28) {
            if (difficulty.equalsIgnoreCase("low")) {
                storyPoint = 13;
            } else if (difficulty.equalsIgnoreCase("medium")) {
                storyPoint = 14;
            } else if (difficulty.equalsIgnoreCase("high")) {
                storyPoint = 15;
            }
        }
        return storyPoint;
    }


    // Method to calculate the estimated end date based on the start date and estimated days
    @NonNull
    private Date calculateEstimatedEndDate(Date startDate, long estimatedDays) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startDate);
        calendar.add(Calendar.DAY_OF_MONTH, (int) estimatedDays);
        return calendar.getTime();
    }



    private void addProjectTask(String projectId, String taskId) {
        // Creates a new projectTask document with an automatically generated ID
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
//    @NonNull
//    private List<String> getTaskNamesFromFirestore() {
//        List<String> taskNames = new ArrayList<>();
//
//        taskNames.add("   ");
//
//        // Replace "Tasks" with the actual name of your collection
//        FirebaseFirestore.getInstance().collection("Tasks")
//                .get()
//                .addOnCompleteListener(task -> {
//                    if (task.isSuccessful()) {
//                        for (QueryDocumentSnapshot document : task.getResult()) {
//                            String taskName = document.getString("taskName");
//                            taskNames.add(taskName);
//                        }
//                        // Notify the adapter that the data set has changed
//                        prerequisitesAdapter.notifyDataSetChanged();
//                    } else {
//                        // Handle errors
//                        Toast.makeText(getActivity(), "Failed to fetch task names", Toast.LENGTH_SHORT).show();
//                    }
//                });
//        return taskNames;
//    }

    @NonNull
    private List<String> getTaskNamesFromFirestore(String projectId) {
        List<String> taskNames = new ArrayList<>();
        taskNames.add("   ");

        // Query ProjectTasks collection to get tasks associated with the given projectId
        FirebaseFirestore.getInstance().collection("projectTasks")
                .whereEqualTo("projectId", projectId) // Filter by projectId
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String taskId = document.getString("taskId");
                            // Fetch task details from Tasks collection using taskId
                            fetchTaskName(taskId, taskNames);
                        }
                    } else {
                        // Handle errors
                        Toast.makeText(getActivity(), "Failed to fetch task names", Toast.LENGTH_SHORT).show();
                    }
                });
        return taskNames;
    }

    // Helper method to fetch task names using taskId
    private void fetchTaskName(String taskId, List<String> taskNames) {
        FirebaseFirestore.getInstance().collection("Tasks")
                .document(taskId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            String taskName = document.getString("taskName");
                            taskNames.add(taskName);
                            // Notify the adapter that the data set has changed
                            prerequisitesAdapter.notifyDataSetChanged();
                        } else {
                            Log.e("error", "No such document");
                        }
                    } else {
                        Log.e("error", "get failed with ", task.getException());
                    }
                });
    }


    private void setupPrerequisitesListener() {
        FirebaseFirestore.getInstance().collection("Tasks")
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Toast.makeText(getActivity(), "Error fetching task names", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    List<String> taskNames = new ArrayList<>();
                    taskNames.add("   ");  // Add an empty option

                    for (QueryDocumentSnapshot document : value) {
                        String taskName = document.getString("taskName");
                        taskNames.add(taskName);
                    }

                    prerequisitesAdapter.clear();
                    prerequisitesAdapter.addAll(taskNames);
                    prerequisitesAdapter.notifyDataSetChanged();
                });
    }


    // Fetch task ID from Firestore based on task name
    public void getTaskIdFromName(String selectedTaskName, OnTaskIdFetchedListener listener) {
        FirebaseFirestore.getInstance().collection("Tasks")
                .whereEqualTo("taskName", selectedTaskName)
                .limit(1)
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
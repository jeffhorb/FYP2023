package com.ecom.fyp2023.Fragments;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.ecom.fyp2023.AppManagers.TimeConverter;
import com.ecom.fyp2023.ModelClasses.Projects;
import com.ecom.fyp2023.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import org.jetbrains.annotations.Contract;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
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

public class UpdateProject extends BottomSheetDialogFragment {

    @NonNull
    @Contract(" -> new")
    public static UpdateProject newInstance() {
        return new UpdateProject();
    }

//    EditText pTitle;
//    EditText pDesc;
//    EditText startDate;

    TextInputEditText pTitle,pDesc,startDate;
    Spinner proPriority;

    String pProgress, eDate;

    Projects project;

    Date actualEndD;
    private FirebaseFirestore fb;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_update_project, container, false);
        view.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white));

        fb = FirebaseFirestore.getInstance();

        pTitle = view.findViewById(R.id.proT);
        pDesc = view.findViewById(R.id.proD);
        startDate = view.findViewById(R.id.startD);
        proPriority = view.findViewById(R.id.proP);

        Button updateCourseBtn = view.findViewById(R.id.updateProjectBt);

        Bundle bundle = getArguments();
        if (bundle != null && bundle.containsKey("project")) {
            project = (Projects) bundle.getSerializable("project");
            pTitle.setText(project.getTitle());
            pDesc.setText(project.getDescription());
            startDate.setText(project.getStartDate());
            String priorityValue = project.getPriority();
            String[] prioritySpinnerItems = getResources().getStringArray(R.array.projectPriority);
            int position = Arrays.asList(prioritySpinnerItems).indexOf(priorityValue);
            proPriority.setSelection(position);
        }

        //Received from projectActivity
        Bundle bundle1 = getArguments();
        if (bundle1 != null && bundle1.containsKey("proJ")) {
            project = (Projects) bundle1.getSerializable("proJ");
            //project.getProjectId();
            pTitle.setText(project.getTitle());
            pDesc.setText(project.getDescription());
            startDate.setText(project.getStartDate());
            String priorityValue = project.getPriority();
            String[] prioritySpinnerItems = getResources().getStringArray(R.array.projectPriority);
            int position = Arrays.asList(prioritySpinnerItems).indexOf(priorityValue);
            proPriority.setSelection(position);
        }

        Bundle bundle2 = getArguments();
        if (bundle2 != null && bundle2.containsKey("proTid")) {
            String projectId = bundle2.getString("proTid");
            DocumentReference projectRef = fb.collection("Projects").document(projectId);
            projectRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        // Document exists, retrieve existing values
                        String existingTitle = document.getString("title");
                        pTitle.setText(existingTitle);
                        String existingProD = document.getString("description");
                        pDesc.setText(existingProD);
                        String existingPriority = document.getString("priority");
                        String[] prioritySpinnerItems = getResources().getStringArray(R.array.projectPriority);
                        int position = Arrays.asList(prioritySpinnerItems).indexOf(existingPriority);
                        proPriority.setSelection(position);
                        String existingStartDate = document.getString("startDate");
                        startDate.setText(existingStartDate);
                    }
                }
            });

        }

        ImageView closeImageView = view.findViewById(R.id.closeImageView);

        // Set an OnClickListener to handle the close action
        closeImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Close the BottomSheetDialogFragment when the close icon is clicked
                dismiss();
            }
        });

        startDate.setOnClickListener(v -> {
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            @SuppressLint("SetTextI18n") DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(), (view1, year1, monthOfYear, dayOfMonth) -> {
                startDate.setText(dayOfMonth + "-" + (monthOfYear + 1) + "-" + year1);
            }, year, month, day);

            // Set minimum date to today
            datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
            datePickerDialog.show();
        });

        updateCourseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String proT, proDesc, stDate, pPriority;
                proT = pTitle.getText().toString();
                proDesc = pDesc.getText().toString();
                stDate = startDate.getText().toString();
                pPriority = proPriority.getSelectedItem().toString();

                if (TextUtils.isEmpty(proT)) {
                        pTitle.setError("Field required");
                    } else if (TextUtils.isEmpty(proDesc)) {
                        pDesc.setError("Field Required");
                    } else if (TextUtils.isEmpty(pPriority)) {
                        Toast.makeText(getActivity(), "Select priority", Toast.LENGTH_LONG).show();
                    } else if (TextUtils.isEmpty(stDate)) {
                        startDate.setError("Field required");
                    } else if (!isValidDateFormat(stDate)) {
                        Toast.makeText(getActivity(), "Invalid date format. Use dd-MM-yyyy.", Toast.LENGTH_LONG).show();
                    } else {
                        // Your existing code for valid input
                        Bundle bundle2 = getArguments();
                        if (bundle2 != null && bundle2.containsKey("proTid")) {
                            String projectId = bundle2.getString("proTid");
                            //updateProject2(projectId, proT, proDesc, pPriority, stDate, eDate, pProgress, actualEndD);
                            // Check if the project title already exists before updating
                            checkProjectTitleExistsAndUpdate2(projectId, proT, proDesc, pPriority, stDate, eDate, pProgress, actualEndD);
                        }
                        Bundle bundle = getArguments();
                        if (bundle != null && bundle.containsKey("project")) {
                            // Your code to handle the valid input
                            updateProject(project, proT, proDesc, pPriority, stDate, eDate, pProgress, actualEndD);
                        }
                        Bundle bundle1 = getArguments();
                        if (bundle1 != null && bundle1.containsKey("proJ")) {
                            updateProject(project, proT, proDesc, pPriority, stDate, eDate, pProgress, actualEndD);
                        }
                        dismiss();
                    }
            }
        });
        return view;
    }
    // Function to validate the date format
    private boolean isValidDateFormat(String date) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
            // Set lenient to false to enforce strict parsing
            sdf.setLenient(false);
            sdf.parse(date);
            return true;
        } catch (ParseException e) {
            Log.e("DateValidation", "Invalid date format: " + date, e);
            return false;
        }
    }

//    private void updateProject(@NonNull Projects projects, String proTitle, String proD, String priority, String startDate, String endDate, String progres, Date actualEdate) {
//
//        String existingProgress = projects.getProgress();
//        String existingEndDate = projects.getEndDate();
//        Date existingActualEndDate = projects.getActualEndDate();
//
//        // Create the updated project with the existing progress value
//        Projects udpatedPorject = new Projects(proTitle, proD, priority, startDate, existingEndDate, existingProgress, existingActualEndDate);
//
//        fb.collection("Projects").
//                document(projects.getProjectId()).
//                set(udpatedPorject).
//                addOnSuccessListener(new OnSuccessListener<Void>() {
//                    @Override
//                    public void onSuccess(Void aVoid) {
//                        String id = project.getProjectId();
//                        calculateTotalEstimatedTimeAndEndDate(id);
//
//                        Toast.makeText(requireContext(), "Project has been updated..", Toast.LENGTH_SHORT).show();
//                    }
//                }).addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//
//                        Toast.makeText(requireContext(), "Fail to update the data..", Toast.LENGTH_SHORT).show();
//                    }
//                });
//    }
private void updateProject(@NonNull Projects projects, String proTitle, String proD, String priority, String startDate, String endDate, String progres, Date actualEdate) {
    // Query Projects collection to check if a project with the new title exists
    FirebaseFirestore.getInstance().collection("Projects")
            .whereEqualTo("title", proTitle)
            .get()
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        // If the project title exists and it's not the current project being updated
                        if (!document.getId().equals(projects.getProjectId())) {
                            // Project title already exists
                            pTitle.setError("Project title already exists");
                            return;
                        }
                    }
                    // Project title is unique, proceed with the update
                    performProjectUpdate(projects, proTitle, proD, priority, startDate, endDate, progres, actualEdate);
                } else {
                    // Handle errors
                    Toast.makeText(requireContext(), "Failed to check project title", Toast.LENGTH_SHORT).show();
                }
            });
}

    private void performProjectUpdate(@NonNull Projects projects, String proTitle, String proD, String priority, String startDate, String endDate, String progres, Date actualEdate) {
        String existingProgress = projects.getProgress();
        String existingEndDate = projects.getEndDate();
        Date existingActualEndDate = projects.getActualEndDate();

        // Create the updated project with the existing progress value
        Projects updatedProject = new Projects(proTitle, proD, priority, startDate, existingEndDate, existingProgress, existingActualEndDate);

        fb.collection("Projects")
                .document(projects.getProjectId())
                .set(updatedProject)
                .addOnSuccessListener(aVoid -> {
                    calculateTotalEstimatedTimeAndEndDate(projects.getProjectId());
                    Toast.makeText(requireContext(), "Project has been updated..", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), "Fail to update the data..", Toast.LENGTH_SHORT).show();
                    Log.e("UpdateProject", "Error updating project", e);
                });
    }

    private void checkProjectTitleExistsAndUpdate2(String projectId, String proTitle, String proD, String priority, String startDate, String endDate, String progres, Date actualEdate) {
        // Query Projects collection to check if a project with the new title exists
        FirebaseFirestore.getInstance().collection("Projects")
                .whereEqualTo("title", proTitle)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            // Project title already exists
                            pTitle.setError("Project title already exists");
                            return;
                        }
                        // Project title is unique, proceed with the update
                        updateProject2(projectId, proTitle, proD, priority, startDate, endDate, progres, actualEdate);
                    } else {
                        // Handle errors
                        Toast.makeText(getActivity(), "Failed to check project title", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateProject2(String projectId, String proTitle, String proD, String priority, String startDate, String endDate, String progres, Date actualEdate) {

        // Create a map to store the fields to be updated
        Map<String, Object> updates = new HashMap<>();
        updates.put("title", proTitle);
        updates.put("description", proD);
        updates.put("priority", priority);
        updates.put("startDate", startDate);

        // Check if endDate is provided and update if not empty
        if (!TextUtils.isEmpty(endDate)) {
            updates.put("endDate", endDate);
        }

        // Check if progres is provided and update if not empty
        if (!TextUtils.isEmpty(progres)) {
            updates.put("progress", progres);
        }

        // Check if actualEdate is provided and update if not null
        if (actualEdate != null) {
            updates.put("actualEndDate", actualEdate);
        }

        fb.collection("Projects")
                .document(projectId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    calculateTotalEstimatedTimeAndEndDate(projectId);
                    Toast.makeText(requireContext(), "Project has been updated..", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> Toast.makeText(requireContext(), "Fail to update the data..", Toast.LENGTH_SHORT).show());
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
}
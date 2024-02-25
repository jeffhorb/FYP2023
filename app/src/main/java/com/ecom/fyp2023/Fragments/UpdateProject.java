package com.ecom.fyp2023.Fragments;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
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

import com.ecom.fyp2023.ModelClasses.Projects;
import com.ecom.fyp2023.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.firestore.FirebaseFirestore;

import org.jetbrains.annotations.Contract;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

public class UpdateProject extends BottomSheetDialogFragment {

    @NonNull
    @Contract(" -> new")
    public static UpdateProject newInstance() {
            return new UpdateProject();
    }

    EditText pTitle;
    EditText pDesc;
    EditText startDate;
    Spinner proPriority;


    Projects project;

    String proT, proDesc, stDate, eDate, pPriority,pProgress;

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
        }

        //Received from projectActivity
        Bundle bundle1= getArguments();
        if (bundle1 != null && bundle1.containsKey("proJ")) {
            project = (Projects) bundle1.getSerializable("proJ");
            project.getProjectId();
        }

        //updating project from add project fragment
        //Bundle bundle2 = getArguments();
        //if (bundle2 != null && bundle2.containsKey("proT")) {
          //  project = (Projects) bundle2.getSerializable("proT");
          //  assert project != null;
          //  project.getProjectId();

        //}

        assert project != null;
        pTitle.setText(project.getTitle());
        pDesc.setText(project.getDescription());
        startDate.setText(project.getStartDate());

        String priorityValue = project.getPriority();
        String[] prioritySpinnerItems = getResources().getStringArray(R.array.projectPriority);
        int position = Arrays.asList(prioritySpinnerItems).indexOf(priorityValue);
        proPriority.setSelection(position);

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

            DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(), (view1, year1, monthOfYear, dayOfMonth) -> {
                startDate.setText(dayOfMonth + "-" + (monthOfYear + 1) + "-" + year1);
            }, year, month, day);

            // Set minimum date to today
            datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
            datePickerDialog.show();
        });

        updateCourseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                proT = pTitle.getText().toString();
                proDesc = pDesc.getText().toString();
                stDate = startDate.getText().toString();
                pPriority = proPriority.getSelectedItem().toString();

                if (TextUtils.isEmpty(proT)) {
                    pTitle.setError("Field required");
                } else if (TextUtils.isEmpty(proDesc)) {
                    pDesc.setError("Field Required");
                } else if (TextUtils.isEmpty(stDate)) {
                    startDate.setError("Field required");
                }
                // Your code to handle the valid input
                updateProject(project, proT, proDesc,pPriority, stDate, eDate, pProgress,actualEndD);
                dismiss();
            }
        });
        return view;
    }

    private void updateProject(@NonNull Projects projects, String proTitle, String proD, String priority, String startDate, String endDate, String progres, Date actualEdate) {

        String existingProgress = projects.getProgress();
        String existingEndDate = projects.getEndDate();
        Date existingActualEndDate = projects.getActualEndDate();

        // Create the updated project with the existing progress value
        Projects udpatedPorject = new Projects(proTitle, proD, priority, startDate, existingEndDate, existingProgress,existingActualEndDate);

        fb.collection("Projects").
                        document(projects.getProjectId()).
                        set(udpatedPorject).
                        addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        Toast.makeText(requireContext(), "Project has been updated..", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                Toast.makeText(requireContext(), "Fail to update the data..", Toast.LENGTH_SHORT).show();
            }
        });
    }

    //update end date of project automatically when startdate is updated
   /* private void calculateTotalEstimatedTimeAndEndDate(String projectId) {
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
    }*/
}
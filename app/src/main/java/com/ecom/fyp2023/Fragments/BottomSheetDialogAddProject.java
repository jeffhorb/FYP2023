package com.ecom.fyp2023.Fragments;

import android.app.DatePickerDialog;
import android.content.Intent;
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

import com.ecom.fyp2023.ModelClasses.Projects;
import com.ecom.fyp2023.ProjectActivity;
import com.ecom.fyp2023.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import org.jetbrains.annotations.Contract;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class BottomSheetDialogAddProject extends BottomSheetDialogFragment {

    EditText pTitle;
    EditText pDesc;
    EditText startDate;
    EditText endDate;
    Spinner proPriority,proProgress;
    Button addPro;
    String userId, projectId, role;

    String proTitle,proDesc,startDt,endDt,priority,progress;

    public static final String MESSAGE_KEY="projectId";

    FirebaseFirestore fb;
    private FirebaseAuth mAuth;

    @NonNull
    @Contract(" -> new")
    public static BottomSheetDialogAddProject newInstance() {
        return new BottomSheetDialogAddProject();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bottom_sheet_add_project, container, false);

        pTitle = view.findViewById(R.id.proTitle);
        pDesc = view.findViewById(R.id.proDesc);
        startDate = view.findViewById(R.id.startDate);
        endDate = view.findViewById(R.id.endDate);
        proPriority = view.findViewById(R.id.proPriority);
        proProgress = view.findViewById(R.id.proProgress);

        addPro = view.findViewById(R.id.addProButn);

        fb = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        ImageView closeImageView = view.findViewById(R.id.closeImageView);

        // Set an OnClickListener to handle the close action
        closeImageView.setOnClickListener(v -> {
            // Close the BottomSheetDialogFragment when the close icon is clicked
            dismiss();
        });

        addPro.setOnClickListener(v -> {

            proTitle = pTitle.getText().toString();
            proDesc = pDesc.getText().toString();
            startDt = startDate.getText().toString();
            endDt = endDate.getText().toString();
            priority = proPriority.getSelectedItem().toString();
            progress = proProgress.getSelectedItem().toString();

            if (TextUtils.isEmpty(proTitle)) {
                pTitle.setError("Field required");
            } else if (TextUtils.isEmpty(priority)) {
                Toast.makeText(getActivity(), "Select priority", Toast.LENGTH_LONG).show();
            } else if (TextUtils.isEmpty(proDesc)) {
                pDesc.setError("Field Required");
            } else if (TextUtils.isEmpty(startDt)) {
                startDate.setError("Field required");
            } else if (TextUtils.isEmpty(endDt)) {
                endDate.setError("Field required");
            }else if (!isValidDateFormat(endDt)&&!isValidDateFormat(startDt)) {
                Toast.makeText(getActivity(), "Invalid date format. Use dd-MM-yyyy.", Toast.LENGTH_LONG).show();
            } else if (!isBefore(startDt, endDt)) {
                endDate.setError("End date must be after start date");
            } else {
                // Check if the project title already exists
                checkProjectTitleExists(proTitle, proDesc, priority, startDt, endDt, progress, null);
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
            datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000); // -1000 to avoid issues with rounding to the nearest day
            datePickerDialog.show();
        });

        endDate.setOnClickListener(v -> {
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(), (view12, year12, monthOfYear, dayOfMonth) -> {
                endDate.setText(dayOfMonth + "-" + (monthOfYear + 1) + "-" + year12);
            }, year, month, day);

            // Set minimum date to today
            datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000); // -1000 to avoid issues with rounding to the nearest day
            datePickerDialog.show();
        });

        return view;
    }

    private void checkProjectTitleExists(String title, String description, String priority, String startDate,
                                         String endDate, String progress, Date actualEDate) {
        // Query Projects collection to check if a project with the same title exists
        FirebaseFirestore.getInstance().collection("Projects")
                .whereEqualTo("title", title)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            // Project title already exists
                            pTitle.setError("Project title already exists");
                            return;
                        }
                        // Project title is unique, proceed with adding the project
                        addProjects(title, description, priority, startDate, endDate, progress, actualEDate);
                    } else {
                        // Handle errors
                        Toast.makeText(getActivity(), "Failed to check project title", Toast.LENGTH_SHORT).show();
                    }
                });
    }
    public void addProjects(String title, String description, String priority, String startDate, String endDate, String progres, Date actualEDate) {

        CollectionReference dbProjects = fb.collection("Projects");

        Projects projs = new Projects(title, description, priority, startDate, endDate,progres,actualEDate);
        dbProjects.add(projs).addOnSuccessListener(documentReference -> {

            userId = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
            projectId = documentReference.getId();
            role = "owner";
            addUserProject(userId, projectId, role);

            Projects pro = new Projects();
            pro.setProjectId(projectId);

            Intent intent = new Intent(getActivity(), ProjectActivity.class);
            intent.putExtra(MESSAGE_KEY, projectId);
            intent.putExtra("proTitle", proTitle);
            intent.putExtra("proDesc", proDesc);
            intent.putExtra("startDt", startDt);
            intent.putExtra("endDt", endDt);
            intent.putExtra("priority", priority);
            intent.putExtra("progress", progres);

            startActivity(intent);

        }).addOnFailureListener(e -> Toast.makeText(getActivity(), "Failed \n" + e, Toast.LENGTH_SHORT).show());
    }

    private void addUserProject(String userId, String project_id, String role) {
        // Creates a new userProjects document with an automatically generated ID
        Map<String, Object> userProject = new HashMap<>();
        userProject.put("userId", userId);
        userProject.put("projectId", project_id);
        userProject.put("role", role);

        fb.collection("userProjects").add(userProject).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.d("AddProject", "UserProject added with ID: " + task.getResult().getId());
            } else {
                Log.e("AddProject", "Error adding userProject", task.getException());
            }
        });
    }
    
    private boolean isBefore(String date1, String date2) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
            Date startDate = sdf.parse(date1);
            Date endDate = sdf.parse(date2);
            return startDate != null && endDate != null && startDate.before(endDate);
        } catch (ParseException e) {
            e.printStackTrace();
            return false;
        }
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
}


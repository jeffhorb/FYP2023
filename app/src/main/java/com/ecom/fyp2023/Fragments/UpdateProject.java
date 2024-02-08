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

    String proT, proDesc, stDate, eDate, pPriority,pProgress;
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_update_project, container, false);
        view.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white));

        db = FirebaseFirestore.getInstance();

        pTitle = view.findViewById(R.id.proT);
        pDesc = view.findViewById(R.id.proD);
        startDate = view.findViewById(R.id.startD);
        proPriority = view.findViewById(R.id.proP);

        Button updateCourseBtn = view.findViewById(R.id.updateProjectBt);

        Bundle bundle = getArguments();
        assert bundle != null;
        Projects project = (Projects) bundle.getSerializable("project");

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
                updateProject(project, proT, proDesc,pPriority, stDate, eDate, pProgress);
                dismiss();
            }
        });
        return view;
    }

    private void updateProject(@NonNull Projects projects, String proTitle, String proD, String priority, String startDate, String endDate, String progres) {

        String existingProgress = projects.getProgress();
        String existingEndDate = projects.getEndDate();

        // Create the updated project with the existing progress value
        Projects udpatedPorject = new Projects(proTitle, proD, priority, startDate, existingEndDate, existingProgress);

        db.collection("Projects").
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
}
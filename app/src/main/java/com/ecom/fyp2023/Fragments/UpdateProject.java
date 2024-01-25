package com.ecom.fyp2023.Fragments;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class UpdateProject extends BottomSheetDialogFragment {

    // creating variables for our edit text
    EditText pTitle;
    EditText pDesc;
    EditText startDate;
    EditText endDate;
    Spinner proPriority,progress;

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
        endDate = view.findViewById(R.id.endD);
        proPriority = view.findViewById(R.id.proP);
        progress = view.findViewById(R.id.proPro);

        Button updateCourseBtn = view.findViewById(R.id.updateProjectBt);

        Bundle bundle = getArguments();
        assert bundle != null;
        Projects project = (Projects) bundle.getSerializable("project");

        assert project != null;
        pTitle.setText(project.getTitle());
        pDesc.setText(project.getDescription());
        startDate.setText(project.getStartDate());
        endDate.setText(project.getEndDate());

        String priorityValue = project.getPriority();
        String[] prioritySpinnerItems = getResources().getStringArray(R.array.projectPriority);
        int position = Arrays.asList(prioritySpinnerItems).indexOf(priorityValue);
        proPriority.setSelection(position);

        String progressValue = project.getProgress();
        String[] progressSpinnerItems = getResources().getStringArray(R.array.tasksProgress);
        int p = Arrays.asList(progressSpinnerItems).indexOf(progressValue);
        progress.setSelection(p);

        ImageView closeImageView = view.findViewById(R.id.closeImageView);

        // Set an OnClickListener to handle the close action
        closeImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Close the BottomSheetDialogFragment when the close icon is clicked
                dismiss();
            }
        });

        startDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar c = Calendar.getInstance();
                int year = c.get(Calendar.YEAR);
                int month = c.get(Calendar.MONTH);
                int day = c.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(), new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year,
                                          int monthOfYear, int dayOfMonth) {
                        startDate.setText(dayOfMonth + "-" + (monthOfYear + 1) + "-" + year);
                    }
                },
                        year, month, day);
                datePickerDialog.show();
            }
        });

        endDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar c = Calendar.getInstance();
                int year = c.get(Calendar.YEAR);
                int month = c.get(Calendar.MONTH);
                int day = c.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(), new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year,
                                          int monthOfYear, int dayOfMonth) {
                        endDate.setText(dayOfMonth + "-" + (monthOfYear + 1) + "-" + year);
                    }
                },
                        year, month, day);
                datePickerDialog.show();
            }
        });

        updateCourseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                proT = pTitle.getText().toString();
                proDesc = pDesc.getText().toString();
                stDate = startDate.getText().toString();
                eDate = endDate.getText().toString();


                if (TextUtils.isEmpty(proT)) {
                    pTitle.setError("Field required");
                } else if (TextUtils.isEmpty(proDesc)) {
                    pDesc.setError("Field Required");
                } else if (TextUtils.isEmpty(stDate)) {
                    startDate.setError("Field required");
                } else if (TextUtils.isEmpty(eDate)) {
                    endDate.setError("Field required");
                } else if (!isBefore(stDate, eDate)) {
                    // Check if end date is before start date
                    endDate.setError("End date must be after start date");
                } else {
                    // Your code to handle the valid input
                    updateProject(project, proT, proDesc,pPriority, stDate, eDate, pProgress);
                }
            }
        });
        return view;
    }

    private void updateProject(Projects projects, String proTitle, String proD, String priority, String startDate, String endDate, String progres) {

        Projects udpatedPorject = new Projects(proTitle, proD,priority, startDate,endDate,progres);


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

    private boolean isAfter(String date1, String date2) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
            Date startDate = sdf.parse(date1);
            Date endDate = sdf.parse(date2);
            return startDate != null && endDate != null && startDate.after(endDate);
        } catch (ParseException e) {
            e.printStackTrace();
            return false;
        }
    }


    /*private String formatDateFromTimestamp(Timestamp timestamp) {
        if (timestamp != null) {
            Date date = timestamp.toDate();
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
            return sdf.format(date);
        } else {
            return ""; // Handle the case where Timestamp is null
        }
    }*/


}

package com.ecom.fyp2023;

import android.app.DatePickerDialog;
import android.os.Bundle;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class BottomSheetDialogAddProject extends BottomSheetDialogFragment {

    EditText pTitle;
    EditText pDesc;
    EditText startDate;
    EditText endDate;
    EditText proPriority;
    Button addPro;
    String userId, projectId, role;

    FirebaseFirestore fb;
    private FirebaseAuth mAuth;

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

        addPro = view.findViewById(R.id.addProButn);

        fb = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();


        addPro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String proTitle = pTitle.getText().toString();
                String proDesc = pDesc.getText().toString();
                String startDt = startDate.getText().toString();
                String endDt = endDate.getText().toString();
                String priority = proPriority.getText().toString();

                /*if (TextUtils.isEmpty(proTitle)) {
                    pTitle.setError("Field required");
                }
                else if (TextUtils.isEmpty(proDesc)) {
                    pDesc.setError("Field required");
                }
                else if (TextUtils.isEmpty(startDt)) {
                    startDate.setError("Field required");
                } else if (TextUtils.isEmpty(endDt)) {
                    endDate.setError("required");
                    return;
                }
                else if ( !priority.equals("1")) {
                    proPriority.setError("filed required");
                }
                else {
                    addProjects(proTitle,proDesc,priority,startDt,endDt);
                }*/

                addProjects(proTitle, proDesc, priority, startDt, endDt);

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
        return view;
    }

    public void addProjects(String title, String description, String  priority, String startDate, String endDate) {

        CollectionReference dbProjects = fb.collection("Projects");

        Projects projs = new Projects(title, description, priority, startDate, endDate);
        dbProjects.add(projs).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
            @Override
            public void onSuccess(DocumentReference documentReference) {

                Toast.makeText(getActivity(), "Project created", Toast.LENGTH_SHORT).show();

                userId = mAuth.getCurrentUser().getUid();
                projectId =  documentReference.getId();
                role = "owner";
                addUserProject(userId, projectId, role);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                Toast.makeText(getActivity(), "Failed \n" + e, Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void addUserProject(String userId, String projectId, String role) {
        // Creates a new userProjects document with an automatically generated ID
        Map<String, Object> userProject = new HashMap<>();
        userProject.put("userId", userId);
        userProject.put("projectId", projectId);
        userProject.put("role", role);

        fb.collection("userProjects").add(userProject).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
            @Override
            public void onComplete(@NonNull Task<DocumentReference> task) {
                if (task.isSuccessful()) {
                    Log.d("AddProject", "UserProject added with ID: " + task.getResult().getId());
                } else {
                    Log.e("AddProject", "Error adding userProject", task.getException());
                }
            }
        });
    }

}


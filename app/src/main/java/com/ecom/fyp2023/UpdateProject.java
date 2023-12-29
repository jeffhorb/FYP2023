package com.ecom.fyp2023;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;

public class UpdateProject extends Fragment {

    // creating variables for our edit text
    EditText pTitle;
    EditText pDesc;
    EditText startDate;
    EditText endDate;
    EditText proPriority;

    String proT, proDesc, stDate, eDate, pPriority;
    private FirebaseFirestore db;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_update_project, container, false);
        view.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white));


        // getting our instance from Firebase Firestore.
        db = FirebaseFirestore.getInstance();

        // initializing our edittext and buttons
        pTitle = view.findViewById(R.id.proT);
        pDesc = view.findViewById(R.id.proD);
        startDate = view.findViewById(R.id.startD);
        endDate = view.findViewById(R.id.endD);
        proPriority = view.findViewById(R.id.proP);

        Button updateCourseBtn = view.findViewById(R.id.updateProjectBt);

        Bundle bundle = getArguments();
        Projects project = (Projects) bundle.getSerializable("project");

        pTitle.setText(project.getTitle());
        pDesc.setText(project.getDescription());
        startDate.setText(project.getStartDate());
        endDate.setText(project.getEndDate());
        proPriority.setText(project.getPriority());


        updateCourseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                proT = pTitle.getText().toString();
                proDesc = pDesc.getText().toString();
                stDate = startDate.getText().toString();
                eDate = endDate.getText().toString();
                pPriority = proPriority.getText().toString();


                // validating the text fields if empty or not.
                if (TextUtils.isEmpty(proT)) {
                    pTitle.setError("Field required");
                } else if (TextUtils.isEmpty(proDesc)) {
                    pDesc.setError("Field Required");
                } else if (TextUtils.isEmpty(stDate)) {
                    startDate.setError("Field required");
                } else {

                    updateProject(project, proT, proDesc, stDate,eDate,pPriority);
                }
            }
        });
        return view;
    }

    private void updateProject(Projects projects, String proTitle, String proD, String startD, String endD, String priority) {

        Projects udpatedPorject = new Projects(proTitle, proD, startD,endD,priority);


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

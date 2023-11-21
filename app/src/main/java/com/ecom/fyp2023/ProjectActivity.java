package com.ecom.fyp2023;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class ProjectActivity extends AppCompatActivity {
    RecyclerView recyclerView;
    FloatingActionButton addProjectButn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project);
        addProjectButn = findViewById(R.id.addProject);
        addProjectButn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProjectActivity.this, AddProject.class);
                startActivity(intent);

            }
        });


    }
}
package com.ecom.fyp2023;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;

import java.util.Calendar;

public class AddProject extends AppCompatActivity {

    EditText pTitle;
    EditText pDesc;
    EditText startDate;
    EditText endDate;
    EditText proPriority;
    Button addPro;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_project);

        pTitle = findViewById(R.id.proTitle);
        pDesc = findViewById(R.id.proDesc);
        startDate = findViewById(R.id.startDate);
        endDate = findViewById(R.id.endDate);
        proPriority = findViewById(R.id.proPriority);

        addPro = findViewById(R.id.addProButn);

        String proTitle = pTitle.toString();
        String proDesc = pDesc.toString();
        String startDt = startDate.toString();
        String endDt = endDate.toString();
        String priority = proPriority.toString();

        addPro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(proTitle)) {
                    pTitle.setError("Field Required");
                }
                if (TextUtils.isEmpty(startDt)){
                    startDate.setError("Field required");
                }
                if (TextUtils.isEmpty(endDt)) {
                    endDate.setError("F Required");
                }
                //if (!priority.equals("1") || !priority.equals("2") || !priority.equals("3")) {
               //     proPriority.setError("Priority level must be 1 for (High), 2 for (Med), 3 for (Low)");
              //  }

                DBHandler myDb = new DBHandler(AddProject.this);
                myDb.addProject(proTitle, proDesc, startDt, endDt, priority);

            }
        });

        startDate.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    AddProject.this,
                    new DatePickerDialog.OnDateSetListener() {
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

                DatePickerDialog datePickerDialog = new DatePickerDialog(
                        AddProject.this,
                        new DatePickerDialog.OnDateSetListener() {
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



    }
}
package com.ecom.fyp2023;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SignUpActivity extends AppCompatActivity {
    EditText fullName, email, password, phone;
    TextView textView;
    Button signupButn;
    FirebaseAuth authicate;
    ProgressBar pBar;

    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = authicate.getCurrentUser();
        if(currentUser != null){
            Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        fullName = findViewById(R.id.editTextText2);
        email = findViewById(R.id.editTextTextEmailAddress);
        password = findViewById(R.id.editTextTextPassword);
        phone = findViewById(R.id.editTextText3);
        signupButn = findViewById(R.id.button);
        pBar = findViewById(R.id.progressBar);
        authicate = FirebaseAuth.getInstance();

        textView = findViewById(R.id.loginNow);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SignUpActivity.this, Login_activity.class);
                startActivity(intent);
                finish();
            }
        });

        signupButn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String mail = email.getText().toString().trim();
                String pword = password.getText().toString().trim();
                String fullN = fullName.getText().toString();
                String mobileN = phone.getText().toString();

                if (TextUtils.isEmpty(mail)) {
                    email.setError("Email Required");
                }
                if (TextUtils.isEmpty(pword)) {
                    password.setError("Password Required");
                }
                if (TextUtils.isEmpty(fullN)){
                    fullName.setError("Full name required");
                }
                if (TextUtils.isEmpty(mobileN)){
                    phone.setError("Mobile number is Required");
                }
                if (pword.length() < 8) {
                    password.setError("Password length must be at least 8 characters");
                    return;
                }
                String upperCaseChars = "(.*[A-Z].*)";
                if (!pword.matches(upperCaseChars)) {
                    password.setError(("Password must have at least one uppercase letter"));
                    return;
                }
                pBar.setVisibility(View.VISIBLE);

                authicate.createUserWithEmailAndPassword(mail, pword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        pBar.setVisibility(View.GONE);
                        if (task.isSuccessful()) {
                            Toast.makeText(SignUpActivity.this, "Authentication Successfully.", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(SignUpActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
    }

}
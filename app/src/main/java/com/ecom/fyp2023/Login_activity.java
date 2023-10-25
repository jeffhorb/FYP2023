package com.ecom.fyp2023;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.text.InputType;
import android.widget.LinearLayout;

public class Login_activity extends AppCompatActivity {

    EditText Aemail, Apassword;
    Button loginBtn;
    FirebaseAuth authicate;
    ProgressBar pBar;
    TextView textView, forgotpword;
    public ProgressDialog loginprogress;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Aemail = findViewById(R.id.email);
        Apassword = findViewById(R.id.password);
        loginBtn = findViewById(R.id.loginButton);
        pBar = findViewById(R.id.progressBar2);
        forgotpword = findViewById(R.id.forgotPassword);
        authicate = FirebaseAuth.getInstance();
        loginprogress=new ProgressDialog(this);

        forgotpword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRecoverPasswordDialog();
            }
        });

        textView = findViewById(R.id.signupText);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Login_activity.this, SignUpActivity.class);
                startActivity(intent);
                finish();
            }
        });

       loginBtn.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               String mail = Aemail.getText().toString().trim();
               String pword = Apassword.getText().toString().trim();

               if (TextUtils.isEmpty(mail)  ) {
                   Aemail.setError("Email Required");
               } else if (TextUtils.isEmpty(pword)) {
                   Apassword.setError("Password Required");
               }else{
                   pBar.setVisibility(View.VISIBLE);
                   authicate.signInWithEmailAndPassword(mail, pword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                       @Override
                       public void onComplete(@NonNull Task<AuthResult> task) {
                           pBar.setVisibility(View.GONE);
                           if (task.isSuccessful()) {
                               Toast.makeText(Login_activity.this, "Authentication Successfully.", Toast.LENGTH_SHORT).show();
                               Intent intent = new Intent(Login_activity.this, MainActivity.class);
                               startActivity(intent);
                               finish();
                           } else {
                               Toast.makeText(Login_activity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                           }
                       }
                   });
               }
           }
       });
    }
    ProgressDialog loadingBar;
    private void showRecoverPasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter Email");
        LinearLayout linearLayout = new LinearLayout(this);
        final EditText emailet = new EditText(this);

        // write the email using which you registered
        //emailet.setText("Email");
        emailet.setMinEms(16);
        emailet.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        linearLayout.addView(emailet);
        linearLayout.setPadding(10, 10, 10, 10);
        builder.setView(linearLayout);

        // Click on Recover and a email will be sent to your registered email id
        builder.setPositiveButton("Send Link", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String email = emailet.getText().toString().trim();
                if (TextUtils.isEmpty(email)){
                    emailet.setError("Enter Email");
                }else {
                    beginRecovery(email);
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();

    }
    private void beginRecovery(String email) {
        loadingBar = new ProgressDialog(this);
        loadingBar.setMessage("Sending Email....");
        loadingBar.setCanceledOnTouchOutside(false);
        loadingBar.show();

        // calling sendPasswordResetEmail
        // open your email and write the new
        // password and then you can login
        authicate.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                loadingBar.dismiss();
                if (task.isSuccessful()) {
                    // if isSuccessful then done message will be shown
                    // and you can change the password
                    Toast.makeText(Login_activity.this, "Email sent", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(Login_activity.this, "Error Occurred", Toast.LENGTH_LONG).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                loadingBar.dismiss();
                Toast.makeText(Login_activity.this, "Error Failed", Toast.LENGTH_LONG).show();
            }
        });
    }
}


package com.ecom.fyp2023;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.ecom.fyp2023.AppManagers.SharedPreferenceManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.messaging.FirebaseMessaging;

public class Login_activity extends AppCompatActivity {


  /*public void onStart() {
  super.onStart();
  //Check if user is signed in (non-null) and update UI accordingly.
  FirebaseUser currentUser = authicate.getCurrentUser();
  if (currentUser != null) {
      Intent intent = new Intent(Login_activity.this, HomeScreen.class);
       startActivity(intent);
      finish();
 }
    }*/


    private CheckBox checkBoxRememberMe;

    EditText Aemail, Apassword;
    Button loginBtn;
    FirebaseAuth authicate;
    ProgressBar pBar;
    TextView textView, forgotpword;
    public ProgressDialog loginprogress;
    FirebaseFirestore db;
    String mail,pword;
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
        checkBoxRememberMe = findViewById(R.id.checkBoxRememberMe);

        db = FirebaseFirestore.getInstance();


        forgotpword.setOnClickListener(v -> showRecoverPasswordDialog());

        textView = findViewById(R.id.signupText);
        textView.setOnClickListener(v -> {
            Intent intent = new Intent(Login_activity.this, SignUpActivity.class);
            startActivity(intent);
            finish();
        });

        if (!new SharedPreferenceManager(this).isUserLogedOut()) {
            //user's email and password both are saved in preferences
            Intent intent = new Intent(Login_activity.this, HomeScreen.class);
            startActivity(intent);
            finish();
        }

        //show password code
        ToggleButton showPasswordToggle = findViewById(R.id.showPasswordToggle);
        showPasswordToggle.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                Apassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            } else {
                Apassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            }
            // Move the cursor to the end of the text
            Apassword.setSelection(Apassword.length());
        });

        Apassword.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                Apassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            }
        });

       loginBtn.setOnClickListener(v -> {
            mail = Aemail.getText().toString().trim();
            pword = Apassword.getText().toString().trim();

           if (TextUtils.isEmpty(mail)  ) {
               Aemail.setError("Email Required");
           }
           if (TextUtils.isEmpty(pword)) {
               Apassword.setError("Password Required");
           }else{
               pBar.setVisibility(View.VISIBLE);

               authicate.signInWithEmailAndPassword(mail, pword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                   @Override
                   public void onComplete(@NonNull Task<AuthResult> task) {
                       pBar.setVisibility(View.GONE);
                       if (task.isSuccessful()) {
                           FirebaseMessaging.getInstance().getToken()
                                   .addOnCompleteListener(tasks -> {
                                       if (tasks.isSuccessful() && tasks.getResult() != null) {
                                           String fcmToken = tasks.getResult();
                                           // Call a method to store the FCM token in the Firestore users collection
                                           updateFcmTokenInFirestore(mail,fcmToken);
                                       }
                                   });

                           if (checkBoxRememberMe.isChecked())
                               saveLoginDetails(mail, pword);
                           Intent intent = new Intent(Login_activity.this, HomeScreen.class);
                           startActivity(intent);
                           finish();
                       } else {
                           Toast.makeText(Login_activity.this, "Login failed.", Toast.LENGTH_SHORT).show();
                       }
                   }
               });
           }
       });
    }


    private void updateFcmTokenInFirestore(String userEmail, String newFcmToken) {
        // Creating a reference to the Users collection in Firestore.
        CollectionReference dbUsers = db.collection("Users");

        // Querying for the specific user document based on the userEmail.
        Query query = dbUsers.whereEqualTo("userEmail", userEmail);

        query.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    // Updating the fcmToken field in the retrieved document.
                    document.getReference().update("fcmToken", newFcmToken)
                            .addOnSuccessListener(aVoid -> {
                                // FcmToken updated successfully.
                                Log.d("Firestore", "FcmToken updated for user: " + userEmail);
                            })
                            .addOnFailureListener(e -> {
                                // Handle the failure to update the FcmToken.
                                Log.e("Firestore", "Error updating FcmToken", e);
                            });
                }
            } else {
                // Handle the failure to retrieve the user document.
                Log.e("Firestore", "Error getting user document", task.getException());
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
        builder.setPositiveButton("Send Link", (dialog, which) -> {
            String email = emailet.getText().toString().trim();
            if (TextUtils.isEmpty(email)){
                emailet.setError("Enter Email");
            }else {
                beginRecovery(email);
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
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
        authicate.sendPasswordResetEmail(email).addOnCompleteListener(task -> {
            loadingBar.dismiss();
            if (task.isSuccessful()) {
                // if isSuccessful then done message will be shown
                // and you can change the password
                Toast.makeText(Login_activity.this, "Email sent", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(Login_activity.this, "Error Occurred", Toast.LENGTH_LONG).show();
            }
        }).addOnFailureListener(e -> {
            loadingBar.dismiss();
            Toast.makeText(Login_activity.this, "Error Failed", Toast.LENGTH_LONG).show();
        });
    }
    //sharepre to save user data
    private void saveLoginDetails(String email, String password) {
        new SharedPreferenceManager(this).saveLoginDetails(email, password);
    }
}


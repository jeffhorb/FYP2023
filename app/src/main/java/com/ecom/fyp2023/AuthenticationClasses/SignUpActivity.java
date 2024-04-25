package com.ecom.fyp2023.AuthenticationClasses;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.ecom.fyp2023.ModelClasses.Users;
import com.ecom.fyp2023.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.regex.Pattern;

public class SignUpActivity extends AppCompatActivity {
    EditText  email, password;
    TextView loginN, num, atoz, AtoZ, symbols;
    Button signupButn;
    FirebaseAuth authicate;

    ProgressBar pBar;

    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        //userName = findViewById(R.id.name);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        num = findViewById(R.id.num);
        atoz = findViewById(R.id.atoz);
        AtoZ = findViewById(R.id.AtoZ);
        symbols = findViewById(R.id.symbol);

        db = FirebaseFirestore.getInstance();

        //show password code
        ToggleButton showPasswordToggle = findViewById(R.id.showPasswordToggle);
        showPasswordToggle.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                password.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            } else {
                password.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            }
            // Move the cursor to the end of the text
            password.setSelection(password.length());
        });

        password.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                password.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            }
        });

        signupButn = findViewById(R.id.signupButton);
        pBar = findViewById(R.id.progressBar);
        authicate = FirebaseAuth.getInstance();

        //check if user is already registered in
        loginN = findViewById(R.id.loginNow);
        loginN.setOnClickListener(v -> {
            Intent intent = new Intent(SignUpActivity.this, Login_activity.class);
            startActivity(intent);
            finish();
        });

        password.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String pword = password.getText().toString().trim();
                validatepass(pword);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        signupButn.setOnClickListener(view -> {
            String mail = email.getText().toString().trim();
            String pword = password.getText().toString().trim();

            if (TextUtils.isEmpty(mail)) {
                email.setError("Email Required");
            }
            else if (TextUtils.isEmpty(pword)) {
                password.setError("Password Required");
            } else if (pword.length() < 8) {
                password.setError("Password length must be at least 8 characters");
            } else {
                pBar.setVisibility(View.VISIBLE);

                // Username does not exist, proceed with user authentication
                authicate.createUserWithEmailAndPassword(mail, pword).addOnCompleteListener(authTask -> {
                    pBar.setVisibility(View.GONE);
                    if (authTask.isSuccessful()) {
                        showUsernameInputDialog();
                    } else {
                        Toast.makeText(SignUpActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }


    private void showUsernameInputDialog() {
        // Create an AlertDialog.Builder instance
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter Your Username");

        // Set up the layout for the dialog
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_username_input, null);
        final EditText usernameEditText = dialogView.findViewById(R.id.editTextUsername);
        EditText occupationEditText = dialogView.findViewById(R.id.occupation);
        builder.setView(dialogView);

        // Set up the buttons for positive (OK) and negative (Cancel) actions
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String enteredUsername = usernameEditText.getText().toString();
                String occupation = occupationEditText.getText().toString();
                String mail = email.getText().toString().trim();

                // User could be forced to add a username
                if (enteredUsername.isEmpty()) {
                    // If the username is null or empty, set it to the email
                    enteredUsername = mail;
                }
                if (occupation.isEmpty()) {
                    // If the username is null or empty, set it to the email
                    occupationEditText.setText("Add occupation. Occupation can be updated later");
                }


                // Check if the entered username already exists in Firestore
                checkUsernameExists(enteredUsername,occupation, mail);
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // User canceled, show a warning dialog
                showCancelWarningDialog();
            }
        });

        // Show the dialog
        builder.show();
    }



    private void showUsernameExistsDialog() {
        // Create a dialog to inform the user that the username already exists
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Username Exists");
        builder.setMessage("The entered username already exists. Please choose a different one.");

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Show the username input dialog again
                showUsernameInputDialog();
            }
        });

        // Show the dialog
        builder.show();
    }

    private void checkUsernameExists(String enteredUsername, String occupation, String userEmail) {
        CollectionReference dbUsers = db.collection("Users");

        // Query Firestore to check if the username already exists
        dbUsers.whereEqualTo("userName", enteredUsername)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        boolean usernameExists = false;
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String username = document.getString("userName");
                            // Perform a case-sensitive comparison
                            if (username.equals(enteredUsername)) {
                                // Username already exists, set the flag to true
                                usernameExists = true;
                                break;
                            }
                        }

                        if (usernameExists) {
                            // Username already exists, prompt the user to enter a new one
                            showUsernameExistsDialog();
                        } else {
                            // Username is unique, proceed with authentication
                            FirebaseMessaging.getInstance().getToken()
                                    .addOnCompleteListener(tokenTask -> {
                                        if (tokenTask.isSuccessful() && tokenTask.getResult() != null) {
                                            String fcmToken = tokenTask.getResult();
                                            // Call a method to store the FCM token in the Firestore users collection
                                            addUserToFirestore(enteredUsername, userEmail, fcmToken,occupation);
                                            Toast.makeText(SignUpActivity.this, "Authentication Successfully.", Toast.LENGTH_SHORT).show();
                                            Intent intent = new Intent(SignUpActivity.this, Login_activity.class);
                                            startActivity(intent);
                                        }
                                    });
                        }
                    } else {
                        // Handle the failure to check the username
                        Log.e("Firestore", "Error checking username", task.getException());
                        Toast.makeText(SignUpActivity.this, "Error checking username.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showCancelWarningDialog() {
        // Create a warning dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("username will set to your email");
        builder.setMessage("Are you sure you want to cancel?");
        String mail = email.getText().toString().trim();

        // Set up the buttons for positive (Go Back) and negative (Cancel) actions
        builder.setPositiveButton("Go Back", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // User chose to go back, show the username input dialog again
                showUsernameInputDialog();
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // User chose to completely cancel, do nothing or handle accordingly
                FirebaseMessaging.getInstance().getToken()
                        .addOnCompleteListener(tasks -> {
                            if (tasks.isSuccessful() && tasks.getResult() != null) {
                                String fcmToken = tasks.getResult();
                                // Call a method to store the FCM token in the Firestore users collection
                                addUserToFirestore(mail, mail, fcmToken,null);
                                Toast.makeText(SignUpActivity.this, "Authentication Successfully.", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(SignUpActivity.this, Login_activity.class);
                                startActivity(intent);
                            }
                        });
            }
        });

        // Show the warning dialog
        builder.show();
    }

    private void addUserToFirestore(String userName, String userEmail, String fcmToken,String occupation) {
        // creating a collection reference
        // for our Firebase Firestore database.
        CollectionReference dbUsers = db.collection("Users");

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String userId = currentUser.getUid();
        // adding our data to our courses object class.
        Users users = new Users(userName, userEmail, fcmToken,userId,occupation);

        // below method is use to add data to Firebase Firestore.
        dbUsers.add(users).addOnSuccessListener(documentReference -> {
            // Document added successfully
            Log.d("Firestore", "User added with ID: " + documentReference.getId());
        }).addOnFailureListener(e -> {
            // Handle the failure to add the document
            Log.e("Firestore", "Error adding user", e);
        });
    }

    public void validatepass(String password) {
        String specialC = ("[ \\\\@  [\\\"]\\\\[\\\\]\\\\\\|^{#%'*/<()>}:`;,!& .?_$+-]+");
        // check for pattern
        Pattern uppercase = Pattern.compile("[A-Z]");
        Pattern lowercase = Pattern.compile("[a-z]");
        Pattern digit = Pattern.compile("[0-9]");
        Pattern specialChar = Pattern.compile(specialC);

        // if lowercase character is not present
        if (!lowercase.matcher(password).find()) {
            atoz.setTextColor(Color.RED);
        } else {
            // if lowercase character is  present
            atoz.setTextColor(Color.GREEN);
        }

        // if uppercase character is not present
        if (!uppercase.matcher(password).find()) {
            AtoZ.setTextColor(Color.RED);
        } else {
            // if uppercase character is  present
            AtoZ.setTextColor(Color.GREEN);
        }
        // if digit is not present
        if (!digit.matcher(password).find()) {
            num.setTextColor(Color.RED);
        } else {
            // if digit is present
            num.setTextColor(Color.GREEN);
        }
        // if password symbol is present
        if (!specialChar.matcher(password).find()) {
            symbols.setTextColor(Color.RED);
        } else {
            symbols.setTextColor(Color.GREEN);
        }
    }



      /*private void sendEmailVerification(FirebaseUser user) {
        user.sendEmailVerification().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(SignUpActivity.this, "Verification email sent.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(SignUpActivity.this, "Error occurred.", Toast.LENGTH_SHORT).show();
            }
        });
    }*/
}
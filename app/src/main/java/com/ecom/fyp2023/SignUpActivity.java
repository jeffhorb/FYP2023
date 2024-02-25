package com.ecom.fyp2023;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.appcompat.app.AppCompatActivity;

import com.ecom.fyp2023.ModelClasses.Users;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.regex.Pattern;

public class SignUpActivity extends AppCompatActivity {
    EditText userName, email, password;
    TextView loginN, num, atoz, AtoZ, symbols;
    Button signupButn;
    FirebaseAuth authicate;
    ProgressBar pBar;

    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        userName = findViewById(R.id.name);
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
            String userN = userName.getText().toString();


            if (TextUtils.isEmpty(mail)) {
                email.setError("Email Required");
            }
            else if (TextUtils.isEmpty(userN)) {
                userName.setError("User name required");
            }
            else if (TextUtils.isEmpty(pword)) {
                password.setError("Password Required");
            } else if (pword.length() < 8) {
                password.setError("Password length must be at least 8 characters");
                return;
            } else {
                pBar.setVisibility(View.VISIBLE);

                // Check if the username already exists in Firestore
                checkUsernameExistence(userN, mail, pword);

                /*authicate.createUserWithEmailAndPassword(mail, pword).addOnCompleteListener(task -> {
                    pBar.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        addUserToFirestore(userN, mail);
                        Toast.makeText(SignUpActivity.this, "Authentication Successfully.", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(SignUpActivity.this, Login_activity.class);
                        startActivity(intent);
                        //FirebaseUser user = authicate.getCurrentUser();
                        //sendEmailVerification(user);
                    } else {
                        Toast.makeText(SignUpActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                    }
                });*/
            }

        });
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

    private void checkUsernameExistence(String userName, String email, String password) {
        CollectionReference dbUsers = db.collection("Users");
        Query query = dbUsers.whereEqualTo("userName", userName);

        query.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                QuerySnapshot querySnapshot = task.getResult();
                if (querySnapshot != null && !querySnapshot.isEmpty()) {
                    // Username already exists, show an error message or take appropriate action
                    pBar.setVisibility(View.GONE);
                    Toast.makeText(SignUpActivity.this, "Username already exists. Please use a different one.", Toast.LENGTH_SHORT).show();
                } else {
                    // Username does not exist, proceed with user authentication
                    authicate.createUserWithEmailAndPassword(email, password).addOnCompleteListener(authTask -> {
                        pBar.setVisibility(View.GONE);
                        if (authTask.isSuccessful()) {
                            addUserToFirestore(userName, email);
                            Toast.makeText(SignUpActivity.this, "Authentication Successfully.", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(SignUpActivity.this, Login_activity.class);
                            startActivity(intent);
                            // FirebaseUser user = authicate.getCurrentUser();
                            // sendEmailVerification(user);
                        } else {
                            Toast.makeText(SignUpActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            } else {
                // Handle the case where checking for username existence fails
                pBar.setVisibility(View.GONE);
                Toast.makeText(SignUpActivity.this, "Error checking username existence.", Toast.LENGTH_SHORT).show();
            }
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


    private void addUserToFirestore(String userName, String userEmail) {
        // creating a collection reference
        // for our Firebase Firestore database.
        CollectionReference dbUsers = db.collection("Users");

        // adding our data to our courses object class.
        Users users = new Users(userName, userEmail);

        // below method is use to add data to Firebase Firestore.
        dbUsers.add(users).addOnSuccessListener(documentReference -> {

        }).addOnFailureListener(e -> {

        });

    }
}
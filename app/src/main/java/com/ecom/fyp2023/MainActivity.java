package com.ecom.fyp2023;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {
    FirebaseAuth autenticate;
    FirebaseUser user;
    Button logoutBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        logoutBtn = findViewById(R.id.logout);
        autenticate = FirebaseAuth.getInstance();

        user = autenticate.getCurrentUser();
        if (user == null){
            Intent intent = new Intent(MainActivity.this, Login_activity.class);
            startActivity(intent);
            finish();
        }
        /*
        FirebaseUser verifiedUser = FirebaseAuth.getInstance().getCurrentUser();
        if (verifiedUser != null) {
            boolean isEmailVerified = verifiedUser.isEmailVerified();
            if (isEmailVerified) {
                Toast.makeText(MainActivity.this, "User Verified.", Toast.LENGTH_SHORT).show();
            } else {
                    Toast.makeText(MainActivity.this, "Error occurred", Toast.LENGTH_SHORT).show();
            }
        }
         */
        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(MainActivity.this, Login_activity.class);
                startActivity(intent);
                finish();
            }
        });

    }
}
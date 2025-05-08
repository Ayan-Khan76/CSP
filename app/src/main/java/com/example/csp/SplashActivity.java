package com.example.csp;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.csp.freelancerapp.FreelancerMainActivity;
import com.example.csp.SignUpActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SplashActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("users");

        new Handler().postDelayed(this::checkUserStatus, 3000);
    }

    private void checkUserStatus() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            databaseReference.child(userId).child("role").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String role = snapshot.getValue(String.class);
                        if ("Freelancer".equalsIgnoreCase(role)) {
                            startActivity(new Intent(SplashActivity.this, FreelancerMainActivity.class));
                        } else {
                            startActivity(new Intent(SplashActivity.this, MainActivity.class));
                        }
                    } else {
                        // No role found, send to SignupActivity
                        Toast.makeText(SplashActivity.this, "No role found, please sign up again.", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(SplashActivity.this, SignUpActivity.class));
                    }
                    finish();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("FirebaseDB", "Error fetching role", error.toException());
                    startActivity(new Intent(SplashActivity.this, SignUpActivity.class));
                    finish();
                }
            });
        } else {
            // No user logged in, go to Signup
            startActivity(new Intent(SplashActivity.this, SignUpActivity.class));
            finish();
        }
    }
}

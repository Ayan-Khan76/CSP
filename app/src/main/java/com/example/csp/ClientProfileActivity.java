package com.yourappname;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.csp.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ClientProfileActivity extends AppCompatActivity {
    private TextView nameTextView, locationTextView;
    private ImageView profileImageView;
    private DatabaseReference databaseReference;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_profile);

        nameTextView = findViewById(R.id.clientName);
        locationTextView = findViewById(R.id.clientLocation);
        profileImageView = findViewById(R.id.clientProfileImage);

        userId = getIntent().getStringExtra("userId");
        databaseReference = FirebaseDatabase.getInstance().getReference("clients").child(userId);

        fetchClientData();
    }

    private void fetchClientData() {
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String name = snapshot.child("name").getValue(String.class);
                    String city = snapshot.child("city").getValue(String.class);
                    String state = snapshot.child("state").getValue(String.class);
                    String profileImageUrl = snapshot.child("profileImageUrl").getValue(String.class);

                    nameTextView.setText(name);
                    locationTextView.setText(city + ", " + state);
                    if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                        Glide.with(ClientProfileActivity.this).load(profileImageUrl).into(profileImageView);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e("ClientProfile", "Error fetching client data", error.toException());
            }
        });
    }
}

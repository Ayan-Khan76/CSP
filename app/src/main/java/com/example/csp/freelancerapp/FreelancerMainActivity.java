package com.example.csp.freelancerapp;

import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.cloudinary.android.MediaManager;
import com.example.csp.CloudinaryConfig;
import com.example.csp.R;
import com.example.csp.freelancerapp.Fragment.FreelancerChatFragment;
import com.example.csp.freelancerapp.Fragment.FreelancerProfileFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.HashMap;
import java.util.Map;

public class FreelancerMainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_freelancer_main);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);



        // Set the default fragment
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new FreelancerProfileFragment())
                    .commit();
        }

        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            Fragment selectedFragment = null;

            if (item.getItemId() == R.id.freelancerFragmentProfile) {
                selectedFragment = new FreelancerProfileFragment();
            } else if (item.getItemId() == R.id.freelancerFragmentChat) {
                selectedFragment = new FreelancerChatFragment();
            }

            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment)
                        .commit();
            }

            return true;
        });

    }
}

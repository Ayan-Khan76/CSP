package com.example.csp;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.NavGraph;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Remove the ActionBar0
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Retrieve user role from SharedPreferences
        //Creating Issue, Screen Goes White
        //SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        //String userRole = sharedPreferences.getString("userRole", "Client"); // Default is "Client"

        // Find the NavHostFragment
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);

        // Get the NavController from NavHostFragment
        assert navHostFragment != null;
        NavController navController = navHostFragment.getNavController();

        // Dynamically load the navigation graph
        /*NavGraph navGraph = navController.getNavInflater().inflate(userRole.equals("Freelancer")
                ? R.navigation.navigation_freelancer
                : R.navigation.navigation_client);
        navController.setGraph(navGraph);*/

        // Set up BottomNavigationView
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        NavigationUI.setupWithNavController(bottomNavigationView, navController);
    }
}

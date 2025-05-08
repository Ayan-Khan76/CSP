package com.example.csp.freelancerapp.form;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.csp.LoginActivity;
import com.example.csp.R;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FreelancerFormActivity3 extends AppCompatActivity {

    private ChipGroup chipGroupSkills;
    private EditText editTextHourlyRate, editTextProjectBasedPricing, editTextExperience;
    private List<String> availableSkills = Arrays.asList("Java", "Android", "Animation", "Web Development", "UI/UX Design", "Graphic Design", "Video Editing", "React", "Node.js");
    private DatabaseReference freelancerRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_freelancer_form3);

        initializeFirebase();

        chipGroupSkills = findViewById(R.id.chipGroupSkills);
        editTextHourlyRate = findViewById(R.id.editTextHourlyRate);
        editTextProjectBasedPricing = findViewById(R.id.editTextProjectBasedPricing);
        editTextExperience = findViewById(R.id.editTextExperience);
        Button buttonAddSkill = findViewById(R.id.buttonAddSkill);
        Button buttonBack = findViewById(R.id.buttonBack3);
        Button buttonSubmit = findViewById(R.id.buttonSubmit);

        buttonAddSkill.setOnClickListener(v -> showAddSkillDialog());
        buttonBack.setOnClickListener(v -> finish());
        buttonSubmit.setOnClickListener(v -> submitForm());
    }

    private void initializeFirebase() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        freelancerRef = databaseReference.child("freelancers").child(userId);
    }

    private void showAddSkillDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select a Skill");

        List<String> remainingSkills = new ArrayList<>(availableSkills);
        for (int i = 0; i < chipGroupSkills.getChildCount(); i++) {
            Chip chip = (Chip) chipGroupSkills.getChildAt(i);
            remainingSkills.remove(chip.getText().toString());
        }

        String[] skillsArray = remainingSkills.toArray(new String[0]);
        builder.setItems(skillsArray, (dialog, which) -> {
            String selectedSkill = skillsArray[which];
            addChip(selectedSkill);
        });

        builder.show();
    }

    private void addChip(String skill) {
        LayoutInflater inflater = LayoutInflater.from(this);
        Chip chip = (Chip) inflater.inflate(R.layout.item_skill, chipGroupSkills, false);
        chip.setText(skill);
        chip.setCloseIconVisible(true);
        chip.setOnCloseIconClickListener(v -> chipGroupSkills.removeView(chip));
        chipGroupSkills.addView(chip);
    }

    private void submitForm() {
        String hourlyRate = editTextHourlyRate.getText().toString();
        String projectBasedPricing = editTextProjectBasedPricing.getText().toString();
        String experience = editTextExperience.getText().toString();

        if (hourlyRate.isEmpty() || projectBasedPricing.isEmpty() || experience.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        List<String> selectedSkills = new ArrayList<>();
        for (int i = 0; i < chipGroupSkills.getChildCount(); i++) {
            Chip chip = (Chip) chipGroupSkills.getChildAt(i);
            selectedSkills.add(chip.getText().toString());
        }

        if (selectedSkills.isEmpty()) {
            Toast.makeText(this, "Please add at least one skill", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> freelancerData = new HashMap<>();
        freelancerData.put("skills", selectedSkills);
        freelancerData.put("hourlyRate", parseIntSafely(hourlyRate));
        freelancerData.put("projectBasedPricing", parseIntSafely(projectBasedPricing));
        freelancerData.put("experience", parseIntSafely(experience));

        freelancerRef.updateChildren(freelancerData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(FreelancerFormActivity3.this, "Form submitted successfully!", Toast.LENGTH_LONG).show();
                    navigateToLoginActivity();
                })
                .addOnFailureListener(e -> Toast.makeText(FreelancerFormActivity3.this, "Failed to submit form: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void navigateToLoginActivity() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    //to convert values to int
    private int parseIntSafely(Object value) {
        if (value instanceof Number) {
            return ((Number) value).intValue(); // Already a number, return as int
        }
        try {
            return Integer.parseInt(value.toString().trim()); // Convert String to int
        } catch (NumberFormatException e) {
            return 0; // Default value if conversion fails
        }
    }

}
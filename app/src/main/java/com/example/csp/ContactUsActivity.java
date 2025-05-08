package com.example.csp;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.ViewGroup;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class ContactUsActivity extends AppCompatActivity {

    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_us);

        // Initialize Firebase Database
        databaseReference = FirebaseDatabase.getInstance().getReference("feedbacks");

        Button sendFeedbackButton = findViewById(R.id.sendFeedbackButton);
        sendFeedbackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFeedbackDialog();
            }
        });
    }

    private void showFeedbackDialog() {
        Dialog dialog = new Dialog(ContactUsActivity.this);
        dialog.setContentView(R.layout.feedback_form);

        dialog.getWindow().setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        // Initialize UI components
        Button submitButton = dialog.findViewById(R.id.submitFeedback);
        final EditText emailInput = dialog.findViewById(R.id.emailInput);
        final EditText messageInput = dialog.findViewById(R.id.messageInput);
        final TextView statusMessage = dialog.findViewById(R.id.statusMessage);
        final RatingBar reviewRatingBar = dialog.findViewById(R.id.reviewRatingBar);

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailInput.getText().toString();
                String message = messageInput.getText().toString();
                float rating = reviewRatingBar.getRating();

                if (!email.isEmpty() && !message.isEmpty() && rating > 0) {
                    // Store feedback in Firebase
                    saveFeedback(email, message, rating);

                    // Show success message
                    statusMessage.setText("Feedback submitted successfully!");
                    statusMessage.setTextColor(Color.GREEN);
                    statusMessage.setVisibility(View.VISIBLE);

                    // Clear fields
                    emailInput.setText("");
                    messageInput.setText("");
                    reviewRatingBar.setRating(0);
                } else {
                    statusMessage.setText("Please fill all fields");
                    statusMessage.setTextColor(Color.RED);
                    statusMessage.setVisibility(View.VISIBLE);
                }
            }
        });

        dialog.show();
    }

    private void saveFeedback(String email, String message, float rating) {
        String feedbackId = databaseReference.push().getKey(); // Generate a unique ID
        if (feedbackId != null) {
            Map<String, Object> feedbackData = new HashMap<>();
            feedbackData.put("email", email);
            feedbackData.put("message", message);
            feedbackData.put("rating", rating);

            databaseReference.child(feedbackId).setValue(feedbackData);
        }
    }
}

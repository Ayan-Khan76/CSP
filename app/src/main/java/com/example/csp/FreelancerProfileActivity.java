package com.example.csp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.csp.Adapter.PortfolioAdapter;
import com.example.csp.Adapter.ReviewsAdapter;
import com.example.csp.Adapter.SkillsAdapter;
import com.example.csp.Model.PortfolioItem;
import com.example.csp.Model.Review;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class FreelancerProfileActivity extends AppCompatActivity {

    private CircleImageView profileImage;
    private TextView freelancerName;
    private RecyclerView portfolioRecyclerView;
    private RecyclerView skillsRecyclerView;
    private TextView aboutText;
    private TextView stateText;
    private TextView cityText;
    private TextView landmarkText;
    private TextView pinText;
    private RecyclerView reviewsRecyclerView;
    private TextView hourlyRateText;
    private ImageButton backButton;
    private TextView projectBasedPricingText;
    private Button addReviewButton;
    private View reviewInputSection;
    private RatingBar reviewRatingBar;
    private TextView reviewEditText;
    private Button submitReviewButton;
    private MaterialButton chatButton;
    private ReviewsAdapter reviewsAdapter;
    private List<Review> reviews;
    private List<PortfolioItem> portfolioList;
    private PortfolioAdapter adapter;

    private DatabaseReference freelancerRef;
    private DatabaseReference userRef;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_freelancer_profile);

        initializeFirebase();
        initViews();
        setupBackButton();
        setupAddReviewButton();
        setupSubmitReviewButton();
        setupChatButton();

        portfolioList = new ArrayList<>();
        portfolioRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        adapter = new PortfolioAdapter(this, portfolioList);
        portfolioRecyclerView.setAdapter(adapter);

        loadFreelancerData();
    }

    private void initializeFirebase() {
        userId = getIntent().getStringExtra("userId");
        if (userId == null || userId.isEmpty()) {
            Toast.makeText(this, "Freelancer ID not found!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        freelancerRef = FirebaseDatabase.getInstance().getReference("freelancers").child(userId);
        userRef = FirebaseDatabase.getInstance().getReference("users").child(userId);
    }

    private void initViews() {
        profileImage = findViewById(R.id.profileImage);
        freelancerName = findViewById(R.id.freelancerName);
        portfolioRecyclerView = findViewById(R.id.portfolioRecyclerView);
        skillsRecyclerView = findViewById(R.id.skillsRecyclerView);
        aboutText = findViewById(R.id.aboutText);
        reviewsRecyclerView = findViewById(R.id.reviewsRecyclerView);
        hourlyRateText = findViewById(R.id.hourlyRateText);
        projectBasedPricingText = findViewById(R.id.projectBasedPricingText);
        backButton = findViewById(R.id.backButton);
        addReviewButton = findViewById(R.id.addReviewButton);
        reviewInputSection = findViewById(R.id.reviewInputSection);
        reviewRatingBar = findViewById(R.id.reviewRatingBar);
        reviewEditText = findViewById(R.id.reviewEditText);
        submitReviewButton = findViewById(R.id.submitReviewButton);
        chatButton = findViewById(R.id.chatButton);
        stateText = findViewById(R.id.stateText);
        cityText = findViewById(R.id.cityText);
        landmarkText = findViewById(R.id.landmarkText);
        pinText = findViewById(R.id.pinText);
    }

    private void loadFreelancerData() {
        freelancerRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    try {
                        // Load basic profile info
                        String about = snapshot.child("about").getValue(String.class);
                        String profileImageUrl = snapshot.child("profileImageUrl").getValue(String.class);
                        String landmark = snapshot.child("landmark").getValue(String.class);
                        hourlyRateText.setText("Hourly Rate: ₹" + getIntValue(snapshot, "hourlyRate") + "/hour");
                        projectBasedPricingText.setText("Project Based Pricing: ₹" + getIntValue(snapshot, "projectBasedPricing"));
                        stateText.setText("State: " + snapshot.child("state").getValue(String.class));
                        cityText.setText("City: " + snapshot.child("city").getValue(String.class));
                        pinText.setText("Pin: " + snapshot.child("pincode").getValue(String.class));
                        if (landmark == null || landmark.trim().isEmpty()) {
                            landmarkText.setVisibility(View.GONE);
                        } else {
                            landmarkText.setText("Landmark: " + landmark);
                            landmarkText.setVisibility(View.VISIBLE);
                        }


                        // Load profile image
                        if (profileImageUrl != null) {
                            Glide.with(FreelancerProfileActivity.this)
                                    .load(profileImageUrl)
                                    .placeholder(R.drawable.baseline_person_24)
                                    .error(R.drawable.baseline_person_24)
                                    .into(profileImage);
                        } else {
                            profileImage.setImageResource(R.drawable.baseline_person_24);
                        }

                        // Load freelancer name
                        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                String name = snapshot.child("name").getValue(String.class);
                                freelancerName.setText(name != null ? name : "");
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Log.e("FreelancerProfile", "Error loading name: " + error.getMessage());
                            }
                        });

                        // Set about text
                        aboutText.setText(about != null ? about : "No description available");

                        // Load skills
                        List<String> skillsList = new ArrayList<>();
                        if (snapshot.hasChild("skills")) {
                            for (DataSnapshot skillSnapshot : snapshot.child("skills").getChildren()) {
                                String skill = skillSnapshot.getValue(String.class);
                                if (skill != null) {
                                    skillsList.add(skill);
                                }
                            }
                        }
                        SkillsAdapter skillsAdapter = new SkillsAdapter(skillsList);
                        skillsRecyclerView.setLayoutManager(new LinearLayoutManager(FreelancerProfileActivity.this, LinearLayoutManager.HORIZONTAL, false));
                        skillsRecyclerView.setAdapter(skillsAdapter);

                        // Load portfolio items
                        DataSnapshot portfolioSnapshot = snapshot.child("portfolio");
                        portfolioList.clear();
                        if (portfolioSnapshot.exists()) {
                            for (DataSnapshot itemSnapshot : portfolioSnapshot.getChildren()) {
                                try {
                                    PortfolioItem portfolioItem = new PortfolioItem();
                                    portfolioItem.setTitle(itemSnapshot.child("title").getValue(String.class));
                                    portfolioItem.setDescription(itemSnapshot.child("description").getValue(String.class));
                                    portfolioItem.setLink(itemSnapshot.child("link").getValue(String.class));

                                    // Load media items
                                    Map<String, PortfolioItem.MediaItem> mediaItems = new HashMap<>();
                                    DataSnapshot mediaItemsSnapshot = itemSnapshot.child("mediaItems");
                                    if (mediaItemsSnapshot.exists()) {
                                        for (DataSnapshot mediaSnapshot : mediaItemsSnapshot.getChildren()) {
                                            String uri = mediaSnapshot.child("uri").getValue(String.class);
                                            if (uri != null) {
                                                PortfolioItem.MediaItem mediaItem = new PortfolioItem.MediaItem();
                                                mediaItem.setUri(uri);
                                                mediaItems.put(mediaSnapshot.getKey(), mediaItem);
                                            }
                                        }
                                    }
                                    portfolioItem.setMediaItems(mediaItems);
                                    portfolioList.add(portfolioItem);
                                } catch (Exception e) {
                                    Log.e("Portfolio", "Error parsing portfolio item: " + e.getMessage());
                                }
                            }
                            adapter.notifyDataSetChanged();
                        }
                        loadReviews();

                    } catch (Exception e) {
                        Log.e("FreelancerProfile", "Error loading data: " + e.getMessage());
                        Toast.makeText(FreelancerProfileActivity.this,
                                "Error loading freelancer data: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(FreelancerProfileActivity.this,
                            "Freelancer data not found",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("FreelancerProfile", "Database error: " + error.getMessage());
                Toast.makeText(FreelancerProfileActivity.this,
                        "Failed to load data: " + error.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }



    private void setupChatButton() {
        chatButton.setOnClickListener(v -> {
            String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            String chatId = currentUserId.compareTo(userId) < 0 ? currentUserId + "_" + userId : userId + "_" + currentUserId;

            userRef.child("name").get().addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult().exists()) {
                    String receiverName = task.getResult().getValue(String.class);

                    Intent intent = new Intent(FreelancerProfileActivity.this, ChatActivity.class);
                    intent.putExtra("chatId", chatId);
                    intent.putExtra("receiverId", userId);
                    intent.putExtra("receiverName", receiverName);
                    startActivity(intent);
                }
            });
        });
    }

    private void loadReviews() {
        reviews = new ArrayList<>();
        reviewsAdapter = new ReviewsAdapter(reviews);
        reviewsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        reviewsRecyclerView.setAdapter(reviewsAdapter);

        freelancerRef.child("reviews").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                reviews.clear();
                for (DataSnapshot reviewSnapshot : snapshot.getChildren()) {
                    Review review = reviewSnapshot.getValue(Review.class);
                    if (review != null) {
                        reviews.add(review);
                    }
                }
                reviewsAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("FreelancerProfile", "Failed to load reviews: " + error.getMessage());
            }
        });
    }



    private void setupBackButton() {
        backButton.setOnClickListener(v -> onBackPressed());
    }

    private void setupAddReviewButton() {
        addReviewButton.setOnClickListener(v -> {
            reviewInputSection.setVisibility(View.VISIBLE);
            addReviewButton.setVisibility(View.GONE);
        });
    }

    private void setupSubmitReviewButton() {
        submitReviewButton.setOnClickListener(v -> {
            if (validateReviewInput()) {
                submitReview();
            }
        });
    }

    private boolean validateReviewInput() {
        String reviewText = reviewEditText.getText().toString().trim();
        float rating = reviewRatingBar.getRating();

        if (TextUtils.isEmpty(reviewText)) {
            Toast.makeText(this, "Please write a review", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (rating == 0) {
            Toast.makeText(this, "Please select a rating", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void submitReview() {
        String reviewText = reviewEditText.getText().toString().trim();
        float rating = reviewRatingBar.getRating();
        String reviewerId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(reviewerId);
        userRef.child("name").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String reviewerName = snapshot.getValue(String.class);
                    DatabaseReference reviewsRef = freelancerRef.child("reviews").push();
                    Review newReview = new Review(reviewText, rating, reviewerName);

                    reviewsRef.setValue(newReview)
                            .addOnSuccessListener(aVoid -> {
                                reviewEditText.setText("");
                                reviewRatingBar.setRating(0);
                                reviewInputSection.setVisibility(View.GONE);
                                addReviewButton.setVisibility(View.VISIBLE);
                                Toast.makeText(FreelancerProfileActivity.this, "Review submitted successfully", Toast.LENGTH_SHORT).show();

                                //  Recalculate and update average rating
                                updateAverageRating();

                                loadReviews(); // Reload Reviews
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(FreelancerProfileActivity.this, "Failed to submit review: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                } else {
                    Toast.makeText(FreelancerProfileActivity.this, "User name not found!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("SubmitReview", "Failed to get reviewer name: " + error.getMessage());
            }
        });
    }

    private void updateAverageRating() {
        freelancerRef.child("reviews").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                float totalRating = 0;
                int count = 0;

                for (DataSnapshot reviewSnapshot : snapshot.getChildren()) {
                    Review review = reviewSnapshot.getValue(Review.class);
                    if (review != null) {
                        totalRating += review.getRating();
                        count++;
                    }
                }

                float averageRating = (count > 0) ? totalRating / count : 0;
                freelancerRef.child("averageRating").setValue(averageRating); // 🔥 Real-time update
                Log.d("UpdateRating", "Updated average rating: " + averageRating);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("UpdateRating", "Failed to update rating: " + error.getMessage());
            }
        });
    }




    private int getIntValue(DataSnapshot snapshot, String key) {
        try {
            return snapshot.hasChild(key) ? snapshot.child(key).getValue(Integer.class) : 0;
        } catch (Exception e) {
            return 0;
        }
    }

}
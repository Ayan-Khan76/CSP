package com.example.csp.FreelancersList;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.csp.FreelancerProfileActivity;
import com.example.csp.FreelancersList.Adapter.FreelancerAdapter;
import com.example.csp.FreelancersList.Model.Freelancer;
import com.example.csp.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FreelancersListActivity extends AppCompatActivity {

    private static final String TAG = "FreelancersListActivity";
    private RecyclerView recyclerView;
    private FreelancerAdapter adapter;
    private List<Freelancer> allFreelancers = new ArrayList<>();
    private DatabaseReference freelancersRef;
    private String searchQuery;
    private String selectedSkill;
    private TextView titleText;
    private ImageButton backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_freelancers_list);
        selectedSkill = getIntent().getStringExtra("selectedSkill");
        searchQuery = getIntent().getStringExtra("searchQuery");

        // Initialize views
        recyclerView = findViewById(R.id.freelancersRecyclerView);
        titleText = findViewById(R.id.titletext);
        backButton = findViewById(R.id.backButton);

        // Set up RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(new androidx.recyclerview.widget.DividerItemDecoration(this, androidx.recyclerview.widget.DividerItemDecoration.VERTICAL));

        // Initialize adapter
        adapter = new FreelancerAdapter(this, allFreelancers, freelancer -> {
            Intent intent = new Intent(FreelancersListActivity.this, FreelancerProfileActivity.class);
            intent.putExtra("userId", freelancer.getId());
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);

        // Set title text based on search or skill
        if (searchQuery != null && !searchQuery.isEmpty()) {
            titleText.setText("Search: " + searchQuery);
        } else if (selectedSkill != null) {
            titleText.setText(selectedSkill);
        }else if (getIntent().getBooleanExtra("fromImageSlider", false)) {
            // Set blank title when coming from image slider
            titleText.setText("");
        }
        titleText.setVisibility(View.VISIBLE);

        // Set up back button
        backButton.setOnClickListener(v -> finish());

        // Fetch freelancers data
        freelancersRef = FirebaseDatabase.getInstance().getReference().child("freelancers");
        fetchFreelancersData();
    }

    public void fetchFreelancersData() {
        freelancersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                allFreelancers.clear();
                List<Freelancer> fetchedFreelancers = new ArrayList<>();
                Log.d("DEBUG", "Total freelancers in database: " + dataSnapshot.getChildrenCount());

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String userId = snapshot.getKey();
                    if (userId == null) {
                        Log.w(TAG, "Skipping freelancer due to null user ID.");
                        continue;
                    }

                    Freelancer freelancer = new Freelancer();
                    freelancer.setId(userId);
                    freelancer.setProfileImageUrl(snapshot.child("profileImageUrl").getValue(String.class));
                    freelancer.setExperience(getIntValue(snapshot, "experience"));
                    freelancer.setHourlyRate(getIntValue(snapshot, "hourlyRate"));
                    freelancer.setProjectBasedPricing(getIntValue(snapshot, "projectBasedPricing"));

                    if (snapshot.hasChild("averageRating")) {
                        freelancer.setAverageRating(snapshot.child("averageRating").getValue(Float.class));
                    } else {
                        freelancer.setAverageRating(0); // Default if no rating
                    }

                    // Fetch skills
                    List<String> skillsList = new ArrayList<>();
                    if (snapshot.hasChild("skills")) {
                        for (DataSnapshot skillSnapshot : snapshot.child("skills").getChildren()) {
                            String skill = skillSnapshot.getValue(String.class);
                            if (skill != null) {
                                skillsList.add(skill);
                            }
                        }
                    }
                    freelancer.setSkills(skillsList);

                    // Only filter by skill if selectedSkill is provided and no searchQuery
                    if (!TextUtils.isEmpty(selectedSkill) && TextUtils.isEmpty(searchQuery) &&
                            !hasMatchingSkill(freelancer, selectedSkill)) {
                        Log.d(TAG, "Skipping freelancer " + userId + " because they do not match the selected skill: " + selectedSkill);
                        continue; // Skip adding this freelancer
                    }

                    // Fetch user details (name and profile image)
                    DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(userId);
                    userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot userSnapshot) {
                            if (userSnapshot.exists()) {
                                freelancer.setName(userSnapshot.child("name").getValue(String.class));
                                if (freelancer.getProfileImageUrl() == null) {
                                    freelancer.setProfileImageUrl(userSnapshot.child("profileImageUrl").getValue(String.class));
                                }
                            } else {
                                Log.w(TAG, "User data does not exist for user ID: " + userId);
                            }

                            // Add to fetched freelancers
                            fetchedFreelancers.add(freelancer);

                            // Only update adapter when at least one freelancer is fetched
                            if (!fetchedFreelancers.isEmpty()) {
                                // Use a Set to ensure unique freelancers
                                Set<Freelancer> uniqueFreelancers = new HashSet<>(fetchedFreelancers);

                                allFreelancers.clear();
                                allFreelancers.addAll(uniqueFreelancers);

                                Log.d(TAG, "Total freelancers after filtering: " + allFreelancers.size());

                                // Apply search query if provided
                                if (searchQuery != null && !searchQuery.isEmpty()) {
                                    filterAndSortFreelancers(searchQuery);
                                } else {
                                    adapter.notifyDataSetChanged();
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Log.e(TAG, "Error fetching user data for user ID: " + userId, error.toException());
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Database error: " + databaseError.getMessage(), databaseError.toException());
            }
        });
    }

    private int getIntValue(DataSnapshot snapshot, String key) {
        try {
            return snapshot.hasChild(key) ? snapshot.child(key).getValue(Integer.class) : 0;
        } catch (Exception e) {
            Log.e(TAG, "Error converting " + key + " to integer", e);
            return 0;
        }
    }

    private boolean hasMatchingSkill(Freelancer freelancer, String selectedSkill) {
        // Debugging logs
        Log.d(TAG, "Selected Skill: " + selectedSkill);
        Log.d(TAG, "Freelancer Skills: " + freelancer.getSkills());

        for (String skill : freelancer.getSkills()) {
            if (skill.trim().equalsIgnoreCase(selectedSkill.trim())) {  // Ignore case and trim spaces
                Log.d(TAG, "Match found for: " + skill);
                return true;
            }
        }

        Log.d(TAG, "No match found for " + selectedSkill);
        return false;
    }

    private void processFreelancerSnapshot(DataSnapshot snapshot) {
        String userId = snapshot.getKey();
        if (userId == null) return;

        Freelancer freelancer = new Freelancer();
        freelancer.setId(userId);

        // Get freelancer details
        String name = snapshot.child("name").getValue(String.class);
        List<String> skills = new ArrayList<>();

        // Get skills
        DataSnapshot skillsSnapshot = snapshot.child("skills");
        for (DataSnapshot skillSnapshot : skillsSnapshot.getChildren()) {
            String skill = skillSnapshot.getValue(String.class);
            if (skill != null) {
                skills.add(skill);
            }
        }

        freelancer.setName(name);
        freelancer.setSkills(skills);

        // Add all freelancers without filtering
        allFreelancers.add(freelancer);
        adapter.notifyDataSetChanged();
    }

    private void calculateAverageRating(DataSnapshot reviewsSnapshot, Freelancer freelancer) {
        float totalRating = 0;
        int reviewCount = 0;

        // Iterate through all reviews
        for (DataSnapshot reviewSnapshot : reviewsSnapshot.getChildren()) {
            // Get the rating value from each review
            Float rating = reviewSnapshot.child("rating").getValue(Float.class);
            if (rating != null) {
                totalRating += rating;
                reviewCount++;
            }
        }

        if (reviewCount > 0) {
            float avgRating = totalRating / reviewCount;
            freelancer.setAverageRating(avgRating);
            Log.d(TAG, "Calculated average rating for " + freelancer.getId() + ": " + avgRating + " from " + reviewCount + " reviews");
        } else {
            freelancer.setAverageRating(0); // Default if no reviews
            Log.d(TAG, "No reviews found for " + freelancer.getId());
        }
    }

    private void filterAndSortFreelancers(String query) {
        if (query == null || query.isEmpty()) {
            return;
        }

        String queryLower = query.toLowerCase().trim();
        List<FreelancerSearchResult> searchResults = new ArrayList<>();

        for (Freelancer freelancer : allFreelancers) {
            int relevanceScore = 0;
            boolean nameMatch = false;
            boolean skillMatch = false;

            // Check name match (highest priority)
            if (freelancer.getName() != null) {
                String nameLower = freelancer.getName().toLowerCase();
                if (nameLower.equals(queryLower)) {
                    // Exact name match (highest score)
                    relevanceScore += 1000;
                    nameMatch = true;
                } else if (nameLower.startsWith(queryLower)) {
                    // Name starts with query
                    relevanceScore += 500;
                    nameMatch = true;
                } else if (nameLower.contains(queryLower)) {
                    // Name contains query
                    relevanceScore += 200;
                    nameMatch = true;
                }
            }

            // Check skill match (secondary priority)
            if (freelancer.getSkills() != null) {
                for (String skill : freelancer.getSkills()) {
                    if (skill != null) {
                        String skillLower = skill.toLowerCase();
                        if (skillLower.equals(queryLower)) {
                            // Exact skill match
                            relevanceScore += 100;
                            skillMatch = true;
                        } else if (skillLower.startsWith(queryLower)) {
                            // Skill starts with query
                            relevanceScore += 50;
                            skillMatch = true;
                        } else if (skillLower.contains(queryLower)) {
                            // Skill contains query
                            relevanceScore += 20;
                            skillMatch = true;
                        }
                    }
                }
            }

            // Add to results if there was any match
            if (nameMatch || skillMatch) {
                searchResults.add(new FreelancerSearchResult(freelancer, relevanceScore));
            }
        }

        // Sort by relevance score (highest first)
        Collections.sort(searchResults, (a, b) -> Integer.compare(b.relevanceScore, a.relevanceScore));

        // Update the adapter with sorted results
        List<Freelancer> sortedFreelancers = new ArrayList<>();
        for (FreelancerSearchResult result : searchResults) {
            sortedFreelancers.add(result.freelancer);
        }

        allFreelancers.clear();
        allFreelancers.addAll(sortedFreelancers);
        adapter.notifyDataSetChanged();
    }

    // Helper class to store freelancer with relevance score
    private static class FreelancerSearchResult {
        Freelancer freelancer;
        int relevanceScore;

        FreelancerSearchResult(Freelancer freelancer, int relevanceScore) {
            this.freelancer = freelancer;
            this.relevanceScore = relevanceScore;
        }
    }
}


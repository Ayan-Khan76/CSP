package com.example.csp.freelancerapp.Fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.example.csp.HelpActivity;
import com.example.csp.LoginActivity;
import com.example.csp.OrderHistoryActivity;
import com.example.csp.R;
import com.example.csp.Reviews.ReviewsActivity;
import com.example.csp.freelancerapp.form.FreelancerFormActivity2;
import com.example.csp.freelancerapp.model.MediaItem;
import com.example.csp.freelancerapp.model.PortfolioItem;
import com.google.android.gms.common.util.IOUtils;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class FreelancerProfileFragment extends Fragment {
    private CircleImageView profileImage;
    private TextView changeProfilePicText;
    private TextView nameText;
    private TextView emailText;
    private TextView settingsButton;
    private TextView orderHistoryButton;
    private static final int PICK_IMAGE_REQUEST = 1;
    private TextView portfolioButton;
    private TextView logoutButton;
    private MaterialButton updateProfileButton;
    private List<PortfolioItem> portfolioList;
    private DatabaseReference userRef;
    private String userId;
    private static final String TAG = "FreelancerProfile";
    private TextView reviewsButton;
    private static final int PORTFOLIO_REQUEST_CODE = 100;
    private List<String> availableSkills = Arrays.asList(
            "Java", "Android", "Animation", "Web Development",
            "UI/UX Design", "Graphic Design", "Video Editing",
            "React", "Node.js"
    );
    private Uri imageUri;
    private List<String> selectedSkills = new ArrayList<>();



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_freelancer_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize views
        initViews(view);

        // Initialize Firebase references
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        userRef = FirebaseDatabase.getInstance().getReference("freelancers").child(userId);

        // Load portfolio data from Firebase
        loadPortfolioData();

        // Set click listeners
        setClickListeners();

        // Load user profile data
        loadUserProfileData();
    }

    private void initViews(View view) {
        profileImage = view.findViewById(R.id.profileImage);
        changeProfilePicText = view.findViewById(R.id.changeProfilePicText);
        nameText = view.findViewById(R.id.nameText);
        emailText = view.findViewById(R.id.emailText);
        settingsButton = view.findViewById(R.id.settingsButton);
        orderHistoryButton = view.findViewById(R.id.orderHistoryButton);
        portfolioButton = view.findViewById(R.id.portfolioButton);
        logoutButton = view.findViewById(R.id.logoutButton);
        updateProfileButton = view.findViewById(R.id.updateProfileButton);
        reviewsButton = view.findViewById(R.id.reviewsButton);
        portfolioList = new ArrayList<>();
    }

    private void loadUserProfileData() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

        // Reference to users node
        DatabaseReference userRef = databaseReference.child("users").child(userId);

        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String name = dataSnapshot.child("name").getValue(String.class);
                    String email = dataSnapshot.child("email").getValue(String.class);

                    nameText.setText(name != null ? name : "Name not set");
                    emailText.setText(email != null ? email : "Email not set");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                showToast("Failed to load user data");
            }
        });

        // Reference to freelancers node to get profileImageUrl
        DatabaseReference freelancerRef = databaseReference.child("freelancers").child(userId);

        freelancerRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot freelancerSnapshot) {
                if (freelancerSnapshot.exists()) {
                    String profileImageUrl = freelancerSnapshot.child("profileImageUrl").getValue(String.class);

                    if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                        Glide.with(FreelancerProfileFragment.this)
                                .load(profileImageUrl)
                                .placeholder(R.drawable.baseline_person_24_white)
                                .into(profileImage);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                showToast("Failed to load profile image");
            }
        });
    }

    private void setClickListeners() {
        portfolioButton.setOnClickListener(v -> {
            // Navigate to FreelancerFormActivity2 with existing portfolio items
            Intent intent = new Intent(getActivity(), FreelancerFormActivity2.class);
            intent.putParcelableArrayListExtra("portfolioList", new ArrayList<>(portfolioList));
            intent.putExtra("isFromProfile", true); // Flag to indicate we're coming from profile
            startActivityForResult(intent, PORTFOLIO_REQUEST_CODE);
        });

        settingsButton.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), HelpActivity.class);
            startActivity(intent);
        });

        changeProfilePicText.setOnClickListener(v -> {
            openGallery();
        });

        logoutButton.setOnClickListener(v -> showLogoutDialog());

        updateProfileButton.setOnClickListener(v -> {
            showUpdateProfileDialog();
        });

        reviewsButton.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), ReviewsActivity.class);
            startActivity(intent);
        });

        orderHistoryButton.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), OrderHistoryActivity.class);
            startActivity(intent);
        });

    }

    private void loadPortfolioData() {
        userRef.child("portfolio").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                portfolioList.clear();

                if (snapshot.exists()) {
                    for (DataSnapshot itemSnapshot : snapshot.getChildren()) {
                        try {
                            String title = itemSnapshot.child("title").getValue(String.class);
                            String description = itemSnapshot.child("description").getValue(String.class);
                            String link = itemSnapshot.child("link").getValue(String.class);

                            if (title == null) continue;

                            List<MediaItem> mediaItems = new ArrayList<>();
                            DataSnapshot mediaItemsSnapshot = itemSnapshot.child("mediaItems");
                            if (mediaItemsSnapshot.exists()) {
                                for (DataSnapshot mediaSnapshot : mediaItemsSnapshot.getChildren()) {
                                    String uriString = mediaSnapshot.child("uri").getValue(String.class);
                                    Boolean isVideo = mediaSnapshot.child("isVideo").getValue(Boolean.class);

                                    if (uriString != null) {
                                        MediaItem mediaItem = new MediaItem(Uri.parse(uriString), isVideo != null && isVideo);
                                        mediaItems.add(mediaItem);
                                    }
                                }
                            }

                            PortfolioItem portfolioItem = new PortfolioItem(title, mediaItems, description, link);
                            portfolioList.add(portfolioItem);

                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing portfolio item: " + e.getMessage());
                        }
                    }

                    Log.d(TAG, "Loaded " + portfolioList.size() + " portfolio items");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Database error: " + error.getMessage());
            }
        });
    }

    private void showToast(String message) {
        if (isAdded()) {

        }
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();

            // Display the selected image using Glide
            Glide.with(this)
                    .load(imageUri)
                    .placeholder(R.drawable.ic_profile_client)
                    .into(profileImage);

            // Upload to Cloudinary and update Firebase
            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            DatabaseReference freelancerRef = FirebaseDatabase.getInstance()
                    .getReference("freelancers")
                    .child(userId);

            uploadImageToCloudinary(imageUri, freelancerRef);
        }
    }

    private void showLogoutDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Logout")
                .setMessage("Are you sure you want to log out?")
                .setPositiveButton("Yes", (dialog, which) -> logoutUser())
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void logoutUser() {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }

    private void showUpdateProfileDialog() {
        final Dialog dialog = new Dialog(getContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_update_profile);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        // Set dialog width to 90% of screen width
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.copyFrom(dialog.getWindow().getAttributes());
        layoutParams.width = (int)(getResources().getDisplayMetrics().widthPixels * 0.9);
        dialog.getWindow().setAttributes(layoutParams);

        // Initialize all views
        EditText editName = dialog.findViewById(R.id.editName);
        EditText editPhone = dialog.findViewById(R.id.editPhone);
        EditText editState = dialog.findViewById(R.id.editState);
        EditText editCity = dialog.findViewById(R.id.editCity);
        EditText editPincode = dialog.findViewById(R.id.editPincode);
        EditText editLandmark = dialog.findViewById(R.id.editLandmark);

        // Freelancer specific fields
        View freelancerFields = dialog.findViewById(R.id.freelancerFields);
        EditText editAbout = dialog.findViewById(R.id.editAbout);
        EditText editHourlyRate = dialog.findViewById(R.id.editHourlyRate);
        EditText editProjectPrice = dialog.findViewById(R.id.editProjectPrice);
        EditText editExperience = dialog.findViewById(R.id.editExperience);
        ChipGroup chipGroupSkills = dialog.findViewById(R.id.chipGroupSkills);
        Button btnAddSkill = dialog.findViewById(R.id.btnAddSkill);

        Button btnSave = dialog.findViewById(R.id.btnSave);
        Button btnCancel = dialog.findViewById(R.id.btnCancel);

        // Show freelancer fields
        freelancerFields.setVisibility(View.VISIBLE);

        // Load current user data
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference freelancerRef = FirebaseDatabase.getInstance().getReference()
                .child("freelancers").child(userId);

        // Load freelancer data
        freelancerRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    editPhone.setText(snapshot.child("phone").getValue(String.class));
                    editState.setText(snapshot.child("state").getValue(String.class));
                    editCity.setText(snapshot.child("city").getValue(String.class));
                    editPincode.setText(snapshot.child("pincode").getValue(String.class));
                    editLandmark.setText(snapshot.child("landmark").getValue(String.class));

                    editAbout.setText(snapshot.child("about").getValue(String.class));

                    Long hourlyRate = snapshot.child("hourlyRate").getValue(Long.class);
                    editHourlyRate.setText(hourlyRate != null ? String.valueOf(hourlyRate) : "");

                    Long projectPrice = snapshot.child("projectBasedPricing").getValue(Long.class);
                    editProjectPrice.setText(projectPrice != null ? String.valueOf(projectPrice) : "");

                    Long experience = snapshot.child("experience").getValue(Long.class);
                    editExperience.setText(experience != null ? String.valueOf(experience) : "");

                    // Load skills
                    selectedSkills.clear();
                    DataSnapshot skillsSnapshot = snapshot.child("skills");
                    if (skillsSnapshot.exists()) {
                        for (DataSnapshot skillSnapshot : skillsSnapshot.getChildren()) {
                            String skill = skillSnapshot.getValue(String.class);
                            if (skill != null) {
                                selectedSkills.add(skill);
                                addChip(skill, chipGroupSkills);
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                showToast("Failed to load freelancer data");
            }
        });
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(userId);
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot userSnapshot) {
                if (userSnapshot.exists()) {
                    editName.setText(userSnapshot.child("name").getValue(String.class));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                showToast("Failed to load user name");
            }
        });



        // Set up Add Skill button
        btnAddSkill.setOnClickListener(v -> {
            showSkillSelectionDialog(chipGroupSkills);
        });

        // Set click listeners for buttons
        btnSave.setOnClickListener(v -> {
            saveFreelancerData(
                    editName.getText().toString(),
                    editPhone.getText().toString(),
                    editState.getText().toString(),
                    editCity.getText().toString(),
                    editPincode.getText().toString(),
                    editLandmark.getText().toString(),
                    editAbout.getText().toString(),
                    editHourlyRate.getText().toString(),
                    editProjectPrice.getText().toString(),
                    editExperience.getText().toString(),
                    selectedSkills
            );
            dialog.dismiss();
            showToast("Profile updated successfully");
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void addChip(String skill, ChipGroup chipGroup) {
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        Chip chip = (Chip) inflater.inflate(R.layout.item_skill, chipGroup, false);
        chip.setText(skill);
        chip.setCloseIconVisible(true);
        chip.setOnCloseIconClickListener(v -> {
            chipGroup.removeView(chip);
            selectedSkills.remove(skill);
        });
        chipGroup.addView(chip);
    }

    private void showSkillSelectionDialog(ChipGroup chipGroup) {
        List<String> availableSkillsList = new ArrayList<>(availableSkills);
        availableSkillsList.removeAll(selectedSkills);

        if (availableSkillsList.isEmpty()) {
            showToast("No more skills available to add");
            return;
        }

        String[] skillsArray = availableSkillsList.toArray(new String[0]);

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Select a Skill")
                .setItems(skillsArray, (dialog, which) -> {
                    String selectedSkill = skillsArray[which];
                    if (!selectedSkills.contains(selectedSkill)) {
                        selectedSkills.add(selectedSkill);
                        addChip(selectedSkill, chipGroup);
                    }
                });
        builder.show();
    }

    private void saveFreelancerData(String name, String phone, String state, String city,
                                    String pincode, String landmark, String about,
                                    String hourlyRate, String projectPrice,
                                    String experience, List<String> skills) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference freelancerRef = FirebaseDatabase.getInstance().getReference()
                .child("freelancers").child(userId);
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference()
                .child("users").child(userId);

        Map<String, Object> updates = new HashMap<>();
        updates.put("name", name);
        updates.put("phone", phone);
        updates.put("state", state);
        updates.put("city", city);
        updates.put("pincode", pincode);
        updates.put("landmark", landmark);
        updates.put("about", about);
        updates.put("hourlyRate", !hourlyRate.isEmpty() ? Long.parseLong(hourlyRate) : 0);
        updates.put("projectBasedPricing", !projectPrice.isEmpty() ? Long.parseLong(projectPrice) : 0);
        updates.put("experience", !experience.isEmpty() ? Long.parseLong(experience) : 0);
        updates.put("skills", skills);

        // Update freelancer profile
        freelancerRef.updateChildren(updates)
                .addOnSuccessListener(aVoid -> {
                    // Also update the user node name
                    userRef.child("name").setValue(name)
                            .addOnSuccessListener(unused -> {
                                showToast("Profile updated successfully");
                                loadUserProfileData(); // Refresh displayed data
                            })
                            .addOnFailureListener(e -> showToast("Failed to update user name"));
                })
                .addOnFailureListener(e -> showToast("Failed to update profile"));
    }

    private void uploadImageToCloudinary(Uri imageUri, DatabaseReference freelancerRef) {
        try {
            InputStream inputStream = getActivity().getContentResolver().openInputStream(imageUri);
            byte[] bytes = IOUtils.toByteArray(inputStream);

            // Upload to Cloudinary using MediaManager
            MediaManager.get().upload(imageUri)
                    .option("resource_type", "image")
                    .callback(new UploadCallback() {
                        @Override
                        public void onStart(String requestId) {

                        }

                        @Override
                        public void onProgress(String requestId, long bytes, long totalBytes) {

                        }

                        @Override
                        public void onSuccess(String requestId, Map resultData) {
                            String imageUrl = (String) resultData.get("secure_url");

                            // Update Firebase with new profile image URL
                            freelancerRef.child("profileImageUrl").setValue(imageUrl)
                                    .addOnSuccessListener(aVoid -> {
                                        if (isAdded()) { //to avoid crash
                                            requireActivity().runOnUiThread(() ->
                                                    Toast.makeText(getContext(), "Profile updated", Toast.LENGTH_SHORT).show());
                                        }
                                    })
                                    .addOnFailureListener(e -> {
                                        if (isAdded()) { //to avoid crash
                                            requireActivity().runOnUiThread(() ->
                                                    Toast.makeText(getContext(), "Failed to update profile", Toast.LENGTH_SHORT).show());
                                        }
                                    });
                        }

                        @Override
                        public void onError(String requestId, ErrorInfo error) {
                            requireActivity().runOnUiThread(() ->
                                    Toast.makeText(getContext(), "Upload failed: " + error.getDescription(), Toast.LENGTH_SHORT).show());
                        }

                        @Override
                        public void onReschedule(String requestId, ErrorInfo error) {
                            requireActivity().runOnUiThread(() ->
                                    Toast.makeText(getContext(), "Upload rescheduled", Toast.LENGTH_SHORT).show());
                        }
                    })
                    .dispatch();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

}
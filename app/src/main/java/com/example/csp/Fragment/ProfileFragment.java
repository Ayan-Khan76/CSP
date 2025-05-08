package com.example.csp.Fragment;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
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
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.cloudinary.utils.ObjectUtils;
import com.example.csp.HelpActivity;
import com.example.csp.LoginActivity;
import com.example.csp.OrderHistoryActivity;
import com.example.csp.R;
import com.google.android.gms.common.util.IOUtils;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileFragment extends Fragment {
    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int STORAGE_PERMISSION_CODE = 101;

    private CircleImageView profileImage;
    private TextView changeProfilePicText;
    private TextView nameText;
    private TextView emailText;
    private TextView settingsButton;
    private TextView orderHistoryButton;
    private TextView logoutButton;
    private MaterialButton updateProfileButton;
    private DatabaseReference databaseReference;
    private Uri imageUri;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    private FirebaseAuth mAuth;
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize views
        initViews(view);

        // Load user data from Firebase
        loadUserData();

        // Set click listeners
        setClickListeners();
    }

    private void initViews(View view) {
        profileImage = view.findViewById(R.id.profileImage);
        changeProfilePicText = view.findViewById(R.id.changeProfilePicText);
        nameText = view.findViewById(R.id.nameText);
        emailText = view.findViewById(R.id.emailText);
        settingsButton = view.findViewById(R.id.settingsButton);
        orderHistoryButton = view.findViewById(R.id.orderHistoryButton);
        logoutButton = view.findViewById(R.id.logoutButton);
        updateProfileButton = view.findViewById(R.id.updateProfileButton);
        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();
    }

    private void setClickListeners() {
        changeProfilePicText.setOnClickListener(v -> {
            openGallery();
        });

        settingsButton.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), HelpActivity.class);
            startActivity(intent);
        });

        orderHistoryButton.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), OrderHistoryActivity.class);
            startActivity(intent);
        });

        logoutButton.setOnClickListener(v -> showLogoutDialog());

        updateProfileButton.setOnClickListener(v -> showUpdateProfileDialog());
    }

    private boolean checkStoragePermission() {
        return ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestStoragePermission() {
        ActivityCompat.requestPermissions(requireActivity(),
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                STORAGE_PERMISSION_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openGallery();
            } else {
                showToast("Storage permission denied");
            }
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
            DatabaseReference clientRef = FirebaseDatabase.getInstance()
                    .getReference("clients")
                    .child(userId);

            uploadImageToCloudinary(imageUri, clientRef);
        }
    }

    private void showToast(String message) {
        if (isAdded()) {

        }
    }

    private void showMessage(String message) {
        showToast(message);
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
        mAuth.signOut();
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }

    private void loadUserData() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference userRef = databaseReference.child("users").child(userId);
        DatabaseReference clientRef = databaseReference.child("clients").child(userId);

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

        // Fetch profile image from clients node
        clientRef.child("profileImageUrl").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String profileImageUrl = dataSnapshot.getValue(String.class);
                if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                    Glide.with(ProfileFragment.this)
                            .load(profileImageUrl)
                            .placeholder(R.drawable.baseline_person_24_white)
                            .into(profileImage);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                showToast("Failed to load profile image");
            }
        });
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

        // Initialize dialog views
        EditText editName = dialog.findViewById(R.id.editName);
        EditText editPhone = dialog.findViewById(R.id.editPhone);
        EditText editState = dialog.findViewById(R.id.editState);
        EditText editCity = dialog.findViewById(R.id.editCity);
        EditText editPincode = dialog.findViewById(R.id.editPincode);
        EditText editLandmark = dialog.findViewById(R.id.editLandmark);
        Button btnSave = dialog.findViewById(R.id.btnSave);
        Button btnCancel = dialog.findViewById(R.id.btnCancel);

        // Load current user data into the dialog
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference clientRef = databaseReference.child("clients").child(userId);

        clientRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String phone = dataSnapshot.child("phone").getValue(String.class);
                    String state = dataSnapshot.child("state").getValue(String.class);
                    String city = dataSnapshot.child("city").getValue(String.class);
                    String pincode = dataSnapshot.child("pincode").getValue(String.class);
                    String landmark = dataSnapshot.child("landmark").getValue(String.class);

                    // Get name from users node
                    DatabaseReference userRef = databaseReference.child("users").child(userId);
                    userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                String name = snapshot.child("name").getValue(String.class);
                                editName.setText(name != null ? name : "");
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            showToast("Failed to load user data");
                        }
                    });

                    editPhone.setText(phone != null ? phone : "");
                    editState.setText(state != null ? state : "");
                    editCity.setText(city != null ? city : "");
                    editPincode.setText(pincode != null ? pincode : "");
                    editLandmark.setText(landmark != null ? landmark : "");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                showToast("Failed to load client data");
            }
        });

        // Set click listeners for buttons
        btnSave.setOnClickListener(v -> {
            // Save updated profile data
            saveClientData(
                    editName.getText().toString(),
                    editPhone.getText().toString(),
                    editState.getText().toString(),
                    editCity.getText().toString(),
                    editPincode.getText().toString(),
                    editLandmark.getText().toString()
            );
            dialog.dismiss();
            showToast("Profile updated successfully");
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void saveClientData(String name, String phone, String state, String city, String pincode, String landmark) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Update name in users node
        DatabaseReference userRef = databaseReference.child("users").child(userId);
        userRef.child("name").setValue(name);

        // Update other details in clients node
        DatabaseReference clientRef = databaseReference.child("clients").child(userId);
        clientRef.child("phone").setValue(phone);
        clientRef.child("state").setValue(state);
        clientRef.child("city").setValue(city);
        clientRef.child("pincode").setValue(pincode);
        clientRef.child("landmark").setValue(landmark);

        if (imageUri != null) {
            uploadImageToCloudinary(imageUri, userRef);
        }

        // Update the displayed name after saving
        nameText.setText(name);
    }

    private void uploadImageToCloudinary(Uri imageUri, DatabaseReference clientRef) {
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
                            clientRef.child("profileImageUrl").setValue(imageUrl)
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


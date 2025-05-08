package com.example.csp.freelancerapp.form;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.example.csp.LoginActivity;
import com.example.csp.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class FreelancerFormActivity1 extends AppCompatActivity {

    private static final int PICK_IMAGE = 100;
    private CircleImageView profileImageView;
    private TextInputEditText editNumber, editTextAbout, editTextState, editTextCity, editTextPincode, editTextLandmark;
    private Uri imageUri;
    private String userRole;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_freelancer_form1);

        userRole = getIntent().getStringExtra("role");

        initializeViews();
        setupClickListeners();
        initCloudinary();
    }

    private void initializeViews() {
        profileImageView = findViewById(R.id.profileImageView);
        editNumber = findViewById(R.id.editNumber);
        editTextAbout = findViewById(R.id.editTextAbout);
        editTextState = findViewById(R.id.editTextState);
        editTextCity = findViewById(R.id.editTextCity);
        editTextPincode = findViewById(R.id.editTextPincode);
        editTextLandmark = findViewById(R.id.editTextLandmark);
        databaseReference = FirebaseDatabase.getInstance().getReference();
    }

    private void setupClickListeners() {
        FloatingActionButton fabUploadProfile = findViewById(R.id.fabUploadProfile);
        Button buttonNext = findViewById(R.id.buttonNext1);

        fabUploadProfile.setOnClickListener(v -> openGallery());

        if ("Client".equals(userRole)) {
            buttonNext.setText("Submit");
            editTextAbout.setVisibility(View.GONE);
            buttonNext.setOnClickListener(v -> {
                if (validateClientFields()) {
                    saveClientData();
                    Intent intent = new Intent(FreelancerFormActivity1.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                }
            });
        } else {
            buttonNext.setOnClickListener(v -> {
                if (validateFields()) {
                    saveFreelancerData();
                    proceedToNextActivity();
                }
            });
        }
    }

    private void initCloudinary() {
        Map<String, String> config = new HashMap<>();
        config.put("cloud_name", "drxajyngt");
        config.put("api_key", "278383696121239");
        config.put("api_secret", "kx_cOM8GXjcawAm--rbyZAATkU8");
    }

    private void openGallery() {
        Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        startActivityForResult(gallery, PICK_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == PICK_IMAGE) {
            imageUri = data.getData();
            profileImageView.setImageURI(imageUri);
        } else {
            Toast.makeText(this, "Image selection canceled", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveClientData() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference clientRef = databaseReference.child("clients").child(userId);

        clientRef.child("phone").setValue(editNumber.getText().toString());
        clientRef.child("state").setValue(editTextState.getText().toString());
        clientRef.child("city").setValue(editTextCity.getText().toString());
        clientRef.child("pincode").setValue(editTextPincode.getText().toString());
        clientRef.child("landmark").setValue(editTextLandmark.getText().toString());

        if (imageUri != null) {
            uploadImageToCloudinary(imageUri, clientRef);
        }
    }

    private void saveFreelancerData() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference freelancerRef = databaseReference.child("freelancers").child(userId);

        freelancerRef.child("phone").setValue(editNumber.getText().toString());
        freelancerRef.child("about").setValue(editTextAbout.getText().toString());
        freelancerRef.child("state").setValue(editTextState.getText().toString());
        freelancerRef.child("city").setValue(editTextCity.getText().toString());
        freelancerRef.child("pincode").setValue(editTextPincode.getText().toString());
        freelancerRef.child("landmark").setValue(editTextLandmark.getText().toString());

        if (imageUri != null) {
            uploadImageToCloudinary(imageUri, freelancerRef);
        }
    }




    private void uploadImageToCloudinary(Uri imageUri, final DatabaseReference userRef) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        Map<String, Object> options = new HashMap<>();
        options.put("public_id", "profile_photos/" + userId);
        options.put("overwrite", true);

        MediaManager.get().upload(imageUri).options(options)
                .callback(new UploadCallback() {
                    @Override
                    public void onStart(String requestId) {
                        showMessage("Uploading image...");
                    }

                    @Override
                    public void onProgress(String requestId, long bytes, long totalBytes) {
                        // Update progress if needed
                    }

                    @Override
                    public void onSuccess(String requestId, Map resultData) {
                        String imageUrl = (String) resultData.get("secure_url");
                        userRef.child("profileImageUrl").setValue(imageUrl);
                        showMessage("Image uploaded successfully");
                    }

                    @Override
                    public void onError(String requestId, ErrorInfo error) {
                        showMessage("Error uploading image: " + error.getDescription());
                        Log.e("CloudinaryUpload", "Error: " + error.getDescription());
                    }

                    @Override
                    public void onReschedule(String requestId, ErrorInfo error) {
                        // Handle rescheduling if needed
                    }
                })
                .dispatch();
    }

    private void showMessage(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!isFinishing()) {
                    Toast.makeText(FreelancerFormActivity1.this, message, Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private boolean validateFields() {
        if (TextUtils.isEmpty(editNumber.getText())) {
            editNumber.setError("Number is required");
            editNumber.requestFocus();
            return false;
        }

        if (editNumber.getText().length() != 10) {
            editNumber.setError("Please enter a valid 10-digit phone number");
            editNumber.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(editTextAbout.getText())) {
            editTextAbout.setError("About is required");
            editTextAbout.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(editTextState.getText())) {
            editTextState.setError("State is required");
            editTextState.requestFocus();
            return false;
        }

        if (!editTextState.getText().toString().matches("^[a-zA-Z ]+$")) {
            editTextState.setError("State must contain only letters");
            editTextState.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(editTextCity.getText())) {
            editTextCity.setError("City is required");
            editTextCity.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(editTextPincode.getText())) {
            editTextPincode.setError("Pin code is required");
            editTextPincode.requestFocus();
            return false;
        }

        if (editTextPincode.getText().length() != 6) {
            editTextPincode.setError("Please enter a valid 6-digit pin code");
            editTextPincode.requestFocus();
            return false;
        }

        return true;
    }

    private boolean validateClientFields() {
        if (TextUtils.isEmpty(editNumber.getText())) {
            editNumber.setError("Number is required");
            editNumber.requestFocus();
            return false;
        }

        if (editNumber.getText().length() != 10) {
            editNumber.setError("Please enter a valid 10-digit phone number");
            editNumber.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(editTextState.getText())) {
            editTextState.setError("State is required");
            editTextState.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(editTextCity.getText())) {
            editTextCity.setError("City is required");
            editTextCity.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(editTextPincode.getText())) {
            editTextPincode.setError("Pincode is required");
            editTextPincode.requestFocus();
            return false;
        }

        return true;
    }

    private void proceedToNextActivity() {
        Intent intent = new Intent(FreelancerFormActivity1.this, FreelancerFormActivity2.class);

        intent.putExtra("number", editNumber.getText().toString());
        intent.putExtra("about", editTextAbout.getText().toString());
        intent.putExtra("state", editTextState.getText().toString());
        intent.putExtra("city", editTextCity.getText().toString());
        intent.putExtra("pincode", editTextPincode.getText().toString());
        intent.putExtra("landmark", editTextLandmark.getText().toString());

        if (imageUri != null) {
            intent.putExtra("profileImage", imageUri.toString());
        }

        startActivity(intent);
    }

}


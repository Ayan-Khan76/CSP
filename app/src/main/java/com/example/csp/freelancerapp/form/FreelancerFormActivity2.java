package com.example.csp.freelancerapp.form;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.example.csp.R;
import com.example.csp.freelancerapp.adapter.MediaAdapter;
import com.example.csp.freelancerapp.adapter.PortfolioAdapter;
import com.example.csp.freelancerapp.model.MediaItem;
import com.example.csp.freelancerapp.model.PortfolioItem;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
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
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class FreelancerFormActivity2 extends AppCompatActivity {

    private static final String TAG = "FreelancerFormActivity2";
    private RecyclerView recyclerViewPortfolio;
    private List<PortfolioItem> portfolioItems;
    private PortfolioAdapter portfolioAdapter;
    private List<MediaItem> tempMediaItems;
    private MediaAdapter tempMediaAdapter;
    private ActivityResultLauncher<Intent> mediaPickerLauncher;
    private DatabaseReference freelancerRef;
    private boolean isFromProfile = false;
    private Button buttonSubmit;
    private Button buttonBack;
    private Button buttonNext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_freelancer_form2);

        setupMediaPicker();
        initializeFirebase();

        recyclerViewPortfolio = findViewById(R.id.recyclerViewPortfolio);
        Button buttonAddPortfolio = findViewById(R.id.buttonAddPortfolio);
        buttonBack = findViewById(R.id.buttonBack2);
        buttonNext = findViewById(R.id.buttonNext2);
        buttonSubmit = findViewById(R.id.buttonSubmit);

        // Check if we're coming from profile fragment
        isFromProfile = getIntent().getBooleanExtra("isFromProfile", false);

        // Set up UI based on where we're coming from
        if (isFromProfile) {
            buttonBack.setVisibility(View.GONE);
            buttonNext.setVisibility(View.GONE);
            buttonSubmit.setVisibility(View.VISIBLE);
        } else {
            buttonBack.setVisibility(View.VISIBLE);
            buttonNext.setVisibility(View.VISIBLE);
            buttonSubmit.setVisibility(View.GONE);
        }

        portfolioItems = new ArrayList<>();
        portfolioAdapter = new PortfolioAdapter(portfolioItems);
        recyclerViewPortfolio.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewPortfolio.setAdapter(portfolioAdapter);

        // Set up delete listener for portfolio items
        portfolioAdapter.setOnPortfolioDeleteListener(position -> {
            // The adapter already removes the item from the list
            // We just need to update the UI
        });

        // Load portfolio items from Firebase
        loadPortfolioItems();

        buttonAddPortfolio.setOnClickListener(v -> showAddPortfolioDialog());
        buttonBack.setOnClickListener(v -> finish());
        buttonNext.setOnClickListener(v -> savePortfolioAndNavigate());
        buttonSubmit.setOnClickListener(v -> {
            savePortfolioAndFinish();
        });
    }

    private void loadPortfolioItems() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference portfolioRef = FirebaseDatabase.getInstance().getReference()
                .child("freelancers").child(userId).child("portfolio");
        Toast.makeText(this, "Loading portfolio items...", Toast.LENGTH_SHORT).show();

        portfolioRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                portfolioItems.clear();

                if (dataSnapshot.exists()) {
                    for (DataSnapshot itemSnapshot : dataSnapshot.getChildren()) {
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
                                        Log.d(TAG, "Added media item: " + uriString + " isVideo: " + isVideo);
                                    }
                                }
                            }

                            PortfolioItem portfolioItem = new PortfolioItem(title, mediaItems, description, link);
                            portfolioItems.add(portfolioItem);
                            Log.d(TAG, "Added portfolio item: " + title + " with " + mediaItems.size() + " media items");

                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing portfolio item: " + e.getMessage(), e);
                        }
                    }

                    // Update the adapter with the loaded items
                    portfolioAdapter = new PortfolioAdapter(portfolioItems);
                    recyclerViewPortfolio.setAdapter(portfolioAdapter);

                    // Set up delete listener for portfolio items
                    portfolioAdapter.setOnPortfolioDeleteListener(position -> {
                        // The adapter already removes the item from the list
                        // We just need to update the UI
                    });

                    Log.d(TAG, "Loaded " + portfolioItems.size() + " portfolio items");

                    // Notify user that loading is complete
                    if (portfolioItems.size() > 0) {
                        Toast.makeText(FreelancerFormActivity2.this,
                                "Loaded " + portfolioItems.size() + " portfolio items",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(FreelancerFormActivity2.this,
                                "No portfolio items found",
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.d(TAG, "No portfolio items found");
                    Toast.makeText(FreelancerFormActivity2.this,
                            "No portfolio items found",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "Error loading portfolio items: " + databaseError.getMessage());
                Toast.makeText(FreelancerFormActivity2.this, "Failed to load portfolio items", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initializeFirebase() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        freelancerRef = databaseReference.child("freelancers").child(userId);
    }

    private void setupMediaPicker() {
        mediaPickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri selectedMedia = result.getData().getData();
                        String mimeType = getContentResolver().getType(selectedMedia);
                        boolean isVideo = mimeType != null && mimeType.startsWith("video/");

                        // Log the selected media type
                        Log.d("MediaPicker", "Selected media type: " + mimeType);

                        tempMediaItems.add(new MediaItem(selectedMedia, isVideo));
                        tempMediaAdapter.notifyItemInserted(tempMediaItems.size() - 1);
                    }
                }
        );
    }

    private void showAddPortfolioDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.DialogTheme);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_portfolio, null);
        builder.setView(view);

        final TextInputEditText editTextTitle = view.findViewById(R.id.editTextPortfolioTitle);
        final TextInputEditText editTextDescription = view.findViewById(R.id.editTextPortfolioDescription);
        final TextInputEditText editTextLink = view.findViewById(R.id.editTextPortfolioLink);
        final RecyclerView recyclerViewMedia = view.findViewById(R.id.recyclerViewMedia);
        final FloatingActionButton fabAddMedia = view.findViewById(R.id.fabAddMedia);
        Button buttonAdd = view.findViewById(R.id.buttonAddPortfolioItem);

        tempMediaItems = new ArrayList<>();
        tempMediaAdapter = new MediaAdapter(tempMediaItems);
        recyclerViewMedia.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recyclerViewMedia.setAdapter(tempMediaAdapter);

        fabAddMedia.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("*/*");
            String[] mimeTypes = {"image/*", "video/*"};
            intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
            mediaPickerLauncher.launch(intent);
        });

        final AlertDialog dialog = builder.create();

        buttonAdd.setOnClickListener(v -> {
            String title = editTextTitle.getText().toString();
            String description = editTextDescription.getText().toString();
            String link = editTextLink.getText().toString();

            if (!title.isEmpty() && !description.isEmpty()) {
                PortfolioItem item = new PortfolioItem(title, new ArrayList<>(tempMediaItems), description, link);
                portfolioItems.add(item);
                portfolioAdapter.notifyItemInserted(portfolioItems.size() - 1);
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void savePortfolioAndNavigate() {
        if (portfolioItems.isEmpty()) {
            // If there are no portfolio items, just navigate to the next activity
            navigateToNextActivity();
            return;
        }

        // Show loading indicator
        Toast.makeText(this, "Uploading portfolio items...", Toast.LENGTH_SHORT).show();

        Map<String, Object> portfolioData = new HashMap<>();
        AtomicInteger totalMediaItems = new AtomicInteger(0);
        AtomicInteger uploadedMediaItems = new AtomicInteger(0);

        // Count total media items across all portfolio items
        for (PortfolioItem item : portfolioItems) {
            totalMediaItems.addAndGet(item.getMediaItems().size());
        }

        // If there are no media items, just save the portfolio data
        if (totalMediaItems.get() == 0) {
            for (int i = 0; i < portfolioItems.size(); i++) {
                PortfolioItem item = portfolioItems.get(i);
                Map<String, Object> itemData = new HashMap<>();
                itemData.put("title", item.getTitle());
                itemData.put("description", item.getDescription());
                itemData.put("link", item.getLink());
                itemData.put("mediaItems", new ArrayList<>());
                portfolioData.put("item" + i, itemData);
            }
            updateFreelancerData(portfolioData);
            return;
        }

        // Process each portfolio item
        for (int i = 0; i < portfolioItems.size(); i++) {
            PortfolioItem item = portfolioItems.get(i);
            Map<String, Object> itemData = new HashMap<>();
            itemData.put("title", item.getTitle());
            itemData.put("description", item.getDescription());
            itemData.put("link", item.getLink());

            List<Map<String, Object>> mediaItemsList = new ArrayList<>();
            itemData.put("mediaItems", mediaItemsList);

            portfolioData.put("item" + i, itemData);

            final int itemIndex = i;

            // Process each media item in the portfolio item
            for (int j = 0; j < item.getMediaItems().size(); j++) {
                MediaItem mediaItem = item.getMediaItems().get(j);

                // Check if the media item already has a remote URI (already uploaded)
                if (mediaItem.getUri().toString().startsWith("http")) {
                    // This is already a remote URL, no need to upload again
                    Map<String, Object> mediaData = new HashMap<>();
                    mediaData.put("uri", mediaItem.getUri().toString());
                    mediaData.put("isVideo", mediaItem.isVideo());

                    // Add media data to the media items list
                    List<Map<String, Object>> mediaItemsList2 = (List<Map<String, Object>>) ((Map<String, Object>) portfolioData.get("item" + itemIndex)).get("mediaItems");
                    mediaItemsList2.add(mediaData);

                    // Increment uploaded media items counter
                    int uploaded = uploadedMediaItems.incrementAndGet();

                    // If all media items have been processed, update Firebase
                    if (uploaded == totalMediaItems.get()) {
                        updateFreelancerData(portfolioData);
                    }
                    continue;
                }

                String publicId = "portfolio/" + FirebaseAuth.getInstance().getCurrentUser().getUid() + "/" + UUID.randomUUID().toString();

                final int mediaIndex = j;

                Log.d(TAG, "Uploading " + (mediaItem.isVideo() ? "video" : "image") + " for item " + itemIndex);

                // Upload the media item to Cloudinary
                MediaManager.get().upload(mediaItem.getUri())
                        .option("public_id", publicId)
                        .option("resource_type", mediaItem.isVideo() ? "video" : "image")
                        .callback(new UploadCallback() {
                            @Override
                            public void onStart(String requestId) {
                                Log.d(TAG, "Started uploading " + (mediaItem.isVideo() ? "video" : "image") + " for item " + itemIndex);
                            }

                            @Override
                            public void onSuccess(String requestId, Map resultData) {
                                String mediaUrl = (String) resultData.get("secure_url");
                                Log.d(TAG, "Successfully uploaded " + (mediaItem.isVideo() ? "video" : "image") + " for item " + itemIndex + ": " + mediaUrl);

                                // Create media data map
                                Map<String, Object> mediaData = new HashMap<>();
                                mediaData.put("uri", mediaUrl);
                                mediaData.put("isVideo", mediaItem.isVideo());

                                // Add media data to the media items list
                                List<Map<String, Object>> mediaItemsList = (List<Map<String, Object>>) ((Map<String, Object>) portfolioData.get("item" + itemIndex)).get("mediaItems");
                                mediaItemsList.add(mediaData);

                                // Increment uploaded media items counter
                                int uploaded = uploadedMediaItems.incrementAndGet();

                                // If all media items have been uploaded, update Firebase
                                if (uploaded == totalMediaItems.get()) {
                                    updateFreelancerData(portfolioData);
                                }
                            }

                            @Override
                            public void onError(String requestId, ErrorInfo error) {
                                Log.e(TAG, "Error uploading " + (mediaItem.isVideo() ? "video" : "image") + " for item " + itemIndex + ": " + error.getDescription());
                                Toast.makeText(FreelancerFormActivity2.this, "Error uploading media: " + error.getDescription(), Toast.LENGTH_SHORT).show();

                                // Increment uploaded media items counter even on error
                                int uploaded = uploadedMediaItems.incrementAndGet();

                                // If all media items have been processed, update Firebase
                                if (uploaded == totalMediaItems.get()) {
                                    updateFreelancerData(portfolioData);
                                }
                            }

                            @Override
                            public void onProgress(String requestId, long bytes, long totalBytes) {
                                double progress = (double) bytes / totalBytes * 100;
                                Log.d(TAG, "Upload progress for " + (mediaItem.isVideo() ? "video" : "image") + " for item " + itemIndex + ": " + progress + "%");
                            }

                            @Override
                            public void onReschedule(String requestId, ErrorInfo error) {
                                Log.d(TAG, "Upload rescheduled for " + (mediaItem.isVideo() ? "video" : "image") + " for item " + itemIndex + ": " + error.getDescription());
                            }
                        })
                        .dispatch();
            }
        }
    }

    private void savePortfolioAndFinish() {
        if (portfolioItems.isEmpty()) {
            Toast.makeText(this, "No portfolio items to save", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show loading indicator
        Toast.makeText(this, "Saving portfolio items...", Toast.LENGTH_SHORT).show();

        // Use the same save logic as savePortfolioAndNavigate but finish instead of navigate
        Map<String, Object> portfolioData = new HashMap<>();
        AtomicInteger totalMediaItems = new AtomicInteger(0);
        AtomicInteger uploadedMediaItems = new AtomicInteger(0);

        // Count total media items across all portfolio items
        for (PortfolioItem item : portfolioItems) {
            totalMediaItems.addAndGet(item.getMediaItems().size());
        }

        // If there are no media items, just save the portfolio data
        if (totalMediaItems.get() == 0) {
            for (int i = 0; i < portfolioItems.size(); i++) {
                PortfolioItem item = portfolioItems.get(i);
                Map<String, Object> itemData = new HashMap<>();
                itemData.put("title", item.getTitle());
                itemData.put("description", item.getDescription());
                itemData.put("link", item.getLink());
                itemData.put("mediaItems", new ArrayList<>());
                portfolioData.put("item" + i, itemData);
            }
            updateFreelancerData(portfolioData);
            return;
        }

        // Process each portfolio item
        for (int i = 0; i < portfolioItems.size(); i++) {
            PortfolioItem item = portfolioItems.get(i);
            Map<String, Object> itemData = new HashMap<>();
            itemData.put("title", item.getTitle());
            itemData.put("description", item.getDescription());
            itemData.put("link", item.getLink());

            List<Map<String, Object>> mediaItemsList = new ArrayList<>();
            itemData.put("mediaItems", mediaItemsList);

            portfolioData.put("item" + i, itemData);

            final int itemIndex = i;

            // Process each media item in the portfolio item
            for (int j = 0; j < item.getMediaItems().size(); j++) {
                MediaItem mediaItem = item.getMediaItems().get(j);

                // Check if the media item already has a remote URI (already uploaded)
                if (mediaItem.getUri().toString().startsWith("http")) {
                    // This is already a remote URL, no need to upload again
                    Map<String, Object> mediaData = new HashMap<>();
                    mediaData.put("uri", mediaItem.getUri().toString());
                    mediaData.put("isVideo", mediaItem.isVideo());

                    // Add media data to the media items list
                    List<Map<String, Object>> mediaItemsList2 = (List<Map<String, Object>>) ((Map<String, Object>) portfolioData.get("item" + itemIndex)).get("mediaItems");
                    mediaItemsList2.add(mediaData);

                    // Increment uploaded media items counter
                    int uploaded = uploadedMediaItems.incrementAndGet();

                    // If all media items have been processed, update Firebase
                    if (uploaded == totalMediaItems.get()) {
                        updateFreelancerData(portfolioData);
                    }
                    continue;
                }

                String publicId = "portfolio/" + FirebaseAuth.getInstance().getCurrentUser().getUid() + "/" + UUID.randomUUID().toString();

                final int mediaIndex = j;

                Log.d(TAG, "Uploading " + (mediaItem.isVideo() ? "video" : "image") + " for item " + itemIndex);

                // Upload the media item to Cloudinary
                MediaManager.get().upload(mediaItem.getUri())
                        .option("public_id", publicId)
                        .option("resource_type", mediaItem.isVideo() ? "video" : "image")
                        .callback(new UploadCallback() {
                            @Override
                            public void onStart(String requestId) {
                                Log.d(TAG, "Started uploading " + (mediaItem.isVideo() ? "video" : "image") + " for item " + itemIndex);
                            }

                            @Override
                            public void onSuccess(String requestId, Map resultData) {
                                String mediaUrl = (String) resultData.get("secure_url");
                                Log.d(TAG, "Successfully uploaded " + (mediaItem.isVideo() ? "video" : "image") + " for item " + itemIndex + ": " + mediaUrl);

                                // Create media data map
                                Map<String, Object> mediaData = new HashMap<>();
                                mediaData.put("uri", mediaUrl);
                                mediaData.put("isVideo", mediaItem.isVideo());

                                // Add media data to the media items list
                                List<Map<String, Object>> mediaItemsList = (List<Map<String, Object>>) ((Map<String, Object>) portfolioData.get("item" + itemIndex)).get("mediaItems");
                                mediaItemsList.add(mediaData);

                                // Increment uploaded media items counter
                                int uploaded = uploadedMediaItems.incrementAndGet();

                                // If all media items have been uploaded, update Firebase
                                if (uploaded == totalMediaItems.get()) {
                                    updateFreelancerData(portfolioData);
                                }
                            }

                            @Override
                            public void onError(String requestId, ErrorInfo error) {
                                Log.e(TAG, "Error uploading " + (mediaItem.isVideo() ? "video" : "image") + " for item " + itemIndex + ": " + error.getDescription());
                                Toast.makeText(FreelancerFormActivity2.this, "Error uploading media: " + error.getDescription(), Toast.LENGTH_SHORT).show();

                                // Increment uploaded media items counter even on error
                                int uploaded = uploadedMediaItems.incrementAndGet();

                                // If all media items have been processed, update Firebase
                                if (uploaded == totalMediaItems.get()) {
                                    updateFreelancerData(portfolioData);
                                }
                            }

                            @Override
                            public void onProgress(String requestId, long bytes, long totalBytes) {
                                double progress = (double) bytes / totalBytes * 100;
                                Log.d(TAG, "Upload progress for " + (mediaItem.isVideo() ? "video" : "image") + " for item " + itemIndex + ": " + progress + "%");
                            }

                            @Override
                            public void onReschedule(String requestId, ErrorInfo error) {
                                Log.d(TAG, "Upload rescheduled for " + (mediaItem.isVideo() ? "video" : "image") + " for item " + itemIndex + ": " + error.getDescription());
                            }
                        })
                        .dispatch();
            }
        }
    }

    private void updateFreelancerData(Map<String, Object> portfolioData) {
        // First clear existing portfolio data
        freelancerRef.child("portfolio").removeValue()
                .addOnSuccessListener(aVoid -> {
                    // Then add the new portfolio data
                    freelancerRef.child("portfolio").updateChildren(portfolioData)
                            .addOnSuccessListener(aVoid2 -> {
                                Toast.makeText(FreelancerFormActivity2.this, "Portfolio saved successfully", Toast.LENGTH_SHORT).show();

                                if (isFromProfile) {
                                    setResult(RESULT_OK);
                                    finish();
                                } else {
                                    navigateToNextActivity();
                                }
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(FreelancerFormActivity2.this, "Failed to save portfolio: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                Log.e(TAG, "Failed to save portfolio", e);
                            });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(FreelancerFormActivity2.this, "Failed to clear existing portfolio: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Failed to clear existing portfolio", e);
                });
    }

    private void navigateToNextActivity() {
        Intent intent = new Intent(FreelancerFormActivity2.this, FreelancerFormActivity3.class);
        startActivity(intent);
    }
}
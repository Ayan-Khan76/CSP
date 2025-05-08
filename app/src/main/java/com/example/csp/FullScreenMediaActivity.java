package com.example.csp;

import android.Manifest;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.ui.StyledPlayerView;

public class FullScreenMediaActivity extends AppCompatActivity {
    private static final int REQUEST_PERMISSION_CODE = 1001;

    private ImageView imageView;
    private StyledPlayerView videoView;
    private ImageButton downloadButton;
    private String mediaUrl;
    private String mediaType;
    private String messageId;
    private DatabaseReference messageRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_screen_media);

        imageView = findViewById(R.id.fullscreen_image);
        videoView = findViewById(R.id.fullscreen_video);
        downloadButton = findViewById(R.id.download_button);

        Intent intent = getIntent();
        mediaUrl = intent.getStringExtra("mediaUrl");
        mediaType = intent.getStringExtra("mediaType");
        messageId = intent.getStringExtra("messageId");

        // Skip Firebase logic if messageId is "portfolio" (from FreelancerProfileActivity)
        if (!"portfolio".equals(messageId)) {
            // Validate messageId
            if (messageId == null || messageId.isEmpty()) {
                Toast.makeText(this, "Error: Message ID is missing!", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            messageRef = FirebaseDatabase.getInstance().getReference("chats/messages").child(messageId);
        }

        if (mediaUrl != null && !mediaUrl.isEmpty()) {
            if ("image".equals(mediaType)) {
                imageView.setVisibility(View.VISIBLE);
                Glide.with(this).load(mediaUrl).into(imageView);
            } else if ("video".equals(mediaType)) {
                videoView.setVisibility(View.VISIBLE);
                ExoPlayer player = new ExoPlayer.Builder(this).build();
                videoView.setPlayer(player);
                MediaItem mediaItem = MediaItem.fromUri(Uri.parse(mediaUrl));
                player.setMediaItem(mediaItem);
                player.prepare();
                player.setPlayWhenReady(true);
            }

            // Show download button for both images and videos
            downloadButton.setVisibility(View.VISIBLE);
            downloadButton.setOnClickListener(v -> checkPermissionsAndDownload());
        } else {
            Toast.makeText(this, "Error: Media URL is missing!", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    // Method to check permissions and start download
    private void checkPermissionsAndDownload() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ (API 33 and above)
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_VIDEO) == PackageManager.PERMISSION_GRANTED) {
                startDownload();
            } else {
                ActivityCompat.requestPermissions(this, new String[]{
                        Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_MEDIA_VIDEO
                }, REQUEST_PERMISSION_CODE);
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Android 10 to 12 (Scoped Storage is enabled, no permission needed)
            startDownload();
        } else {
            // Below Android 10 (need WRITE_EXTERNAL_STORAGE permission)
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                startDownload();
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_PERMISSION_CODE);
            }
        }
    }

    // Handle permission request result
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startDownload();
            } else {
                Toast.makeText(this, "Permission denied! Can't download media.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Method to start media download
    private void startDownload() {
        if (mediaUrl == null || mediaUrl.isEmpty()) {
            Toast.makeText(this, "Error: Media URL is missing!", Toast.LENGTH_SHORT).show();
            return;
        }

        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(mediaUrl));
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE);
        request.setTitle("Downloading media...");
        request.setDescription("Downloading " + mediaType);

        // Set correct file name and extension
        String fileName = "downloaded_" + System.currentTimeMillis();
        if ("image".equals(mediaType)) {
            fileName += ".jpg";
        } else if ("video".equals(mediaType)) {
            fileName += ".mp4";
        }

        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

        // Enqueue download request
        DownloadManager downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        if (downloadManager != null) {
            downloadManager.enqueue(request);
            Toast.makeText(this, "Download started...", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Error: DownloadManager not available!", Toast.LENGTH_SHORT).show();
        }
    }
}
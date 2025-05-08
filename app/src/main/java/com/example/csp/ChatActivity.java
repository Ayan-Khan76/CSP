package com.example.csp;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.example.csp.Adapter.ChatLiveAdapter;
import com.example.csp.Model.Message;
import com.example.csp.Model.Order;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class ChatActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks {
    private static final String TAG = "ChatActivity";
    private static final int RC_CAMERA_AND_STORAGE = 123;
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_PICK_IMAGE = 2;
    private static final int REQUEST_PICK_VIDEO = 3;

    private RecyclerView chatRecyclerView;
    private EditText messageInput;
    private ImageButton sendButton, backButton, attachButton;
    private Button placeOrderBtn;
    private ImageView profileImage;
    private TextView userName;
    private ChatLiveAdapter chatAdapter;
    private List<Message> messages;
    private DatabaseReference chatRef, ordersRef;
    private String userId, chatId, receiverId;
    private Uri currentPhotoUri;
    private ProgressDialog progressDialog;
    private boolean isClient = false;
    private boolean isFreelancer = false;
    private String userRole = "";
    private String receiverRole = "";
    private Order currentOrder = null;
    private String receiverName = "";

    // Trigger words for automatic order creation
    private final String[] orderTriggerWords = {
            "confirm order", "start work", "begin project", "accept job",
            "let's start", "i'll do it", "i accept", "deal", "agreed"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        chatRecyclerView = findViewById(R.id.chatRecyclerView);
        messageInput = findViewById(R.id.messageInput);
        sendButton = findViewById(R.id.sendButton);
        backButton = findViewById(R.id.backButton);
        attachButton = findViewById(R.id.attachButton);
        profileImage = findViewById(R.id.profilePhoto);
        userName = findViewById(R.id.userName);
        placeOrderBtn = findViewById(R.id.btnPlaceOrder);

        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Get chatId and receiverId from Intent
        Intent intent = getIntent();
        chatId = intent.getStringExtra("chatId");
        receiverId = intent.getStringExtra("receiverId");
        receiverName = intent.getStringExtra("receiverName");
        userName.setText(receiverName); // Display receiver's name

        chatRef = FirebaseDatabase.getInstance().getReference("chats").child(chatId).child("messages");
        ordersRef = FirebaseDatabase.getInstance().getReference("orders");

        messages = new ArrayList<>();
        chatAdapter = new ChatLiveAdapter(messages, userId);
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        chatRecyclerView.setAdapter(chatAdapter);

        // Set up order status listener
        chatAdapter.setOrderStatusListener(new ChatLiveAdapter.OrderStatusListener() {
            @Override
            public void onOrderStatusChanged(String orderId, String status) {
                updateOrderStatus(orderId, status);
            }

            @Override
            public void onOrderCompleted(String orderId) {
                markOrderAsCompleted(orderId);
            }
        });

        // Determine if user is client or freelancer
        determineUserRole();

        // Check for active orders in this chat
        checkForActiveOrders();

        // Load profile image
        loadProfileImage();

        // Load messages
        loadMessages();

        sendButton.setOnClickListener(v -> {
            String messageText = messageInput.getText().toString().trim();
            if (!TextUtils.isEmpty(messageText)) {
                // Check if message contains trigger words for order creation
                if (shouldCreateOrderFromMessage(messageText) && messages.size() >= 5) {
                    createOrder();
                } else {
                    sendMessage(messageText);
                }
                messageInput.setText("");
            }
        });

        // Order Button
        placeOrderBtn.setOnClickListener(v -> openOrderDialog());

        // Back button to close activity
        backButton.setOnClickListener(v -> finish());

        // Attach button to send media
        attachButton.setOnClickListener(v -> showAttachmentOptions());

        // Initialize progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Uploading media...");
        progressDialog.setCancelable(false);
    }

    private void checkForActiveOrders() {
        ordersRef.orderByChild("chatId").equalTo(chatId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot orderSnapshot : snapshot.getChildren()) {
                        Order order = orderSnapshot.getValue(Order.class);
                        if (order != null && !order.getStatus().equals("Completed") && !order.getStatus().equals("Rejected")) {
                            currentOrder = order;
                            break;
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error checking active orders: " + error.getMessage());
            }
        });
    }

    private void determineUserRole() {
        DatabaseReference freelancersRef = FirebaseDatabase.getInstance().getReference("freelancers");
        DatabaseReference clientsRef = FirebaseDatabase.getInstance().getReference("clients");

        // Check if current user is a freelancer
        freelancersRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    isFreelancer = true;
                    userRole = "freelancer";
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error checking freelancer status: " + error.getMessage());
            }
        });

        // Check if current user is a client
        clientsRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    isClient = true;
                    userRole = "client";
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error checking client status: " + error.getMessage());
            }
        });

        // Check if receiver is a freelancer
        freelancersRef.child(receiverId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    receiverRole = "freelancer";
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error checking receiver freelancer status: " + error.getMessage());
            }
        });

        // Check if receiver is a client
        clientsRef.child(receiverId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    receiverRole = "client";
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error checking receiver client status: " + error.getMessage());
            }
        });
    }

    private boolean shouldCreateOrderFromMessage(String message) {
        String lowerCaseMessage = message.toLowerCase();
        for (String trigger : orderTriggerWords) {
            if (lowerCaseMessage.contains(trigger)) {
                return true;
            }
        }
        return false;
    }

    private void createOrder() {
        // Check if we have enough messages (at least 5) to create an order
        if (messages.size() < 5) {
            Toast.makeText(this, "Please exchange at least 5 messages before creating an order", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if we already have an active order
        if (currentOrder != null && !currentOrder.getStatus().equals("Completed") && !currentOrder.getStatus().equals("Rejected")) {
            Toast.makeText(this, "You already have an active order", Toast.LENGTH_SHORT).show();
            return;
        }

        // Determine client and freelancer IDs
        String clientId, freelancerId;
        if (isClient) {
            clientId = userId;
            freelancerId = receiverId;
        } else if (isFreelancer) {
            clientId = receiverId;
            freelancerId = userId;
        } else {
            Toast.makeText(this, "Could not determine user roles", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create a new order
        String orderId = ordersRef.push().getKey();
        Order order = new Order(orderId, clientId, freelancerId, "Pending", "Service from chat", 0, System.currentTimeMillis());
        order.setChatId(chatId);

        // Save order to Firebase
        ordersRef.child(orderId).setValue(order);

        // Send a message to the chat
        String messageId = chatRef.push().getKey();
        Message orderCreatedMessage = new Message(
                messageId,
                "Order created. Waiting for details and confirmation!",
                userId,
                System.currentTimeMillis(),
                "order_created",
                orderId
        );
        chatRef.child(messageId).setValue(orderCreatedMessage);

        // Show success message
        Toast.makeText(this, "Order created successfully", Toast.LENGTH_SHORT).show();
    }

    private void loadProfileImage() {
        DatabaseReference freelancerRef = FirebaseDatabase.getInstance().getReference("freelancers").child(receiverId);
        DatabaseReference clientRef = FirebaseDatabase.getInstance().getReference("clients").child(receiverId);

        // First, check if receiver is a freelancer
        freelancerRef.child("profileImageUrl").get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult().exists()) {
                String profileImageUrl = task.getResult().getValue(String.class);

                // Load freelancer's profile image
                Glide.with(ChatActivity.this)
                        .load(profileImageUrl)
                        .placeholder(R.drawable.baseline_person_24)
                        .error(R.drawable.baseline_person_24)
                        .into(profileImage);
            } else {
                // If not found in freelancers, check in clients
                clientRef.child("profileImageUrl").get().addOnCompleteListener(task2 -> {
                    if (task2.isSuccessful() && task2.getResult().exists()) {
                        String profileImageUrl = task2.getResult().getValue(String.class);

                        // Load client's profile image
                        Glide.with(ChatActivity.this)
                                .load(profileImageUrl)
                                .placeholder(R.drawable.baseline_person_24)
                                .error(R.drawable.baseline_person_24)
                                .into(profileImage);
                    }
                });
            }
        });
    }

    private void loadMessages() {
        chatRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                messages.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    Message message = data.getValue(Message.class);
                    messages.add(message);
                }
                chatAdapter.notifyDataSetChanged();
                chatRecyclerView.scrollToPosition(messages.size() - 1);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    private void sendMessage(String text) {
        String messageId = chatRef.push().getKey(); // Generate unique message ID
        Message message = new Message(messageId, text, userId, System.currentTimeMillis());
        chatRef.child(messageId).setValue(message);
    }

    private void sendMediaMessage(String text, String type, String mediaUrl) {
        String messageId = chatRef.push().getKey();
        Message mediaMessage = Message.createMediaMessage(
                messageId,
                text,
                userId,
                System.currentTimeMillis(),
                type,
                mediaUrl
        );
        chatRef.child(messageId).setValue(mediaMessage);
    }

    private void showAttachmentOptions() {
        String[] options = {"Choose Photo from Gallery", "Choose Video from Gallery"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Send Media");
        builder.setItems(options, (dialog, which) -> {
            switch (which) {
                case 0: // Choose Photo
                    checkPermissionsAndPickImage();
                    break;
                case 1: // Choose Video
                    checkPermissionsAndPickVideo();
                    break;
            }
        });
        builder.show();
    }

    @AfterPermissionGranted(RC_CAMERA_AND_STORAGE)
    private void checkPermissionsAndPickImage() {
        String[] perms = {Manifest.permission.READ_MEDIA_IMAGES};

        if (EasyPermissions.hasPermissions(this, perms)) {
            pickImageFromGallery();
        } else {
            EasyPermissions.requestPermissions(
                    this,
                    "Storage permission is needed to pick an image.",
                    RC_CAMERA_AND_STORAGE,
                    perms
            );
        }
    }

    @AfterPermissionGranted(RC_CAMERA_AND_STORAGE)
    private void checkPermissionsAndPickVideo() {
        String[] perms = {Manifest.permission.READ_MEDIA_VIDEO};

        if (EasyPermissions.hasPermissions(this, perms)) {
            pickVideoFromGallery();
        } else {
            EasyPermissions.requestPermissions(
                    this,
                    "Storage permission is needed to pick a video.",
                    RC_CAMERA_AND_STORAGE,
                    perms
            );
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(null);
        return File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
    }

    private void pickImageFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_PICK_IMAGE);
    }

    private void pickVideoFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_PICK_VIDEO);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            Uri mediaUri = null;
            String mediaType = null;

            switch (requestCode) {
                case REQUEST_IMAGE_CAPTURE:
                    mediaUri = currentPhotoUri;
                    mediaType = "image";
                    break;
                case REQUEST_PICK_IMAGE:
                    if (data != null) {
                        mediaUri = data.getData();
                        mediaType = "image";
                    }
                    break;
                case REQUEST_PICK_VIDEO:
                    if (data != null) {
                        mediaUri = data.getData();
                        mediaType = "video";
                    }
                    break;
            }

            if (mediaUri != null && mediaType != null) {
                uploadMediaToCloudinary(mediaUri, mediaType);
            }
        }
    }

    private void uploadMediaToCloudinary(Uri mediaUri, String mediaType) {
        progressDialog.show();

        String caption = messageInput.getText().toString().trim();
        messageInput.setText("");

        String requestId = MediaManager.get().upload(mediaUri)
                .option("resource_type", mediaType)
                .callback(new UploadCallback() {
                    @Override
                    public void onStart(String requestId) {
                        Log.d(TAG, "Upload started: " + requestId);
                    }
                    @Override
                    public void onProgress(String requestId, long bytes, long totalBytes) {
                        double progress = (bytes * 100) / totalBytes;
                        Log.d(TAG, "Upload progress: " + progress + "%");
                    }

                    @Override
                    public void onSuccess(String requestId, Map resultData) {
                        progressDialog.dismiss();
                        String mediaUrl = (String) resultData.get("secure_url");
                        Log.d(TAG, "Upload successful: " + mediaUrl);
                        sendMediaMessage(caption, mediaType, mediaUrl);
                    }

                    @Override
                    public void onError(String requestId, ErrorInfo error) {
                        progressDialog.dismiss();
                        Log.e(TAG, "Upload error: " + error.getDescription());
                        Toast.makeText(ChatActivity.this, "Upload failed: " + error.getDescription(), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onReschedule(String requestId, ErrorInfo error) {
                        Log.d(TAG, "Upload rescheduled: " + error.getDescription());
                    }
                })
                .dispatch();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        Log.d(TAG, "Permissions granted: " + perms.size());
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        Log.d(TAG, "Permissions denied: " + perms.size());
        Toast.makeText(this, "Permissions are required to send media", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (chatRecyclerView != null && chatRecyclerView.getAdapter() != null) {
            chatRecyclerView.getAdapter().notifyDataSetChanged();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        releaseAllVideoPlayers();
    }

    private void releaseAllVideoPlayers() {
        if (chatRecyclerView != null) {
            for (int i = 0; i < chatRecyclerView.getChildCount(); i++) {
                RecyclerView.ViewHolder holder = chatRecyclerView.getChildViewHolder(chatRecyclerView.getChildAt(i));
                if (holder instanceof ChatLiveAdapter.ReceivedVideoMessageHolder) {
                    ((ChatLiveAdapter.ReceivedVideoMessageHolder) holder).releasePlayer();
                }
            }
        }
    }

    private void openOrderDialog() {
        // Check if there's already an active order
        if (currentOrder != null && !currentOrder.getStatus().equals("Completed") && !currentOrder.getStatus().equals("Rejected")) {
            showActiveOrderDialog();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Place Order");

        // Inflate custom layout
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_place_order, null);
        EditText serviceInput = view.findViewById(R.id.serviceInput);
        EditText descriptionInput = view.findViewById(R.id.descriptionInput);
        EditText priceInput = view.findViewById(R.id.priceInput);
        EditText deadlineInput = view.findViewById(R.id.deadlineInput);

        builder.setView(view);

        builder.setPositiveButton("Send Order", (dialog, which) -> {
            String service = serviceInput.getText().toString().trim();
            String description = descriptionInput.getText().toString().trim();
            String priceText = priceInput.getText().toString().trim();
            String deadline = deadlineInput.getText().toString().trim();

            if (service.isEmpty()) {
                Toast.makeText(this, "Please enter a service name", Toast.LENGTH_SHORT).show();
                return;
            }

            int price = 0;
            if (!priceText.isEmpty()) {
                try {
                    price = Integer.parseInt(priceText);
                } catch (NumberFormatException e) {
                    Toast.makeText(this, "Please enter a valid price", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            createNewOrder(service, description, price, deadline);
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        builder.show();
    }

    private void showActiveOrderDialog() {
        if (currentOrder == null) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Active Order");

        // Inflate custom layout
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_active_order, null);
        TextView orderIdText = view.findViewById(R.id.orderId);
        TextView serviceText = view.findViewById(R.id.service);
        TextView descriptionText = view.findViewById(R.id.description);
        TextView priceText = view.findViewById(R.id.price);
        TextView statusText = view.findViewById(R.id.status);
        TextView deadlineText = view.findViewById(R.id.deadline);
        Button updateStatusBtn = view.findViewById(R.id.updateStatusBtn);
        Button markCompletedBtn = view.findViewById(R.id.markCompletedBtn);

        // Set values
        orderIdText.setText("Order ID: " + currentOrder.getOrderId());
        serviceText.setText("Service: " + currentOrder.getService());
        descriptionText.setText("Description: " + (currentOrder.getDescription() != null ?
                currentOrder.getDescription() : "No description"));
        priceText.setText("Price: ₹" + currentOrder.getPrice());
        statusText.setText("Status: " + currentOrder.getStatus());
        deadlineText.setText("Deadline: " + (currentOrder.getDeadline() != null ?
                currentOrder.getDeadline() : "Not specified"));

        // Show/hide buttons based on user role and order status
        // FIXED: Always show buttons for freelancer unless order is completed or rejected
        boolean isOrderCompleted = currentOrder.getStatus().equals("Completed");
        boolean isOrderRejected = currentOrder.getStatus().equals("Rejected");

        if (isFreelancer && !isOrderCompleted && !isOrderRejected) {
            updateStatusBtn.setVisibility(View.VISIBLE);
            markCompletedBtn.setVisibility(View.VISIBLE);
        } else if (isClient && currentOrder.getStatus().equals("Completed") && !currentOrder.isConfirmedByClient()) {
            markCompletedBtn.setText("Confirm Completion");
            markCompletedBtn.setVisibility(View.VISIBLE);
            updateStatusBtn.setVisibility(View.GONE);
        } else {
            updateStatusBtn.setVisibility(View.GONE);
            markCompletedBtn.setVisibility(View.GONE);
        }

        updateStatusBtn.setOnClickListener(v -> {
            showUpdateStatusDialog(currentOrder.getOrderId());
        });

        markCompletedBtn.setOnClickListener(v -> {
            if (isFreelancer) {
                updateOrderStatus(currentOrder.getOrderId(), "Completed");
                Toast.makeText(this, "Order marked as completed", Toast.LENGTH_SHORT).show();
            } else if (isClient) {
                markOrderAsCompleted(currentOrder.getOrderId());
                Toast.makeText(this, "Order completion confirmed", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setView(view);
        builder.setPositiveButton("Close", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void showUpdateStatusDialog(String orderId) {
        String[] statuses = {"In Progress", "On Hold", "Almost Done"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Update Order Status");
        builder.setItems(statuses, (dialog, which) -> {
            updateOrderStatus(orderId, statuses[which]);
            Toast.makeText(this, "Status updated to: " + statuses[which], Toast.LENGTH_SHORT).show();
        });
        builder.show();
    }

    private void createNewOrder(String service, String description, int price, String deadline) {
        // Determine client and freelancer IDs
        String clientId, freelancerId;
        if (isClient) {
            clientId = userId;
            freelancerId = receiverId;
        } else if (isFreelancer) {
            clientId = receiverId;
            freelancerId = userId;
        } else {
            Toast.makeText(this, "Could not determine user roles", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create a new order
        String orderId = ordersRef.push().getKey();
        Order order = new Order(
                orderId,
                clientId,
                freelancerId,
                "Pending",
                service,
                price,
                System.currentTimeMillis()
        );
        order.setDescription(description);
        order.setDeadline(deadline);
        order.setChatId(chatId);
        order.setOrderSenderId(userId); // ADDED: Track who sent the order

        // Save order to Firebase
        ordersRef.child(orderId).setValue(order);

        // Create order message content
        StringBuilder orderDetails = new StringBuilder();
        orderDetails.append("📋 New Order\n");
        orderDetails.append("Service: ").append(service).append("\n");
        if (!description.isEmpty()) {
            orderDetails.append("Description: ").append(description).append("\n");
        }
        orderDetails.append("Price: ₹").append(price).append("\n");
        if (!deadline.isEmpty()) {
            orderDetails.append("Deadline: ").append(deadline);
        }

        // Send a message to the chat
        String messageId = chatRef.push().getKey();
        Message orderMessage = new Message(
                messageId,
                orderDetails.toString(),
                userId,
                System.currentTimeMillis(),
                "order",
                orderId
        );
        chatRef.child(messageId).setValue(orderMessage);

        // Set as current order
        currentOrder = order;

        // Show success message
        Toast.makeText(this, "Order created successfully", Toast.LENGTH_SHORT).show();
    }

    private void updateOrderStatus(String orderId, String status) {
        if (orderId == null) return;

        DatabaseReference orderRef = FirebaseDatabase.getInstance().getReference("orders").child(orderId);

        // Prepare updates
        Map<String, Object> updates = new HashMap<>();
        updates.put("status", status);

        //  Add timestamp only if status is "Completed"
        if (status.equals("Completed")) {
            updates.put("completionTimestamp", System.currentTimeMillis());
        }

        orderRef.updateChildren(updates).addOnSuccessListener(aVoid -> {
            // Send a status update message
            String messageId = chatRef.push().getKey();
            String statusMessage = "🔄 Order Status Updated: " + status;

            Message message = new Message(
                    messageId,
                    statusMessage,
                    userId,
                    System.currentTimeMillis(),
                    "order_status",
                    orderId
            );
            chatRef.child(messageId).setValue(message);
        }).addOnFailureListener(e -> Log.e("FirebaseError", "Failed to update order status", e));
    }


    private void markOrderAsCompleted(String orderId) {
        if (orderId == null) return;

        DatabaseReference orderRef = FirebaseDatabase.getInstance().getReference("orders").child(orderId);

        // Update the order without timestamp
        Map<String, Object> updates = new HashMap<>();
        updates.put("status", "Completed");

        if (isClient) {
            updates.put("confirmedByClient", true);
        }

        orderRef.updateChildren(updates).addOnSuccessListener(aVoid -> {
            // Send a completion message
            String messageId = chatRef.push().getKey();
            String completionMessage = isClient
                    ? "✅ Order Completion Confirmed by Client"
                    : "✅ Order Marked as Completed by Freelancer";

            Message message = new Message(
                    messageId,
                    completionMessage,
                    userId,
                    System.currentTimeMillis(),
                    "order_completed",
                    orderId
            );
            chatRef.child(messageId).setValue(message);
        });
    }

}
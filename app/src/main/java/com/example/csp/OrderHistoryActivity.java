package com.example.csp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.csp.Adapter.OrderHistoryAdapter;
import com.example.csp.Model.Order;
import com.example.csp.Model.OrderHistoryItem;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class OrderHistoryActivity extends AppCompatActivity implements OrderHistoryAdapter.OrderHistoryListener {
    private static final String TAG = "OrderHistoryActivity";

    private RecyclerView recyclerView;
    private OrderHistoryAdapter adapter;
    private List<OrderHistoryItem> orderHistoryItems;
    private ProgressBar progressBar;
    private TextView emptyView;
    private ImageButton backButton;

    private String currentUserId;
    private boolean isFreelancer = false;
    private boolean isClient = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_history);

        // Initialize views
        recyclerView = findViewById(R.id.orderHistoryRecyclerView);
        progressBar = findViewById(R.id.progressBar);
        emptyView = findViewById(R.id.emptyView);
        backButton = findViewById(R.id.backButton);

        // Set up RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        orderHistoryItems = new ArrayList<>();
        adapter = new OrderHistoryAdapter(orderHistoryItems, this);
        recyclerView.setAdapter(adapter);

        // Get current user ID
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Set up back button
        backButton.setOnClickListener(v -> finish());

        // Determine if user is client or freelancer
        determineUserRole();
    }

    private void determineUserRole() {
        DatabaseReference freelancersRef = FirebaseDatabase.getInstance().getReference("freelancers");
        DatabaseReference clientsRef = FirebaseDatabase.getInstance().getReference("clients");

        // Check if current user is a freelancer
        freelancersRef.child(currentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    isFreelancer = true;
                    loadCompletedOrders();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error checking freelancer status: " + error.getMessage());
            }
        });

        // Check if current user is a client
        clientsRef.child(currentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    isClient = true;
                    loadCompletedOrders();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error checking client status: " + error.getMessage());
            }
        });
    }

    private void loadCompletedOrders() {
        DatabaseReference ordersRef = FirebaseDatabase.getInstance().getReference("orders");

        // Show progress bar
        progressBar.setVisibility(View.VISIBLE);

        // Query orders where user is either client or freelancer
        ordersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                orderHistoryItems.clear();

                for (DataSnapshot orderSnapshot : snapshot.getChildren()) {
                    Order order = orderSnapshot.getValue(Order.class);

                    if (order != null && "Completed".equals(order.getStatus())) {
                        // Check if current user is involved in this order
                        boolean isUserInvolved = false;
                        String otherUserId = null;
                        boolean userIsFreelancer = false;

                        if (order.getClientId().equals(currentUserId)) {
                            isUserInvolved = true;
                            otherUserId = order.getFreelancerId();
                            userIsFreelancer = false;
                        } else if (order.getFreelancerId().equals(currentUserId)) {
                            isUserInvolved = true;
                            otherUserId = order.getClientId();
                            userIsFreelancer = true;
                        }

                        if (isUserInvolved && otherUserId != null) {
                            // Create order history item
                            OrderHistoryItem item = new OrderHistoryItem(
                                    order.getOrderId(),
                                    order.getService(),
                                    order.getDescription(),
                                    order.getPrice(),
                                    order.getCompletedTimestamp(),
                                    otherUserId,
                                    userIsFreelancer,
                                    order.getChatId()
                            );

                            orderHistoryItems.add(item);

                            // Fetch other user's name
                            fetchUserName(item);
                        }
                    }
                }

                // Sort by completion date (newest first)
                Collections.sort(orderHistoryItems, (o1, o2) ->
                        Long.compare(o2.getCompletionTimestamp(), o1.getCompletionTimestamp()));

                // Update UI
                progressBar.setVisibility(View.GONE);

                if (orderHistoryItems.isEmpty()) {
                    emptyView.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                } else {
                    emptyView.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error loading orders: " + error.getMessage());
                progressBar.setVisibility(View.GONE);
                emptyView.setVisibility(View.VISIBLE);
            }
        });
    }

    private void fetchUserName(OrderHistoryItem item) {
        String otherUserId = item.getOtherUserId();
        if (otherUserId == null) return; // Prevent null reference issues

        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(otherUserId);

        userRef.child("name").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && snapshot.getValue() != null) {
                    item.setOtherUserName(snapshot.getValue(String.class));
                } else {
                    item.setOtherUserName("Unknown user");
                }
                adapter.notifyDataSetChanged(); // Refresh UI after fetching the name
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error fetching user name: " + error.getMessage());
            }
        });
    }


    @Override
    public void onOrderClicked(OrderHistoryItem order) {
        // Open chat with the other user
        if (order.getChatId() != null) {
            Intent intent = new Intent(this, ChatActivity.class);
            intent.putExtra("chatId", order.getChatId());
            intent.putExtra("receiverId", order.getOtherUserId());
            intent.putExtra("receiverName", order.getOtherUserName());
            startActivity(intent);
        }
    }
}
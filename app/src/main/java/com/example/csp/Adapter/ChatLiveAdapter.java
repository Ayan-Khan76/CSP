package com.example.csp.Adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.csp.FullScreenMediaActivity;
import com.example.csp.Model.Message;
import com.example.csp.Model.Order;
import com.example.csp.R;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.ui.StyledPlayerView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChatLiveAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int VIEW_TYPE_MESSAGE_SENT_TEXT = 1;
    private static final int VIEW_TYPE_MESSAGE_RECEIVED_TEXT = 2;
    private static final int VIEW_TYPE_MESSAGE_SENT_IMAGE = 3;
    private static final int VIEW_TYPE_MESSAGE_RECEIVED_IMAGE = 4;
    private static final int VIEW_TYPE_MESSAGE_SENT_VIDEO = 5;
    private static final int VIEW_TYPE_MESSAGE_RECEIVED_VIDEO = 6;
    private static final int VIEW_TYPE_ORDER_MESSAGE = 7;
    private static final int VIEW_TYPE_ORDER_STATUS_MESSAGE = 8;
    private static final int VIEW_TYPE_ORDER_COMPLETED_MESSAGE = 9;

    private final List<Message> messages;
    private final String currentUserId;
    private Context context;
    private OrderStatusListener orderStatusListener;

    public interface OrderStatusListener {
        void onOrderStatusChanged(String orderId, String status);
        void onOrderCompleted(String orderId);
    }

    public void setOrderStatusListener(OrderStatusListener listener) {
        this.orderStatusListener = listener;
    }

    public ChatLiveAdapter(List<Message> messages, String currentUserId) {
        this.messages = messages;
        this.currentUserId = currentUserId;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        switch (viewType) {
            case VIEW_TYPE_MESSAGE_SENT_TEXT:
                View sentTextView = inflater.inflate(R.layout.item_message_sent, parent, false);
                return new SentTextMessageHolder(sentTextView);
            case VIEW_TYPE_MESSAGE_RECEIVED_TEXT:
                View receivedTextView = inflater.inflate(R.layout.item_message_received, parent, false);
                return new ReceivedTextMessageHolder(receivedTextView);
            case VIEW_TYPE_MESSAGE_SENT_IMAGE:
                View sentImageView = inflater.inflate(R.layout.item_message_sent_image, parent, false);
                return new SentImageMessageHolder(sentImageView);
            case VIEW_TYPE_MESSAGE_RECEIVED_IMAGE:
                View receivedImageView = inflater.inflate(R.layout.item_message_received_image, parent, false);
                return new ReceivedImageMessageHolder(receivedImageView);
            case VIEW_TYPE_MESSAGE_SENT_VIDEO:
                View sentVideoView = inflater.inflate(R.layout.item_message_sent_video, parent, false);
                return new SentVideoMessageHolder(sentVideoView);
            case VIEW_TYPE_MESSAGE_RECEIVED_VIDEO:
                View receivedVideoView = inflater.inflate(R.layout.item_message_received_video, parent, false);
                return new ReceivedVideoMessageHolder(receivedVideoView);
            case VIEW_TYPE_ORDER_MESSAGE:
                View orderView = inflater.inflate(R.layout.item_order_message, parent, false);
                return new OrderMessageHolder(orderView);
            case VIEW_TYPE_ORDER_STATUS_MESSAGE:
                View orderStatusView = inflater.inflate(R.layout.item_order_status, parent, false);
                return new OrderStatusMessageHolder(orderStatusView);
            case VIEW_TYPE_ORDER_COMPLETED_MESSAGE:
                View orderCompletedView = inflater.inflate(R.layout.item_order_completed, parent, false);
                return new OrderCompletedMessageHolder(orderCompletedView);
            default:
                View defaultView = inflater.inflate(R.layout.item_message_received, parent, false);
                return new ReceivedTextMessageHolder(defaultView);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message message = messages.get(position);
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        String time = sdf.format(new Date(message.getTimestamp()));

        if (holder instanceof SentImageMessageHolder) {
            SentImageMessageHolder imageHolder = (SentImageMessageHolder) holder;
            imageHolder.bind(message, time);
            imageHolder.imageView.setOnClickListener(v -> openFullScreen(message));
        } else if (holder instanceof ReceivedImageMessageHolder) {
            ReceivedImageMessageHolder imageHolder = (ReceivedImageMessageHolder) holder;
            imageHolder.bind(message, time);
            imageHolder.imageView.setOnClickListener(v -> openFullScreen(message));
        } else if (holder instanceof SentVideoMessageHolder) {
            SentVideoMessageHolder videoHolder = (SentVideoMessageHolder) holder;
            videoHolder.bind(message, time);
            videoHolder.playerView.setOnClickListener(v -> openFullScreen(message));
        } else if (holder instanceof ReceivedVideoMessageHolder) {
            ReceivedVideoMessageHolder videoHolder = (ReceivedVideoMessageHolder) holder;
            videoHolder.bind(message, time);
            videoHolder.playerView.setOnClickListener(v -> openFullScreen(message));
        } else if (holder instanceof OrderMessageHolder) {
            OrderMessageHolder orderHolder = (OrderMessageHolder) holder;
            orderHolder.bind(message, time);
        } else if (holder instanceof OrderStatusMessageHolder) {
            OrderStatusMessageHolder statusHolder = (OrderStatusMessageHolder) holder;
            statusHolder.bind(message, time);
        } else if (holder instanceof OrderCompletedMessageHolder) {
            OrderCompletedMessageHolder completedHolder = (OrderCompletedMessageHolder) holder;
            completedHolder.bind(message, time);
        } else {
            switch (holder.getItemViewType()) {
                case VIEW_TYPE_MESSAGE_SENT_TEXT:
                    ((SentTextMessageHolder) holder).bind(message, time);
                    break;
                case VIEW_TYPE_MESSAGE_RECEIVED_TEXT:
                    ((ReceivedTextMessageHolder) holder).bind(message, time);
                    break;
            }
        }
    }

    private void openFullScreen(Message message) {
        if (message.getMessageId() == null) {
            Log.e("FullScreenMedia", "messageId is null for message: " + message.toString());
            return;
        }
        Intent intent = new Intent(context, FullScreenMediaActivity.class);
        intent.putExtra("mediaUrl", message.getMediaUrl());
        intent.putExtra("mediaType", message.getType());
        intent.putExtra("messageId", message.getMessageId());
        context.startActivity(intent);
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (messages == null || messages.isEmpty() || position < 0 || position >= messages.size()) {
            return VIEW_TYPE_MESSAGE_RECEIVED_TEXT; // Default safe fallback
        }

        Message message = messages.get(position);

        if (message == null || message.getSenderId() == null || message.getType() == null) {
            return VIEW_TYPE_MESSAGE_RECEIVED_TEXT; // Default if message data is incomplete
        }

        boolean isCurrentUser = message.getSenderId().equals(currentUserId);

        switch (message.getType()) {
            case "text":
                return isCurrentUser ? VIEW_TYPE_MESSAGE_SENT_TEXT : VIEW_TYPE_MESSAGE_RECEIVED_TEXT;
            case "image":
                return isCurrentUser ? VIEW_TYPE_MESSAGE_SENT_IMAGE : VIEW_TYPE_MESSAGE_RECEIVED_IMAGE;
            case "video":
                return isCurrentUser ? VIEW_TYPE_MESSAGE_SENT_VIDEO : VIEW_TYPE_MESSAGE_RECEIVED_VIDEO;
            case "order":
                return VIEW_TYPE_ORDER_MESSAGE;
            case "order_status":
                return VIEW_TYPE_ORDER_STATUS_MESSAGE;
            case "order_created":
                return VIEW_TYPE_ORDER_STATUS_MESSAGE;
            case "order_completed":
                return VIEW_TYPE_ORDER_COMPLETED_MESSAGE;
            default:
                return VIEW_TYPE_MESSAGE_RECEIVED_TEXT; // Default to received text if type is unknown
        }
    }

    // Text message view holders
    class SentTextMessageHolder extends RecyclerView.ViewHolder {
        TextView messageText, timeText;

        SentTextMessageHolder(View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.messageText);
            timeText = itemView.findViewById(R.id.timestamp);
        }

        void bind(Message message, String time) {
            messageText.setText(message.getText());
            timeText.setText(time);
        }
    }

    class ReceivedTextMessageHolder extends RecyclerView.ViewHolder {
        TextView messageText, timeText;

        ReceivedTextMessageHolder(View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.messageText);
            timeText = itemView.findViewById(R.id.timestamp);
        }

        void bind(Message message, String time) {
            messageText.setText(message.getText());
            timeText.setText(time);
        }
    }

    class SentImageMessageHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView captionText, timeText;

        SentImageMessageHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image_message_body);
            captionText = itemView.findViewById(R.id.text_message_caption);
            timeText = itemView.findViewById(R.id.text_message_time);
        }

        void bind(Message message, String time) {
            if (!message.getText().isEmpty()) {
                captionText.setVisibility(View.VISIBLE);
                captionText.setText(message.getText());
            } else {
                captionText.setVisibility(View.GONE);
            }

            timeText.setText(time);

            Glide.with(itemView.getContext())
                    .load(message.getMediaUrl())
                    .placeholder(R.drawable.ic_image_placeholder)
                    .error(R.drawable.ic_image_error)
                    .into(imageView);
        }
    }

    class ReceivedImageMessageHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView captionText, timeText;

        ReceivedImageMessageHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image_message_body);
            captionText = itemView.findViewById(R.id.text_message_caption);
            timeText = itemView.findViewById(R.id.text_message_time);
        }

        void bind(Message message, String time) {
            if (!message.getText().isEmpty()) {
                captionText.setVisibility(View.VISIBLE);
                captionText.setText(message.getText());
            } else {
                captionText.setVisibility(View.GONE);
            }

            timeText.setText(time);

            Glide.with(itemView.getContext())
                    .load(message.getMediaUrl())
                    .placeholder(R.drawable.ic_image_placeholder)
                    .error(R.drawable.ic_image_error)
                    .into(imageView);
        }
    }

    // Video message view holders
    class SentVideoMessageHolder extends RecyclerView.ViewHolder {
        StyledPlayerView playerView;
        TextView captionText, timeText;
        ExoPlayer player;

        SentVideoMessageHolder(View itemView) {
            super(itemView);
            playerView = itemView.findViewById(R.id.video_message_body);
            captionText = itemView.findViewById(R.id.text_message_caption);
            timeText = itemView.findViewById(R.id.text_message_time);
        }

        void bind(Message message, String time) {
            if (!message.getText().isEmpty()) {
                captionText.setVisibility(View.VISIBLE);
                captionText.setText(message.getText());
            } else {
                captionText.setVisibility(View.GONE);
            }

            timeText.setText(time);

            // Initialize player if needed
            if (player == null) {
                player = new ExoPlayer.Builder(context).build();
                playerView.setPlayer(player);
            }

            // Set up the video
            MediaItem mediaItem = MediaItem.fromUri(message.getMediaUrl());
            player.setMediaItem(mediaItem);
            player.prepare();
            // Don't auto-play to save data
            player.setPlayWhenReady(false);
        }
    }

    public class ReceivedVideoMessageHolder extends RecyclerView.ViewHolder {
        StyledPlayerView playerView;
        TextView captionText, timeText;
        ExoPlayer player;

        ReceivedVideoMessageHolder(View itemView) {
            super(itemView);
            playerView = itemView.findViewById(R.id.video_message_body);
            captionText = itemView.findViewById(R.id.text_message_caption);
            timeText = itemView.findViewById(R.id.text_message_time);
        }

        void bind(Message message, String time) {
            if (!message.getText().isEmpty()) {
                captionText.setVisibility(View.VISIBLE);
                captionText.setText(message.getText());
            } else {
                captionText.setVisibility(View.GONE);
            }

            timeText.setText(time);

            // Initialize player if needed
            if (player == null) {
                player = new ExoPlayer.Builder(context).build();
                playerView.setPlayer(player);
            }

            // Set up the video
            MediaItem mediaItem = MediaItem.fromUri(message.getMediaUrl());
            player.setMediaItem(mediaItem);
            player.prepare();
            // Don't auto-play to save data
            player.setPlayWhenReady(false);
        }

        public void releasePlayer() {
            if (player != null) {
                player.stop();
                player.release();
                player = null;
            }
        }
    }

    // Order message view holder
    class OrderMessageHolder extends RecyclerView.ViewHolder {
        TextView orderText, timeText;
        Button acceptButton, rejectButton, viewDetailsButton;
        CardView orderCard;

        OrderMessageHolder(View itemView) {
            super(itemView);
            orderText = itemView.findViewById(R.id.orderText);
            timeText = itemView.findViewById(R.id.orderTime);
            acceptButton = itemView.findViewById(R.id.acceptButton);
            rejectButton = itemView.findViewById(R.id.rejectButton);
            viewDetailsButton = itemView.findViewById(R.id.viewDetailsButton);
            orderCard = itemView.findViewById(R.id.orderCard);
        }

        void bind(Message message, String time) {
            orderText.setText(message.getText());
            timeText.setText(time);

            // Get order details to determine buttons visibility
            if (message.getOrderId() != null) {
                DatabaseReference orderRef = FirebaseDatabase.getInstance().getReference("orders").child(message.getOrderId());
                orderRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            Order order = snapshot.getValue(Order.class);
                            if (order != null) {
                                // FIXED: Show accept/reject buttons to the receiver of the order, not based on role
                                boolean isPending = "Pending".equals(order.getStatus());
                                boolean isOrderReceiver = !currentUserId.equals(order.getOrderSenderId());

                                acceptButton.setVisibility(isOrderReceiver && isPending ? View.VISIBLE : View.GONE);
                                rejectButton.setVisibility(isOrderReceiver && isPending ? View.VISIBLE : View.GONE);

                                // Set button colors based on status
                                if (isPending) {
                                    orderCard.setCardBackgroundColor(context.getResources().getColor(android.R.color.holo_orange_light));
                                } else if ("Accepted".equals(order.getStatus()) || "In Progress".equals(order.getStatus())) {
                                    orderCard.setCardBackgroundColor(context.getResources().getColor(android.R.color.holo_blue_light));
                                } else if ("Completed".equals(order.getStatus())) {
                                    orderCard.setCardBackgroundColor(context.getResources().getColor(android.R.color.holo_green_light));
                                } else if ("Rejected".equals(order.getStatus())) {
                                    orderCard.setCardBackgroundColor(context.getResources().getColor(android.R.color.holo_red_light));
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("OrderSystem", "Failed to load order: " + error.getMessage());
                    }
                });

                // Set button click listeners
                acceptButton.setOnClickListener(v -> {
                    if (orderStatusListener != null) {
                        orderStatusListener.onOrderStatusChanged(message.getOrderId(), "Accepted");
                        acceptButton.setVisibility(View.GONE);
                        rejectButton.setVisibility(View.GONE);
                    }
                });

                rejectButton.setOnClickListener(v -> {
                    if (orderStatusListener != null) {
                        orderStatusListener.onOrderStatusChanged(message.getOrderId(), "Rejected");
                        acceptButton.setVisibility(View.GONE);
                        rejectButton.setVisibility(View.GONE);
                    }
                });

                viewDetailsButton.setOnClickListener(v -> {
                    // Show order details dialog
                    showOrderDetailsDialog(message.getOrderId());
                });
            }
        }

        private void showOrderDetailsDialog(String orderId) {
            DatabaseReference orderRef = FirebaseDatabase.getInstance().getReference("orders").child(orderId);
            orderRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        Order order = snapshot.getValue(Order.class);
                        if (order != null) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(context);
                            builder.setTitle("Order Details");

                            View view = LayoutInflater.from(context).inflate(R.layout.dialog_order_details, null);
                            TextView orderIdText = view.findViewById(R.id.orderId);
                            TextView serviceText = view.findViewById(R.id.service);
                            TextView descriptionText = view.findViewById(R.id.description);
                            TextView priceText = view.findViewById(R.id.price);
                            TextView statusText = view.findViewById(R.id.status);
                            TextView deadlineText = view.findViewById(R.id.deadline);
                            TextView createdText = view.findViewById(R.id.created);

                            // Set values
                            orderIdText.setText("Order ID: " + order.getOrderId());
                            serviceText.setText("Service: " + order.getService());

                            if (order.getDescription() != null && !order.getDescription().isEmpty()) {
                                descriptionText.setText("Description: " + order.getDescription());
                                descriptionText.setVisibility(View.VISIBLE);
                            } else {
                                descriptionText.setVisibility(View.GONE);
                            }

                            priceText.setText("Price: ₹" + order.getPrice());
                            statusText.setText("Status: " + order.getStatus());

                            if (order.getDeadline() != null && !order.getDeadline().isEmpty()) {
                                deadlineText.setText("Deadline: " + order.getDeadline());
                                deadlineText.setVisibility(View.VISIBLE);
                            } else {
                                deadlineText.setVisibility(View.GONE);
                            }

                            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault());
                            createdText.setText("Created: " + sdf.format(new Date(order.getTimestamp())));

                            builder.setView(view);
                            builder.setPositiveButton("Close", (dialog, which) -> dialog.dismiss());
                            builder.show();
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("OrderSystem", "Failed to load order details: " + error.getMessage());
                }
            });
        }
    }

    // Order status message view holder
    class OrderStatusMessageHolder extends RecyclerView.ViewHolder {
        TextView messageText, timeText;
        CardView statusCard;

        OrderStatusMessageHolder(View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.statusText);
            timeText = itemView.findViewById(R.id.statusTime);
            statusCard = itemView.findViewById(R.id.statusCard);
        }

        void bind(Message message, String time) {
            messageText.setText(message.getText());
            timeText.setText(time);

            // Set card color based on message type
            if (message.getType().equals("order_created")) {
                statusCard.setCardBackgroundColor(context.getResources().getColor(android.R.color.holo_orange_light));
            } else if (message.getType().equals("order_status")) {
                if (message.getText().contains("Accepted") || message.getText().contains("In Progress")) {
                    statusCard.setCardBackgroundColor(context.getResources().getColor(android.R.color.holo_blue_light));
                } else if (message.getText().contains("Completed")) {
                    statusCard.setCardBackgroundColor(context.getResources().getColor(android.R.color.holo_green_light));
                } else if (message.getText().contains("Rejected")) {
                    statusCard.setCardBackgroundColor(context.getResources().getColor(android.R.color.holo_red_light));
                }
            }
        }
    }

    // Order completed message view holder
    class OrderCompletedMessageHolder extends RecyclerView.ViewHolder {
        TextView messageText, timeText;
        Button confirmButton;
        CardView completedCard;

        OrderCompletedMessageHolder(View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.completedText);
            timeText = itemView.findViewById(R.id.completedTime);
            confirmButton = itemView.findViewById(R.id.confirmButton);
            completedCard = itemView.findViewById(R.id.completedCard);
        }

        void bind(Message message, String time) {
            messageText.setText(message.getText());
            timeText.setText(time);

            // Set card color
            completedCard.setCardBackgroundColor(context.getResources().getColor(android.R.color.holo_green_light));

            // Check if this is a client and if they need to confirm completion
            if (message.getOrderId() != null) {
                DatabaseReference orderRef = FirebaseDatabase.getInstance().getReference("orders").child(message.getOrderId());
                orderRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            Order order = snapshot.getValue(Order.class);
                            if (order != null) {
                                boolean isClient = order.getClientId().equals(currentUserId);
                                boolean needsConfirmation = !order.isConfirmedByClient() && "Completed".equals(order.getStatus());

                                confirmButton.setVisibility(isClient && needsConfirmation ? View.VISIBLE : View.GONE);

                                confirmButton.setOnClickListener(v -> {
                                    if (orderStatusListener != null) {
                                        orderStatusListener.onOrderCompleted(message.getOrderId());
                                        confirmButton.setVisibility(View.GONE);
                                    }
                                });
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("OrderSystem", "Failed to load order: " + error.getMessage());
                    }
                });
            }
        }
    }
}
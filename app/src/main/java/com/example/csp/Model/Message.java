package com.example.csp.Model;

import com.google.firebase.database.DatabaseReference;

public class Message {
    private String messageId; // Unique ID for each message
    private String text;
    private String senderId;
    private long timestamp;
    private String type; // "text", "image", "video", "order", "order_created", "order_status", "order_completed"
    private String mediaUrl;
    private String orderId; // Reference to an order if this message is order-related

    // Required empty constructor for Firebase
    public Message() {}

    // Constructor for text messages
    public Message(String messageId, String text, String senderId, long timestamp) {
        this(messageId, text, senderId, timestamp, "text", null, null);
    }

    // Constructor for order-related messages (used in your code examples)
    public Message(String messageId, String text, String senderId, long timestamp, String type, String orderId) {
        this(messageId, text, senderId, timestamp, type, null, orderId);
    }

    // Constructor for messages with senderId, text, type, and orderId
    public Message(String messageId, String senderId, String text, String type, String orderId) {
        this.messageId = messageId;
        this.senderId = senderId;
        this.text = text;
        this.type = type;
        this.orderId = orderId;
        this.timestamp = System.currentTimeMillis();
    }

    // Main constructor with all fields
    public Message(String messageId, String text, String senderId, long timestamp, String type, String mediaUrl, String orderId) {
        this.messageId = messageId;
        this.text = text;
        this.senderId = senderId;
        this.timestamp = timestamp;
        this.type = type;
        this.mediaUrl = mediaUrl;
        this.orderId = orderId;
    }

    // Static factory methods for different message types
    public static Message createMediaMessage(String messageId, String text, String senderId, long timestamp, String type, String mediaUrl) {
        return new Message(messageId, text, senderId, timestamp, type, mediaUrl, null);
    }

    public static Message createOrderMessage(String messageId, String text, String senderId, long timestamp, String type, String orderId) {
        return new Message(messageId, text, senderId, timestamp, type, null, orderId);
    }

    // Getters and setters
    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getText() {
        return text != null ? text : "";
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getType() {
        return type != null ? type : "text";
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMediaUrl() {
        return mediaUrl != null ? mediaUrl : "";
    }

    public void setMediaUrl(String mediaUrl) {
        this.mediaUrl = mediaUrl;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    @Override
    public String toString() {
        return "Message{" +
                "messageId='" + messageId + '\'' +
                ", text='" + text + '\'' +
                ", senderId='" + senderId + '\'' +
                ", timestamp=" + timestamp +
                ", type='" + type + '\'' +
                ", mediaUrl='" + mediaUrl + '\'' +
                ", orderId='" + orderId + '\'' +
                '}';
    }
}
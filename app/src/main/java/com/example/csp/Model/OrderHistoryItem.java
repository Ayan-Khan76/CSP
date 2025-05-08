package com.example.csp.Model;

public class OrderHistoryItem {
    private String orderId;
    private String service;
    private String description;
    private int price;
    private long completionTimestamp;
    private String otherUserId;
    private String otherUserName;
    private boolean userFreelancer; // Whether the current user is the freelancer
    private String chatId;

    public OrderHistoryItem() {
        // Required empty constructor for Firebase
    }

    public OrderHistoryItem(String orderId, String service, String description, int price,
                            long completionTimestamp, String otherUserId, boolean userFreelancer, String chatId) {
        this.orderId = orderId;
        this.service = service;
        this.description = description;
        this.price = price;
        this.completionTimestamp = completionTimestamp;
        this.otherUserId = otherUserId;
        this.userFreelancer = userFreelancer;
        this.chatId = chatId;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public long getCompletionTimestamp() {
        return completionTimestamp;
    }

    public void setCompletionTimestamp(long completionTimestamp) {
        this.completionTimestamp = completionTimestamp;
    }

    public String getOtherUserId() {
        return otherUserId;
    }

    public void setOtherUserId(String otherUserId) {
        this.otherUserId = otherUserId;
    }

    public String getOtherUserName() {
        return otherUserName != null ? otherUserName : "Unknown User";
    }

    public void setOtherUserName(String otherUserName) {
        this.otherUserName = otherUserName;
    }

    public boolean isUserFreelancer() {
        return userFreelancer;
    }

    public void setUserFreelancer(boolean userFreelancer) {
        this.userFreelancer = userFreelancer;
    }

    public String getChatId() {
        return chatId;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }
}
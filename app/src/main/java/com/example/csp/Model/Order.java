package com.example.csp.Model;

public class Order {
    private String orderId;
    private String clientId;
    private String freelancerId;
    private String status;
    private String service;
    private int price;
    private String description;
    private String deadline;
    private long timestamp;
    private boolean confirmedByClient;
    private boolean confirmedByFreelancer;
    private long completedTimestamp;
    private String chatId;
    private String orderSenderId; // Added to track who sent the order

    // Default constructor required for Firebase
    public Order() {
    }

    public Order(String orderId, String clientId, String freelancerId, String status, String service, int price, long timestamp) {
        this.orderId = orderId;
        this.clientId = clientId;
        this.freelancerId = freelancerId;
        this.status = status;
        this.service = service;
        this.price = price;
        this.timestamp = timestamp;
        this.confirmedByClient = false;
        this.confirmedByFreelancer = false;
    }

    // Constructor for backward compatibility
    public Order(String orderId, String userId, String freelancerId, String clientId) {
        this.orderId = orderId;
        this.clientId = clientId;
        this.freelancerId = freelancerId;
        this.status = "Pending";
        this.service = "Unspecified Service";
        this.price = 0;
        this.timestamp = System.currentTimeMillis();
        this.confirmedByClient = false;
        this.confirmedByFreelancer = false;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getFreelancerId() {
        return freelancerId;
    }

    public void setFreelancerId(String freelancerId) {
        this.freelancerId = freelancerId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDeadline() {
        return deadline;
    }

    public void setDeadline(String deadline) {
        this.deadline = deadline;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isConfirmedByClient() {
        return confirmedByClient;
    }

    public void setConfirmedByClient(boolean confirmedByClient) {
        this.confirmedByClient = confirmedByClient;
    }

    public boolean isConfirmedByFreelancer() {
        return confirmedByFreelancer;
    }

    public void setConfirmedByFreelancer(boolean confirmedByFreelancer) {
        this.confirmedByFreelancer = confirmedByFreelancer;
    }

    public long getCompletedTimestamp() {
        return completedTimestamp;
    }

    public void setCompletedTimestamp(long completedTimestamp) {
        this.completedTimestamp = completedTimestamp;
    }

    public String getChatId() {
        return chatId;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

    public String getOrderSenderId() {
        return orderSenderId;
    }

    public void setOrderSenderId(String orderSenderId) {
        this.orderSenderId = orderSenderId;
    }
}
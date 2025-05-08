package com.example.csp.Model;

public class ChatItem {
    private String chatId;
    private String receiverId;
    private String name;
    private String lastMessage;
    private String profileImageUrl;

    public ChatItem(String chatId, String receiverId, String name, String lastMessage, String profileImageUrl) {
        this.chatId = chatId;
        this.receiverId = receiverId;
        this.name = name;
        this.lastMessage = lastMessage;
        this.profileImageUrl = profileImageUrl;
    }

    public String getChatId() {
        return chatId;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public String getName() {
        return name;
    }

    public String getLastMessage() {
        return lastMessage;
    }
    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }
}

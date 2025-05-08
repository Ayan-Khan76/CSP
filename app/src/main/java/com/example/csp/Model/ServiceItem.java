package com.example.csp.Model;

public class ServiceItem {
    private int imageResId;  // Resource ID for the image
    private String title;    // Headline
    private String description; // Description

    public ServiceItem(int imageResId, String title, String description) {
        this.imageResId = imageResId;
        this.title = title;
        this.description = description;
    }

    public int getImageResId() {
        return imageResId;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }
}

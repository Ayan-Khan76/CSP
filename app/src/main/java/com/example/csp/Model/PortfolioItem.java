package com.example.csp.Model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PortfolioItem {
    private String title;
    private Map<String, MediaItem> mediaItems; // Map to store numbered media items
    private String description;
    private String link;

    public static class MediaItem {
        private String uri;

        public MediaItem() {
            // Required empty constructor for Firebase
        }

        public String getUri() {
            return uri != null ? uri : "";
        }

        public void setUri(String uri) {
            this.uri = uri;
        }
    }

    public PortfolioItem() {
        // Required empty constructor for Firebase
    }

    public String getTitle() {
        return title != null ? title : "";
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Map<String, MediaItem> getMediaItems() {
        return mediaItems;
    }

    public void setMediaItems(Map<String, MediaItem> mediaItems) {
        this.mediaItems = mediaItems;
    }

    // Helper method to get all media URIs as a list
    public List<String> getMediaUris() {
        List<String> uris = new ArrayList<>();
        if (mediaItems != null) {
            for (MediaItem item : mediaItems.values()) {
                if (item != null && item.getUri() != null) {
                    uris.add(item.getUri());
                }
            }
        }
        return uris;
    }

    public String getDescription() {
        return description != null ? description : "";
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLink() {
        return link != null ? link : "";
    }

    public void setLink(String link) {
        this.link = link;
    }
}
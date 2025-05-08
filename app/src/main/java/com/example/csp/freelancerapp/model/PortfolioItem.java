package com.example.csp.freelancerapp.model;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.ArrayList;
import java.util.List;

public class PortfolioItem implements Parcelable {
    private String title;
    private String description;
    private String link;
    private List<MediaItem> mediaItems;

    public PortfolioItem(String title, List<MediaItem> mediaItems, String description, String link) {
        this.title = title;
        this.mediaItems = mediaItems;
        this.description = description;
        this.link = link;
    }

    protected PortfolioItem(Parcel in) {
        title = in.readString();
        description = in.readString();
        link = in.readString();
        mediaItems = new ArrayList<>();
        in.readTypedList(mediaItems, MediaItem.CREATOR);
    }

    public static final Creator<PortfolioItem> CREATOR = new Creator<PortfolioItem>() {
        @Override
        public PortfolioItem createFromParcel(Parcel in) {
            return new PortfolioItem(in);
        }

        @Override
        public PortfolioItem[] newArray(int size) {
            return new PortfolioItem[size];
        }
    };

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public List<MediaItem> getMediaItems() {
        return mediaItems;
    }

    public void setMediaItems(List<MediaItem> mediaItems) {
        this.mediaItems = mediaItems;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeString(description);
        dest.writeString(link);
        dest.writeTypedList(mediaItems);
    }
}
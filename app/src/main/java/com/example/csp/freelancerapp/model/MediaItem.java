package com.example.csp.freelancerapp.model;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

public class MediaItem implements Parcelable {
    private Uri uri;
    private boolean isVideo;

    public MediaItem(Uri uri, boolean isVideo) {
        this.uri = uri;
        this.isVideo = isVideo;
    }

    protected MediaItem(Parcel in) {
        uri = in.readParcelable(Uri.class.getClassLoader());
        isVideo = in.readByte() != 0;
    }

    public static final Creator<MediaItem> CREATOR = new Creator<MediaItem>() {
        @Override
        public MediaItem createFromParcel(Parcel in) {
            return new MediaItem(in);
        }

        @Override
        public MediaItem[] newArray(int size) {
            return new MediaItem[size];
        }
    };

    public Uri getUri() {
        return uri;
    }

    public void setUri(Uri uri) {
        this.uri = uri;
    }

    public boolean isVideo() {
        return isVideo;
    }

    public void setVideo(boolean video) {
        isVideo = video;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(uri, flags);
        dest.writeByte((byte) (isVideo ? 1 : 0));
    }
}
package com.example.csp.freelancerapp.adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.VideoView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.csp.R;
import com.example.csp.freelancerapp.model.MediaItem;
import java.util.List;

public class MediaAdapter extends RecyclerView.Adapter<MediaAdapter.MediaViewHolder> {
    private static final String TAG = "MediaAdapter";
    private final List<MediaItem> mediaItems;

    public MediaAdapter(List<MediaItem> mediaItems) {
        this.mediaItems = mediaItems;
        // Log the number of media items for debugging
        Log.d(TAG, "MediaAdapter created with " + (mediaItems != null ? mediaItems.size() : 0) + " items");
    }

    @NonNull
    @Override
    public MediaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.media_items, parent, false);
        return new MediaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MediaViewHolder holder, int position) {
        MediaItem item = mediaItems.get(position);

        // Log the media item being bound
        Log.d(TAG, "Binding media item at position " + position + ": " +
                (item.isVideo() ? "Video" : "Image") + " - " + item.getUri());

        if (item.isVideo()) {
            holder.videoView.setVisibility(View.VISIBLE);
            holder.imageView.setVisibility(View.GONE);
            holder.videoView.setVideoURI(item.getUri());
            holder.playButton.setVisibility(View.VISIBLE);

            // Reset video state
            holder.videoView.seekTo(0);
            holder.playButton.setImageResource(R.drawable.ic_play);
        } else {
            holder.videoView.setVisibility(View.GONE);
            holder.imageView.setVisibility(View.VISIBLE);
            holder.imageView.setImageURI(item.getUri());
            holder.playButton.setVisibility(View.GONE);
        }

        holder.deleteButton.setOnClickListener(v -> {
            int pos = holder.getAdapterPosition();
            if (pos != RecyclerView.NO_POSITION) {
                mediaItems.remove(pos);
                notifyItemRemoved(pos);
                notifyItemRangeChanged(pos, mediaItems.size()); // Update remaining items
            }
        });
    }

    @Override
    public int getItemCount() {
        return mediaItems.size();
    }

    static class MediaViewHolder extends RecyclerView.ViewHolder {
        final ImageView imageView;
        final VideoView videoView;
        final ImageButton playButton,deleteButton;

        MediaViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
            videoView = itemView.findViewById(R.id.videoView);
            playButton = itemView.findViewById(R.id.playButton);
            deleteButton = itemView.findViewById(R.id.deleteButton);

            playButton.setOnClickListener(v -> {
                if (videoView.isPlaying()) {
                    videoView.pause();
                    playButton.setImageResource(R.drawable.ic_play);
                } else {
                    videoView.start();
                    playButton.setImageResource(R.drawable.ic_pause);
                }
            });

            // Add completion listener to reset play button
            videoView.setOnCompletionListener(mp -> {
                playButton.setImageResource(R.drawable.ic_play);
                videoView.seekTo(0);
            });
        }
    }

    public void addItem(MediaItem item) {
        mediaItems.add(item);
        notifyItemInserted(mediaItems.size() - 1);
    }

    public void removeItem(int position) {
        if (position >= 0 && position < mediaItems.size()) {
            mediaItems.remove(position);
            notifyItemRemoved(position);
        }
    }
}
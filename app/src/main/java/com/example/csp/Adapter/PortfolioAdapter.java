package com.example.csp.Adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.csp.FullScreenMediaActivity;
import com.example.csp.Model.PortfolioItem;
import com.example.csp.R;

import java.util.List;

public class PortfolioAdapter extends RecyclerView.Adapter<PortfolioAdapter.ViewHolder> {
    private Context context;
    private List<PortfolioItem> portfolioItems;

    public PortfolioAdapter(Context context, List<PortfolioItem> portfolioItems) {
        this.context = context;
        this.portfolioItems = portfolioItems;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_portfolio, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PortfolioItem item = portfolioItems.get(position);

        // Set title
        holder.titleText.setText(item.getTitle());
        holder.titleText.setVisibility(TextUtils.isEmpty(item.getTitle()) ? View.GONE : View.VISIBLE);

        // Set description
        holder.descriptionText.setText(item.getDescription());
        holder.descriptionText.setVisibility(TextUtils.isEmpty(item.getDescription()) ? View.GONE : View.VISIBLE);

        // Set link
        if (!TextUtils.isEmpty(item.getLink())) {
            holder.linkText.setText(item.getLink());
            holder.linkText.setVisibility(View.VISIBLE);
            holder.linkText.setOnClickListener(v -> {
                try {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(item.getLink()));
                    context.startActivity(intent);
                } catch (Exception e) {
                    Toast.makeText(context, "Invalid URL", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            holder.linkText.setVisibility(View.GONE);
        }

        // Set up media items in ViewFlipper
        List<String> mediaUris = item.getMediaUris();
        if (!mediaUris.isEmpty()) {
            holder.mediaFlipper.removeAllViews();
            for (String uri : mediaUris) {
                ImageView imageView = new ImageView(context);
                imageView.setLayoutParams(new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT));
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

                Glide.with(context)
                        .load(uri)
                        .placeholder(R.drawable.baseline_person_24)
                        .error(R.drawable.baseline_person_24)
                        .into(imageView);

                holder.mediaFlipper.addView(imageView);
            }

            // Set up click listeners for navigation if there are multiple images
            if (mediaUris.size() > 1) {
                holder.mediaFlipper.setOnClickListener(v -> {
                    holder.mediaFlipper.showNext();
                });
            }

            // Set up long-press gesture for full-screen viewing
            holder.mediaFlipper.setOnLongClickListener(v -> {
                int displayedChild = holder.mediaFlipper.getDisplayedChild();
                String mediaUrl = mediaUris.get(displayedChild);
                String mediaType = mediaUrl.endsWith(".mp4") ? "video" : "image";

                Intent intent = new Intent(context, FullScreenMediaActivity.class);
                intent.putExtra("mediaUrl", mediaUrl);
                intent.putExtra("mediaType", mediaType);
                intent.putExtra("messageId", "portfolio"); // Dummy messageId for FreelancerProfileActivity
                context.startActivity(intent);
                return true;
            });

            holder.mediaFlipper.setVisibility(View.VISIBLE);
        } else {
            holder.mediaFlipper.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return portfolioItems.size();
    }

    public void updateItems(List<PortfolioItem> newItems) {
        this.portfolioItems.clear();
        this.portfolioItems.addAll(newItems);
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ViewFlipper mediaFlipper;
        TextView titleText;
        TextView descriptionText;
        TextView linkText;

        ViewHolder(View itemView) {
            super(itemView);
            mediaFlipper = itemView.findViewById(R.id.mediaFlipper);
            titleText = itemView.findViewById(R.id.portfolioTitle);
            descriptionText = itemView.findViewById(R.id.portfolioDescription);
            linkText = itemView.findViewById(R.id.portfolioLink);
        }
    }
}
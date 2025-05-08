package com.example.csp.freelancerapp.adapter;

import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.csp.R;
import com.example.csp.freelancerapp.model.PortfolioItem;

import java.util.List;

public class PortfolioAdapter extends RecyclerView.Adapter<PortfolioAdapter.ViewHolder> {

    private List<PortfolioItem> portfolioItems;
    private OnPortfolioDeleteListener deleteListener;

    // Constructor to initialize the adapter with a list of portfolio items
    public PortfolioAdapter(List<PortfolioItem> portfolioItems) {
        this.portfolioItems = portfolioItems;
    }

    // Set a listener for portfolio item deletion
    public void setOnPortfolioDeleteListener(OnPortfolioDeleteListener listener) {
        this.deleteListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the layout for each portfolio item
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_portfolio_form, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Get the portfolio item at the current position
        PortfolioItem item = portfolioItems.get(position);

        // Set the title and description of the portfolio item
        holder.titleText.setText(item.getTitle());
        holder.descriptionText.setText(item.getDescription());

        // Set up the media RecyclerView with the media items from the portfolio item
        MediaAdapter mediaAdapter = new MediaAdapter(item.getMediaItems());
        holder.mediaRecyclerView.setAdapter(mediaAdapter);
        holder.mediaRecyclerView.setLayoutManager(new LinearLayoutManager(holder.itemView.getContext(), LinearLayoutManager.HORIZONTAL, false));

        // Force the media RecyclerView to refresh
        mediaAdapter.notifyDataSetChanged();

        // Set delete button click listener
        holder.deleteButton.setOnClickListener(v -> {
            int adapterPosition = holder.getAdapterPosition();
            if (adapterPosition != RecyclerView.NO_POSITION) {
                removePortfolioItem(adapterPosition);
            }
        });

        // Handle link visibility and click
        if (item.getLink() != null && !item.getLink().isEmpty()) {
            holder.linkText.setVisibility(View.VISIBLE);
            holder.linkText.setText(item.getLink());
            holder.linkText.setOnClickListener(v -> {
                try {
                    String url = item.getLink();
                    // Ensure the URL has a valid scheme (http:// or https://)
                    if (!url.startsWith("http://") && !url.startsWith("https://")) {
                        url = "http://" + url;
                    }
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    holder.itemView.getContext().startActivity(intent);
                } catch (Exception e) {
                    // Handle malformed URL
                    Toast.makeText(holder.itemView.getContext(), "Invalid URL format", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            holder.linkText.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return portfolioItems.size();
    }

    /**
     * Updates the portfolio items list
     *
     * @param newItems New list of portfolio items
     */
    public void updateItems(List<PortfolioItem> newItems) {
        this.portfolioItems = newItems;
        notifyDataSetChanged();
    }

    /**
     * Adds a new portfolio item to the list
     *
     * @param item Portfolio item to add
     */
    public void addItem(PortfolioItem item) {
        portfolioItems.add(item);
        notifyItemInserted(portfolioItems.size() - 1);
    }

    /**
     * Removes a portfolio item from the list
     *
     * @param position Position of the item to remove
     */
    private void removePortfolioItem(int position) {
        if (position >= 0 && position < portfolioItems.size()) {
            portfolioItems.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, portfolioItems.size());

            // Notify the listener about the deletion
            if (deleteListener != null) {
                deleteListener.onDeletePortfolio(position);
            }
        }
    }

    /**
     * ViewHolder class for portfolio items
     */
    static class ViewHolder extends RecyclerView.ViewHolder {
        RecyclerView mediaRecyclerView;
        TextView titleText;
        TextView descriptionText;
        TextView linkText;
        ImageButton deleteButton;

        ViewHolder(View itemView) {
            super(itemView);
            mediaRecyclerView = itemView.findViewById(R.id.recyclerViewPortfolioMedia);
            titleText = itemView.findViewById(R.id.portfolioTitle);
            descriptionText = itemView.findViewById(R.id.portfolioDescription);
            linkText = itemView.findViewById(R.id.portfolioLink);
            deleteButton = itemView.findViewById(R.id.portfolioDeleteButton);
        }
    }

    /**
     * Interface for handling portfolio item deletion
     */
    public interface OnPortfolioDeleteListener {
        void onDeletePortfolio(int position);
    }
}
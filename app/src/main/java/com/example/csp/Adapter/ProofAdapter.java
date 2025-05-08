package com.example.csp.Adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.csp.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ProofAdapter extends RecyclerView.Adapter<ProofAdapter.ProofViewHolder> {

    private final Context context;
    private final List<ProofItem> proofItems;

    public ProofAdapter(Context context, Map<String, String> proofMap) {
        this.context = context;
        this.proofItems = new ArrayList<>();

        if (proofMap != null) {
            for (Map.Entry<String, String> entry : proofMap.entrySet()) {
                String url = entry.getValue();
                boolean isImage = url.contains("cloudinary.com") ||
                        url.endsWith(".jpg") ||
                        url.endsWith(".jpeg") ||
                        url.endsWith(".png") ||
                        url.endsWith(".gif");

                proofItems.add(new ProofItem(entry.getKey(), url, isImage));
            }
        }
    }

    @NonNull
    @Override
    public ProofViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_proof, parent, false);
        return new ProofViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProofViewHolder holder, int position) {
        ProofItem item = proofItems.get(position);

        if (item.isImage) {
            holder.proofImage.setVisibility(View.VISIBLE);
            holder.proofLink.setVisibility(View.GONE);

            Glide.with(context)
                    .load(item.url)
                    .placeholder(R.drawable.ic_image_placeholder)
                    .error(R.drawable.ic_image_error)
                    .into(holder.proofImage);

            holder.proofImage.setOnClickListener(v -> {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(item.url));
                context.startActivity(intent);
            });
        } else {
            holder.proofImage.setVisibility(View.GONE);
            holder.proofLink.setVisibility(View.VISIBLE);
            holder.proofLink.setText(item.url);
        }
    }

    @Override
    public int getItemCount() {
        return proofItems.size();
    }

    static class ProofViewHolder extends RecyclerView.ViewHolder {
        ImageView proofImage;
        TextView proofLink;

        ProofViewHolder(@NonNull View itemView) {
            super(itemView);
            proofImage = itemView.findViewById(R.id.proofImage);
            proofLink = itemView.findViewById(R.id.proofLink);
        }
    }

    static class ProofItem {
        String id;
        String url;
        boolean isImage;

        ProofItem(String id, String url, boolean isImage) {
            this.id = id;
            this.url = url;
            this.isImage = isImage;
        }
    }
}


package com.example.csp.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.csp.Model.ServiceItem;
import com.example.csp.R;

import java.util.List;

public class HorizontalAdapter extends RecyclerView.Adapter<HorizontalAdapter.HorizontalViewHolder> {

    private List<ServiceItem> serviceItemList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(ServiceItem item);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public HorizontalAdapter(List<ServiceItem> serviceItemList) {
        this.serviceItemList = serviceItemList;
    }

    @NonNull
    @Override
    public HorizontalViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_service, parent, false);
        return new HorizontalViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HorizontalViewHolder holder, int position) {
        ServiceItem serviceItem = serviceItemList.get(position);
        holder.serviceImage.setImageResource(serviceItem.getImageResId());
        holder.serviceTitle.setText(serviceItem.getTitle());
        holder.serviceDescription.setText(serviceItem.getDescription());

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(serviceItem);
            }
        });
    }

    @Override
    public int getItemCount() {
        return serviceItemList.size();
    }

    public static class HorizontalViewHolder extends RecyclerView.ViewHolder {
        ImageView serviceImage;
        TextView serviceTitle;
        TextView serviceDescription;

        public HorizontalViewHolder(View itemView) {
            super(itemView);
            serviceImage = itemView.findViewById(R.id.serviceImage);
            serviceTitle = itemView.findViewById(R.id.serviceTitle);
            serviceDescription = itemView.findViewById(R.id.serviceDescription);
        }
    }
}
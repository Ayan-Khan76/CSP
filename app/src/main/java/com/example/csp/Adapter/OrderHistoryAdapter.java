package com.example.csp.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.csp.Model.OrderHistoryItem;
import com.example.csp.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class OrderHistoryAdapter extends RecyclerView.Adapter<OrderHistoryAdapter.OrderHistoryViewHolder> {

    private final List<OrderHistoryItem> orderHistoryItems;
    private final OrderHistoryListener listener;

    public interface OrderHistoryListener {
        void onOrderClicked(OrderHistoryItem order);
    }

    public OrderHistoryAdapter(List<OrderHistoryItem> orderHistoryItems, OrderHistoryListener listener) {
        this.orderHistoryItems = orderHistoryItems;
        this.listener = listener;
    }

    @NonNull
    @Override
    public OrderHistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_order_history, parent, false);
        return new OrderHistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderHistoryViewHolder holder, int position) {
        OrderHistoryItem item = orderHistoryItems.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return orderHistoryItems.size();
    }

    class OrderHistoryViewHolder extends RecyclerView.ViewHolder {
        private final TextView serviceText;
        private final TextView priceText;
        private final TextView userTypeText;
        private final TextView otherUserText;
        private final CardView orderCard;

        public OrderHistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            serviceText = itemView.findViewById(R.id.serviceText);
            priceText = itemView.findViewById(R.id.priceText);
            userTypeText = itemView.findViewById(R.id.userTypeText);
            otherUserText = itemView.findViewById(R.id.otherUserText);
            orderCard = itemView.findViewById(R.id.orderCard);
        }

        void bind(OrderHistoryItem item) {
            serviceText.setText(item.getService());
            priceText.setText("₹" + item.getPrice());

            // Set user type text
            if (item.isUserFreelancer()) {
                userTypeText.setText("You worked for:");
            } else {
                userTypeText.setText("You hired:");
            }

            // Set other user name
            otherUserText.setText(item.getOtherUserName());

            // Set click listener
            orderCard.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onOrderClicked(item);
                }
            });
        }
    }
}
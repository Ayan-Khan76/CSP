package com.example.csp.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.csp.ChatActivity;
import com.example.csp.Model.ChatItem;
import com.example.csp.R;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {
    private List<ChatItem> chatItems;
    private Context context;

    public ChatAdapter(Context context, List<ChatItem> chatItems) {
        this.context = context;
        this.chatItems = chatItems;
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_chat, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        ChatItem item = chatItems.get(position);

        holder.textViewName.setText(item.getName()); // Chat partner's name
        holder.textViewLastMessage.setText(item.getLastMessage()); // Last message

        // 🔥 Load Profile Image Using Glide
        if (!item.getProfileImageUrl().isEmpty()) {
            Glide.with(context)
                    .load(item.getProfileImageUrl())
                    .placeholder(R.drawable.baseline_person_24) // Default profile image
                    .error(R.drawable.baseline_person_24) // In case URL is broken
                    .into(holder.imageViewProfile);
        } else {
            holder.imageViewProfile.setImageResource(R.drawable.baseline_person_24);
        }

        // 🔥 Open chat when clicked
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ChatActivity.class);
            intent.putExtra("chatId", item.getChatId());
            intent.putExtra("receiverId", item.getReceiverId());
            intent.putExtra("receiverName", item.getName());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return chatItems.size();
    }

    static class ChatViewHolder extends RecyclerView.ViewHolder {
        ImageView imageViewProfile;
        TextView textViewName, textViewLastMessage;

        ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewProfile = itemView.findViewById(R.id.imageViewProfile);
            textViewName = itemView.findViewById(R.id.textViewName);
            textViewLastMessage = itemView.findViewById(R.id.textViewLastMessage);
        }
    }
}

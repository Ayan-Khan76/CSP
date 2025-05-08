package com.example.csp.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.csp.Adapter.ChatAdapter;
import com.example.csp.Model.ChatItem;
import com.example.csp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ChatFragment extends Fragment {
    private RecyclerView recyclerView;
    private ChatAdapter chatAdapter;
    private List<ChatItem> chatItems;
    private DatabaseReference chatRef, usersRef;
    private String userId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewChats);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        chatItems = new ArrayList<>();
        chatAdapter = new ChatAdapter(getContext(), chatItems);
        recyclerView.setAdapter(chatAdapter);

        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        chatRef = FirebaseDatabase.getInstance().getReference("chats");
        usersRef = FirebaseDatabase.getInstance().getReference("users");

        loadChatList();

        return view;
    }

    private void loadChatList() {
        chatRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                chatItems.clear();

                for (DataSnapshot chatSnapshot : snapshot.getChildren()) {
                    String chatId = chatSnapshot.getKey();

                    if (chatId != null && chatId.contains(userId)) { // Check if user is part of this chat
                        String[] ids = chatId.split("_");
                        String receiverId = ids[0].equals(userId) ? ids[1] : ids[0]; // Get the other user

                        // Get name from "users"
                        usersRef.child(receiverId).child("name").get().addOnCompleteListener(task -> {
                            if (task.isSuccessful() && task.getResult().exists()) {
                                String receiverName = task.getResult().getValue(String.class);

                                // 🔥 Now fetch profileImageUrl from "freelancers"
                                DatabaseReference freelancerRef = FirebaseDatabase.getInstance().getReference("freelancers").child(receiverId);
                                freelancerRef.child("profileImageUrl").get().addOnCompleteListener(task2 -> {
                                    String profileImageUrl = "";
                                    if (task2.isSuccessful() && task2.getResult().exists()) {
                                        profileImageUrl = task2.getResult().getValue(String.class);
                                    }

                                    String lastMessage = "No messages yet";
                                    long lastTimestamp = 0;

                                    for (DataSnapshot messageSnapshot : chatSnapshot.child("messages").getChildren()) {
                                        long timestamp = messageSnapshot.child("timestamp").getValue(Long.class);
                                        if (timestamp > lastTimestamp) {
                                            lastTimestamp = timestamp;
                                            lastMessage = messageSnapshot.child("text").getValue(String.class);
                                        }
                                    }

                                    chatItems.add(new ChatItem(chatId, receiverId, receiverName, lastMessage, profileImageUrl));
                                    chatAdapter.notifyDataSetChanged();
                                });
                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

}

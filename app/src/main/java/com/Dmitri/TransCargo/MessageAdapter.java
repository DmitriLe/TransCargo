package com.Dmitri.TransCargo;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_SENT = 0;
    private static final int TYPE_RECEIVED = 1;

    private List<Message> messages;
    private String currentUserId;
    private Map<String, String> userEmails = new HashMap<>();
    private DatabaseReference usersRef;
    private DatabaseReference driversRef;

    public MessageAdapter(List<Message> messages, String currentUserId) {
        this.messages = messages;
        this.currentUserId = currentUserId;
        this.usersRef = FirebaseDatabase.getInstance().getReference("users");
        this.driversRef = FirebaseDatabase.getInstance().getReference("drivers");

        loadUserEmails();
        loadDriverEmails();
    }

    private void loadUserEmails() {
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    String userId = userSnapshot.getKey();
                    String email = userSnapshot.child("email").getValue(String.class);
                    if (email != null) {
                        userEmails.put(userId, email);
                    }
                }
                notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Обработка ошибки
            }
        });
    }

    private void loadDriverEmails() {
        driversRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot driverSnapshot : snapshot.getChildren()) {
                    String driverId = driverSnapshot.getKey();
                    String email = driverSnapshot.child("email").getValue(String.class);
                    if (email != null) {
                        userEmails.put(driverId, email);
                    }
                }
                notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Обработка ошибки
            }
        });
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        if (viewType == TYPE_SENT) {
            View view = inflater.inflate(R.layout.item_message_sent, parent, false);
            return new SentMessageHolder(view);
        } else {
            View view = inflater.inflate(R.layout.item_message_received, parent, false);
            return new ReceivedMessageHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message message = messages.get(position);

        if (holder.getItemViewType() == TYPE_SENT) {
            ((SentMessageHolder) holder).bind(message);
        } else {
            ((ReceivedMessageHolder) holder).bind(message);
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    @Override
    public int getItemViewType(int position) {
        return messages.get(position).getSenderId().equals(currentUserId) ? TYPE_SENT : TYPE_RECEIVED;
    }

    class SentMessageHolder extends RecyclerView.ViewHolder {
        TextView messageText;
        TextView timeText;
        TextView receiverInfo;

        SentMessageHolder(View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.message_text);
            timeText = itemView.findViewById(R.id.time_text);
            receiverInfo = itemView.findViewById(R.id.receiver_info);
        }

        void bind(Message message) {
            messageText.setText(message.getText());
            timeText.setText(formatTime(message.getTimestamp()));

            String receiverEmail = userEmails.get(message.getReceiverId());
            receiverInfo.setText("Кому: " + (receiverEmail != null ?
                    receiverEmail : "Водитель"));
        }
    }

    class ReceivedMessageHolder extends RecyclerView.ViewHolder {
        TextView messageText;
        TextView timeText;
        TextView senderInfo;

        ReceivedMessageHolder(View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.message_text);
            timeText = itemView.findViewById(R.id.time_text);
            senderInfo = itemView.findViewById(R.id.sender_info);
        }

        void bind(Message message) {
            messageText.setText(message.getText());
            timeText.setText(formatTime(message.getTimestamp()));

            String senderEmail = userEmails.get(message.getSenderId());
            senderInfo.setText("От: " + (senderEmail != null ?
                    senderEmail : "Клиент"));
        }
    }

    private static String formatTime(long timestamp) {
        return new SimpleDateFormat("HH:mm", Locale.getDefault())
                .format(new Date(timestamp));
    }
}
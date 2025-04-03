package com.Dmitri.TransCargo;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class Chat_driver extends AppCompatActivity {
    private static final int MAX_MESSAGE_LENGTH = 1000;
    private RecyclerView messagesRecyclerView;
    private EditText messageEditText;
    private Button sendButton;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private String currentUserId;
    private String selectedUserId;
    private String chatId;
    private List<Message> messagesList = new ArrayList<>();
    private MessageAdapter messageAdapter;
    private ValueEventListener messagesListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_chat_driver);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance("https://transcargo-573ce-default-rtdb.firebaseio.com/").getReference();
        currentUserId = mAuth.getCurrentUser().getUid();

        initViews();
        setupRecyclerView();
        loadChatData();
    }

    @SuppressLint("WrongViewCast")
    private void initViews() {
        messagesRecyclerView = findViewById(R.id.messagesRecyclerView);
        messageEditText = findViewById(R.id.messageEditText);
        sendButton = findViewById(R.id.sendButton);

        sendButton.setOnClickListener(v -> sendMessage());
    }

    private void setupRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        messagesRecyclerView.setLayoutManager(layoutManager);
        messageAdapter = new MessageAdapter(messagesList, currentUserId);
        messagesRecyclerView.setAdapter(messageAdapter);

        messageAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                scrollToBottom();
            }
        });
    }

    private void loadChatData() {
        // Аналогичная логика как в Chat.java
        Intent intent = getIntent();
        selectedUserId = intent.getStringExtra("selectedUserId");

        if (selectedUserId == null) {
            mDatabase.child("users").child(currentUserId).child("selected_user_id")
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                selectedUserId = dataSnapshot.getValue(String.class);
                                if (selectedUserId != null && !selectedUserId.isEmpty()) {
                                    initializeChat();
                                } else {
                                    Toast.makeText(Chat_driver.this, "No selected user", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Log.e("ChatDriver", "Error loading selected user", databaseError.toException());
                        }
                    });
        } else {
            initializeChat();
        }
    }

    private void initializeChat() {
        chatId = generateChatId(currentUserId, selectedUserId);
        setupMessagesListener();
    }

    private String generateChatId(String userId1, String userId2) {
        return userId1.compareTo(userId2) < 0 ?
                userId1 + "_" + userId2 :
                userId2 + "_" + userId1;
    }

    private void setupMessagesListener() {
        messagesListener = mDatabase.child("chats").child(chatId).child("messages")
                .orderByChild("timestamp")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        messagesList.clear();
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            Message message = snapshot.getValue(Message.class);
                            if (message != null) {
                                messagesList.add(message);
                            }
                        }
                        messageAdapter.notifyDataSetChanged();
                        scrollToBottom();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.e("ChatDriver", "Error loading messages", databaseError.toException());
                    }
                });
    }

    private void sendMessage() {
        String messageText = messageEditText.getText().toString().trim();
        if (!messageText.isEmpty()) {
            sendMessageToDatabase(messageText);
            messageEditText.setText("");
        }
    }

    private void sendMessageToDatabase(String messageText) {
        if (selectedUserId == null) {
            Toast.makeText(this, "Recipient not selected", Toast.LENGTH_SHORT).show();
            return;
        }

        String messageId = mDatabase.child("chats").child(chatId).child("messages").push().getKey();
        Message message = new Message(currentUserId, selectedUserId, messageText, System.currentTimeMillis());

        if (messageId != null) {
            mDatabase.child("chats").child(chatId).child("messages").child(messageId)
                    .setValue(message)
                    .addOnFailureListener(e -> {
                        Toast.makeText(Chat_driver.this, "Failed to send message", Toast.LENGTH_SHORT).show();
                        Log.e("ChatDriver", "Error sending message", e);
                    });
        }
    }

    private void scrollToBottom() {
        if (!messagesList.isEmpty()) {
            messagesRecyclerView.smoothScrollToPosition(messagesList.size() - 1);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (messagesListener != null) {
            mDatabase.child("chats").child(chatId).child("messages").removeEventListener(messagesListener);
        }
    }

    public void onClickProfile(View v) {
        Intent intent = new Intent(Chat_driver.this, Profile.class);
        startActivity(intent);
    }
}
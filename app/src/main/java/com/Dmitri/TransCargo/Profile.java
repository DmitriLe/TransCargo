package com.Dmitri.TransCargo;

import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.HashMap;
import java.util.Map;

public class Profile extends AppCompatActivity {
    private TextView tvEmail, tvAverageRating;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Инициализация Firebase
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance("https://transcargo-573ce-default-rtdb.firebaseio.com/").getReference();
        currentUserId = mAuth.getCurrentUser().getUid();

        // Инициализация UI элементов
        tvEmail = findViewById(R.id.textViewEmail);
        tvAverageRating = findViewById(R.id.tvAverageRating);

        // Загрузка данных пользователя
        loadUserData();
    }

    private void loadUserData() {
        // Проверяем, является ли пользователь водителем
        mDatabase.child("users").child("drivers").child(currentUserId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot driverSnapshot) {
                        if (driverSnapshot.exists()) {
                            // Это водитель - берем данные из узла drivers
                            displayDriverData(driverSnapshot);
                        } else {
                            // Это не водитель - ищем в основном узле users
                            mDatabase.child("users").child(currentUserId)
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot userSnapshot) {
                                            if (userSnapshot.exists()) {
                                                displayUserData(userSnapshot);
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {
                                            showError("Ошибка загрузки данных пользователя");
                                        }
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        showError("Ошибка проверки типа пользователя");
                    }
                });
    }

    private void displayDriverData(DataSnapshot snapshot) {
        String email = snapshot.child("email").getValue(String.class);
        Double rating = snapshot.child("rating").getValue(Double.class);

        if (email != null) {
            tvEmail.setText(email);
        } else {
            tvEmail.setText("Email не указан");
        }

        if (rating != null) {
            tvAverageRating.setText(String.format("Средний рейтинг: %.1f", rating));
        } else {
            tvAverageRating.setText("Средний рейтинг: нет оценок");
        }
    }

    private void displayUserData(DataSnapshot snapshot) {
        String email = snapshot.child("email").getValue(String.class);

        if (email != null) {
            tvEmail.setText(email);
        } else {
            tvEmail.setText("Email не указан");
        }

        // Для обычных пользователей рейтинг может не быть
        tvAverageRating.setText("Рейтинг: не доступен");
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    public void onClick(View v) {
        DatabaseReference chatsRef = mDatabase.child("chats");

        chatsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String chatPartnerId = null;
                long latestTimestamp = 0;

                // Ищем все чаты, где участвует текущий пользователь
                for (DataSnapshot chatSnapshot : dataSnapshot.getChildren()) {
                    String chatId = chatSnapshot.getKey();

                    // Проверяем, содержит ли ID чата ID текущего пользователя
                    if (chatId.contains(currentUserId)) {
                        // Разбиваем ID чата на части, чтобы получить ID второго участника
                        String[] userIds = chatId.split("_");
                        String otherUserId = userIds[0].equals(currentUserId) ? userIds[1] : userIds[0];

                        // Проверяем последнее сообщение в чате
                        DataSnapshot messagesSnapshot = chatSnapshot.child("messages");
                        if (messagesSnapshot.exists()) {
                            for (DataSnapshot messageSnapshot : messagesSnapshot.getChildren()) {
                                Long messageTimestamp = messageSnapshot.child("timestamp").getValue(Long.class);
                                if (messageTimestamp != null && messageTimestamp > latestTimestamp) {
                                    latestTimestamp = messageTimestamp;
                                    chatPartnerId = otherUserId;
                                }
                            }
                        }
                    }
                }

                if (chatPartnerId != null) {
                    // Переходим в чат с найденным пользователем
                    Intent intent = new Intent(Profile.this, Chat_driver.class);
                    intent.putExtra("selectedUserId", chatPartnerId);
                    startActivity(intent);
                } else {
                    Toast.makeText(Profile.this, "У вас нет активных чатов", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(Profile.this, "Ошибка при загрузке чатов", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void onClickExit(View v) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}
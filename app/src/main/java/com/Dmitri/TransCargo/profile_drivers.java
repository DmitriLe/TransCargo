package com.Dmitri.TransCargo;

import android.content.Intent;
import android.os.Bundle;
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

public class profile_drivers extends AppCompatActivity {
    private TextView tvEmail, tvAverageRating, tvFullName;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private RatingBar ratingBar;
    private Button rateButton;
    private String driverId;
    private String currentUserId;
    private String chatId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile_drivers);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance("https://transcargo-573ce-default-rtdb.firebaseio.com/").getReference();
        currentUserId = mAuth.getCurrentUser().getUid();

        ratingBar = findViewById(R.id.ratingBar);
        rateButton = findViewById(R.id.rateButton);
        tvEmail = findViewById(R.id.textViewEmail);
        tvAverageRating = findViewById(R.id.tvAverageRating);
        tvFullName = findViewById(R.id.textView26);

        // Получаем driverId из Intent или из Firebase
        driverId = getIntent().getStringExtra("DRIVER_ID");
        if (driverId == null) {
            getDriverIdFromFirebase();
        } else {
            initializeAfterDriverIdObtained();
        }
    }

    private void getDriverIdFromFirebase() {
        mDatabase.child("users").child(currentUserId).child("selected_user_id")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            driverId = dataSnapshot.getValue(String.class);
                            initializeAfterDriverIdObtained();
                        } else {
                            Toast.makeText(profile_drivers.this,
                                    "Водитель не выбран", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(profile_drivers.this,
                                "Ошибка загрузки данных водителя", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
    }

    private void initializeAfterDriverIdObtained() {
        if (driverId == null || driverId.isEmpty()) {
            Toast.makeText(this, "Ошибка: ID водителя не получен", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Генерируем chatId после получения driverId
        chatId = generateChatId(currentUserId, driverId);
        loadDriverData();

        rateButton.setOnClickListener(v -> {
            float rating = ratingBar.getRating();
            if (rating > 0) {
                saveRatingToFirebase(rating);
            } else {
                Toast.makeText(profile_drivers.this,
                        "Пожалуйста, выберите оценку", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String generateChatId(String userId1, String userId2) {
        return userId1.compareTo(userId2) < 0 ?
                userId1 + "_" + userId2 :
                userId2 + "_" + userId1;
    }

    private void loadDriverData() {
        mDatabase.child("users").child("drivers").child(driverId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            String email = dataSnapshot.child("email").getValue(String.class);
                            if (email != null) {
                                tvEmail.setText(email);
                            }

                            String fullName = dataSnapshot.child("name").getValue(String.class);
                            if (fullName != null) {
                                tvFullName.setText("ФИО: " + fullName);
                            }

                            Double currentRating = dataSnapshot.child("rating").getValue(Double.class);
                            if (currentRating != null) {
                                tvAverageRating.setText(String.format("Средний рейтинг: %.1f", currentRating));
                                ratingBar.setRating(currentRating.floatValue());
                            } else {
                                tvAverageRating.setText("Средний рейтинг: нет оценок");
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(profile_drivers.this,
                                "Ошибка загрузки данных водителя", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void saveRatingToFirebase(float rating) {
        mDatabase.child("users").child("drivers").child(driverId).child("rating")
                .setValue(rating)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(profile_drivers.this,
                                "Оценка успешно сохранена", Toast.LENGTH_SHORT).show();
                        deleteChat();
                    } else {
                        Toast.makeText(profile_drivers.this,
                                "Ошибка сохранения оценки", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void deleteChat() {
        if (chatId != null && !chatId.isEmpty()) {
            mDatabase.child("chats").child(chatId).removeValue()
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(profile_drivers.this,
                                "Заявка закрыта", Toast.LENGTH_SHORT).show();
                        navigateToClientProfile();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(profile_drivers.this,
                                "Ошибка при закрытии заявки", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void navigateToClientProfile() {
        Intent intent = new Intent(profile_drivers.this, Profile_Client.class);
        startActivity(intent);
        finish();
    }

    public void onClickProfile(View v) {
        navigateToClientProfile();
    }
}
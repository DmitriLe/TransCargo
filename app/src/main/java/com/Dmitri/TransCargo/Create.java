package com.Dmitri.TransCargo;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class Create extends AppCompatActivity {
    private TextView tvUserEmail;
    private EditText etUserDescription;
    private Button btnSaveProfile;

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        etUserDescription = findViewById(R.id.editTextData);
        btnSaveProfile = findViewById(R.id.btnSave);

        btnSaveProfile.setOnClickListener(v -> saveUserDescription());
    }

    private void saveUserDescription() {
        String userDescription = etUserDescription.getText().toString().trim();

        if (userDescription.isEmpty()) {
            Toast.makeText(this, "Введите описание!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Сохраняем описание в профиль пользователя
        Map<String, Object> userData = new HashMap<>();
        userData.put("email", currentUser.getEmail());
        userData.put("description", userDescription);

        mDatabase.child("users").child(currentUser.getUid()).updateChildren(userData)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Переходим к выбору водителя
                        Intent intent = new Intent(Create.this, Choice.class);
                        // Передаем описание заявки для будущего сообщения
                        intent.putExtra("order_description", userDescription);
                        startActivity(intent);
                        Toast.makeText(Create.this, "Данные внесены!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(Create.this, "Ошибка: " + task.getException(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void onClickOffer(View v) {
        // Переход на SecondActivity
        Intent intent = new Intent(Create.this, Profile_Client.class);
        startActivity(intent);
    }

    public void onClickProfile(View v) {
        // Переход на SecondActivity
        Intent intent = new Intent(Create.this, Profile_Client.class);
        startActivity(intent);
    }
}


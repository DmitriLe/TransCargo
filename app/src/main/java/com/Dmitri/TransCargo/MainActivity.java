package com.Dmitri.TransCargo;

import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {
    public TextInputEditText etEmail, etPassword; // Объявление переменных для полей ввода
    public String email, password;      // Переменные для хранения значений
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });



        mAuth = FirebaseAuth.getInstance();

        etEmail = findViewById(R.id.textInputLogin);       // R.id.etEmail — ID из XML
        etPassword = findViewById(R.id.textInputPassword); // R.id.etPassword — ID из XML
    }

    public void onClick(View v) {
        // Переход на SecondActivity
        Intent intent = new Intent(MainActivity.this, Registration.class);
        startActivity(intent);
    }

    public void onClickInput(View v) {
        email = etEmail.getText().toString().trim();
        password = etPassword.getText().toString().trim();

        // Проверка на пустые поля
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(MainActivity.this, "Заполните все поля", Toast.LENGTH_SHORT).show();
            return;
        }

        // Правильное сравнение строк через equals()
        if (email.equals("transcargo1@gmail.com") ||
                email.equals("transcargo2@gmail.com") ||
                email.equals("transcargo3@gmail.com") ||
                email.equals("transcargo4@gmail.com")) {

            signInUser(email, password, Profile.class); // Переход в Profile для транскарго
        } else {
            signInUser(email, password, Profile_Client.class); // Переход в Profile_Client для остальных
        }
    }

    // Вынесенный метод для авторизации (убираем дублирование кода)
    private void signInUser(String email, String password, final Class<?> targetActivity) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Log.d("signup", "signInWithEmail:success");
                        Intent intent = new Intent(MainActivity.this, targetActivity);
                        startActivity(intent);
                    } else {
                        Log.w("signup", "signInWithEmail:failure", task.getException());
                        String errorMessage = task.getException() != null ?
                                task.getException().getMessage() : "Неизвестная ошибка";
                        Toast.makeText(MainActivity.this,
                                "Ошибка: " + errorMessage, Toast.LENGTH_LONG).show();
                    }
                });
    }
}
package com.Dmitri.TransCargo;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Profile_Client extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile_client);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        String userEmail = currentUser.getEmail();
        if (userEmail != null) {
            // Выводим в Logcat
            //Log.d("UserInfo", "User email: " + userEmail);

            // Устанавливаем в TextView (например, в активити)
            TextView textViewUserEmail = findViewById(R.id.textViewEmail);
            textViewUserEmail.setText(userEmail);
        }
    }

    public void onClickProfile(View v) {
        // Переход на SecondActivity
        Intent intent = new Intent(Profile_Client.this, Profile_Client.class);
        startActivity(intent);
    }

    public void onClickSMS(View v) {
        // Переход на SecondActivity
        Intent intent = new Intent(Profile_Client.this, Chat.class);
        startActivity(intent);
    }

    public void onClickExit(View v) {
        // Переход на SecondActivity
        Intent intent = new Intent(Profile_Client.this, MainActivity.class);
        startActivity(intent);
    }

    public void onClickCreate(View v) {
        // Переход на SecondActivity
        //Intent intent = new Intent(Profile_Client.this, Create.class);
        Intent intent = new Intent(Profile_Client.this, Create.class);
        startActivity(intent);
    }
}
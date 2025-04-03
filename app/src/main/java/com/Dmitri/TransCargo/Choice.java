package com.Dmitri.TransCargo;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Choice extends AppCompatActivity {
    private FirebaseDatabase database;
    private DatabaseReference driversRef;
    private ListView listView;
    private ArrayAdapter<User> adapter;

    private String orderDescription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_choice);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        orderDescription = getIntent().getStringExtra("order_description");


        listView = findViewById(R.id.listView);
        database = FirebaseDatabase.getInstance();
        // Исправленный путь к данным водителей
        driversRef = database.getReference("users").child("drivers");

        readDriversFromFirebase();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                User selectedDriver = adapter.getItem(position);
                if (selectedDriver != null) {
                    String selectedDriverId = selectedDriver.getId();
                    FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                    if (currentUser != null) {
                        updateCurrentUsersProfile(currentUser.getUid(), selectedDriverId);
                    }
                }
            }
        });
    }

    private void updateCurrentUsersProfile(String currentUserId, String selectedDriverId) {
        DatabaseReference currentUserRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(currentUserId);

        // Создаем обновления для профиля пользователя
        Map<String, Object> updates = new HashMap<>();
        updates.put("selected_user_id", selectedDriverId); // ID выбранного водителя
        updates.put("description", orderDescription); // Текст заявки

        currentUserRef.updateChildren(updates)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // После успешного обновления создаем чат
                        createInitialChatMessage(currentUserId, selectedDriverId);
                    } else {
                        // Обработка ошибки
                    }
                });
    }

    private void createInitialChatMessage(String senderId, String receiverId) {
        DatabaseReference chatsRef = FirebaseDatabase.getInstance().getReference("chats");

        // Генерируем уникальный ID чата (комбинация ID пользователей)
        String chatId = generateChatId(senderId, receiverId);

        // Создаем уникальный ID для сообщения
        String messageId = chatsRef.child(chatId).child("messages").push().getKey();

        // Создаем объект сообщения
        Message initialMessage = new Message(
                senderId,       // ID отправителя (клиента)
                receiverId,     // ID получателя (водителя)
                orderDescription, // Текст заявки
                System.currentTimeMillis() // Текущее время
        );

        // Сохраняем сообщение в Firebase
        chatsRef.child(chatId).child("messages").child(messageId)
                .setValue(initialMessage)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Переходим в чат
                        Intent intent = new Intent(Choice.this, Chat.class);
                        startActivity(intent);
                    } else {
                        // Обработка ошибки
                    }
                });
    }

    private String generateChatId(String userId1, String userId2) {
        // Сортируем ID, чтобы всегда получать одинаковый chatId для пары пользователей
        return userId1.compareTo(userId2) < 0 ?
                userId1 + "_" + userId2 :
                userId2 + "_" + userId1;
    }

    private void readDriversFromFirebase() {
        driversRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<User> drivers = new ArrayList<>();

                for (DataSnapshot driverSnapshot : snapshot.getChildren()) {
                    // Получаем данные каждого водителя
                    String id = driverSnapshot.getKey(); // ID водителя
                    String name = driverSnapshot.child("name").getValue(String.class);
                    String car = driverSnapshot.child("car").getValue(String.class);
                    Double rating = driverSnapshot.child("rating").getValue(Double.class);

                    if (id != null && name != null) {
                        User driver = new User();
                        driver.setId(id);
                        driver.setName(name);
                        driver.setCar(car != null ? car : "Не указано");
                        driver.setRating(rating != null ? rating : 0.0);
                        drivers.add(driver);
                    }
                }

                updateAdapter(drivers);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w("Firebase", "Error getting drivers data", error.toException());
                Toast.makeText(Choice.this, "Ошибка загрузки данных водителей", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateAdapter(ArrayList<User> drivers) {
        if (adapter == null) {
            adapter = new ArrayAdapter<User>(this, android.R.layout.simple_list_item_1, drivers) {
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    View view = super.getView(position, convertView, parent);
                    TextView textView = (TextView) view;

                    textView.setTextColor(Color.WHITE);
                    textView.setTextSize(20);

                    User driver = getItem(position);
                    String text = "Имя: " + driver.getName() +
                            ", Рейтинг: " + String.format("%.1f", driver.getRating()) +
                            ", Машина: " + driver.getCar();
                    textView.setText(text);

                    return view;
                }
            };
            listView.setAdapter(adapter);
        } else {
            adapter.clear();
            adapter.addAll(drivers);
            adapter.notifyDataSetChanged();
        }
    }
}
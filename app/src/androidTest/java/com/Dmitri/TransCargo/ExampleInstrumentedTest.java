package com.Dmitri.TransCargo;

import android.content.Context;
import android.util.Log;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private Context appContext;
    private CountDownLatch latch;

    @Before
    public void setUp() throws InterruptedException {
        // Инициализация Firebase Auth + эмулятор
        auth = FirebaseAuth.getInstance();
        auth.useEmulator("10.0.2.2", 9099);

        // Инициализация Firebase Database + эмулятор
        database = FirebaseDatabase.getInstance();
        database.useEmulator("10.0.2.2", 9000);
        database.setPersistenceEnabled(false); // Отключаем кэширование для тестов

        latch = new CountDownLatch(1);

        // Очистка предыдущего пользователя (если есть)
        auth.signOut();

        // Создаём тестового пользователя
        auth.createUserWithEmailAndPassword("testuser@example.com", "Password123!")
                .addOnCompleteListener(task -> latch.countDown());

        latch.await(5, TimeUnit.SECONDS);
    }

    @After
    public void tearDown() {
        if (auth.getCurrentUser() != null) {
            auth.getCurrentUser().delete();
        }
        if (database != null) {
            database.getReference("users").removeValue(); // Очищаем тестовые данные
        }
    }

    @Test
    public void testFirebaseRegistration() throws InterruptedException {
        String email = "test_" + System.currentTimeMillis() + "@example.com";
        String password = "Password123!";

        // Регистрация пользователя
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    assertTrue(task.isSuccessful());
                    assertNotNull(auth.getCurrentUser());
                    assertEquals(email, auth.getCurrentUser().getEmail());
                });

        // Ждём завершения асинхронной операции
        Thread.sleep(3000); // Простое решение для демонстрации. В реальности используйте CountDownLatch.
    }

    @Test
    public void testFirebaseLogin() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        String email = "testuser@example.com";
        String password = "Password123!";

        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.e("TEST_LOGIN", "Ошибка входа", task.getException());
                    }
                    assertTrue("Вход не удался: " + task.getException(), task.isSuccessful());
                    latch.countDown();
                });

        latch.await(5, TimeUnit.SECONDS);
    }

    @Test
    public void testUserDataSavedToDatabase() throws InterruptedException {
        CountDownLatch writeLatch = new CountDownLatch(1);
        CountDownLatch readLatch = new CountDownLatch(1);
        String userId = auth.getCurrentUser().getUid();
        String testName = "Test User";

        // 1. Записываем данные в базу
        database.getReference("users").child(userId).child("name").setValue(testName)
                .addOnCompleteListener(writeTask -> {
                    assertTrue("Данные не записались", writeTask.isSuccessful());
                    writeLatch.countDown();

                    // 2. Читаем данные после записи
                    database.getReference("users").child(userId).child("name").get()
                            .addOnCompleteListener(readTask -> {
                                assertTrue("Не удалось прочитать данные", readTask.isSuccessful());
                                assertEquals(testName, readTask.getResult().getValue(String.class));
                                readLatch.countDown();
                            });
                });

        // Ждём завершения обеих операций
        writeLatch.await(5, TimeUnit.SECONDS);
        readLatch.await(5, TimeUnit.SECONDS);
    }
}
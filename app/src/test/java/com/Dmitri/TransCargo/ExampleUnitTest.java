package com.Dmitri.TransCargo;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {

    @Test
    public void testEmailValidation() {
        assertTrue(AuthUtils.isValidEmail("test@example.com")); // Валидный email
        assertFalse(AuthUtils.isValidEmail("invalid.email"));   // Невалидный email
    }

    @Test
    public void testPasswordValidation() {
        assertTrue(AuthUtils.isValidPassword("Password123!")); // Валидный пароль
        assertFalse(AuthUtils.isValidPassword("short"));       // Невалидный пароль
    }

    @Test
    public void testRegistrationDataValidation() {
        // Тест на корректность данных перед отправкой в Firebase
        Users user = new Users("test@example.com", "Password123!", "John Doe");
        assertNotNull(user.getEmail());
        assertNotNull(user.getPassword());
        assertTrue(user.getPassword().length() >= 6);
    }
}
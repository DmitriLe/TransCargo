package com.Dmitri.TransCargo;

public class User {
    private String id;         // Уникальный ID пользователя из Firebase (ключ в базе данных)
    private String name;       // ФИО водителя
    private double rating;     // Рейтинг (должен быть double, так как в Firebase он может быть дробным)
    private String car;        // Марка машины

    private String email;
    private String password;

    // Конструкторы
    public User() {
        // Пустой конструктор необходим для Firebase
    }

    public User(String id, String name, double rating, String car) {
        this.id = id;
        this.name = name;
        this.rating = rating;
        this.car = car;
    }

    public User(String mail, String s, String johnDoe) {
    }

    // Геттеры и сеттеры
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public String getCar() {
        return car;
    }

    public void setCar(String car) {
        this.car = car;
    }
}
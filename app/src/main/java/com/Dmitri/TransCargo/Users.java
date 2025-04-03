package com.Dmitri.TransCargo;

public class Users {
    private String email;
    private String password;
    private String name;

    public Users() {
        // Пустой конструктор необходим для Firebase
    }

    public Users(String email, String password, String name) {
        this.email = email;
        this.password = password;
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }


}

package com.example.iot15.classes;

import java.io.Serializable;

public class User implements Serializable {
    private int id;
    private String userName;
    private String token;

    public User(int id, String userName, String token) {
        this.id = id;
        this.userName = userName;
        this.token = token;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}

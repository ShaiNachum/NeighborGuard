package com.example.neighborguard.model;


import java.io.Serializable;

public class User extends NewUser implements Serializable{
    private static final long serialVersionUID = 1L;
    private String uid;

    public User() {
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    @Override
    public String toString() {
        return "User{" +
                "uid='" + uid + '\'' +
                ", " + super.toString() +
                '}';
    }
}

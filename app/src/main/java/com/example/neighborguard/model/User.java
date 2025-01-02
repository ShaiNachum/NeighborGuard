package com.example.neighborguard.model;



public class User extends NewUser {
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

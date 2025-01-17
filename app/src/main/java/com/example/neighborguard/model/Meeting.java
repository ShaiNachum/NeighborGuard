package com.example.neighborguard.model;


import java.io.Serializable;

public class Meeting extends NewMeeting implements Serializable {
    private static final long serialVersionUID = 1L;
    private String uid;

    public Meeting() {
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    @Override
    public String toString() {
        return "Meeting{" +
                "uid='" + uid + '\'' +
                ", " + super.toString() +
                '}';
    }
}

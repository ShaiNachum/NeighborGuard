package com.example.neighborguard.model;



public class Meeting extends NewMeeting{
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

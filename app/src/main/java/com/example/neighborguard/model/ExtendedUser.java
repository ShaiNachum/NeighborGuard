package com.example.neighborguard.model;

import java.util.ArrayList;

public class ExtendedUser extends User{
    private ArrayList<Meeting> meetings;

    public ExtendedUser(){
    }

    public ArrayList<Meeting> getMeetings() {
        return meetings;
    }

    public void setMeetings(ArrayList<Meeting> meetings) {
        this.meetings = meetings;
    }

    @Override
    public String toString() {
        return "ExtendedUser{" +
                "meetings=" + meetings +
                '}';
    }
}

package com.example.neighborguard.model;

import java.util.ArrayList;

public class SearchMeetingsResponseSchema {
    private ArrayList<Meeting> meetings = new ArrayList<>();

    public SearchMeetingsResponseSchema() {
    }

    public ArrayList<Meeting> getMeetings() {
        return meetings;
    }

    public void setMeetings(ArrayList<Meeting> meetings) {
        this.meetings = meetings;
    }

    public Meeting getFirstMeeting() {
        if (meetings.isEmpty()) {
            return null;
        }
        return meetings.get(0);
    }

    @Override
    public String toString() {
        return "SearchMeetingsResponseSchema{" +
                "meetings=" + meetings +
                '}';
    }
}
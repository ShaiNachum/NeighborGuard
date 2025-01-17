package com.example.neighborguard.model;

import com.example.neighborguard.enums.MeetingStatusEnum;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;




public class NewMeeting implements Serializable {
    private static final long serialVersionUID = 1L;
    private User recipient;
    private User volunteer;
    private long date;
    private MeetingStatusEnum meetingStatus;
    private ArrayList<String> services;


    public NewMeeting() {
    }

    public User getRecipient() {
        return recipient;
    }

    public void setRecipient(User recipient) {
        this.recipient = recipient;
    }

    public User getVolunteer() {
        return volunteer;
    }

    public void setVolunteer(User volunteer) {
        this.volunteer = volunteer;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public MeetingStatusEnum getStatus() {
        return meetingStatus;
    }

    public void setStatus(MeetingStatusEnum status) {
        this.meetingStatus = status;
    }

    public ArrayList<String> getServices() {
        return services;
    }

    public void setServices(ArrayList<String> services) {
        this.services = services;
    }

    @Override
    public String toString() {
        return "Meeting{" +
                "recipient=" + recipient +
                ", volunteer=" + volunteer +
                ", date=" + date +
                ", meetingStatus=" + meetingStatus +
                ", services=" + services +
                '}';
    }
}

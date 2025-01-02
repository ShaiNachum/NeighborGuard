package com.example.neighborguard.model;

import com.example.neighborguard.enums.MeetingStatusEnum;

import java.util.Date;




public class NewMeeting {
    private User recipient;
    private User volunteer;
    private long date;
    private MeetingStatusEnum status;


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
        return status;
    }

    public void setStatus(MeetingStatusEnum status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Meeting{" +
                "recipient=" + recipient +
                ", volunteer=" + volunteer +
                ", date=" + date +
                ", status=" + status +
                '}';
    }
}

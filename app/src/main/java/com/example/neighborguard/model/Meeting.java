package com.example.neighborguard.model;

import java.util.Date;

public class Meeting {
    private User recipient;
    private User volunteer;
    private Date date;


    public Meeting() {
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

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    @Override
    public String toString() {
        return "Meeting{" +
                "recipient=" + recipient +
                ", volunteer=" + volunteer +
                ", date=" + date +
                '}';
    }
}

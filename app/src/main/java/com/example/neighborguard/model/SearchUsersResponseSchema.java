package com.example.neighborguard.model;

import java.util.ArrayList;

public class SearchUsersResponseSchema {
    private ArrayList<User> users = new ArrayList<>();

    public SearchUsersResponseSchema() {
    }

    public ArrayList<User> getUsers() {
        return users;
    }

    public void setUsers(ArrayList<User> users) {
        this.users = users;
    }

    public User getFirstUser() {
        if (users.isEmpty()) {
            return null;
        }
        return users.get(0);
    }

    @Override
    public String toString() {
        return "SearchUsersResponseSchema{" +
                "users=" + users +
                '}';
    }
}

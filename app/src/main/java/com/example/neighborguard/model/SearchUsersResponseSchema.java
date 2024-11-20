package com.example.neighborguard.model;

import java.util.ArrayList;

public class SearchUsersResponseSchema {
    private ArrayList<ExtendedUser> users = new ArrayList<>();

    public SearchUsersResponseSchema() {
    }

    public ArrayList<ExtendedUser> getUsers() {
        return users;
    }

    public void setUsers(ArrayList<ExtendedUser> users) {
        this.users = users;
    }

    public ExtendedUser getFirstExtendedUser() {
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

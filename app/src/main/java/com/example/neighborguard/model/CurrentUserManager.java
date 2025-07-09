package com.example.neighborguard.model;

public class CurrentUserManager {
    private static  CurrentUserManager instance;
    private User user;

    private  CurrentUserManager(){
    }

    public  static CurrentUserManager getInstance(){
        if (instance == null)
            instance = new CurrentUserManager();
        return instance;
    }


    public User getUser(){
        return user;
    }


    public void setUser(User user){
        this.user = user;
    }
}

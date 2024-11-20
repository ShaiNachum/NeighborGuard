package com.example.neighborguard.model;

public class CurrentUserManager {
    private static  CurrentUserManager instance;
    private ExtendedUser user;

    private  CurrentUserManager(){
    }

    public  static CurrentUserManager getInstance(){
        if (instance == null)
            instance = new CurrentUserManager();
        return instance;
    }

    public ExtendedUser getUser(){
        return user;
    }


    public void setUser(ExtendedUser user){
        this.user = user;
    }
}

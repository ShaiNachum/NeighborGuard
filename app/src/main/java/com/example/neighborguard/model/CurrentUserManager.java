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


    public void setExtendedUserFromExtendedUser(ExtendedUser user){
        this.user = user;
    }

    public void setExtendedUserFromUser(User user){
        ExtendedUser newUser = new ExtendedUser();
        newUser.setRole(user.getRole());
        newUser.setFirstName(user.getFirstName());
        newUser.setLastName(user.getLastName());
        newUser.setPhoneNumber(user.getPhoneNumber());
        newUser.setAge(user.getAge());
        newUser.setGender(user.getGender());
        newUser.setLanguages(user.getLanguages());
        newUser.setServices(user.getServices());
        newUser.setAddress(user.getAddress());
        newUser.setEmail(user.getEmail());
        newUser.setPassword(user.getPassword());
        newUser.setLonLat(user.getLonLat());
        newUser.setLastOK(user.getLastOK());
        newUser.setProfileImage(user.getProfileImage());
        newUser.setUid(user.getUid());
        newUser.setMeetings(this.user.getMeetings());
        this.user = newUser;
    }

    public User getUserFromExtendedUser(){
        User user = new User();
        user.setRole(this.user.getRole());
        user.setFirstName(this.user.getFirstName());
        user.setLastName(this.user.getLastName());
        user.setPhoneNumber(this.user.getPhoneNumber());
        user.setAge(this.user.getAge());
        user.setGender(this.user.getGender());
        user.setLanguages(this.user.getLanguages());
        user.setServices(this.user.getServices());
        user.setAddress(this.user.getAddress());
        user.setEmail(this.user.getEmail());
        user.setPassword(this.user.getPassword());
        user.setLonLat(this.user.getLonLat());
        user.setLastOK(this.user.getLastOK());
        user.setProfileImage(this.user.getProfileImage());
        user.setUid(this.user.getUid());
        return user;
    }
}

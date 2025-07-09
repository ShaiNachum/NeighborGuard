package com.example.neighborguard.model;

import com.example.neighborguard.enums.MeetingAssistanceStatusEnum;
import com.example.neighborguard.enums.UserGenderEnum;
import com.example.neighborguard.enums.UserRoleEnum;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;


public class NewUser implements Serializable {
    private static final long serialVersionUID = 1L;
    private UserRoleEnum role;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private int age;
    private UserGenderEnum gender;
    private ArrayList<String> languages;
    private HashMap<String, MeetingAssistanceStatusEnum> services;
    private Address address;
    private String email;
    private String password;
    private LonLat lonLat;
    private long lastOK;
    private String profileImage;


    public NewUser() {
    }

    public UserRoleEnum getRole() {
        return role;
    }

    public void setRole(UserRoleEnum role) {
        this.role = role;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public UserGenderEnum getGender() {
        return gender;
    }

    public void setGender(UserGenderEnum gender) {
        this.gender = gender;
    }

    public HashMap<String, MeetingAssistanceStatusEnum> getServices() {
        return services;
    }

    public void setServices(HashMap<String, MeetingAssistanceStatusEnum> services) {
        this.services = services;
    }

    public ArrayList<String> getLanguages() {
        return languages;
    }

    public void setLanguages(ArrayList<String> languages) {
        this.languages = languages;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public LonLat getLonLat() {
        return lonLat;
    }

    public void setLonLat(LonLat lonLat) {
        this.lonLat = lonLat;
    }

    public long getLastOK() {
        return lastOK;
    }

    public void setLastOK(long lastOK) {
        this.lastOK = lastOK;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }


    @Override
    public String toString() {
        return "NewUser{" +
                "role=" + role +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", age=" + age +
                ", gender=" + gender +
                ", languages=" + languages +
                ", services=" + services +
                ", address=" + address +
                ", email='" + email + '\'' +
                ", password='" + password + '\'' +
                ", lonLat=" + lonLat +
                ", lastOK=" + lastOK +
                ", profileImage='" + profileImage + '\'' +
                '}';
    }
}

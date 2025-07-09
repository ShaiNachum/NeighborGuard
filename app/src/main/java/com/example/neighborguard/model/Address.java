package com.example.neighborguard.model;

import java.io.Serializable;

public class Address implements Serializable {
    private static final long serialVersionUID = 1L;
    private String city;
    private String street;
    private int houseNumber;
    private int apartmentNumber = 0;
    private String addressString;


    public Address(){
    }

    public String makeAddressString(String city, String street, int houseNumber) {
        return String.format("%d %s, %s, Israel", houseNumber, street, city);
    }

    public String getAddressString(){
        return String.format("%d %s, %s, Israel", houseNumber, street, city);
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public int getHouseNumber() {
        return houseNumber;
    }

    public void setHouseNumber(int houseNumber) {
        this.houseNumber = houseNumber;
    }

    public int getApartmentNumber() {
        return apartmentNumber;
    }

    public void setApartmentNumber(int apartmentNumber) {
        this.apartmentNumber = apartmentNumber;
    }



    @Override
    public String toString() {
        return "Address{" +
                "city='" + city + '\'' +
                ", street='" + street + '\'' +
                ", houseNumber=" + houseNumber +
                ", apartmentNumber=" + apartmentNumber +
                '}';
    }
}

package com.example.neighborguard.model;

import java.io.Serializable;

public class LonLat implements Serializable {
    private static final long serialVersionUID = 1L;
    private double longitude = 0;
    private double latitude = 0;


    public LonLat() {
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    @Override
    public String toString() {
        return "LonLat{" +
                "longitude=" + longitude +
                ", latitude=" + latitude +
                '}';
    }
}

package com.example.onecall.models;

public class ModelData {
    public double latitude;
    public double longitude;
    public String username;

    // Optional: Constructor
    public ModelData(double latitude, double longitude, String username) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.username = username;
    }

    // Optional: Default constructor
    public ModelData() {
    }
}
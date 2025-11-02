package com.example.onecall.models;

public class HospitalData {
    public String name;
    public String address;
    public double lat;
    public double lng;

    public HospitalData(String name, String address, double lat, double lng) {
        this.name = name;
        this.address = address;
        this.lat = lat;
        this.lng = lng;
    }
}

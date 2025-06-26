package com.example.onecall.models;

public class UserDetails {
    private String userid;
    private String name;
    private String email;
    private String phone;
    private Double lon;
    private Double lat;

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Double getLon() {
        return lon;
    }

    public void setLon(Double lon) {
        this.lon = lon;
    }

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public UserDetails(String userid, String name, String email, String phone, Double lon, Double lat) {
        this.userid = userid;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.lon = lon;
        this.lat = lat;
    }
}

package com.example.reservationapp;

public class ModelPickUp {
    private double latitude, longitude;
    private String id, address, name;

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ModelPickUp(){

    }

    public ModelPickUp(String i, String n, double lat, double lng, String a){
        id = i;
        latitude = lat;
        longitude = lng;
        name = n;
        address = a;
    }
}

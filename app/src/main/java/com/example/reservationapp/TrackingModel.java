package com.example.reservationapp;

public class TrackingModel {
    private String id, address, altitude, speed, destination, status;
    private float accuracy;
    private double latitude, longitude;

    public TrackingModel() {
    }

    public TrackingModel(String id, String address, String altitude, String speed, String destination, String status, float accuracy, double latitude, double longitude) {
        this.id = id;
        this.address = address;
        this.altitude = altitude;
        this.speed = speed;
        this.destination = destination;
        this.accuracy = accuracy;
        this.latitude = latitude;
        this.longitude = longitude;
        this.status = status;
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

    public String getAltitude() {
        return altitude;
    }

    public void setAltitude(String altitude) {
        this.altitude = altitude;
    }

    public String getSpeed() {
        return speed;
    }

    public void setSpeed(String speed) {
        this.speed = speed;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public float getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(float accuracy) {
        this.accuracy = accuracy;
    }

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
}

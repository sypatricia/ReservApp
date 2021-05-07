package com.example.reservationapp;

public class ModelShuttleListItem {
    String driver;
    String status;
    String destination;

    public ModelShuttleListItem(String driver, String status, String destination) {
        this.driver = driver;
        this.status = status;
        this.destination = destination;
    }

    public String getDriver() {
        return driver;
    }

    public void setDriver(String driver) {
        this.driver = driver;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }
}

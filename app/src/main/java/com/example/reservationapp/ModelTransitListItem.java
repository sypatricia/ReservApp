package com.example.reservationapp;

public class ModelTransitListItem {
    String from;
    String to;
    String driver;

    public ModelTransitListItem(String from, String to, String driver) {
        this.from = from;
        this.to = to;
        this.driver = driver;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getDriver() {
        return driver;
    }

    public void setDriver(String driver) {
        this.driver = driver;
    }
}

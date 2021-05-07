package com.example.reservationapp;

public class ModelReservationListItem {
    String driver;
    String from;
    String destination;
    String sched;

    public ModelReservationListItem() {
    }

    public ModelReservationListItem(String driver, String from, String destination, String sched) {
        this.driver = driver;
        this.from = from;
        this.destination = destination;
        this.sched = sched;
    }

    public String getDriver() {
        return driver;
    }

    public void setDriver(String driver) {
        this.driver = driver;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getSched() {
        return sched;
    }

    public void setSched(String sched) {
        this.sched = sched;
    }
}

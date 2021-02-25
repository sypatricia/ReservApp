package com.example.reservationapp;

public class ModelTransit {
    private String id;
    private String driver;
    private String sched;
    private String from;
    private String to;

    public ModelTransit(String id, String driver, String sched, String from, String to) {
        this.id = id;
        this.driver = driver;
        this.sched = sched;
        this.from = from;
        this.to = to;
    }

    public ModelTransit(){}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDriver() {
        return driver;
    }

    public void setDriver(String driver) {
        this.driver = driver;
    }

    public String getSched() {
        return sched;
    }

    public void setSched(String sched) {
        this.sched = sched;
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
}

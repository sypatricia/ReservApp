package com.example.reservationapp;

public class ScheduleModel {
    private int hour;
    private int minute;
    private String id;

    public ScheduleModel() {
    }

    public ScheduleModel(String id, int hour, int minute) {
        this.hour = hour;
        this.minute = minute;
        this.id = id;
    }

    public int getHour() {
        return hour;
    }

    public void setHour(int hour) {
        this.hour = hour;
    }

    public int getMinute() {
        return minute;
    }

    public void setMinute(int minute) {
        this.minute = minute;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}

package com.example.reservationapp;

public class ModelDriver {
    private String id, firstName, lastName;
    private int capacity;

    public ModelDriver() { }

    public ModelDriver(String id, String firstName, String lastName, int capacity) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.capacity = capacity;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }
}

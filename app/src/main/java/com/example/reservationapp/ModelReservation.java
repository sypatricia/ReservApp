package com.example.reservationapp;

public class ModelReservation {
    String transitId;
    String driverId;
    String schedId;
    String fromId;
    String destinationId;

    public ModelReservation() { }

    public ModelReservation(String transitId, String driverId, String schedId, String fromId, String destinationId) {
        this.transitId = transitId;
        this.driverId = driverId;
        this.schedId = schedId;
        this.fromId = fromId;
        this.destinationId = destinationId;
    }

    public String getTransitId() {
        return transitId;
    }

    public void setTransitId(String transitId) {
        this.transitId = transitId;
    }

    public String getDriverId() {
        return driverId;
    }

    public void setDriverId(String driverId) {
        this.driverId = driverId;
    }

    public String getSchedId() {
        return schedId;
    }

    public void setSchedId(String schedId) {
        this.schedId = schedId;
    }

    public String getFromId() {
        return fromId;
    }

    public void setFromId(String fromId) {
        this.fromId = fromId;
    }

    public String getDestinationId() {
        return destinationId;
    }

    public void setDestinationId(String destinationId) {
        this.destinationId = destinationId;
    }
}

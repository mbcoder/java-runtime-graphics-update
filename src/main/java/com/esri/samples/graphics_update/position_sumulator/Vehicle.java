package com.esri.samples.graphics_update.position_sumulator;

public class Vehicle {

    private String vehicleID;
    private int routeID;
    private int positionAlongRoute;
    private STATUS status;

    public enum STATUS {
        AVAILABLE,
        OFF_DUTY,
        ON_ROUTE,
        ATTENDING_CALL
    };

    public Vehicle(String vehicleID, int routeID, int positionAlongRoute, STATUS status) {
        this.vehicleID = vehicleID;
        this.routeID = routeID;
        this.positionAlongRoute = positionAlongRoute;
        this.status = status;
    }

    public String getVehicleID() {
        return vehicleID;
    }

    public int getRouteID() {
        return routeID;
    }

    public int getPositionAlongRoute() {
        return positionAlongRoute;
    }

    public void setPositionAlongRoute(int positionAlongRoute) {
        this.positionAlongRoute = positionAlongRoute;
    }

    public STATUS getStatus() {
        return status;
    }

    public void setStatus(STATUS status) {
        this.status = status;
    }
}

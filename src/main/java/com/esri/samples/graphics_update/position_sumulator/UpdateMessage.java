package com.esri.samples.graphics_update.position_sumulator;

import com.esri.arcgisruntime.geometry.Point;

public class UpdateMessage {

    private String vehicleID;
    private Point position;
    private Vehicle.STATUS status;

    public UpdateMessage(String vehicleID, Point position, Vehicle.STATUS status) {
        this.vehicleID = vehicleID;
        this.position = position;
        this.status = status;
    }

    public String getVehicleID() {
        return vehicleID;
    }

    public Point getPosition() {
        return position;
    }

    public Vehicle.STATUS getStatus() {
        return status;
    }
}

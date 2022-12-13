/**
 * Copyright 2021 Esri
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package com.esri.samples.graphics_update.position_simulator;

import com.esri.arcgisruntime.geometry.Point;

/**
 * A class representing a vehicle update message.
 */
public class UpdateMessage {

    private String vehicleID;
    private Point position;
    private Vehicle.STATUS status;

    /**
     * Constructor for a new vehicle update message
     * @param vehicleID unique identifier for vehicle
     * @param position a point showing the new vehicle position
     * @param status the new vehicle status
     */
    public UpdateMessage(String vehicleID, Point position, Vehicle.STATUS status) {
        this.vehicleID = vehicleID;
        this.position = position;
        this.status = status;
    }

    /**
     * Returns the unique vehicle identifier
     * @return the vehicle identifier
     */
    public String getVehicleID() {
        return vehicleID;
    }

    /**
     * Returns the new vehicle position
     * @return new vehicle position point
     */
    public Point getPosition() {
        return position;
    }

    /**
     * Returns the vehicle status
     * @return the vehicle status
     */
    public Vehicle.STATUS getStatus() {
        return status;
    }
}

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

package com.esri.samples.graphics_update.position_sumulator;

/**
 * A class to represent the internal implementation of a vehicle
 */
public class Vehicle {

    private String vehicleID;
    private int routeID;
    private int positionAlongRoute;
    private STATUS status;
    public enum STATUS {
        AVAILABLE,
        OFF_DUTY,
        ON_ROUTE,
        ATTENDING_CALL};

    /**
     * Constructor for creating a new vehicle.
     *
     * @param vehicleID Unique identifier for the vehicle
     * @param routeID reference to the route the vehicle is following
     * @param positionAlongRoute reference to the current position along the route
     * @param status current vehicle status
     */
    public Vehicle(String vehicleID, int routeID, int positionAlongRoute, STATUS status) {
        this.vehicleID = vehicleID;
        this.routeID = routeID;
        this.positionAlongRoute = positionAlongRoute;
        this.status = status;
    }

    /**
     * Returns the unique identifier for the vehicle
     * @return the vehicle identifier
     */
    public String getVehicleID() {
        return vehicleID;
    }

    /**
     * Returns the identifier the vehicle is following
     * @return route identifier
     */
    public int getRouteID() {
        return routeID;
    }

    /**
     * Returns the position along the route which the vehicle is following
     * @return position along route
     */
    public int getPositionAlongRoute() {
        return positionAlongRoute;
    }

    /**
     * Updates the position along the route which the vehicle is following
     * @param positionAlongRoute new position along route
     */
    public void setPositionAlongRoute(int positionAlongRoute) {
        this.positionAlongRoute = positionAlongRoute;
    }

    /**
     * Returns the current vehicle status
     * @return vehicle status
     */
    public STATUS getStatus() {
        return status;
    }

    /**
     * Updates the current vehicle status
     * @param status vehicle status
     */
    public void setStatus(STATUS status) {
        this.status = status;
    }
}

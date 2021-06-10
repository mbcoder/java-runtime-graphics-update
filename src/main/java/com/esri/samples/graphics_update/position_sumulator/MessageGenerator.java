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

import com.esri.arcgisruntime.geometry.Point;
import java.io.*;
import java.util.*;

/**
 * A class for generating simulated vehicle update messages
 */
public class MessageGenerator {

    private HashMap<Integer, ArrayList<Point>> routes = new HashMap<>();
    private ArrayList<Vehicle> vehicles = new ArrayList();
    private UpdateMessageEventRunner updateMessageEventRunner = null;
    private Timer timer;

    /**
     * Constructor for a new vehicle message generator.
     * @param totalVehicles the number of vehicles messages will be generated for
     */
    public MessageGenerator(int totalVehicles) {
        Random random = new Random();

        // read in the route files
        ReadFiles();

        // create vehicles
        for (int vehicleID=1; vehicleID<=totalVehicles; vehicleID++ ) {

            //random route
            int routeID = random.nextInt(routes.size()) + 1;

            // random position along route
            int positionAlongRoute = random.nextInt(routes.get(routeID).size()) + 1;

            // random vehicle status
            Vehicle.STATUS status = Vehicle.STATUS.values()[random.nextInt(Vehicle.STATUS.values().length)];

            // create the new vehicle with random initial values
            Vehicle vehicle = new Vehicle("VEH" + vehicleID,routeID,positionAlongRoute, status);

            // add vehicle to list
            vehicles.add(vehicle);
        }
    }

    /**
     * Adds a listener for receiving vehicle update messages.  Only one listener is supported.
     * @param listener the listener
     */
    public void addUpdateMessageListener(UpdateMessageListener listener) {
        // singleton; only one listener
        if (updateMessageEventRunner == null) {
            updateMessageEventRunner = new UpdateMessageEventRunner(listener);
        }
    }

    /**
     * Method to start generating update messages for each vehicle
     */
    public void startMessages() {
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                if (updateMessageEventRunner != null) {
                    moveAllGraphics();
                }
            }
        };

        // timer to generate new messages on a separate thread every 20ms
        timer = new Timer();
        timer.schedule(timerTask,1000,20);
    }

    /**
     * Method to iterate through all vehicles and provide an update message with a new position
     */
    private void moveAllGraphics() {
        // loop through all the vehicles and increment the position along the route
        int newPosition;
        ArrayList<Point> routePoints;

        for (Vehicle vehicle : vehicles) {

            // new position along route
            newPosition = vehicle.getPositionAlongRoute() + 1;

            // get points for route
            routePoints = routes.get(vehicle.getRouteID());

            // check we've not reached end end of the route
            if (newPosition < routePoints.size()) {
                // update position to next point along route
                vehicle.setPositionAlongRoute(newPosition);
            } else {
                // return to start of route
                newPosition = 1;
                vehicle.setPositionAlongRoute(newPosition);
            }

            // construct an update message
            UpdateMessage updateMessage =
                    new UpdateMessage(vehicle.getVehicleID(), routePoints.get(newPosition), vehicle.getStatus());

            // send the message to the subscriber
            UpdateMessageEvent event = new UpdateMessageEvent(this, updateMessage);
            updateMessageEventRunner.run(event);
        }
    }

    /**
     * Method to stop vehicle update messages.  This method should be called when closing the JavaFX application
     */
    public void stopMessages() {
        timer.cancel();
    }

    /**
     * Method to read route information from CVS files contained in a data directory
     */
    private void ReadFiles() {
        int routeID = 1;

        // loop through all the route files
        File folder = new File("./data/");
        File[] listOfFiles = folder.listFiles();

        for (File file : listOfFiles) {
            ArrayList<Point> path = new ArrayList();
            String line = null;
            String csvItem;
            double xPos;
            double yPos;
            // open file
            try {
                BufferedReader reader = new BufferedReader(new FileReader(file));

                while (true) {
                    try {
                        if (!((line = reader.readLine()) != null)) break;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    StringTokenizer tokenizer = new StringTokenizer(line,",");

                    //read the x position
                    csvItem = tokenizer.nextToken();
                    xPos = Double.parseDouble(csvItem);

                    // read the y position
                    csvItem = tokenizer.nextToken();
                    yPos = Double.parseDouble(csvItem);

                    Point point = new Point(xPos, yPos);
                    path.add(point);
                }
                // having read all the points for the path, create the route
                routes.put(routeID++, path);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
}

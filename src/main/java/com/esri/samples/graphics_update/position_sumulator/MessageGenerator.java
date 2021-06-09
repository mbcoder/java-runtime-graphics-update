package com.esri.samples.graphics_update.position_sumulator;

import com.esri.arcgisruntime.geometry.Point;

import java.io.*;
import java.util.*;

public class MessageGenerator {

    private HashMap<Integer, ArrayList<Point>> routes = new HashMap<>();
    private ArrayList<Vehicle> vehicles = new ArrayList();
    private UpdateMessageEventRunner updateMessageEventRunner = null;
    private Timer timer;

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

    public void addUpdateMessageListener(UpdateMessageListener listener) {
        // singleton; only one listener
        if (updateMessageEventRunner == null) {
            updateMessageEventRunner = new UpdateMessageEventRunner(listener);
        }
    }

    public void startMessages() {
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                if (updateMessageEventRunner != null) {
                    moveAllGraphics();
                }
            }
        };

        timer = new Timer();
        timer.schedule(timerTask,1000,20);
    }

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

    public void stopMessages() {
        timer.cancel();
    }


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

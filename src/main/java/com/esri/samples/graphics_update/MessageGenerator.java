package com.esri.samples.graphics_update;

import com.esri.arcgisruntime.geometry.Point;

import java.io.*;
import java.util.*;

public class MessageGenerator {

    private HashMap<Integer, ArrayList<Point>> routes = new HashMap<>();
    private ArrayList<Vehicle> vehicles = new ArrayList();
    private UpdateMessageEventRunner updateMessageEventRunner = null;
    private Timer timer;


    public MessageGenerator(int vehices) {
        Random random = new Random();

        ReadFiles();

        System.out.println(" routes : " + routes.size());

        System.out.println("keys :" + routes.keySet());

        ArrayList<Point> path = routes.get(1);

        System.out.println("path 1" + path.size());

        // create vehicles
        for (int vehicleID=1; vehicleID<=vehices; vehicleID++ ) {

            //random route
            int routeID = random.nextInt(routes.size()) + 1;

            // random position along route
            int positionAlongRoute = random.nextInt(routes.get(routeID).size()) + 1;

            Vehicle vehicle = new Vehicle("VEH" + vehicleID,routeID,positionAlongRoute, Vehicle.STATUS.AVAILABLE);

            // add vehicle to list
            vehicles.add(vehicle);

            System.out.println("added " + vehicle.getVehicleID() + " on route " + routeID);
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

                    //UpdateMessage message = new UpdateMessage("AAA", null, null);
                    //UpdateMessageEvent event = new UpdateMessageEvent(this, message);
                    //updateMessageEventRunner.run(event);

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

            //System.out.print("old pos " + vehicle.getPositionAlongRoute());

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

            //System.out.println(" new pos " + vehicle.getPositionAlongRoute());

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
                    //System.out.println("point " + point.toString());
                }

                // having read all the points for the path, create the route
                routes.put(routeID++, path);

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

        }


    }
}

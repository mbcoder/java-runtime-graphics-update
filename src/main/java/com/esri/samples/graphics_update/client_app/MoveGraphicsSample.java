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

package com.esri.samples.graphics_update.client_app;
import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.view.Graphic;
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.symbology.*;
import com.esri.samples.graphics_update.position_sumulator.MessageGenerator;
import com.esri.samples.graphics_update.position_sumulator.UpdateMessage;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.File;
import java.util.*;
import java.util.concurrent.ExecutionException;

public class MoveGraphicsSample extends Application {
    private MapView mapView;
    private MessageGenerator messageGenerator;
    private GraphicsOverlay graphicsOverlay;
    private HashMap<String, Graphic> vehicles = new HashMap<>();

    public static void main(String[] args) {
        Application.launch(args);
    }

    @Override
    public void start(Stage stage) {

        // set the title and size of the stage and show it
        stage.setTitle("Vehicle position monitoring app");
        stage.setWidth(800);
        stage.setHeight(700);
        stage.show();

        // create a JavaFX scene with a stack pane as the root node and add it to the scene
        StackPane stackPane = new StackPane();
        Scene scene = new Scene(stackPane);
        stage.setScene(scene);

        // create a MapView to display the map and add it to the stack pane
        mapView = new MapView();
        stackPane.getChildren().add(mapView);

        // create an ArcGISMap with a basemap
        ArcGISMap map = new ArcGISMap(Basemap.createLightGrayCanvasVector());

        // graphics overlay for vehicles
        graphicsOverlay = new GraphicsOverlay();
        mapView.getGraphicsOverlays().add(graphicsOverlay);


        File stylxFile = new File(System.getProperty("data.dir"), "./resources/Vehicles.stylx");
        SymbolStyle vehicleStyle = new SymbolStyle(stylxFile.getAbsolutePath());
        vehicleStyle.loadAsync();
        vehicleStyle.addDoneLoadingListener(()-> {
            makeRenderer(vehicleStyle);
        });

        // display the map by setting the map on the map view
        mapView.setMap(map);

        // create the message simulator which generates vehicle position updates
        messageGenerator = new MessageGenerator(5000);

        // set up a listener for update messages
        messageGenerator.addUpdateMessageListener(listener -> {
            UpdateGraphic(listener.getUpdateMessage());
        });

        // start the messages from the simulator
        messageGenerator.startMessages();

        // set the initial viewpoint of the map view
        mapView.setViewpointCenterAsync(new Point(-286323, 7556611), 3000);
    }

    /**
     * Method to update a graphic from a vehicle update message.  If the message has come from a new vehicle
     * then a new graphic will be added.
     * @param updateMessage
     */
    private void UpdateGraphic(UpdateMessage updateMessage) {
        Point position = updateMessage.getPosition();

        // does graphic already exist?
        if (vehicles.containsKey(updateMessage.getVehicleID())) {
            //update the existing graphic with a new point geometry
            Graphic existingVehicle = vehicles.get(updateMessage.getVehicleID());
            existingVehicle.setGeometry(updateMessage.getPosition());

        } else {
            // create new graphic
            Graphic vehicleGraphic = new Graphic(position);
            vehicleGraphic.getAttributes().put("Status", updateMessage.getStatus().toString());
            graphicsOverlay.getGraphics().add(vehicleGraphic);

            // add vehicle graphic to hash map
            vehicles.put(updateMessage.getVehicleID(), vehicleGraphic);
        }
    }

    /**
     * Method to get symbols from style file and apply to renderer
     * @param vehicleStyle
     */

    private void makeRenderer(SymbolStyle vehicleStyle) {

        // create the unique value renderer
        UniqueValueRenderer uniqueValueRenderer = new UniqueValueRenderer();
        uniqueValueRenderer.getFieldNames().add("STATUS");

        // default symbol
        SimpleMarkerSymbol simpleMarkerSymbol = new SimpleMarkerSymbol(SimpleMarkerSymbol.Style.TRIANGLE, 0xFFFF0000,10);
        uniqueValueRenderer.setDefaultSymbol(simpleMarkerSymbol);

        // create a list of all of the potential symbol names that correspond to status values for the vehicles
        ArrayList<String> symbolNames = new ArrayList<>(Arrays.asList("Available", "Off duty", "On route", "Attending call"));

        // loop through the symbol names to create unique values for each status within the unique value renderer
        for (String symbolName : symbolNames) {
            ListenableFuture<Symbol> searchResult = vehicleStyle.getSymbolAsync(Collections.singletonList(symbolName));
            searchResult.addDoneListener(()-> {
                try {
                    Symbol symbol = searchResult.get();

                    switch (symbolName) {
                        case "Available" :
                            UniqueValueRenderer.UniqueValue uniqueAvailableValue =
                                    new UniqueValueRenderer.UniqueValue(symbolName, symbolName, symbol, Collections.singletonList("AVAILABLE"));
                            uniqueValueRenderer.getUniqueValues().add(uniqueAvailableValue);
                            break;
                        case "Off duty" :
                            UniqueValueRenderer.UniqueValue uniqueOffDutyValue =
                                    new UniqueValueRenderer.UniqueValue(symbolName, symbolName, symbol, Collections.singletonList("OFF_DUTY"));
                            uniqueValueRenderer.getUniqueValues().add(uniqueOffDutyValue);
                            break;
                        case "On route" :
                            UniqueValueRenderer.UniqueValue uniqueOnRouteValue =
                                    new UniqueValueRenderer.UniqueValue(symbolName, symbolName, symbol, Collections.singletonList("ON_ROUTE"));
                            uniqueValueRenderer.getUniqueValues().add(uniqueOnRouteValue);
                            break;
                        case "Attending call":
                            UniqueValueRenderer.UniqueValue uniqueAttendingCallValue =
                                    new UniqueValueRenderer.UniqueValue(symbolName, symbolName, symbol, Collections.singletonList("ATTENDING_CALL"));
                            uniqueValueRenderer.getUniqueValues().add(uniqueAttendingCallValue);
                            break;
                        default:
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            });

            // apply unique value renderer to graphics overlay
            graphicsOverlay.setRenderer(uniqueValueRenderer);
        }
    }

    /**
     * Stops and releases all resources used in application.
     */
    @Override
    public void stop() {
        if (mapView != null) {
            mapView.dispose();
        }
        messageGenerator.stopMessages();
    }
}

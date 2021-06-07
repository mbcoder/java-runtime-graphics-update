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

import com.esri.arcgisruntime.ArcGISRuntimeEnvironment;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.view.Graphic;
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.symbology.SimpleMarkerSymbol;
import com.esri.arcgisruntime.symbology.SimpleRenderer;
import com.esri.samples.graphics_update.position_sumulator.MessageGenerator;
import com.esri.samples.graphics_update.position_sumulator.UpdateMessage;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.util.HashMap;

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
        stage.setTitle("My Map App");
        stage.setWidth(800);
        stage.setHeight(700);
        stage.show();

        // create a JavaFX scene with a stack pane as the root node and add it to the scene
        StackPane stackPane = new StackPane();
        Scene scene = new Scene(stackPane);
        stage.setScene(scene);

        // Note: it is not best practice to store API keys in source code.
        // An API key is required to enable access to services, web maps, and web scenes hosted in ArcGIS Online.
        // If you haven't already, go to your developer dashboard to get your API key.
        // Please refer to https://developers.arcgis.com/java/get-started/ for more information
        String yourApiKey = "YOUR_API_KEY";
        ArcGISRuntimeEnvironment.setApiKey(yourApiKey);

        // create a MapView to display the map and add it to the stack pane
        mapView = new MapView();
        stackPane.getChildren().add(mapView);

        // create an ArcGISMap with an imagery basemap
        ArcGISMap map = new ArcGISMap(Basemap.createLightGrayCanvas());

        // graphics overlay for vehicles
        graphicsOverlay = new GraphicsOverlay();
        mapView.getGraphicsOverlays().add(graphicsOverlay);

        // set up and apply a renderer
        SimpleMarkerSymbol simpleMarkerSymbol = new SimpleMarkerSymbol(SimpleMarkerSymbol.Style.CIRCLE, 0xFFFF0000,10);
        SimpleRenderer renderer = new SimpleRenderer();
        renderer.setSymbol(simpleMarkerSymbol);
        graphicsOverlay.setRenderer(renderer);

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
    }

    private void UpdateGraphic(UpdateMessage updateMessage) {

        Point position = updateMessage.getPosition();

        // does graphic already exist?
        if (vehicles.containsKey(updateMessage.getVehicleID())) {
            //update the existing graphic
            Graphic existingVehicle = vehicles.get(updateMessage.getVehicleID());
            existingVehicle.setGeometry(updateMessage.getPosition());

        } else {
            // create new graphic
            Graphic vehicleGraphic = new Graphic(position);
            graphicsOverlay.getGraphics().add(vehicleGraphic);

            vehicles.put(updateMessage.getVehicleID(), vehicleGraphic);
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

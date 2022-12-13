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

import java.util.EventObject;

/**
 * Class for the update message event
 */
public final class UpdateMessageEvent extends EventObject {

    private final UpdateMessage updateMessage;

    /**
     * Constructs a prototypical Event.
     *
     * @param source the object on which the Event initially occurred
     * @param newUpdateMessage the update message containing vehicle position and status update
     * @throws IllegalArgumentException if source is null
     */
    public UpdateMessageEvent(Object source, UpdateMessage newUpdateMessage) {
        super(source);
        this.updateMessage = newUpdateMessage;
    }

    public UpdateMessage getUpdateMessage() {
        return updateMessage;
    }
}

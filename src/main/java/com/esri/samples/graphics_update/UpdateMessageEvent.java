package com.esri.samples.graphics_update;

import java.util.EventObject;

public final class UpdateMessageEvent extends EventObject {
    /**
     * Constructs a prototypical Event.
     *
     * @param source the object on which the Event initially occurred
     * @param newUpdateMessage the update message containing vehicle position and status update
     * @throws IllegalArgumentException if source is null
     */


    private final UpdateMessage updateMessage;

    public UpdateMessageEvent(Object source, UpdateMessage newUpdateMessage) {
        super(source);
        this.updateMessage = newUpdateMessage;
    }

    public UpdateMessage getUpdateMessage() {
        return updateMessage;
    }
}

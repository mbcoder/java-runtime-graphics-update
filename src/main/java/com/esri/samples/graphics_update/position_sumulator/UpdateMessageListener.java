package com.esri.samples.graphics_update.position_sumulator;

import java.util.EventListener;


public interface UpdateMessageListener extends EventListener {
    /**
     * Invoked when the <code>LoadStatus</code> of a <code>Loadable</code> resource changes.
     *
     * @param loadStatusChangedEvent provides the source of the event as well as the new load status
     * @since 100.0.0
     */
    public void updateMessage(UpdateMessageEvent loadStatusChangedEvent);
}


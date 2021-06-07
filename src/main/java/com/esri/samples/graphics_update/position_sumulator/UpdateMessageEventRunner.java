package com.esri.samples.graphics_update.position_sumulator;

public class UpdateMessageEventRunner {

    private UpdateMessageListener listener;

    public UpdateMessageEventRunner(UpdateMessageListener listener) {
        this.listener = listener;
    }

    public void run(UpdateMessageEvent messageEvent) {
        listener.updateMessage(messageEvent);
    }
}

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.chatapp.callbacks;

import com.chatapp.listeners.StatusListener;
import com.chatapp.status.StatusUpdate;
import io.grpc.stub.StreamObserver;
import java.util.ArrayList;
import java.util.logging.Logger;

/**
 *
 * @author Mariano
 */
public class StatusCallback implements StreamObserver<StatusUpdate> {

    private final ArrayList<StatusListener> statusListeners;

    public StatusCallback(ArrayList<StatusListener> statusListeners) {
        this.statusListeners = statusListeners;
    }

    @Override
    public void onNext(StatusUpdate statusUpdate) {
        switch (statusUpdate.getStatus()) {

            case ONLINE -> {
                System.out.println(statusUpdate.getUser().getUsername() + " is online.");
                for (StatusListener listener : statusListeners) {
                    listener.online(statusUpdate.getUser().getUsername());
                }
            }
            case OFFLINE -> {
                for (StatusListener listener : statusListeners) {
                    listener.offline(statusUpdate.getUser().getUsername());
                }
            }

            default ->
                Logger.getLogger(StatusCallback.class.getName()).info("Error occurred");
        }
    }

    @Override
    public void onError(Throwable error) {
        Logger.getLogger(StatusCallback.class.getName()).info(error.getCause().toString());
    }

    @Override
    public void onCompleted() {
        Logger.getLogger(StatusCallback.class.getName()).info("Stream Ended");
    }

}

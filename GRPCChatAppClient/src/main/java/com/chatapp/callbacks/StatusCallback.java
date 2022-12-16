package com.chatapp.callbacks;

import com.chatapp.listeners.StatusListener;
import com.chatapp.status.StatusUpdate;
import io.grpc.stub.StreamObserver;
import java.util.ArrayList;
import java.util.logging.Logger;

/**
 * Callback for gRPC Async <code>StatusUpdate</code> server stream.
 *
 * @author Mariano Cuneo
 */
public class StatusCallback implements StreamObserver<StatusUpdate> {

    private final ArrayList<StatusListener> statusListeners;

    /**
     * Class Constructor.
     *
     * @param statusListeners listeners to be updated on callbacks.
     */
    public StatusCallback(ArrayList<StatusListener> statusListeners) {
        this.statusListeners = statusListeners;
    }

    /**
     * Retrieves online/offline status updates and notifies listeners.
     *
     * @param statusUpdate server message containing online/offline status
     * updates.
     */
    @Override
    public void onNext(StatusUpdate statusUpdate) {
        switch (statusUpdate.getStatus()) {

            case ONLINE -> {
                System.out.println(statusUpdate.getUser().getUsername() + " is online.");
                for (StatusListener listener : statusListeners) {
                    listener.online(statusUpdate.getUser().getUserId());
                }
            }
            case OFFLINE -> {
                for (StatusListener listener : statusListeners) {
                    listener.offline(statusUpdate.getUser().getUserId());
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

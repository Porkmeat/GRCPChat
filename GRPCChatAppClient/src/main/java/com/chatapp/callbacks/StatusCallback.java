/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.chatapp.callbacks;

import com.chatapp.status.StatusUpdate;
import io.grpc.stub.StreamObserver;
import java.util.logging.Logger;

/**
 *
 * @author Mariano
 */
public class StatusCallback implements StreamObserver<StatusUpdate>{

    @Override
    public void onNext(StatusUpdate v) {
        // implement new message get
    }

    @Override
    public void onError(Throwable thrwbl) {
        Logger.getLogger(StatusCallback.class.getName()).info("Error occurred");
    }

    @Override
    public void onCompleted() {
        Logger.getLogger(StatusCallback.class.getName()).info("Stream Ended");
    }
    
}

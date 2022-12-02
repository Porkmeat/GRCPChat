/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.chatapp.callbacks;

import com.chatapp.common.ServiceResponse;
import io.grpc.stub.StreamObserver;
import java.util.logging.Logger;

/**
 * Callback for gRPC unary calls returning a <code>ServiceResponse</code>.
 *
 * @author Mariano Cuneo
 */
public class ServiceResponseCallback implements StreamObserver<ServiceResponse> {

    /**
     * Retrives and logs the result of the server call.
     *
     * @param value server message containing request results information.
     */
    @Override
    public void onNext(ServiceResponse value) {
        if (value.getResponseCode() == 1) {
            Logger.getLogger(ServiceResponseCallback.class.getName()).info("Request successful");
        } else {
            Logger.getLogger(ServiceResponseCallback.class.getName()).info(value.getResponse());
        }
    }

    @Override
    public void onError(Throwable error) {
        Logger.getLogger(ServiceResponseCallback.class.getName()).info(error.getCause().toString());
    }

    @Override
    public void onCompleted() {
        Logger.getLogger(ServiceResponseCallback.class.getName()).info("Stream completed");
    }
}

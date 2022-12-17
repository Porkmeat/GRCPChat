package com.chatapp.callbacks;

import com.chatapp.common.ResponseCode;
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
     * Retrieves and logs the result of the server call.
     *
     * @param value server message containing request results information.
     */
    @Override
    public void onNext(ServiceResponse value) {
        if (value.getResponseCode() == ResponseCode.SUCCESS) {
            Logger.getLogger(ServiceResponseCallback.class.getName()).info("Request successful");
        } else {
            Logger.getLogger(ServiceResponseCallback.class.getName()).info(value.getResponseCode().toString());
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

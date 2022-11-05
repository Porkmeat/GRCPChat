/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.chatapp.callbacks;


import com.chatapp.common.ServiceResponse;
import io.grpc.stub.StreamObserver;
import java.util.logging.Logger;


/**
 *
 * @author Mariano
 */
public class ServiceResponseCallback implements  StreamObserver<ServiceResponse>{

  @Override
  public void onNext(ServiceResponse value) {
      if (value.getResponseCode() == 1) {
          Logger.getLogger(ServiceResponseCallback.class.getName()).info("Message sent");
      } else {
          Logger.getLogger(ServiceResponseCallback.class.getName()).info("Error occurred");
      }
  }

  @Override
  public void onError(Throwable cause) {
    Logger.getLogger(ServiceResponseCallback.class.getName()).info("Error occurred");
  }

  @Override
  public void onCompleted() {
    Logger.getLogger(ServiceResponseCallback.class.getName()).info("Stream completed");
  }
}


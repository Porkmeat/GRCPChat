/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.chatapp.callbacks;

import com.chatapp.chat.MessageList;
import io.grpc.stub.StreamObserver;
import java.util.logging.Logger;

/**
 *
 * @author Mariano
 */
public class GetMessagesCallback implements StreamObserver<MessageList>{

    @Override
    public void onNext(MessageList v) {
        //implement call
    }

    @Override
    public void onError(Throwable thrwbl) {
        Logger.getLogger(GetMessagesCallback.class.getName()).info("Error occurred");
    }

    @Override
    public void onCompleted() {
        Logger.getLogger(GetMessagesCallback.class.getName()).info("Messages Loaded");
    }
    
}

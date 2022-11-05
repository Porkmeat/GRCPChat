/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.chatapp.callbacks;

import com.chatapp.chat.ChatMessage;
import io.grpc.stub.StreamObserver;
import java.util.logging.Logger;

/**
 *
 * @author Mariano
 */
public class NewMessageCallback implements StreamObserver<ChatMessage>{

    @Override
    public void onNext(ChatMessage v) {
        // implement new message get
    }

    @Override
    public void onError(Throwable thrwbl) {
        Logger.getLogger(NewMessageCallback.class.getName()).info("Error occurred");
    }

    @Override
    public void onCompleted() {
        Logger.getLogger(NewMessageCallback.class.getName()).info("Stream Ended");
    }
    
}

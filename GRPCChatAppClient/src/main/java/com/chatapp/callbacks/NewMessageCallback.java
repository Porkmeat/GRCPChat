/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.chatapp.callbacks;

import com.chatapp.chat.ChatMessage;
import com.chatapp.chatappgui.Chat;
import com.chatapp.grpcchatappclient.MessageListener;
import io.grpc.stub.StreamObserver;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.logging.Logger;

/**
 *
 * @author Mariano
 */
public class NewMessageCallback implements StreamObserver<ChatMessage> {

    private final ArrayList<MessageListener> messageListeners;

    public NewMessageCallback(ArrayList<MessageListener> messageListeners) {
        this.messageListeners = messageListeners;
    }

    @Override
    public void onNext(ChatMessage newChat) {
        Instant timestampUTC = Instant.parse(newChat.getTimestamp() + "Z");
        LocalDateTime messageLocalTime = timestampUTC.atZone(ZoneId.systemDefault()).toLocalDateTime();

        var chat = new Chat(newChat.getMessage(), newChat.getSeen(), messageLocalTime);
        
        for (MessageListener listener : messageListeners) {
            listener.messageGet(newChat.getSenderId(), chat);
        }
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

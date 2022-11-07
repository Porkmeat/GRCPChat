/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.chatapp.callbacks;

import com.chatapp.chat.MessageList;
import com.chatapp.chatappgui.Chat;
import com.chatapp.grpcchatappclient.MessageListener;
import io.grpc.stub.StreamObserver;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 *
 * @author Mariano
 */
public class GetMessagesCallback implements StreamObserver<MessageList> {
    
    private final ArrayList<MessageListener> messageListeners;

    public GetMessagesCallback(ArrayList<MessageListener> messageListeners) {
        this.messageListeners = messageListeners;
    }

    @Override
    public void onNext(MessageList messageList) {
        var list = messageList.getMessagesList();
        ObservableList<Chat> chatList = FXCollections.observableArrayList();
        LocalDateTime lastMessageTime = null;

        for (int i = 0; i < list.size()-1; i++) {
            var newChat = list.get(i);
            
            Instant timestampUTC = Instant.parse(newChat.getTimestamp() + "Z");
            LocalDateTime messageLocalTime = timestampUTC.atZone(ZoneId.systemDefault()).toLocalDateTime();

            if (i == 0 || !messageLocalTime.toLocalDate().equals(lastMessageTime.toLocalDate())) {
                chatList.add(new Chat(messageLocalTime));
            }

            lastMessageTime = messageLocalTime;

            var chat = new Chat(newChat.getMessage(), newChat.getSeen(), messageLocalTime);

            chatList.add(chat);
        }
        
        for (MessageListener listener : messageListeners) {
            listener.loadMessages(messageList.getFriendId(), chatList);
        }
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

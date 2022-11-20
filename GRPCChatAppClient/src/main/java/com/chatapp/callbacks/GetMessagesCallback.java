/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.chatapp.callbacks;

import com.chatapp.chat.MessageList;
import com.chatapp.dataobjects.Chat;
import com.chatapp.listeners.MessageListener;
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
        int senderId = messageList.getFriendId();
        var list = messageList.getMessagesList();
        ObservableList<Chat> chatList = FXCollections.observableArrayList();
        LocalDateTime lastMessageTime = null;

        if (list.isEmpty()) {
            chatList.add(new Chat(LocalDateTime.now()));
        } else {
            for (int i = 0; i < list.size(); i++) {
                var newChat = list.get(i);

                Instant timestampUTC = Instant.parse(newChat.getTimestamp() + "Z");
                LocalDateTime messageLocalTime = timestampUTC.atZone(ZoneId.systemDefault()).toLocalDateTime();

                if (i == 0 || !messageLocalTime.toLocalDate().equals(lastMessageTime.toLocalDate())) {
                    chatList.add(new Chat(messageLocalTime));
                }

                lastMessageTime = messageLocalTime;

                var chat = new Chat(newChat.getMessage(), newChat.getSenderId() != senderId, messageLocalTime);

                chatList.add(chat);
            }
        }

        for (MessageListener listener : messageListeners) {
            listener.loadMessages(senderId, chatList);
        }
    }

    @Override
    public void onError(Throwable error) {
        Logger.getLogger(GetMessagesCallback.class.getName()).info(error.getCause().toString());
    }

    @Override
    public void onCompleted() {
        Logger.getLogger(GetMessagesCallback.class.getName()).info("Messages Loaded");
    }

}

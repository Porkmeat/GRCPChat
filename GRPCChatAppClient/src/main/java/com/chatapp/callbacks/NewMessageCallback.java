package com.chatapp.callbacks;

import com.chatapp.chat.ChatMessage;
import com.chatapp.dataobjects.Chat;
import com.chatapp.listeners.MessageListener;
import io.grpc.stub.StreamObserver;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.logging.Logger;

/**
 * Callback for gRPC Async <code>ChatMessage</code> server stream.
 *
 * @author Mariano Cuneo
 */
public class NewMessageCallback implements StreamObserver<ChatMessage> {

    private final ArrayList<MessageListener> messageListeners;

    /**
     * Class Constructor.
     *
     * @param messageListeners listeners to be updated on callbacks.
     */
    public NewMessageCallback(ArrayList<MessageListener> messageListeners) {
        this.messageListeners = messageListeners;
    }

    /**
     * Handles incoming server messages, generates a <code>Chat</code> object
     * and notifies the listeners.
     *
     * @param newChat  <code>ChatMessage</code> message returned sent by server.
     */
    @Override
    public void onNext(ChatMessage newChat) {
        Instant timestampUTC = Instant.parse(newChat.getTimestamp());
        LocalDateTime messageLocalTime = timestampUTC.atZone(ZoneId.systemDefault()).toLocalDateTime();

        Chat chat = new Chat(newChat.getMessage(), false, messageLocalTime, newChat.getIsFile());

        for (MessageListener listener : messageListeners) {
            listener.messageGet(newChat.getSenderId(), chat);
        }
    }

    @Override
    public void onError(Throwable error) {
        Logger.getLogger(NewMessageCallback.class.getName()).info(error.getCause().toString());
    }

    @Override
    public void onCompleted() {
        Logger.getLogger(NewMessageCallback.class.getName()).info("Stream Ended");
    }

}

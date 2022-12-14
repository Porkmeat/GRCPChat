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
 * Callback for gRPC unary Async server call.
 *
 * @author Mariano Cuneo
 */
public class GetMessagesCallback implements StreamObserver<MessageList> {

    private final ArrayList<MessageListener> messageListeners;

    /**
     * Class Constructor.
     *
     * @param messageListeners listeners to be updated on callback.
     */
    public GetMessagesCallback(ArrayList<MessageListener> messageListeners) {
        this.messageListeners = messageListeners;
    }

    /**
     * Handles server response, generates <code>Chat</code> objects and notifies
     * the listeners.
     *
     * @param messageList message list returned by server.
     */
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

                Chat chat = new Chat(newChat.getMessage(), newChat.getSenderId() != senderId, messageLocalTime, newChat.getIsFile());

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

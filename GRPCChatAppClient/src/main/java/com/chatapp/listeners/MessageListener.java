package com.chatapp.listeners;

import com.chatapp.dataobjects.Chat;
import javafx.collections.ObservableList;

/**
 * Listener for chat messages sent by server.
 *
 * @author Mariano Cuneo
 */
public interface MessageListener {

    /**
     * Handles new single message.
     *
     * @param fromUser sender's user ID.
     * @param message chat object containing message data.
     */
    public void messageGet(int fromUser, Chat message);

    /**
     * Handles retrieval of all previous chat messages with a single friend.
     *
     * @param withUser friend's user ID.
     * @param messages list with all retrieved messages.
     */
    public void loadMessages(int withUser, ObservableList messages);
}

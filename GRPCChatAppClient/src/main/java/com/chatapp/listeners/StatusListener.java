package com.chatapp.listeners;

/**
 * Listener for online/offline status updates from server.
 *
 * @author Mariano Cuneo
 */
public interface StatusListener {

    /**
     * Updates friend status to online.
     *
     * @param userId user ID of friend to be updated.
     */
    public void online(int userId);

    /**
     * Updates friend status to offline.
     *
     * @param userId user ID of friend to be updated.
     */
    public void offline(int userId);
}

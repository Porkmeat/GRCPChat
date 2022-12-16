package com.chatapp.listeners;

import com.chatapp.dataobjects.Chat;

/**
 * Listener file upload attempts status updates.
 *
 * @author Mariano Cuneo
 */
public interface FileListener {

    /**
     * Updates success or failure status of file upload attempt.
     *
     * @param chat chat object displaying file upload status. Can be
     * <code>null</code>.
     * @param success status of file upload attempt.
     * @param fileName name of uploaded file.
     */
    public void fileSent(Chat chat, boolean success, String fileName);
}

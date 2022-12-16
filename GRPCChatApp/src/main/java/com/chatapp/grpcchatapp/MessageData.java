package com.chatapp.grpcchatapp;

/**
 * Object containing message data.
 *
 * @author Mariano Cuneo
 */
public class MessageData {

    private String message;
    private int senderId;
    private String timestamp;
    private boolean seen;
    private boolean isFile;

    /**
     * Class Constructor.
     *
     * @param message message text.
     * @param senderId message sender's user ID.
     * @param timestamp message time stamp.
     * @param seen boolean stating if message has been seen by receiver.
     * @param isFile boolean stating if message is file information.
     */
    public MessageData(String message, int senderId, String timestamp, boolean seen, boolean isFile) {
        this.message = message;
        this.senderId = senderId;
        this.timestamp = timestamp;
        this.seen = seen;
        this.isFile = isFile;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getSenderId() {
        return senderId;
    }

    public void setSenderId(int senderId) {
        this.senderId = senderId;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isSeen() {
        return seen;
    }

    public void setSeen(boolean seen) {
        this.seen = seen;
    }

    public boolean isFile() {
        return isFile;
    }

    public void setIsFile(boolean isFile) {
        this.isFile = isFile;
    }

}

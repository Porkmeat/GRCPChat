/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.chatapp.grpcchatapp;


/**
 *
 * @author Mariano
 */
public class MessageData {
    
    private String message;
    private int senderId;
    private String timestamp;
    private boolean seen;
    private boolean isFile;

    public MessageData(String message, int senderId, String timestamp, boolean seen, boolean isFile) {
        this.message = message;
        this.senderId = senderId;
        this.timestamp = timestamp;
        this.seen = seen;
        this.isFile = isFile;
    }
    
    public MessageData(String message, int senderId) {
        this.message = message;
        this.senderId = senderId;
        this.timestamp = "";
        this.seen = false;
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

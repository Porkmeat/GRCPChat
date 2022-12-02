/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.chatapp.dataobjects;

import java.time.LocalDateTime;

/**
 * Object containing message data.
 *
 * @author Mariano Cuneo
 */
public class Chat {

    private String message;
    private boolean userIsSender;
    private LocalDateTime timestamp;
    private boolean isFile;

    /**
     * Default Constructor.
     *
     * @param message message's text content. In the case of a file, file name,
     * extension and size.
     * @param userIsSender states if the message was sent by the current user.
     * @param timestamp date and time when the message was sent.
     * @param isFile states if the message is a file to be downloaded.
     */
    public Chat(String message, boolean userIsSender, LocalDateTime timestamp, boolean isFile) {
        this.message = message;
        this.userIsSender = userIsSender;
        this.timestamp = timestamp;
        this.isFile = isFile;
    }

    /**
     * Constructor for messages sent by user.
     *
     * @param message message's text content.
     * @param timestamp date and time when the message was sent.
     */
    public Chat(String message, LocalDateTime timestamp) {
        this.message = message;
        this.userIsSender = true;
        this.timestamp = timestamp;
        this.isFile = false;
    }

    /**
     * Constructor for timestamp only chat objects to be used as day separators.
     * This constructor is called automatically when adding chat objects to a
     * conversation if the date of the last object and the day of the new object
     * are different.
     *
     * @param timestamp date of the new message to be added.
     */
    public Chat(LocalDateTime timestamp) {
        this.message = "";
        this.userIsSender = true;
        this.timestamp = timestamp;
        this.isFile = false;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean userIsSender() {
        return userIsSender;
    }

    public void setUserIsSender(boolean userIsSender) {
        this.userIsSender = userIsSender;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isFile() {
        return isFile;
    }

    public void setIsFile(boolean isFile) {
        this.isFile = isFile;
    }

}

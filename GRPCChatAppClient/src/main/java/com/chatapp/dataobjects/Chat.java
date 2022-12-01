/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.chatapp.dataobjects;

import java.time.LocalDateTime;

/**
 *
 * @author julia
 */
public class Chat {
    
    private String message;
    private boolean userIsSender;
    private LocalDateTime timestamp;
    private boolean isFile;

    public Chat(String message, boolean userIsSender, LocalDateTime timestamp) {
        this.message = message;
        this.userIsSender = userIsSender;
        this.timestamp = timestamp;
        this.isFile = false;
    }
    
    public Chat(String message, boolean userIsSender, LocalDateTime timestamp, boolean isFile) {
        this.message = message;
        this.userIsSender = userIsSender;
        this.timestamp = timestamp;
        this.isFile = isFile;
    }
    
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

    public boolean isUserIsSender() {
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

    public boolean IsFile() {
        return isFile;
    }

    public void setIsFile(boolean isFile) {
        this.isFile = isFile;
    }
            
    
}

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.chatapp.chatappgui;

import java.time.LocalDateTime;

/**
 *
 * @author julia
 */
public class Chat {
    
    private String message;
    private boolean userIsSender;
    private LocalDateTime timestamp;

    public Chat(String message, boolean userIsSender, LocalDateTime timestamp) {
        this.message = message;
        this.userIsSender = userIsSender;
        this.timestamp = timestamp;
    }
    
    public Chat(LocalDateTime timestamp) {
        this.message = "";
        this.userIsSender = true;
        this.timestamp = timestamp;
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
            
    
}

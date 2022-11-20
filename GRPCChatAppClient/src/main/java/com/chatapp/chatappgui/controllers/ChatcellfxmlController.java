/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package com.chatapp.chatappgui.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;


/**
 * FXML Controller class
 *
 * @author Mariano
 */
public class ChatcellfxmlController {

    @FXML
    private Label chatMessage;
    
    @FXML
    private Label chatTimestamp;
    
    @FXML
    private AnchorPane chatBubble;

    
    public void setMessageText(String message) {
        chatMessage.setText(message);
    }
    
    public void setTimestampText(String message) {
        chatTimestamp.setText(message);
    }

    public AnchorPane getChatBubble() {
        return chatBubble;
    }
    
    public void setBubbleColor(String color) {
        chatBubble.setStyle(color + " -fx-background-radius: 7;");
    } 


}

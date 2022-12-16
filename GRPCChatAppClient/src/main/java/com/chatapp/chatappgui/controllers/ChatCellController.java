package com.chatapp.chatappgui.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;

/**
 * FXML Controller class for the custom <code>ChatListCell</code>.
 *
 * @author Mariano Cuneo
 */
public class ChatCellController {

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

    /**
     * Sets the background color of the <code>chatBubble</code> component.
     *
     * @param color color to be applied. The value can be RGB, hex or the
     * color's name.
     */
    public void setBubbleColor(String color) {
        chatBubble.setStyle(color + " -fx-background-radius: 7;");
    }

}

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package com.chatapp.chatappgui.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;

/**
 * FXML Controller class
 *
 * @author Mariano
 */
public class FriendsfxmlController {

    @FXML
    private Label usernamelabel;

    @FXML
    private Label timestamplabel;

    @FXML
    private Label lastmessagelabel;

    @FXML
    private Circle profilepicture;

    @FXML
    private StackPane unreadnotification;

    @FXML
    private Text unreadnumber;

    @FXML
    private AnchorPane friendcard;

    public void setUsername(String username) {
        usernamelabel.setText(username);
    }

    public void setLastMessage(String lastMessage) {
        lastmessagelabel.setText(lastMessage);
    }

    public void setTimestamp(String timestamp) {
        timestamplabel.setText(timestamp);
    }

    public void setOnlineStatus(boolean isOnline) {
        if (isOnline) {
            profilepicture.setEffect(new DropShadow(BlurType.GAUSSIAN, Color.GREEN, 10, 0, 0, 0));
        } else {
            profilepicture.setEffect(null);
        }
    }

    public void setProfilePicture(Image image) {

        double radius = profilepicture.getRadius();
        double hRad = radius;   // horizontal "radius"
        double vRad = radius;   // vertical "radius"
        if (image.getWidth() != image.getHeight()) {
            double ratio = image.getWidth() / image.getHeight();
            if (ratio > 1) {
                // Width is longer, left anchor is outside
                hRad = radius * ratio;
            } else {
                // Height is longer, top anchor is outside
                vRad = radius / ratio;
            }
        }
        profilepicture.setFill(new ImagePattern(image, -hRad, -vRad, 2 * hRad, 2 * vRad, false));
    }

    public AnchorPane getFriendcard() {
        return friendcard;
    }

}

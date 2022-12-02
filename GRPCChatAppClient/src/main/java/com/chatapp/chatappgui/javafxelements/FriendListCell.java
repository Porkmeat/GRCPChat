/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.chatapp.chatappgui.javafxelements;

import com.chatapp.chatappgui.Appgui;
import com.chatapp.dataobjects.Friend;
import com.chatapp.chatappgui.controllers.FriendCellController;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ListCell;
import javafx.scene.layout.AnchorPane;

/**
 * Custom JavaFX <code>ListCell</code> node for displaying the user's contacts.
 *
 * @author Mariano Cuneo
 */
public class FriendListCell extends ListCell<Friend> {

    private AnchorPane content;
    private FriendCellController controller;

    /**
     * Class Constructor.
     */
    public FriendListCell() {
        super();
        {
            try {
                FXMLLoader fxmlLoader = new FXMLLoader(Appgui.class.getResource("friendsfxml.fxml"));
                fxmlLoader.load();
                controller = fxmlLoader.getController();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        content = controller.getFriendcard();
    }

    /**
     * Updates the contents of the <code>ListCell</code>.
     *
     * @param item   <code>Friend</code> to be displayed.
     * @param empty boolean stating if the cell is empty.
     */
    @Override
    protected void updateItem(Friend item, boolean empty) {
        super.updateItem(item, empty);
        if (item != null && !empty) { // <== test for null item and empty parameter

            controller.setUsername(item.getAlias());
            controller.setLastMessage(item.getLastMsg());
            LocalDateTime messageTime = item.getTimestamp();
            LocalDateTime now = LocalDateTime.now();
            controller.setTimestamp(setTimeString(now, messageTime));
            controller.setOnlineStatus(item.isOnline());
            content = controller.getFriendcard();

            controller.setProfilePicture(item.getProfilePicture());

            setGraphic(content);

        } else {
            setGraphic(null);
        }
    }

    private String setTimeString(LocalDateTime now, LocalDateTime messageTime) {
        LocalDate today = now.toLocalDate();
        LocalDate messageDate = messageTime.toLocalDate();
        long days = ChronoUnit.DAYS.between(messageDate, today);
        if (days == 0) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
            return messageTime.format(formatter);
        } else if (days == 1) {
            return "Yesterday";
        } else {
            return messageDate.toString();
        }
    }
}

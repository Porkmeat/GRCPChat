package com.chatapp.chatappgui.javafxelements;

import com.chatapp.chatappgui.Appgui;
import com.chatapp.dataobjects.Chat;
import com.chatapp.chatappgui.controllers.ChatCellController;
import com.chatapp.chatappgui.controllers.MainScreenController;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.control.ListCell;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;

/**
 * Custom JavaFX <code>ListCell</code> node for displaying chat messages.
 *
 * @author Mariano Cuneo
 */
public class ChatListCell extends ListCell<Chat> {

    private final HBox content;
    private final MainScreenController mainScreenController;

    private ChatCellController controller;
    private AnchorPane bubble;

    {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(Appgui.class.getResource("chatcellfxml.fxml"));
            fxmlLoader.load();
            controller = fxmlLoader.getController();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Class constructor.
     *
     * @param mainScreenController the controller for the main screen.
     */
    public ChatListCell(MainScreenController mainScreenController) {
        super();
        this.mainScreenController = mainScreenController;
        bubble = controller.getChatBubble();
        content = new HBox(bubble);
        content.setSpacing(10);
        content.setPrefWidth(1);

    }

    /**
     * Updates the contents of the <code>ListCell</code>.
     *
     * @param item   <code>Chat</code> to be displayed.
     * @param empty boolean stating if the cell is empty.
     */
    @Override
    protected void updateItem(Chat item, boolean empty) {
        super.updateItem(item, empty);
        if (item != null && !empty) {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime messageTime = item.getTimestamp();
            if (item.getMessage().isEmpty()) { // <-- generates a banner displaying the date in the center.

                controller.setMessageText(setTimeString(now, messageTime));
                controller.setTimestampText("");
                controller.setBubbleColor("-fx-background-color: #b0bec5;");
                content.setAlignment(Pos.CENTER);
                bubble = controller.getChatBubble();
            } else {
                if (item.isFile()) { //  <-- code for handling files.
                    int i = item.getMessage().lastIndexOf(" ");
                    String[] fileInfo = {item.getMessage().substring(0, i), item.getMessage().substring(i+1)};
                    int extensionIndex = fileInfo[0].lastIndexOf(".");
                    String[] fileName = {fileInfo[0].substring(0, extensionIndex), fileInfo[0].substring(extensionIndex+1)};
                    controller.setMessageText("Download: " + fileInfo[0] + " - " + fileInfo[1] + " MB");
                    content.setOnMouseClicked(e -> {
                        item.setIsFile(false);
                        content.setOnMouseClicked(null);
                        mainScreenController.downloadFile(fileName[0], fileName[1], item);
                    });
                } else {
                    content.setOnMouseClicked(null);
                    controller.setMessageText(item.getMessage());
                }
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
                controller.setTimestampText(messageTime.format(formatter));
                if (item.userIsSender()) { // <-- changes formatting based on message sender.
                    content.setAlignment(Pos.CENTER_RIGHT);
                    controller.setBubbleColor("-fx-background-color: #80deea;");
                } else {
                    content.setAlignment(Pos.CENTER_LEFT);
                    controller.setBubbleColor("-fx-background-color: #a5d6a7;");
                }
                bubble = controller.getChatBubble();
            }
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
            return "TODAY";
        } else if (days == 1) {
            return "YESTERDAY";
        } else if (days < 7) {
            return messageDate.getDayOfWeek().toString();
        } else {
            return messageDate.toString();
        }
    }
}

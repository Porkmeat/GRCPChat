/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.chatapp.dataobjects;

import java.io.File;
import java.time.LocalDateTime;
import javafx.beans.Observable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.image.Image;
import javafx.util.Callback;

/**
 * Object containing other users' data.
 *
 * @author Mariano
 */
public class Friend implements Comparable<Friend> {

    private String username;
    private int userId;
    private String alias;
    private boolean friendIsSender;
    private int unseenChats;
    private Image profilePicture;
    private String lastMsg;
    private LocalDateTime timestamp;

    /**
     * This is a JavaFX property that can be observed in order to update the UI
     * when it changes.
     *
     */
    public final BooleanProperty isOnline = new SimpleBooleanProperty();

    /**
     * Class Constructor.
     *
     * @param username the contact's username.
     * @param userId the contact's user ID.
     * @param alias the contact's alias.
     * @param friendIsSender boolean stating if the last message in the chat was
     * sent by the contact.
     * @param unseenChats number of unseen messages in conversation between the
     * current user and the contact.
     * @param lastMsg text of the last message in the chat between the current
     * user and the contact.
     * @param timestamp date and time of the last message in the chat between
     * the current user and the contact.
     */
    public Friend(String username, int userId, String alias, boolean friendIsSender, int unseenChats, String lastMsg, LocalDateTime timestamp) {
        this.username = username;
        this.userId = userId;
        this.alias = alias;
        this.friendIsSender = friendIsSender;
        this.unseenChats = unseenChats;
        this.lastMsg = lastMsg;
        this.timestamp = timestamp;
        this.isOnline.set(false);
        this.profilePicture = new Image(new File("src/main/resources/defaults/default.jpg").toURI().toString());

    }

    /**
     * Returns a callback for an observable property. This method is used to be
     * able to observe the <code>isOnline</code> field with JavaFX UI elements.
     *
     * @return the callback to observe <code>isOnline</code>
     */
    public static Callback<Friend, Observable[]> extractor() {
        return (Friend param) -> new Observable[]{param.isOnline};
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public boolean friendIsSender() {
        return friendIsSender;
    }

    public void setFriendIsSender(boolean friendIsSender) {
        this.friendIsSender = friendIsSender;
    }

    public int getUnseenChats() {
        return unseenChats;
    }

    public void setUnseenChats(int unseenChats) {
        this.unseenChats = unseenChats;
    }

    public String getLastMsg() {
        return lastMsg;
    }

    public void setLastMsg(String lastMsg) {
        this.lastMsg = lastMsg;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isOnline() {
        return this.isOnline.get();
    }

    public void setIsOnline(boolean isOnline) {
        this.isOnline.set(isOnline);
    }

    public Image getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(Image profilePicture) {
        this.profilePicture = profilePicture;
    }

    /**
     * Overwritten compareTo method to sort items by <code>timestamp</code>.
     *
     * @param friend2  <code>Friend</code> object to compare.
     * @return result of the comparison.
     */
    @Override
    public int compareTo(Friend friend2) {
        return friend2.getTimestamp().compareTo(timestamp);
    }

}

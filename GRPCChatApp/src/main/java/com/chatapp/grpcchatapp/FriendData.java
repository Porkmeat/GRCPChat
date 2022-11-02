/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.chatapp.grpcchatapp;


/**
 *
 * @author Mariano
 */
public class FriendData {

    private UserData user;
    private String alias;
    private boolean isSender;
    private int unseenChats;
    private String profilePicture;
    private String lastMsg;
    private String timestamp;

    public FriendData(UserData user, String alias, boolean isSender, String profilePicture, String lastMsg, String timestamp, int unseenChats) {
        this.user = user;
        this.alias = alias;
        this.isSender = isSender;
        this.unseenChats = unseenChats;
        this.profilePicture = profilePicture;
        this.lastMsg = lastMsg;
        this.timestamp = timestamp;
    }

    public UserData getUser() {
        return user;
    }

    public void setUser(UserData user) {
        this.user = user;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public boolean isIsSender() {
        return isSender;
    }

    public void setIsSender(boolean isSender) {
        this.isSender = isSender;
    }

    public int getUnseenChats() {
        return unseenChats;
    }

    public void setUnseenChats(int unseenChats) {
        this.unseenChats = unseenChats;
    }

    public String getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
    }

    public String getLastMsg() {
        return lastMsg;
    }

    public void setLastMsg(String lastMsg) {
        this.lastMsg = lastMsg;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    

}

package com.chatapp.grpcchatapp;

import com.chatapp.friends.UserFriend;


/**
 * Object containing friend data.
 * 
 * @author Mariano Cuneo
 */
public class FriendData {
    
    private UserData user;
    private String alias;
    private boolean isSender;
    private int unseenChats;
    private String profilePicture;
    private String lastMsg;
    private String timestamp;
    private UserFriend.Type type;
   
    /**
     * Class Constructor.
     * 
     * @param user <code>UserData</code> object containing the username and user ID.
     * @param alias user's alias, default is the same as the username.
     * @param isSender boolean stating if user is sender of <code>lastMst</code>.
     * @param profilePicture string containing the name of the file containing the user's profile picture, default is an empty String.
     * @param lastMsg last message sent in chat.
     * @param timestamp time stamp of <code>lastMsg</code>.
     * @param unseenChats number of unseen messages in chat.
     * @param type can be FRIEND or REQUEST.
     */
    public FriendData(UserData user, String alias, boolean isSender, String profilePicture, String lastMsg, String timestamp, int unseenChats, UserFriend.Type type) {
        this.user = user;
        this.alias = alias;
        this.isSender = isSender;
        this.unseenChats = unseenChats;
        this.profilePicture = profilePicture;
        this.lastMsg = lastMsg;
        this.timestamp = timestamp;
        this.type = type;
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

    public boolean isSender() {
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

    public UserFriend.Type getType() {
        return type;
    }

    public void setType(UserFriend.Type type) {
        this.type = type;
    }
}

package com.chatapp.grpcchatapp;

/**
 * Object containing basic user information.
 * 
 * @author Mariano Cuneo
 */
public class UserData {
    
    private String username;
    private int userId;

    /**
     * Class constructor.
     * 
     * @param username user's username.
     * @param userId user's ID.
     */
    public UserData(String username, int userId) {
        this.username = username;
        this.userId = userId;
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
}

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.chatapp.listeners;

import com.chatapp.dataobjects.Friend;

/**
 * Listener for friend and request updates from server.
 *
 * @author Mariano Cuneo
 */
public interface FriendListener {

    /**
     * Adds friend to user's friend list.
     *
     * @param friend user marked as friend by server.
     */
    public void addChat(Friend friend);

    /**
     * Adds friend request from another user to request list.
     *
     * @param fromUser user marked as requester by server.
     */
    public void request(Friend fromUser);
}

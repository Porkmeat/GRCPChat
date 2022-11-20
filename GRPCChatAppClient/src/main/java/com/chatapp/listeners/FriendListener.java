/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.chatapp.listeners;

import com.chatapp.dataobjects.Friend;

/**
 *
 * @author julia
 */
public interface FriendListener {
    public void addChat(Friend friend);
    public void request(Friend fromUser);
}

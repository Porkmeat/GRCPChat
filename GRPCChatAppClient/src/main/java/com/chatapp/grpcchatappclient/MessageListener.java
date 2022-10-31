/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.chatapp.grpcchatappclient;

import com.chatapp.chatappgui.Chat;
import javafx.collections.ObservableList;

/**
 *
 * @author Mariano
 */
public interface MessageListener {
    public void messageGet (String fromUser, Chat message);
    public void loadMessages (String fromUser, ObservableList messages);
}

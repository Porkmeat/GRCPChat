/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.chatapp.listeners;

import com.chatapp.dataobjects.Chat;

/**
 *
 * @author julia
 */
public interface FileListener {
    public void fileSent(Chat chat, boolean success, String fileName);
}

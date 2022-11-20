/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.chatapp.listeners;

/**
 *
 * @author Mariano
 */
public interface StatusListener {
    public void online(String username);
    public void offline(String username);
}

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.chatapp.service;

import com.chatapp.chat.Chat;
import com.chatapp.chat.ChatMessage;
import com.chatapp.chat.ChatServiceGrpc;

import com.chatapp.chat.MessageList;
import com.chatapp.common.Empty;
import com.chatapp.common.ServiceResponse;

import io.grpc.stub.StreamObserver;

/**
 *
 * @author maria
 */
public class ChatService extends ChatServiceGrpc.ChatServiceImplBase {

    @Override
    public void getMessages(Chat request, StreamObserver<MessageList> responseObserver) {
        
    }

    @Override
    public void sendMessage(ChatMessage request, StreamObserver<ServiceResponse> responseObserver) {
        
    }


    @Override
    public void receiveMessage(Empty request, StreamObserver<ChatMessage> responseObserver) {
        
    }
}

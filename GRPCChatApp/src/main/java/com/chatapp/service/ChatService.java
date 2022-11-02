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
import com.chatapp.database.MySqlConnection;
import com.chatapp.grpcchatapp.MessageData;
import io.grpc.stub.StreamObserver;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author maria
 */
public class ChatService extends ChatServiceGrpc.ChatServiceImplBase {

    @Override
    public void getMessages(Chat request, StreamObserver<MessageList> responseObserver) {
        int userId = request.getUser().getUserId();
        int friendId = request.getFriend().getUserId();

        MessageList.Builder response = MessageList.newBuilder();
        ChatMessage.Builder chatMessage = ChatMessage.newBuilder();

        MySqlConnection database = new MySqlConnection();
        try {
            ArrayList<MessageData> results = database.fetchMessages(userId, friendId);
            for (MessageData message : results) {

                chatMessage.setChat(request)
                        .setMessage(message.getMessage())
                        .setUserIsSender(message.getSenderId() == userId)
                        .setTimestamp(message.getTimestamp())
                        .setSeen(message.isSeen());
                response.addMessages(chatMessage.build());
                chatMessage.clear();
            }

        } catch (Exception ex) {
            Logger.getLogger(ChatService.class.getName()).log(Level.SEVERE, null, ex);
        }
        responseObserver.onNext(response.build());
        responseObserver.onCompleted();
    }

    @Override
    public void sendMessage(ChatMessage request, StreamObserver<ServiceResponse> responseObserver) {
        String message = request.getMessage();
        int userId = request.getChat().getUser().getUserId();
        int friendId = request.getChat().getFriend().getUserId();

        ServiceResponse.Builder response = ServiceResponse.newBuilder();

        MySqlConnection database = new MySqlConnection();
        try {
            database.saveMsg(userId, friendId, message);

            response.setResponse("Message sent");
            response.setResponseCode(1);
        } catch (Exception ex) {
            Logger.getLogger(ChatService.class.getName()).log(Level.SEVERE, null, ex);
            response.setResponse("Internal error");
            response.setResponseCode(0);
        }
        responseObserver.onNext(response.build());
        responseObserver.onCompleted();
    }

    @Override
    public void receiveMessage(Empty request, StreamObserver<ChatMessage> responseObserver) {

    }
}

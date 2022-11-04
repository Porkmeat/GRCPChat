/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.chatapp.server;

import com.chatapp.chat.ChatMessage;
import com.chatapp.friends.UserFriend;
import com.chatapp.service.ChatService;
import com.chatapp.service.FriendManagementService;
import com.chatapp.service.LoginService;
import com.chatapp.service.StatusService;
import com.chatapp.status.StatusUpdate;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import java.io.IOException;
import java.util.HashMap;

/**
 *
 * @author maria
 */
public class GRPCChatApp {
    
    public static void main(String args[]) throws IOException, InterruptedException {
        HashMap<Integer, StreamObserver<ChatMessage>> messageObservers = new HashMap<>();
        HashMap<Integer, StreamObserver<UserFriend>> userObservers = new HashMap<>();
        HashMap<Integer, StreamObserver<StatusUpdate>> statusObservers = new HashMap<>();
        
        Server server = ServerBuilder.forPort(8818).addService(new LoginService(messageObservers,userObservers,statusObservers))
                .addService(new FriendManagementService(userObservers))
                .addService(new ChatService(messageObservers))
                .addService(new StatusService(statusObservers)).build();
        
        server.start();
        
        System.out.println("Server started on port: " + server.getPort());
        
        server.awaitTermination();
    }
    
}

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.chatapp.server;

import com.chatapp.chat.ChatMessage;
import com.chatapp.service.ChatService;
import com.chatapp.service.FriendManagementService;
import com.chatapp.service.LoginService;
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
        HashMap<Integer, StreamObserver<ChatMessage>> onlineClients = new HashMap<>();
        
        Server server = ServerBuilder.forPort(8818).addService(new LoginService()).addService(new FriendManagementService()).addService(new ChatService(onlineClients)).build();
        
        server.start();
        
        System.out.println("Server started on port: " + server.getPort());
        
        server.awaitTermination();
    }
    
}

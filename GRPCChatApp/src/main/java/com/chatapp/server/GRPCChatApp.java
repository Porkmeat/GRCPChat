package com.chatapp.server;

import com.chatapp.chat.ChatMessage;
import com.chatapp.friends.UserFriend;
import com.chatapp.service.ChatService;
import com.chatapp.service.FileService;
import com.chatapp.service.FriendManagementService;
import com.chatapp.service.LoginService;
import com.chatapp.service.StatusService;
import com.chatapp.status.StatusUpdate;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Server's Main Class.
 * This server uses a gRPC protocol for all calls.
 * 
 * @author Mariano Cuneo.
 */
public class GRPCChatApp {

    private static final int SERVER_PORT = 8818;
    /**
     * Creates all required shared objects and starts up the server.
     * 
     */
    public static void main(String args[]) throws IOException, InterruptedException {
        ConcurrentHashMap<Integer, StreamObserver<ChatMessage>> messageObservers = new ConcurrentHashMap<>();
        ConcurrentHashMap<Integer, StreamObserver<UserFriend>> userObservers = new ConcurrentHashMap<>();
        ConcurrentHashMap<Integer, StreamObserver<StatusUpdate>> statusObservers = new ConcurrentHashMap<>();

        Server server = ServerBuilder.forPort(SERVER_PORT).addService(new LoginService(messageObservers, userObservers, statusObservers))
                .addService(new FriendManagementService(userObservers, statusObservers))
                .addService(new ChatService(messageObservers))
                .addService(new StatusService(statusObservers))
                .addService(new FileService(messageObservers)).build();

        server.start();

        System.out.println("Server started on port: " + server.getPort());

        server.awaitTermination();
    }

}

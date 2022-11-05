/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Project/Maven2/JavaApp/src/main/java/${packagePath}/${mainClassName}.java to edit this template
 */
package com.chatapp.grpcchatappclient;

import com.chatapp.callbacks.FriendCallback;
import com.chatapp.callbacks.GetMessagesCallback;
import com.chatapp.callbacks.NewMessageCallback;
import com.chatapp.callbacks.ServiceResponseCallback;
import com.chatapp.callbacks.StatusCallback;
import com.chatapp.chat.ChatServiceGrpc;
import com.chatapp.chat.GetChatRequest;
import com.chatapp.chat.SendMessageRequest;
import com.chatapp.common.GetRequest;
import com.chatapp.common.User;
import com.chatapp.friends.AnswerRequest;
import com.chatapp.friends.FriendManagingServiceGrpc;
import com.chatapp.friends.FriendRequest;
import com.chatapp.login.LoginRequest;
import com.chatapp.login.LoginServiceGrpc;
import com.chatapp.login.ServerResponse;
import com.chatapp.status.StatusServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author maria
 */
public class GRPCChatAppClient {

    private final int port;
    private final String serverName;
    private ManagedChannel channel;
    private String tempdir;
    private final ArrayList<StatusListener> statusListeners = new ArrayList<>();
    private final ArrayList<MessageListener> messageListeners = new ArrayList<>();
    private final ArrayList<RequestListener> requestListeners = new ArrayList<>();
    private final ArrayList<FriendListener> friendListeners = new ArrayList<>();
    private LoginServiceGrpc.LoginServiceBlockingStub loginBlockingStub;
    private StatusServiceGrpc.StatusServiceStub statusStub;
    private ChatServiceGrpc.ChatServiceStub chatStub;
    private FriendManagingServiceGrpc.FriendManagingServiceStub friendStub;
    private String JWToken;

    public GRPCChatAppClient(String serverName, int port) {
        this.serverName = serverName;
        this.port = port;
    }

    public boolean connect() {
        this.channel = ManagedChannelBuilder.forAddress(serverName, port).usePlaintext().build();
        loginBlockingStub = LoginServiceGrpc.newBlockingStub(channel);
        return true;
    }

    public boolean login(String username, String password) {

        LoginRequest request = LoginRequest.newBuilder().setUsername(username).setPassword(password).build();

        try {
            ServerResponse response = loginBlockingStub.login(request);
            if (response.getResponseCode() == 1) {
                JWToken = response.getToken();
                statusStub = StatusServiceGrpc.newStub(channel);
                chatStub = ChatServiceGrpc.newStub(channel);
                friendStub = FriendManagingServiceGrpc.newStub(channel);

                return true;
            } else {
                return false;
            }
        } catch (StatusRuntimeException e) {
            Logger.getLogger(GRPCChatAppClient.class.getName()).log(Level.WARNING, "RPC failed: {0}", e.getStatus());
            return false;
        }

    }

    public void logoff() {
        GetRequest request = GetRequest.newBuilder().setToken(JWToken).build();

        try {
            ServerResponse response = loginBlockingStub.logout(request);
            if (response.getResponseCode() == 1) {
                System.out.println("Logout successful");
            } else {
                System.out.println("Logout failed");
            }

        } catch (StatusRuntimeException e) {
            Logger.getLogger(GRPCChatAppClient.class.getName()).log(Level.WARNING, "RPC failed: {0}", e.getStatus());
        }
    }

    public boolean createUser(String username, String password) {

        LoginRequest request = LoginRequest.newBuilder().setUsername(username).setPassword(password).build();
        try {
            ServerResponse response = loginBlockingStub.login(request);
            if (response.getResponseCode() == 1) {
                return login(username, password);
            } else {
                return false;
            }
        } catch (StatusRuntimeException e) {
            Logger.getLogger(GRPCChatAppClient.class.getName()).log(Level.WARNING, "RPC failed: {0}", e.getStatus());
            return false;
        }
    }

    public boolean requestStreams() {
        GetRequest request = GetRequest.newBuilder().setToken(JWToken).build();

        try {
            chatStub.receiveMessage(request, new NewMessageCallback());
            statusStub.receiveStatus(request, new StatusCallback());
            friendStub.recieveUsers(request, new FriendCallback());
            return true;
        } catch (StatusRuntimeException e) {
            Logger.getLogger(GRPCChatAppClient.class.getName()).log(Level.WARNING, "RPC failed: {0}", e.getStatus());
            return false;
        }
    }

    public void getFriendsAndRequests() {

        GetRequest request = GetRequest.newBuilder().setToken(JWToken).build();

        try {
            friendStub.getFriendsAndRequests(request, new ServiceResponseCallback());
        } catch (StatusRuntimeException e) {
            Logger.getLogger(GRPCChatAppClient.class.getName()).log(Level.WARNING, "RPC failed: {0}", e.getStatus());
        }
    }

    public void respondToRequest(String requester, int requesterId, int response) {

        AnswerRequest.Builder request = AnswerRequest.newBuilder().setToken(JWToken)
                .setRequester(User.newBuilder().setUserId(requesterId).setUsername(requester));

        switch (response) {
            case 1 ->
                request.setAnswer(AnswerRequest.Answer.ACCEPTED);

            case 2 ->
                request.setAnswer(AnswerRequest.Answer.DENIED);

            case 3 ->
                request.setAnswer(AnswerRequest.Answer.BLOCKED);

            default ->
                request.setAnswer(AnswerRequest.Answer.UNRECOGNIZED);
        }

        try {
            friendStub.setFriendship(request.build(), new ServiceResponseCallback());
        } catch (StatusRuntimeException e) {
            Logger.getLogger(GRPCChatAppClient.class.getName()).log(Level.WARNING, "RPC failed: {0}", e.getStatus());
        }
    }

    public void addFriend(String friendname) {
        
        FriendRequest request = FriendRequest.newBuilder().setToken(JWToken).setFriend(friendname).build();
        try {
            friendStub.addFriendship(request, new ServiceResponseCallback());
        } catch (StatusRuntimeException e) {
            Logger.getLogger(GRPCChatAppClient.class.getName()).log(Level.WARNING, "RPC failed: {0}", e.getStatus());
        }
    }

    public void fetchMessages(String friendLogin, int friendId) {

        GetChatRequest request = GetChatRequest.newBuilder().setToken(JWToken)
                .setFriend(User.newBuilder().setUserId(friendId).setUsername(friendLogin).build()).build();

        try {
            chatStub.getMessages(request, new GetMessagesCallback());
        } catch (StatusRuntimeException e) {
            Logger.getLogger(GRPCChatAppClient.class.getName()).log(Level.WARNING, "RPC failed: {0}", e.getStatus());
        }

    }

    public void msg(String recipientName, int recipientId, String message) {

        SendMessageRequest request = SendMessageRequest.newBuilder().setToken(JWToken)
                .setReciever(User.newBuilder().setUsername(recipientName).setUserId(recipientId))
                .setMessage(message).build();

        try {
            chatStub.sendMessage(request, new ServiceResponseCallback());
        } catch (StatusRuntimeException e) {
            Logger.getLogger(GRPCChatAppClient.class.getName()).log(Level.WARNING, "RPC failed: {0}", e.getStatus());
        }
    }

    public void addStatusListener(StatusListener listener) {
        statusListeners.add(listener);
    }

    public void removeStatusListener(StatusListener listener) {
        statusListeners.remove(listener);
    }

    public void addRequestListener(RequestListener listener) {
        requestListeners.add(listener);
    }

    public void removeRequestListener(RequestListener listener) {
        requestListeners.remove(listener);
    }

    public void addMessageListener(MessageListener listener) {
        messageListeners.add(listener);
    }

    public void removeMessageListener(MessageListener listener) {
        messageListeners.remove(listener);
    }

    public void addFriendListener(FriendListener listener) {
        friendListeners.add(listener);
    }

    public void removeFriendListener(FriendListener listener) {
        friendListeners.remove(listener);
    }
}

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Project/Maven2/JavaApp/src/main/java/${packagePath}/${mainClassName}.java to edit this template
 */
package com.chatapp.grpcchatappclient;

import com.chatapp.chatappgui.Chat;
import com.chatapp.chatappgui.Friend;
import com.chatapp.login.LoginServiceGrpc;
import com.chatapp.status.StatusServiceGrpc;

import io.grpc.ManagedChannel;
import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author Mariano
 */
public class ChatAppClient {

    private ManagedChannel channel;
    private Socket socket;
    private String tempdir;
    private final ArrayList<StatusListener> statusListeners = new ArrayList<>();
    private final ArrayList<MessageListener> messageListeners = new ArrayList<>();
    private final ArrayList<RequestListener> requestListeners = new ArrayList<>();
    private final ArrayList<FriendListener> friendListeners = new ArrayList<>();
    private LoginServiceGrpc.LoginServiceBlockingStub loginBlockingStub;
    private StatusServiceGrpc.StatusServiceStub statusStub;
    private String JWToken;
    
    




    public void requestProfilePicture(String username) throws IOException {
        if (tempdir == null) {
            tempdir = Files.createTempDirectory("tmpDirPrefix").toFile().getAbsolutePath();
        }

    }


    private void handleOnline(String[] tokens) {
        String username = tokens[1];
        for (StatusListener listener : statusListeners) {
            listener.online(username);
        }
    }

    private void handleOffline(String[] tokens) {
        String username = tokens[1];
        for (StatusListener listener : statusListeners) {
            listener.offline(username);
        }
    }

    private void handleMessage(String[] tokensMsg) {
        String fromUser = tokensMsg[1];
        String message = tokensMsg[2];
        LocalDateTime timestamp = LocalDateTime.now();
        Chat newMessage = new Chat(message, false, timestamp);
        for (MessageListener listener : messageListeners) {
            listener.messageGet(fromUser, newMessage);
        }
    }

    private void handleRequest(String[] tokens) {
        String username = tokens[1];
        for (RequestListener listener : requestListeners) {
            listener.request(username);
        }
    }

    private void handleLoadMessages(String[] tokens) {
        String friendLogin = tokens[1];
        JSONArray messages = new JSONArray(tokens[2]);
        System.out.println(messages.toString());
        ObservableList<Chat> chatList = FXCollections.observableArrayList();
        LocalDateTime lastMessageTime = null;

        for (int i = 0; i < messages.length(); i++) {
            JSONObject jsonobject = messages.getJSONObject(i);
            Instant timestampUTC = Instant.parse(jsonobject.getString("message_datetime") + "Z");
            LocalDateTime messageLocalTime = timestampUTC.atZone(ZoneId.systemDefault()).toLocalDateTime();

            if (i == 0 || !messageLocalTime.toLocalDate().equals(lastMessageTime.toLocalDate())) {
                chatList.add(new Chat(messageLocalTime));
            }

            lastMessageTime = messageLocalTime;
            String msg = jsonobject.getString("message_text");
            boolean userIsSender = jsonobject.getBoolean("user_is_sender");
            Chat newMessage = new Chat(msg, userIsSender, messageLocalTime);

            chatList.add(newMessage);
        }
        for (MessageListener listener : messageListeners) {
            listener.loadMessages(friendLogin, chatList);
        }

    }


    private void handleFriend(String string) {
        JSONObject jsonobject = new JSONObject(string);
        System.out.println(jsonobject.toString());

        Instant timestampUTC = Instant.parse(jsonobject.getString("last_message_time") + "Z");
        LocalDateTime localTime = timestampUTC.atZone(ZoneId.systemDefault()).toLocalDateTime();

        Friend friend = new Friend(jsonobject.getString("user_login"), jsonobject.getString("contact_alias"),
                jsonobject.getBoolean("friend_is_sender"), jsonobject.getInt("unseen_chats"),
                jsonobject.getString("last_message"), localTime);
        for (FriendListener listener : friendListeners) {
            listener.addChat(friend);
        }
    }

}

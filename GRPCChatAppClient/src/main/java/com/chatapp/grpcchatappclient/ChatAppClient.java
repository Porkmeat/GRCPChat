/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Project/Maven2/JavaApp/src/main/java/${packagePath}/${mainClassName}.java to edit this template
 */
package com.chatapp.grpcchatappclient;

import com.chatapp.chatappgui.Chat;
import com.chatapp.chatappgui.Friend;
import com.chatapp.login.LoginRequest;
import com.chatapp.login.LoginServiceGrpc;
import com.chatapp.login.ServerResponse;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author Mariano
 */
public class ChatAppClient {

    private final int port;
    private final String serverName;
    private ManagedChannel channel;
    private Socket socket;
    private OutputStream serverOut;
    private InputStream serverIn;
    private BufferedReader reader;
    private String tempdir;
    private final ArrayList<StatusListener> statusListeners = new ArrayList<>();
    private final ArrayList<MessageListener> messageListeners = new ArrayList<>();
    private final ArrayList<RequestListener> requestListeners = new ArrayList<>();
    private final ArrayList<FriendListener> friendListeners = new ArrayList<>();

    public ChatAppClient(String serverName, int port) {
        this.serverName = serverName;
        this.port = port;
    }

    public static void main(String[] args) throws IOException {

//        ChatAppClient client = new ChatAppClient("localhost", 8818);
//
//        // try to connect
//        if (!client.connect()) {
//            System.err.println("Connection failed!");
//        } else {
//            System.out.println("Connection successful!");
//        }
    }

    public boolean connect() {
        this.channel = ManagedChannelBuilder.forAddress(serverName, port).usePlaintext().build();
        return true;
    }

    public boolean login(String username, String password) throws IOException {
        
        LoginServiceGrpc.LoginServiceBlockingStub stub = LoginServiceGrpc.newBlockingStub(channel);
        LoginRequest request = LoginRequest.newBuilder().setUsername(username).setPassword(password).build();
        ServerResponse response = stub.login(request);
        
        if (response.getResponseCode() == 1) {
            return true;
        } else {
            return false;
        }
    }

    public void requestOnlineUsers() throws IOException {
        String cmd = "getusers\r\n";
        serverOut.write(cmd.getBytes());
    }

    public void logoff() throws IOException {
        String cmd = "logoff\r\n";
        serverOut.write(cmd.getBytes());
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

    private void startServerListener() {
        Thread t = new Thread() {
            @Override
            public void run() {
                serverListenLoop();
            }
        };
        t.start();
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

    public void requestProfilePicture(String username) throws IOException {
        if (tempdir == null) {
            tempdir = Files.createTempDirectory("tmpDirPrefix").toFile().getAbsolutePath();
        }
        
    }

    private void serverListenLoop() {
        try {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] tokens = StringUtils.split(line);
                if (tokens != null && tokens.length > 0) {
                    String cmd = tokens[0];
                    if ("online".equalsIgnoreCase(cmd)) {
                        handleOnline(tokens);
                    } else if ("offline".equalsIgnoreCase(cmd)) {
                        handleOffline(tokens);
                    } else if ("request".equalsIgnoreCase(cmd)) {
                        handleRequest(tokens);
                    } else if ("msg".equalsIgnoreCase(cmd)) {
                        String[] tokensMsg = StringUtils.split(line, null, 3);
                        handleMessage(tokensMsg);
                    } else if ("msgload".equalsIgnoreCase(cmd)) {
                        String[] tokensMsg = StringUtils.split(line, null, 3);
                        handleLoadMessages(tokensMsg);
                    } else if ("friend".equalsIgnoreCase(cmd)) {
                        String[] tokensMsg = StringUtils.split(line, null, 2);
                        handleFriend(tokensMsg[1]);
                    } else {
                        System.out.println("command unknown");
                    }
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            try {
                socket.close();
            } catch (IOException ex1) {
                Logger.getLogger(ChatAppClient.class.getName()).log(Level.SEVERE, null, ex1);
            }
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

    public void msg(String recipient, String message) throws IOException {
        String cmd = "msg " + recipient + " " + message + "\r\n";
        serverOut.write(cmd.getBytes());
    }

    public boolean createUser(String username, String password) throws IOException {
        String cmd = "newuser " + username + " " + password + "\r\n";
        serverOut.write(cmd.getBytes());
        String response = reader.readLine();

        return response.equals("account created");
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

    public void addFriend(String friendname) throws IOException {
        String cmd = "addfriend " + friendname + "\r\n";
        serverOut.write(cmd.getBytes());
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

    public void fetchFriends() throws IOException {
        String cmd = "getfriends\r\n";
        serverOut.write(cmd.getBytes());
    }

    public void fetchRequests() throws IOException {
        String cmd = "getrequests\r\n";
        serverOut.write(cmd.getBytes());
    }

    public void respondToRequest(String requester, int response) throws IOException {
        switch (response) {
            case 1: {
                System.out.println("top kek " + requester);
                String cmd = "acceptrequest " + requester + "\r\n";
                serverOut.write(cmd.getBytes());
                break;
            }
            case 2: {
                System.out.println("no kek " + requester);
                String cmd = "denyrequest " + requester + "\r\n";
                serverOut.write(cmd.getBytes());
                break;
            }
            case 3: {
                System.out.println("blocked " + requester);
                String cmd = "blockrequest " + requester + "\r\n";
                serverOut.write(cmd.getBytes());
                break;
            }
            default:
                break;
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

    public void fetchMessages(String friendLogin) throws IOException {
        String cmd = "loadmessages " + friendLogin + "\r\n";
        serverOut.write(cmd.getBytes());
    }

}

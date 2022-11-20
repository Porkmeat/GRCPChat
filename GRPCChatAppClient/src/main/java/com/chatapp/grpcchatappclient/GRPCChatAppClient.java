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
import com.chatapp.chatappgui.Friend;
import com.chatapp.common.GetRequest;
import com.chatapp.common.User;
import com.chatapp.file.FileChunk;
import com.chatapp.file.FileDownloadRequest;
import com.chatapp.file.FileDownloadResponse;
import com.chatapp.file.FileServiceGrpc;
import com.chatapp.file.FileUploadRequest;
import com.chatapp.file.MetaData;
import com.chatapp.friends.AnswerRequest;
import com.chatapp.friends.FriendManagingServiceGrpc;
import com.chatapp.friends.FriendRequest;
import com.chatapp.login.LoginRequest;
import com.chatapp.login.LoginServiceGrpc;
import com.chatapp.login.ServerResponse;
import com.chatapp.observers.FileUploadObserver;
import com.chatapp.status.StatusServiceGrpc;

import com.google.protobuf.ByteString;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import java.time.LocalDateTime;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

/**
 *
 * @author maria
 */
public class GRPCChatAppClient {

    private final int port;
    private final String serverName;
    private ManagedChannel channel;
    private String tmpFolder;
    private final ArrayList<StatusListener> statusListeners = new ArrayList<>();
    private final ArrayList<MessageListener> messageListeners = new ArrayList<>();
    private final ArrayList<FriendListener> friendListeners = new ArrayList<>();
    private LoginServiceGrpc.LoginServiceBlockingStub loginBlockingStub;
    private StatusServiceGrpc.StatusServiceStub statusStub;
    private ChatServiceGrpc.ChatServiceStub chatStub;
    private FriendManagingServiceGrpc.FriendManagingServiceStub friendStub;
    private FileServiceGrpc.FileServiceStub fileStub;
    private FileServiceGrpc.FileServiceBlockingStub fileBlockingStub;
    private String JWToken;

    public GRPCChatAppClient(String serverName, int port) {
        this.serverName = serverName;
        this.port = port;

        Path path = Paths.get(FileUtils.getTempDirectory().getAbsolutePath(), UUID.randomUUID().toString());
        try {
            this.tmpFolder = Files.createDirectories(path).toFile().getAbsolutePath();
        } catch (IOException ex) {
            Logger.getLogger(GRPCChatAppClient.class.getName()).log(Level.SEVERE, null, ex);
        }
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
                fileStub = FileServiceGrpc.newStub(channel);
                fileBlockingStub = FileServiceGrpc.newBlockingStub(channel);

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
                JWToken = "";
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
            ServerResponse response = loginBlockingStub.createAccount(request);
            return response.getResponseCode() == 1;
        } catch (StatusRuntimeException e) {
            Logger.getLogger(GRPCChatAppClient.class.getName()).log(Level.WARNING, "RPC failed: {0}", e.getStatus());
            return false;
        }
    }

    public boolean requestStreams() {
        GetRequest request = GetRequest.newBuilder().setToken(JWToken).build();

        try {
            chatStub.receiveMessage(request, new NewMessageCallback(messageListeners));
            statusStub.receiveStatus(request, new StatusCallback(statusListeners));
            friendStub.recieveUsers(request, new FriendCallback(friendListeners, tmpFolder));
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

    public void uploadProfilePicture(String filePath) {

        try {
            StreamObserver<FileUploadRequest> streamObserver = this.fileStub.fileUpload(new FileUploadObserver());

// file for testing
            Path path = Paths.get("D:\\Documents\\NetBeansProjects\\GRCPChat\\GRPCChatAppClient\\src\\main\\resources\\com\\chatapp\\chatappgui\\phantom-of-the-opera-mask-22876.jpg");

// build metadata
            FileUploadRequest metadata = FileUploadRequest.newBuilder()
                    .setMetadata(MetaData.newBuilder()
                            .setToken(JWToken)
                            .setFileType(FilenameUtils.getExtension(path.toString()))
                            .setIsProfilePic(true))
                    .build();
            streamObserver.onNext(metadata);
// upload file as chunk
            try ( InputStream inputStream = Files.newInputStream(path)) {

                byte[] bytes = new byte[4096];
                int size;
                while ((size = inputStream.read(bytes)) > 0) {
                    FileUploadRequest uploadRequest = FileUploadRequest.newBuilder()
                            .setFileChunk(FileChunk.newBuilder().setContent(ByteString.copyFrom(bytes, 0, size)).build())
                            .build();
                    streamObserver.onNext(uploadRequest);
                }
// close the stream
            }
            streamObserver.onCompleted();
        } catch (IOException ex) {
            Logger.getLogger(GRPCChatAppClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public String fetchFile(String fileName, boolean isProfilePicture) {
        OutputStream writer;
        String filePath = "";
        FileDownloadRequest request = FileDownloadRequest.newBuilder()
                .setMetadata(MetaData.newBuilder().setToken(JWToken).setFileName(fileName).setIsProfilePic(isProfilePicture))
                .build();

        Path saveLocation = Paths.get(tmpFolder);

        Iterator<FileDownloadResponse> fileChunks;
        try {
            try {
                fileChunks = fileBlockingStub.fileDownload(request);
                if (!fileChunks.hasNext()) {
                    return filePath;
                }
                writer = Files.newOutputStream(saveLocation.resolve(fileName), StandardOpenOption.CREATE,
                        StandardOpenOption.TRUNCATE_EXISTING);
                while (fileChunks.hasNext()) {
                    FileDownloadResponse fileChunk = fileChunks.next();
                    writer.write(fileChunk.getFileChunk().getContent().toByteArray());
                    writer.flush();
                }
                writer.close();
                filePath = saveLocation.toString() + "/" + fileName;
            } catch (StatusRuntimeException e) {
                Logger.getLogger(GRPCChatAppClient.class.getName()).log(Level.SEVERE, null, e);
            }
        } catch (IOException ex) {
            Logger.getLogger(GRPCChatAppClient.class.getName()).log(Level.SEVERE, null, ex);
        }
        return filePath;
    }

    public void respondToRequest(Friend requester, int response) {

        AnswerRequest.Builder request = AnswerRequest.newBuilder().setToken(JWToken)
                .setRequester(User.newBuilder().setUserId(requester.getUserId()).setUsername(requester.getUsername()));

        switch (response) {
            case 1 -> {
                request.setAnswer(AnswerRequest.Answer.ACCEPTED);
                for (FriendListener listener : friendListeners) {
                    requester.setTimestamp(LocalDateTime.now());
                    requester.setAlias(requester.getUsername());
                    listener.addChat(requester);
                }
            }

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
            chatStub.getMessages(request, new GetMessagesCallback(messageListeners));
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

    public String getTmpFolder() {
        return tmpFolder;
    }

}

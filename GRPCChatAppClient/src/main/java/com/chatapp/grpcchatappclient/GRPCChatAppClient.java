package com.chatapp.grpcchatappclient;

import com.chatapp.listeners.MessageListener;
import com.chatapp.listeners.StatusListener;
import com.chatapp.listeners.FriendListener;
import com.chatapp.callbacks.FriendCallback;
import com.chatapp.callbacks.GetMessagesCallback;
import com.chatapp.callbacks.NewMessageCallback;
import com.chatapp.callbacks.ServiceResponseCallback;
import com.chatapp.callbacks.StatusCallback;
import com.chatapp.chat.ChatServiceGrpc;
import com.chatapp.chat.GetChatRequest;
import com.chatapp.chat.SendMessageRequest;
import com.chatapp.dataobjects.Friend;
import com.chatapp.common.GetRequest;
import com.chatapp.common.User;
import com.chatapp.dataobjects.Chat;
import com.chatapp.filetransfer.FileChunk;
import com.chatapp.filetransfer.FileDownloadRequest;
import com.chatapp.filetransfer.FileDownloadResponse;
import com.chatapp.filetransfer.FileServiceGrpc;
import com.chatapp.filetransfer.FileUploadRequest;
import com.chatapp.filetransfer.MetaData;
import com.chatapp.friends.AnswerRequest;
import com.chatapp.friends.FriendManagingServiceGrpc;
import com.chatapp.friends.FriendRequest;
import com.chatapp.listeners.FileListener;
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
import java.io.File;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import java.time.LocalDateTime;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.coobird.thumbnailator.Thumbnails;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

/**
 * Program's main client Class. This class handles all non-UI logic and server
 * calls. It maintains a list of listeners to be able to notify the different UI
 * controllers when necessary. The server uses a gRCP protocall for all calls.
 *
 * @author Mariano Cuneo
 */
public class GRPCChatAppClient {

    private final int port;
    private final String serverName;
    private ManagedChannel channel;
    private String tmpFolder;
    private final ArrayList<StatusListener> statusListeners = new ArrayList<>();
    private final ArrayList<MessageListener> messageListeners = new ArrayList<>();
    private final ArrayList<FriendListener> friendListeners = new ArrayList<>();
    private final ArrayList<FileListener> fileListeners = new ArrayList<>();
    private LoginServiceGrpc.LoginServiceBlockingStub loginBlockingStub;
    private StatusServiceGrpc.StatusServiceStub statusStub;
    private ChatServiceGrpc.ChatServiceStub chatStub;
    private FriendManagingServiceGrpc.FriendManagingServiceStub friendStub;
    private FileServiceGrpc.FileServiceStub fileStub;
    private FileServiceGrpc.FileServiceBlockingStub fileBlockingStub;
    private String JWToken;

    /**
     * Class Constructor. It sets up the server's address and connection port.
     * It also creates a temporary directory to be used in the current user
     * session.
     *
     * @param serverName the server's IP address. Can also be 'localhost'.
     * @param port the server's connection port.
     */
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

    /**
     * Connects to the gRCP server.
     *
     */
    public void connect() {
        this.channel = ManagedChannelBuilder.forAddress(serverName, port).usePlaintext().build();
        loginBlockingStub = LoginServiceGrpc.newBlockingStub(channel);
    }

    /**
     * Attempts to log in to the server. This method sends the username and
     * password provided by the user and sends it to the server to be verified.
     * If the information is correct, the server returns a <code>JWToken</code>
     * to be used in all subsequent calls and the client sets up all necessary
     * gRCP stubs and returns true. If the login information is wrong the method
     * simply returns false and no stubs are created.
     *
     * @param username username to be sent to server.
     * @param password password to be sent to server.
     * @return          <code>true</code> if server response is successful, otherwise
     * <code>false</code>.
     */
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

    /**
     * Sends a logoff request to the server.
     */
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

    /**
     * Sends an account creation request to the server.
     *
     * @param username new account's desired username.
     * @param password new account's desired password.
     * @return          <code>true</code> if new account was created successfully,
     * otherwise <code>false</code>.
     */
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

    /**
     * Requests server service streams.
     *
     * @return  <code>true</code> streams were established successfully,
     * otherwise <code>false</code>.
     */
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

    /**
     * Sends request to server for current user's friends and active friend
     * requests.
     */
    public void getFriendsAndRequests() {

        GetRequest request = GetRequest.newBuilder().setToken(JWToken).build();

        try {
            friendStub.getFriendsAndRequests(request, new ServiceResponseCallback());
        } catch (StatusRuntimeException e) {
            Logger.getLogger(GRPCChatAppClient.class.getName()).log(Level.WARNING, "RPC failed: {0}", e.getStatus());
        }
    }

    /**
     * Sends request to server for a profile picture upload. The method
     * generates a new resized picture to be uploaded.
     *
     * @param picture a <code>File</code> object containing an image file.
     * @throws IOException if resized image can't be saved or read.
     */
    public void uploadProfilePicture(File picture) throws IOException {

        String fileName = UUID.randomUUID().toString() + ".jpg";

        Thumbnails.of(picture)
                .size(100, 100)
                .toFile(new File(tmpFolder + "/" + fileName));

        StreamObserver<FileUploadRequest> streamObserver = this.fileStub.fileUpload(new FileUploadObserver(fileListeners, tmpFolder + "/" + fileName));

        Path path = Paths.get(tmpFolder + "/" + fileName);

// build metadata
        FileUploadRequest metadata = FileUploadRequest.newBuilder()
                .setMetadata(MetaData.newBuilder()
                        .setToken(JWToken)
                        .setFileType("jpg")
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
        }
        streamObserver.onCompleted();
    }

    /**
     * Sends request to server for a file upload.
     *
     * @param file a <code>File</code> object containing a file. File cannot be
     * bigger than 20MB.
     * @param friendId user Id of file recipient.
     * @param friendName username of file recipient.
     * @param chat <code>Chat</code> object displaying upload status.
     * @throws IOException if file can't be found or read.
     */
    public void uploadFile(File file, int friendId, String friendName, Chat chat) throws IOException {

        StreamObserver<FileUploadRequest> streamObserver = this.fileStub.fileUpload(new FileUploadObserver(fileListeners, file.getName(), chat));

        Path path = file.toPath();

        double fileSize = (double) file.length() / (1024 * 1024);

        FileUploadRequest metadata = FileUploadRequest.newBuilder()
                .setMetadata(MetaData.newBuilder()
                        .setToken(JWToken)
                        .setFileName(FilenameUtils.getBaseName(path.toString()))
                        .setFileType(FilenameUtils.getExtension(path.toString()))
                        .setIsProfilePic(false)
                        .setFileSize(fileSize)
                        .setFriend(User.newBuilder().setUserId(friendId).setUsername(friendName)))
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
        }
        streamObserver.onCompleted();
    }

    /**
     * Requests a file download from the server. This method is used to retrive
     * both profile pictures aswell as user sent files.
     *
     * @param fileName name of the file to be downloaded.
     * @param fileType file extension of the file to be downloaded.
     * @param saveDir directory where file will be saved.
     * @param isProfilePicture boolean stating if requested file is a profile
     * picture.
     * @param friendId user ID of file's uploader. Can be 0 if
     * <code>isProfilePicture</code> is <code>true</code>.
     * @return path to downloaded file, empty String if download failed.
     */
    public String fetchFile(String fileName, String fileType, String saveDir, boolean isProfilePicture, int friendId) {
        OutputStream writer;
        String filePath = "";
        FileDownloadRequest request = FileDownloadRequest.newBuilder()
                .setMetadata(MetaData.newBuilder().setToken(JWToken).setFileName(fileName).setFileType(fileType).setIsProfilePic(isProfilePicture).setFriend(User.newBuilder().setUserId(friendId)))
                .build();

        Path saveLocation;

        if (saveDir.isEmpty()) {
            saveLocation = Paths.get(tmpFolder);
        } else {
            saveLocation = Paths.get(saveDir);
        }

        Iterator<FileDownloadResponse> fileChunks;

        try {
            fileChunks = fileBlockingStub.fileDownload(request);
            if (!fileChunks.hasNext()) {
                return filePath;
            }
            writer = Files.newOutputStream(saveLocation.resolve(fileName + "." + fileType), StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING);
            while (fileChunks.hasNext()) {
                FileDownloadResponse fileChunk = fileChunks.next();
                writer.write(fileChunk.getFileChunk().getContent().toByteArray());
                writer.flush();
            }
            writer.close();
            filePath = saveLocation.toString() + "\\" + fileName + "." + fileType;
        } catch (StatusRuntimeException | IOException e) {
            Logger.getLogger(GRPCChatAppClient.class.getName()).log(Level.SEVERE, null, e);
        }
        return filePath;
    }

    /**
     * Sends request to server to respond to a friend request.
     *
     * @param requester  <code>Friend</code> object containing the user who made
     * the friend request.
     * @param response number representing response to friend request.
     * 1->ACCEPTED, 2->DENIED, 3->BLOCKED.
     */
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

    /**
     * Sends request to server to add a user as a friend.
     *
     * @param friendname username of user to be sent the friend request.
     */
    public void addFriend(String friendname) {

        FriendRequest request = FriendRequest.newBuilder().setToken(JWToken).setFriend(friendname).build();
        try {
            friendStub.addFriendship(request, new ServiceResponseCallback());
        } catch (StatusRuntimeException e) {
            Logger.getLogger(GRPCChatAppClient.class.getName()).log(Level.WARNING, "RPC failed: {0}", e.getStatus());
        }
    }

    /**
     * Sends request to server for all chat messages between current user and a
     * friend.
     *
     * @param friendLogin username of friend.
     * @param friendId user ID of friend.
     */
    public void fetchMessages(String friendLogin, int friendId) {

        GetChatRequest request = GetChatRequest.newBuilder().setToken(JWToken)
                .setFriend(User.newBuilder().setUserId(friendId).setUsername(friendLogin).build()).build();

        try {
            chatStub.getMessages(request, new GetMessagesCallback(messageListeners));
        } catch (StatusRuntimeException e) {
            Logger.getLogger(GRPCChatAppClient.class.getName()).log(Level.WARNING, "RPC failed: {0}", e.getStatus());
        }

    }

    /**
     * Sends a chat message to the server.
     *
     * @param recipientName username of message recipient.
     * @param recipientId user ID of message recipient.
     * @param message text of the message to be sent.
     */
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

    public void addFileListener(FileListener listener) {
        fileListeners.add(listener);
    }

    public void removeFileListener(FileListener listener) {
        fileListeners.remove(listener);
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

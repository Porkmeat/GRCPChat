package com.chatapp.service;

import com.chatapp.chat.ChatMessage;
import com.chatapp.common.GetRequest;
import com.chatapp.common.User;
import com.chatapp.database.MySqlConnection;
import com.chatapp.friends.UserFriend;
import com.chatapp.grpcchatapp.JWToken;
import com.chatapp.login.LoginRequest;
import com.chatapp.login.LoginServiceGrpc;
import com.chatapp.login.ServerResponse;
import com.chatapp.status.StatusUpdate;
import io.grpc.stub.StreamObserver;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.codec.digest.DigestUtils;

/**
 * gRPC service for handling logins and logouts. This service is also used to
 * create new users.
 *
 * @author Mariano Cuneo
 */
public class LoginService extends LoginServiceGrpc.LoginServiceImplBase {

    private final ConcurrentHashMap<Integer, StreamObserver<ChatMessage>> messageObservers;
    private final ConcurrentHashMap<Integer, StreamObserver<UserFriend>> userObservers;
    private final ConcurrentHashMap<Integer, StreamObserver<StatusUpdate>> statusObservers;

    /**
     * Class Constructor.
     *
     * @param messageObservers contains all currently active
     * <code>ChatService</code> streams.
     * @param userObservers contains all currently active
     * <code>FriendManagementService</code> streams.
     * @param statusObservers contains all currently active
     * <code>StatusService</code> streams.
     */
    public LoginService(ConcurrentHashMap<Integer, StreamObserver<ChatMessage>> messageObservers,
            ConcurrentHashMap<Integer, StreamObserver<UserFriend>> userObservers,
            ConcurrentHashMap<Integer, StreamObserver<StatusUpdate>> statusObservers) {

        this.messageObservers = messageObservers;
        this.userObservers = userObservers;
        this.statusObservers = statusObservers;
    }

    /**
     * RPC method to call for a logout. This method closes all streams mapped to
     * the user and notifies other users of the offline status change.
     *
     * @param request client request message. Must contain a valid JWToken.
     * @param responseObserver the call's stream observer.
     */
    @Override
    public void logout(GetRequest request, StreamObserver<ServerResponse> responseObserver) {
        JWToken token = new JWToken(request.getToken());
        ServerResponse.Builder response = ServerResponse.newBuilder();
        if (token.isValid()) {
            String username = token.getUsername();
            int userId = token.getUserId();

            if (userObservers.containsKey(userId)) {
                userObservers.get(userId).onCompleted();
                userObservers.remove(userId);
            }
            if (messageObservers.containsKey(userId)) {
                messageObservers.get(userId).onCompleted();
                messageObservers.remove(userId);
            }
            if (statusObservers.containsKey(userId)) {
                statusObservers.get(userId).onCompleted();
                statusObservers.remove(userId);
            }

            MySqlConnection database = new MySqlConnection();
            StatusUpdate statusUpdate = StatusUpdate.newBuilder()
                    .setUser(User.newBuilder().setUsername(username).setUserId(userId))
                    .setStatus(StatusUpdate.Status.OFFLINE).build();

            try {
                ArrayList<Integer> friendList = database.getFriendsIDs(userId);
                for (int friend : friendList) {
                    if (statusObservers.containsKey(friend)) {
                        statusObservers.get(friend).onNext(statusUpdate);
                    }
                }
                response.setToken("SUCCESS");
                response.setResponseCode(1);
            } catch (SQLException ex) {
                Logger.getLogger(LoginService.class.getName()).log(Level.SEVERE, null, ex);
                response.setToken("INTERNAL_ERROR");
                response.setResponseCode(0);
            }

        } else {
            response.setToken("INVALID_CREDENTIALS");
            response.setResponseCode(0);
        }
        responseObserver.onNext(response.build());
        responseObserver.onCompleted();
    }

    /**
     * RPC method to request a login. This method verifies the username and
     * password provided. If the information is correct, it generates a valid
     * JWToken for the client to use on further calls to the server.
     *
     * @param request client request message. Must contain a username and a
     * password to be verified.
     * @param responseObserver the call's stream observer.
     */
    @Override
    public void login(LoginRequest request, StreamObserver<ServerResponse> responseObserver) {
        String username = request.getUsername();
        String password = request.getPassword();

        ServerResponse.Builder response = ServerResponse.newBuilder();
        MySqlConnection database = new MySqlConnection();
        try {
            if (database.checkPassword(username, password)) {
                int userId = database.getUserId(username);
                JWToken token = new JWToken(username, userId);
                response.setToken(token.toString()).setResponseCode(1);
            } else {
                response.setToken("INVALID_CREDENTIALS").setResponseCode(0);
            }

        } catch (SQLException ex) {
            Logger.getLogger(LoginService.class.getName()).log(Level.SEVERE, null, ex);
        }
        responseObserver.onNext(response.build());
        responseObserver.onCompleted();
    }

    /**
     * RPC method to request a new account to be created. This method first
     * checks if the username is available, and if so, it generates a salted
     * password with a random 4 digit number and hashes it. The username, salt
     * and hashed password are then saved to the database.
     *
     * @param request client request message. Must contain desired username and
     * password.
     * @param responseObserver the call's stream observer.
     */
    @Override
    public void createAccount(LoginRequest request, StreamObserver<ServerResponse> responseObserver) {
        String username = request.getUsername();
        String password = request.getPassword();

        ServerResponse.Builder response = ServerResponse.newBuilder();
        MySqlConnection database = new MySqlConnection();
        try {
            if (database.getUserId(username) == 0) {

                Random random = new Random();
                int salt = random.nextInt(10000);
                String saltedpass = password + String.valueOf(salt);
                String hashedpass = DigestUtils.sha256Hex(saltedpass);

                database.addNewUser(username, hashedpass, salt);
                response.setToken("SUCCESS");
                response.setResponseCode(1);
            } else {
                response.setToken("INVALID_ARGUMENTS");
                response.setResponseCode(0);
            }
        } catch (SQLException ex) {
            Logger.getLogger(LoginService.class.getName()).log(Level.SEVERE, null, ex);
            response.setToken("INTERNAL_ERROR");
            response.setResponseCode(0);
        }

        responseObserver.onNext(response.build());
        responseObserver.onCompleted();
    }

}

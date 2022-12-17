package com.chatapp.service;

import com.chatapp.chat.ChatMessage;
import com.chatapp.common.GetRequest;
import com.chatapp.common.LoginToken;
import com.chatapp.common.ResponseCode;
import com.chatapp.common.ServiceResponse;
import com.chatapp.common.User;
import com.chatapp.database.MySqlConnection;
import com.chatapp.friends.UserFriend;
import com.chatapp.grpcchatapp.JWToken;
import com.chatapp.login.LoginRequest;
import com.chatapp.login.LoginResponse;
import com.chatapp.login.LoginServiceGrpc;
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
    public void logout(GetRequest request, StreamObserver<ServiceResponse> responseObserver) {
        JWToken token = new JWToken(request.getToken().getToken());
        ServiceResponse.Builder response = ServiceResponse.newBuilder();
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
                response.setResponseCode(ResponseCode.SUCCESS);
            } catch (SQLException ex) {
                Logger.getLogger(LoginService.class.getName()).log(Level.SEVERE, null, ex);
                response.setResponseCode(ResponseCode.INTERNAL_ERROR);
            }

        } else {
            response.setResponseCode(ResponseCode.INVALID_CREDENTIALS);
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
    public void login(LoginRequest request, StreamObserver<LoginResponse> responseObserver) {
        String username = request.getUsername();
        String password = request.getPassword();

        LoginResponse.Builder response = LoginResponse.newBuilder();
        MySqlConnection database = new MySqlConnection();
        
        try {
            int userId = database.getUserId(username);
            if (userId != 0 && database.checkPassword(userId, password)) {
                JWToken token = new JWToken(username, userId);
                response.setToken(LoginToken.newBuilder().setToken(token.toString()))
                        .setResponseCode(ResponseCode.SUCCESS);
            } else {
                response.setResponseCode(ResponseCode.INVALID_CREDENTIALS);
            }
        } catch (SQLException ex) {
            Logger.getLogger(LoginService.class.getName()).log(Level.SEVERE, null, ex);
            response.setResponseCode(ResponseCode.INTERNAL_ERROR);
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
    public void createAccount(LoginRequest request, StreamObserver<ServiceResponse> responseObserver) {
        String username = request.getUsername();
        String password = request.getPassword();

        ServiceResponse.Builder response = ServiceResponse.newBuilder();
        MySqlConnection database = new MySqlConnection();
        try {
            if (database.getUserId(username) == 0) {

                Random random = new Random();
                int salt = random.nextInt(10000);
                String saltedpass = password + String.valueOf(salt);
                String hashedpass = DigestUtils.sha256Hex(saltedpass);

                database.addNewUser(username, hashedpass, salt);
                response.setResponseCode(ResponseCode.SUCCESS);
            } else {
                response.setResponseCode(ResponseCode.INVALID_ARGUMENTS);
            }
        } catch (SQLException ex) {
            Logger.getLogger(LoginService.class.getName()).log(Level.SEVERE, null, ex);
            response.setResponseCode(ResponseCode.INTERNAL_ERROR);
        }

        responseObserver.onNext(response.build());
        responseObserver.onCompleted();
    }

}

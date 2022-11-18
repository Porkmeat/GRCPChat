/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.chatapp.service;

import com.chatapp.common.GetRequest;
import com.chatapp.common.ServiceResponse;
import com.chatapp.common.User;
import com.chatapp.database.MySqlConnection;
import com.chatapp.friends.AnswerRequest;
import com.chatapp.friends.AnswerRequest.Answer;
import com.chatapp.friends.FriendManagingServiceGrpc;
import com.chatapp.friends.FriendRequest;
import com.chatapp.friends.UserFriend;
import com.chatapp.grpcchatapp.FriendData;
import com.chatapp.grpcchatapp.JWToken;
import com.chatapp.status.StatusUpdate;
import com.google.protobuf.ByteString;
import io.grpc.stub.StreamObserver;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author maria
 */
public class FriendManagementService extends FriendManagingServiceGrpc.FriendManagingServiceImplBase {

    private final ConcurrentHashMap<Integer, StreamObserver<UserFriend>> userObservers;
    private final ConcurrentHashMap<Integer, StreamObserver<StatusUpdate>> statusObservers;
    private final String PROFILE_PIC_PATH = "src/main/resources/profilepictures/";

    public FriendManagementService(ConcurrentHashMap<Integer, StreamObserver<UserFriend>> userObservers, ConcurrentHashMap<Integer, StreamObserver<StatusUpdate>> statusObservers) {
        this.userObservers = userObservers;
        this.statusObservers = statusObservers;
    }

    @Override
    public void setFriendship(AnswerRequest request, StreamObserver<ServiceResponse> responseObserver) {
        JWToken token = new JWToken(request.getToken());
        ServiceResponse.Builder response = ServiceResponse.newBuilder();

        if (token.isValid()) {
            int userId = token.getUserId();
            int requesterId = request.getRequester().getUserId();

            Answer answer = request.getAnswer();
            MySqlConnection database = new MySqlConnection();

            try {

                switch (answer) {
                    case ACCEPTED -> {
                        FriendData friend = database.acceptRequest(userId, requesterId);
                        if (userObservers.containsKey(requesterId)) {
                            userObservers.get(requesterId).onNext(generateUserFriend(friend));
                        }
                        if (statusObservers.containsKey(userId)) {
                            StatusUpdate statusUpdate = StatusUpdate.newBuilder()
                                    .setUser(User.newBuilder().setUsername(request.getRequester().getUsername())
                                            .setUserId(requesterId))
                                    .setStatus(StatusUpdate.Status.ONLINE).build();
                            statusObservers.get(userId).onNext(statusUpdate);
                        }
                    }

                    case DENIED ->
                        database.denyRequest(userId, requesterId);

                    case BLOCKED ->
                        database.blockRequest(userId, requesterId);
                }

                response.setResponse("Request " + answer.getValueDescriptor().getName());
                response.setResponseCode(1);

            } catch (Exception ex) {

                Logger.getLogger(FriendManagementService.class.getName()).log(Level.SEVERE, null, ex);
                response.setResponse("Internal error");
                response.setResponseCode(0);
            }
        } else {
            response.setResponse("Verification failed");
            response.setResponseCode(0);
        }

        responseObserver.onNext(response.build());
        responseObserver.onCompleted();
    }

    @Override
    public void getFriendsAndRequests(GetRequest request, StreamObserver<ServiceResponse> responseObserver) {
        JWToken token = new JWToken(request.getToken());
        ServiceResponse.Builder response = ServiceResponse.newBuilder();
        if (token.isValid()) {

            int userId = token.getUserId();
            MySqlConnection database = new MySqlConnection();

            try {
                ArrayList<FriendData> friends = database.fetchFriends(userId);
                for (FriendData result : friends) {
                    if (userObservers.containsKey(userId)) {
                        userObservers.get(userId).onNext(generateUserFriend(result));
                    }
                }
                response.setResponse("Friends and requests fetched").setResponseCode(1);
            } catch (Exception ex) {
                Logger.getLogger(FriendManagementService.class.getName()).log(Level.SEVERE, null, ex);
                response.setResponse("Internal error").setResponseCode(0);
            }
        } else {
            response.setResponse("Verification failed").setResponseCode(0);
        }
        responseObserver.onNext(response.build());
        responseObserver.onCompleted();
    }

    @Override
    public void recieveUsers(GetRequest request, StreamObserver<UserFriend> responseObserver) {
        JWToken token = new JWToken(request.getToken());
        if (token.isValid()) {
            int userId = token.getUserId();
            System.out.println("added friend key " + userId);
            userObservers.put(userId, responseObserver);
        }
    }

    @Override
    public void addFriendship(FriendRequest request, StreamObserver<ServiceResponse> responseObserver) {
        JWToken token = new JWToken(request.getToken());
        ServiceResponse.Builder response = ServiceResponse.newBuilder();
        if (token.isValid()) {
            int userId = token.getUserId();
            String username = token.getUsername();
            String friendName = request.getFriend();

            MySqlConnection database = new MySqlConnection();

            try {
                int friendId = database.getUserId(friendName);
                if (friendId > 0) {
                    database.addFriend(userId, username, friendId, friendName);

                    if (userObservers.containsKey(friendId)) {
                        UserFriend.Builder user = UserFriend.newBuilder();
                        user.setUser(User.newBuilder().setUsername(username).setUserId(userId))
                                .setType(UserFriend.Type.REQUEST);
                        userObservers.get(friendId).onNext(user.build());
                        System.out.println("online user notified");
                    }

                    response.setResponse("Request sent to " + friendName);
                    response.setResponseCode(1);
                } else {
                    response.setResponse("User " + friendName + " doesn't exist");
                    response.setResponseCode(0);

                }
            } catch (Exception ex) {
                Logger.getLogger(FriendManagementService.class
                        .getName()).log(Level.SEVERE, null, ex);
                response.setResponse("Internal error");
                response.setResponseCode(0);
            }
        } else {
            response.setResponse("Verification failed");
            response.setResponseCode(0);
        }
        responseObserver.onNext(response.build());
        responseObserver.onCompleted();
    }

    private UserFriend generateUserFriend(FriendData friend)  {
        UserFriend.Builder userFriend = UserFriend.newBuilder();
        userFriend.setUser(User.newBuilder()
                .setUsername(friend.getUser().getUsername())
                .setUserId(friend.getUser().getUserId()))
                .setAlias(friend.getAlias())
                .setType(friend.getType());

        try ( InputStream inputStream = Files.newInputStream(Paths.get(PROFILE_PIC_PATH + friend.getProfilePicture()))) {
            userFriend.setProfilePicture(ByteString.copyFrom(inputStream.readAllBytes()));
        } catch (IOException ex) {
            userFriend.setProfilePicture(ByteString.EMPTY);
        }
        
        switch (friend.getType()) {
            case REQUEST -> {
                userFriend.setLastMsg("")
                        .setTimestamp("")
                        .setIsSender(false)
                        .setUnseenChats(0)
                        .setIsOnline(false);
            }
            case FRIEND -> {
                userFriend.setLastMsg(friend.getLastMsg())
                        .setIsSender(friend.isIsSender())
                        .setTimestamp(friend.getTimestamp())
                        .setUnseenChats(friend.getUnseenChats())
                        .setIsOnline(userObservers.containsKey(friend.getUser().getUserId()));

            }
        }

        return userFriend.build();
    }

}

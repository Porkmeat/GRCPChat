/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.chatapp.service;

import com.chatapp.common.ServiceResponse;
import com.chatapp.common.User;
import com.chatapp.database.MySqlConnection;
import com.chatapp.friends.AnswerRequest;
import com.chatapp.friends.AnswerRequest.Answer;
import com.chatapp.friends.FriendList;
import com.chatapp.friends.FriendManagingServiceGrpc;
import com.chatapp.friends.FriendRequest;
import com.chatapp.friends.UserFriend;
import com.chatapp.grpcchatapp.FriendData;
import com.chatapp.grpcchatapp.UserData;
import io.grpc.stub.StreamObserver;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author maria
 */
public class FriendManagementService extends FriendManagingServiceGrpc.FriendManagingServiceImplBase {

    @Override
    public void setFriendship(AnswerRequest request, StreamObserver<ServiceResponse> responseObserver) {

        int userId = request.getUser().getUserId();
        int requesterId = request.getRequester().getUserId();
        Answer answer = request.getAnswer();
        MySqlConnection database = new MySqlConnection();

        ServiceResponse.Builder response = ServiceResponse.newBuilder();

        try {
            
            switch (answer) {
                case ACCEPTED -> database.acceptRequest(userId, requesterId);
                
                case DENIED -> database.denyRequest(userId, requesterId);
                
                case BLOCKED -> database.blockRequest(userId, requesterId);
            }
            
            response.setResponse("Request " + answer.getValueDescriptor().getName());
            response.setResponseCode(1);
            
        } catch (Exception ex) {
            
            Logger.getLogger(FriendManagementService.class.getName()).log(Level.SEVERE, null, ex);
            response.setResponse("Internal error");
            response.setResponseCode(0);
            
        }

        responseObserver.onNext(response.build());
        responseObserver.onCompleted();
    }

    @Override
    public void getFriends(User request, StreamObserver<FriendList> responseObserver) {
        int userId = request.getUserId();

        FriendList.Builder response = FriendList.newBuilder();
        UserFriend.Builder friend = UserFriend.newBuilder();
        MySqlConnection database = new MySqlConnection();
        try {
            ArrayList<FriendData> results = database.fetchFriends(userId);
            for (FriendData result : results) {

                friend.setUser(User.newBuilder()
                        .setUsername(result.getUser().getUsername())
                        .setUserId(result.getUser().getUserId()).build())
                        .setAlias(result.getAlias())
                        .setIsSender(result.isIsSender())
                        .setLastMsg(result.getLastMsg())
                        .setTimestamp(result.getTimestamp())
                        .setUnseenChats(result.getUnseenChats());
                response.addFriends(friend.build());
                friend.clear();
            }

        } catch (Exception ex) {
            Logger.getLogger(FriendManagementService.class.getName()).log(Level.SEVERE, null, ex);
        }
        responseObserver.onNext(response.build());
        responseObserver.onCompleted();
    }

    @Override
    public void getRequests(User request, StreamObserver<User> responseObserver) {
        int userId = request.getUserId();

        MySqlConnection database = new MySqlConnection();
        User.Builder user = User.newBuilder();
        try {
            ArrayList<UserData> results = database.getRequests(userId);
            for (UserData result : results) {
                System.out.println(result.getUsername());
                user.setUsername(result.getUsername());
                user.setUserId(result.getUserId());
                responseObserver.onNext(user.build());
                user.clear();
            }
        } catch (Exception ex) {
            Logger.getLogger(FriendManagementService.class.getName()).log(Level.SEVERE, null, ex);
        }
        responseObserver.onCompleted();
    }

    @Override
    public void addFriendship(FriendRequest request, StreamObserver<ServiceResponse> responseObserver) {
        int userId = request.getUser().getUserId();
        String username = request.getUser().getUsername();
        String friendName = request.getFriend();
        
        MySqlConnection database = new MySqlConnection();

        ServiceResponse.Builder response = ServiceResponse.newBuilder();
        
        try {
            int friendId = database.getUserId(friendName);
            if (friendId > 0) {
                database.addFriend(userId, username, friendId, friendName);
                response.setResponse("Request sent to " + friendName);
                response.setResponseCode(1);
            } else {
                response.setResponse("User " + friendName + " doesn't exist");
                response.setResponseCode(0);
            }
        } catch (Exception ex) {
            Logger.getLogger(FriendManagementService.class.getName()).log(Level.SEVERE, null, ex);
            response.setResponse("Internal error");
            response.setResponseCode(0);
        }
        
        responseObserver.onNext(response.build());
        responseObserver.onCompleted();
    }
    
    
}

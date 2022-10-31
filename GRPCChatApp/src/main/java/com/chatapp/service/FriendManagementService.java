/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.chatapp.service;

import com.chatapp.common.ServiceResponse;
import com.chatapp.common.User;
import com.chatapp.database.MySqlConnection;
import com.chatapp.friends.AnswerRequest;
import com.chatapp.friends.FriendList;
import com.chatapp.friends.FriendManagingServiceGrpc;
import com.chatapp.friends.UserFriend;
import com.chatapp.friends.UserList;
import com.chatapp.login.ServerResponse;
import com.google.protobuf.MessageLite;
import io.grpc.stub.StreamObserver;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author maria
 */
public class FriendManagementService extends FriendManagingServiceGrpc.FriendManagingServiceImplBase {

    @Override
    public void setFriendship(AnswerRequest request, StreamObserver<ServiceResponse> responseObserver) {
        
    }

    @Override
    public void getFriends(User request, StreamObserver<FriendList> responseObserver) {
        int userId = request.getUserId();
        

        FriendList.Builder response = FriendList.newBuilder();
        UserFriend.Builder friend = UserFriend.newBuilder();
        MySqlConnection database = new MySqlConnection();
        try {
            JSONArray results = database.fetchFriends(userId);
            for (int i = 0; i < results.length(); i++) {
                JSONObject jsonobject = results.getJSONObject(i);
                friend.setUser(User.newBuilder()
                        .setUsername(jsonobject.getString("user_login"))
                        .setUserId(jsonobject.getInt("contact_friend_id")))
                        .setAlias(jsonobject.getString("contact_alias"))
                        .setIsSender(jsonobject.getInt("contact_friend_id") == jsonobject.getInt("chat_user_sender"))
                        .setLastMsg(jsonobject.getString("last_message"))
                        .setTimestamp(jsonobject.getString("last_message_time"))
                        .setUnseenChats(jsonobject.getInt("unseen_chats"));
            response.addFriends(friend.build());
            friend.clear();
            }
            
        } catch (Exception ex) {
            Logger.getLogger(LoginService.class.getName()).log(Level.SEVERE, null, ex);
        }
        responseObserver.onNext(response.build());
        responseObserver.onCompleted();
    }

    @Override
    public void getRequests(User request, StreamObserver<UserList> responseObserver) {
        
    }

}

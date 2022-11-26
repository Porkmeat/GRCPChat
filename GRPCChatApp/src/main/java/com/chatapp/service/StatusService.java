/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.chatapp.service;

import com.chatapp.common.GetRequest;
import com.chatapp.common.User;
import com.chatapp.database.MySqlConnection;
import com.chatapp.grpcchatapp.JWToken;
import com.chatapp.status.StatusServiceGrpc;
import com.chatapp.status.StatusUpdate;
import io.grpc.stub.StreamObserver;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Mariano
 */
public class StatusService extends StatusServiceGrpc.StatusServiceImplBase {

    private final ConcurrentHashMap<Integer, StreamObserver<StatusUpdate>> statusObservers;

    public StatusService(ConcurrentHashMap<Integer, StreamObserver<StatusUpdate>> statusObservers) {
        this.statusObservers = statusObservers;
    }

    @Override
    public void receiveStatus(GetRequest request, StreamObserver<StatusUpdate> responseObserver) {
        JWToken token = new JWToken(request.getToken());
        if (token.isValid()) {
            String username = token.getUsername();
            int userId = token.getUserId();
            statusObservers.put(userId, responseObserver);

            MySqlConnection database = new MySqlConnection();
            StatusUpdate statusUpdate = StatusUpdate.newBuilder()
                    .setUser(User.newBuilder().setUsername(username).setUserId(userId))
                    .setStatus(StatusUpdate.Status.ONLINE).build();
            try {
                ArrayList<Integer> friendList = database.getFriendList(userId);
                for (int friend : friendList) {
                    if (statusObservers.containsKey(friend)) {
                        statusObservers.get(friend).onNext(statusUpdate);
                    }
                }
            } catch (SQLException ex) {
                Logger.getLogger(FriendManagementService.class
                        .getName()).log(Level.SEVERE, null, ex);
            }

        }
    }
}

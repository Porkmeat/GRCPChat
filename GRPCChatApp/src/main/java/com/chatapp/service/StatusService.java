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
 * gRPC service for handling online/offline status updates.
 *
 * @author Mariano Cuneo
 */
public class StatusService extends StatusServiceGrpc.StatusServiceImplBase {

    private final ConcurrentHashMap<Integer, StreamObserver<StatusUpdate>> statusObservers;

    /**
     * Class Constructor.
     *
     * @param statusObservers contains all currently active
     * <code>StatusService</code> streams.
     */
    public StatusService(ConcurrentHashMap<Integer, StreamObserver<StatusUpdate>> statusObservers) {
        this.statusObservers = statusObservers;
    }

    /**
     * RPC method to request a long-lived stream to be started. This method
     * starts a long-lived stream and adds it to <code>statusObservers</code>
     * for later access. It also notifes other users of the online status
     * update.
     *
     * @param request client request message. Must contain a valid JWToken.
     * @param responseObserver the call's stream observer for the long-lived
     * stream.
     */
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

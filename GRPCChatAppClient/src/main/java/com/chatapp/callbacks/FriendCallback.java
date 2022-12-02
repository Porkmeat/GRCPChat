/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.chatapp.callbacks;

import com.chatapp.dataobjects.Friend;
import com.chatapp.friends.UserFriend;
import com.chatapp.listeners.FriendListener;
import com.google.common.io.Files;
import io.grpc.stub.StreamObserver;
import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.image.Image;

/**
 * Callback for gRPC Async <code>UserFriend</code> server stream.
 *
 * @author Mariano Cuneo
 */
public class FriendCallback implements StreamObserver<UserFriend> {

    private final ArrayList<FriendListener> friendListeners;
    private final String tmpFolder;

    /**
     * Class constructor.
     *
     * @param friendListeners listeners to be updated on callbacks.
     * @param tmpFolder directory of temporary folder for current user session.
     */
    public FriendCallback(ArrayList<FriendListener> friendListeners, String tmpFolder) {
        this.friendListeners = friendListeners;
        this.tmpFolder = tmpFolder;
    }

    /**
     * Handles incoming user information and generates a <code>Friend</code>
     * object. This method also saves the friend's profile picture to the
     * <code>tmpFolder</code> directory if a profile picture is present.
     * Finally, it notifies the <code>friendListeners</code>.
     *
     * @param user  <code>UserFriend</code> message sent by server.
     */
    @Override
    public void onNext(UserFriend user) {

        var friend = new Friend(user.getUser().getUsername(), user.getUser().getUserId(),
                user.getAlias(), user.getIsSender(), user.getUnseenChats(), user.getLastMsg(), LocalDateTime.now());

        if (!user.getProfilePicture().isEmpty()) {
            File profilePicture = new File(tmpFolder + "/" + user.getUser().getUsername() + ".jpg");
            try {
                Files.write(user.getProfilePicture().toByteArray(), profilePicture);
                friend.setProfilePicture(new Image(profilePicture.getAbsolutePath()));
            } catch (IOException ex) {
                Logger.getLogger(FriendCallback.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        friend.setIsOnline(user.getIsOnline());

        switch (user.getType()) {
            case FRIEND -> {
                Instant timestampUTC = Instant.parse(user.getTimestamp() + "Z");
                LocalDateTime localTime = timestampUTC.atZone(ZoneId.systemDefault()).toLocalDateTime();
                friend.setTimestamp(localTime);
                for (FriendListener listener : friendListeners) {
                    listener.addChat(friend);
                }
            }
            case REQUEST -> {
                for (FriendListener listener : friendListeners) {
                    listener.request(friend);
                }
            }
            default ->
                Logger.getLogger(FriendCallback.class.getName()).info("Error occurred");
        }
    }

    @Override
    public void onError(Throwable error) {
        Logger.getLogger(FriendCallback.class.getName()).info(error.getCause().toString());
    }

    @Override
    public void onCompleted() {
        Logger.getLogger(FriendCallback.class.getName()).info("Stream Ended");
    }

}

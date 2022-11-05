/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.chatapp.callbacks;

import com.chatapp.friends.UserFriend;
import io.grpc.stub.StreamObserver;
import java.util.logging.Logger;

/**
 *
 * @author Mariano
 */
public class FriendCallback implements StreamObserver<UserFriend>{

    @Override
    public void onNext(UserFriend v) {
        // implement new message get
    }

    @Override
    public void onError(Throwable thrwbl) {
        Logger.getLogger(FriendCallback.class.getName()).info("Error occurred");
    }

    @Override
    public void onCompleted() {
        Logger.getLogger(FriendCallback.class.getName()).info("Stream Ended");
    }
    
}

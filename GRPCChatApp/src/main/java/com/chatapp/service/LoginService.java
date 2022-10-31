/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.chatapp.service;

import com.chatapp.common.Empty;
import com.chatapp.database.MySqlConnection;
import com.chatapp.login.LoginRequest;
import com.chatapp.login.LoginServiceGrpc;
import com.chatapp.login.ServerResponse;
import io.grpc.stub.StreamObserver;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author maria
 */
public class LoginService extends LoginServiceGrpc.LoginServiceImplBase {

    @Override
    public void logout(Empty request, StreamObserver<ServerResponse> responseObserver) {

    }

    @Override
    public void login(LoginRequest request, StreamObserver<ServerResponse> responseObserver) {
        String username = request.getUsername();
        String password = request.getPassword();

        ServerResponse.Builder response = ServerResponse.newBuilder();
        MySqlConnection database = new MySqlConnection();
        try {
            if (database.checkPassword(username, password)) {
                response.setToken("Success").setResponseCode(1);
            } else {
                response.setToken("Connection Failed!").setResponseCode(0);
            }
            
        } catch (Exception ex) {
            Logger.getLogger(LoginService.class.getName()).log(Level.SEVERE, null, ex);
        }
        responseObserver.onNext(response.build());
        responseObserver.onCompleted();
    }
}

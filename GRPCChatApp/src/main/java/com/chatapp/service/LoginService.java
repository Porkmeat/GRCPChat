/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.chatapp.service;

import com.chatapp.common.GetRequest;
import com.chatapp.database.MySqlConnection;
import com.chatapp.grpcchatapp.JWToken;
import com.chatapp.login.LoginRequest;
import com.chatapp.login.LoginServiceGrpc;
import com.chatapp.login.ServerResponse;
import io.grpc.stub.StreamObserver;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.codec.digest.DigestUtils;

/**
 *
 * @author maria
 */
public class LoginService extends LoginServiceGrpc.LoginServiceImplBase {

    @Override
    public void logout(GetRequest request, StreamObserver<ServerResponse> responseObserver) {

    }

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
                response.setToken("Connection Failed!").setResponseCode(0);
            }

        } catch (Exception ex) {
            Logger.getLogger(LoginService.class.getName()).log(Level.SEVERE, null, ex);
        }
        responseObserver.onNext(response.build());
        responseObserver.onCompleted();
    }

    @Override
    public void createAccount(LoginRequest request, StreamObserver<ServerResponse> responseObserver) {
        String username = request.getUsername();
        String password = request.getPassword();

        Random random = new Random();
        int salt = random.nextInt(10000);
        String saltedpass = password + String.valueOf(salt);
        String hashedpass = DigestUtils.sha256Hex(saltedpass);

        ServerResponse.Builder response = ServerResponse.newBuilder();
        MySqlConnection database = new MySqlConnection();
        try {
            if (database.getUserId(username) == 0) {
                database.addNewUser(username, hashedpass, salt);
                response.setToken("Account created!");
                response.setResponseCode(1);
            } else {
                response.setToken("Username not available!");
                response.setResponseCode(0);
            }
        } catch (Exception ex) {
            Logger.getLogger(LoginService.class.getName()).log(Level.SEVERE, null, ex);
            response.setToken("Internal error");
            response.setResponseCode(0);
        }

        responseObserver.onNext(response.build());
        responseObserver.onCompleted();
    }

    

}

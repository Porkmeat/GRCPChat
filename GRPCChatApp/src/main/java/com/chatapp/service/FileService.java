/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.chatapp.service;

import com.chatapp.chat.ChatMessage;
import com.chatapp.database.MySqlConnection;
import com.chatapp.filetransfer.FileChunk;
import com.chatapp.filetransfer.FileDownloadRequest;
import com.chatapp.filetransfer.FileDownloadResponse;
import com.chatapp.filetransfer.FileServiceGrpc;
import com.chatapp.filetransfer.FileUploadRequest;
import com.chatapp.filetransfer.FileUploadResponse;
import com.chatapp.filetransfer.Status;

import com.chatapp.grpcchatapp.JWToken;

import com.google.protobuf.ByteString;

import io.grpc.stub.StreamObserver;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.sql.SQLException;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Mariano
 */
public class FileService extends FileServiceGrpc.FileServiceImplBase {
    
    private final Path SERVER_BASE_PATH = Paths.get("src/main/resources");
    private final ConcurrentHashMap<Integer, StreamObserver<ChatMessage>> messageObservers;
    
    public FileService(ConcurrentHashMap<Integer, StreamObserver<ChatMessage>> messageObservers) {
        this.messageObservers = messageObservers;
    }
    
    @Override
    public StreamObserver<FileUploadRequest> fileUpload(StreamObserver<FileUploadResponse> responseObserver) {
        return new StreamObserver<FileUploadRequest>() {

            // upload context variables
            OutputStream writer;
            Status status = Status.IN_PROGRESS;
            boolean isProfilePicture;
            JWToken validToken = null;
            int friendId;
            String fileName;
            double fileSize;
            
            @Override
            public void onNext(FileUploadRequest fileUploadRequest) {
                try {
                    if (fileUploadRequest.hasMetadata()) {
                        JWToken token = new JWToken(fileUploadRequest.getMetadata().getToken());
                        if (token.isValid()) {
                            validToken = token;
                            friendId = fileUploadRequest.getMetadata().getFriend().getUserId();
                            fileName = fileUploadRequest.getMetadata().getFileName() + "." + fileUploadRequest.getMetadata().getFileType();
                            isProfilePicture = fileUploadRequest.getMetadata().getIsProfilePic();
                            fileSize = Math.round(fileUploadRequest.getMetadata().getFileSize() * 100.0) / 100.0;
                            writer = getFilePath(token, fileUploadRequest);
                        } else {
                            status = Status.FAILED;
                            this.onCompleted();
                        }
                    } else {
                        if (this.validToken != null) {
                            writeFile(writer, fileUploadRequest.getFileChunk().getContent());
                        } else {
                            status = Status.FAILED;
                            this.onCompleted();
                        }
                    }
                } catch (IOException e) {
                    this.onError(e);
                }
            }
            
            @Override
            public void onError(Throwable throwable) {
                status = Status.FAILED;
                this.onCompleted();
            }
            
            @Override
            public void onCompleted() {
                if (writer != null) {
                    closeFile(writer);
                }
                status = Status.IN_PROGRESS.equals(status) ? Status.SUCCESS : status;
                if (!isProfilePicture && Status.SUCCESS.equals(status)) {
                    MySqlConnection database = new MySqlConnection();
                    try {
                        database.saveFile(validToken.getUserId(), friendId, fileName, fileSize);
                    } catch (SQLException ex) {
                        Logger.getLogger(FileService.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    if (messageObservers.containsKey(friendId)) {
                        
                        ChatMessage.Builder chatMessage = ChatMessage.newBuilder();
                        chatMessage.setSenderId(validToken.getUserId())
                                .setMessage(fileName + " " + fileSize)
                                .setTimestamp(Instant.now().toString())
                                .setSeen(false)
                                .setIsFile(true);
                        messageObservers.get(friendId).onNext(chatMessage.build());
                    }
                }
                FileUploadResponse response = FileUploadResponse.newBuilder()
                        .setStatus(status)
                        .build();
                responseObserver.onNext(response);
                responseObserver.onCompleted();
            }
        };
    }
    
    @Override
    public void fileDownload(FileDownloadRequest request, StreamObserver<FileDownloadResponse> responseObserver) {
        
        JWToken token = new JWToken(request.getMetadata().getToken());
        if (token.isValid()) {
            try {
                Path fileLocation;
                if (request.getMetadata().getIsProfilePic()) {
                    fileLocation = Paths.get(SERVER_BASE_PATH.toString(), "/profilepictures/",
                            token.getUsername()+ "."+ request.getMetadata().getFileType());
                } else {
                    long chatUUID = MySqlConnection.generateChatUuid(token.getUserId(), request.getMetadata().getFriend().getUserId());
                    fileLocation = Paths.get(SERVER_BASE_PATH.toString(), "/chatfiles/" + chatUUID,
                             request.getMetadata().getFileName() + "." + request.getMetadata().getFileType());
                }
                try ( InputStream inputStream = Files.newInputStream(fileLocation)) {
                    
                    byte[] bytes = new byte[4 * 1024];
                    int size;
                    while ((size = inputStream.read(bytes)) > 0) {
                        FileDownloadResponse fileChunk = FileDownloadResponse.newBuilder()
                                .setFileChunk(FileChunk.newBuilder().setContent(ByteString.copyFrom(bytes, 0, size))).build();
                        responseObserver.onNext(fileChunk);
                    }
// close the stream
                }
                responseObserver.onCompleted();
            } catch (IOException ex) {
                responseObserver.onCompleted();
                System.out.println("File not found.");
            }
        }
    }
    
    private OutputStream getFilePath(JWToken token, FileUploadRequest request) throws IOException {
        boolean isProfilePicture = request.getMetadata().getIsProfilePic();
        
        Path saveLocation;
        if (request.getMetadata().getIsProfilePic()) {
            saveLocation = Paths.get(SERVER_BASE_PATH.toString(), "/profilepictures");
        } else {
            long chatUUID = MySqlConnection.generateChatUuid(token.getUserId(), request.getMetadata().getFriend().getUserId());
            saveLocation = Paths.get(SERVER_BASE_PATH.toString(), "/chatfiles/" + chatUUID);
            // create new dir in chatfiles with chatuid
            Files.createDirectories(saveLocation);
        }
        
        String fileName = (isProfilePicture ? token.getUsername() : request.getMetadata().getFileName())
                + "." + request.getMetadata().getFileType();
        
        return Files.newOutputStream(saveLocation.resolve(fileName), StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING);
    }
    
    private void writeFile(OutputStream writer, ByteString content) throws IOException {
        writer.write(content.toByteArray());
        writer.flush();
    }
    
    private void closeFile(OutputStream writer) {
        try {
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
}

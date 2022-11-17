/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.chatapp.service;

import com.chatapp.database.MySqlConnection;
import com.chatapp.file.File;
import com.chatapp.file.FileDownloadRequest;
import com.chatapp.file.FileServiceGrpc;
import com.chatapp.file.FileUploadRequest;
import com.chatapp.file.FileUploadResponse;
import com.chatapp.file.Status;
import com.chatapp.grpcchatapp.JWToken;
import com.google.protobuf.ByteString;
import io.grpc.stub.StreamObserver;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/**
 *
 * @author Mariano
 */
public class FileService extends FileServiceGrpc.FileServiceImplBase {

    private final Path SERVER_BASE_PATH = Paths.get("src/main/resources");

    @Override
    public StreamObserver<FileUploadRequest> fileUpload(StreamObserver<FileUploadResponse> responseObserver) {
        return new StreamObserver<FileUploadRequest>() {

            // upload context variables
            OutputStream writer;
            Status status = Status.IN_PROGRESS;
            boolean isProfilePicture;

            @Override
            public void onNext(FileUploadRequest fileUploadRequest) {
                try {
                    if (fileUploadRequest.hasMetadata()) {
                        JWToken token = new JWToken(fileUploadRequest.getMetadata().getToken());
                        if (token.isValid()) {
                            isProfilePicture = fileUploadRequest.getMetadata().getIsProfilePic();
                            writer = getFilePath(token, fileUploadRequest);
                        } else {
                            status = Status.FAILED;
                            this.onCompleted();
                        }
                    } else {
                        writeFile(writer, fileUploadRequest.getFile().getContent());
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
                FileUploadResponse response = FileUploadResponse.newBuilder()
                        .setStatus(status)
                        .build();
                responseObserver.onNext(response);
                responseObserver.onCompleted();
            }
        };
    }

    @Override
    public void fileDownload(FileDownloadRequest request, StreamObserver<File> responseObserver) {

    }

    private OutputStream getFilePath(JWToken token, FileUploadRequest request) throws IOException {
        boolean isProfilePicture = request.getMetadata().getIsProfilePic();
        
        Path saveLocation = Paths.get(SERVER_BASE_PATH.toString(), isProfilePicture ? "/profilepictures" : "/chatfiles");
        
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

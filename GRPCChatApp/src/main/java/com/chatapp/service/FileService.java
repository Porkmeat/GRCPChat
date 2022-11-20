/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.chatapp.service;

import com.chatapp.file.FileChunk;
import com.chatapp.file.FileDownloadRequest;
import com.chatapp.file.FileDownloadResponse;
import com.chatapp.file.FileServiceGrpc;
import com.chatapp.file.FileUploadRequest;
import com.chatapp.file.FileUploadResponse;
import com.chatapp.file.Status;

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

import java.util.Arrays;

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
            boolean tokenIsValid = true;
            boolean isProfilePicture;

            @Override
            public void onNext(FileUploadRequest fileUploadRequest) {
                try {
                    if (fileUploadRequest.hasMetadata()) {
                        JWToken token = new JWToken(fileUploadRequest.getMetadata().getToken());
                        if (token.isValid()) {
                            tokenIsValid = true;
                            isProfilePicture = fileUploadRequest.getMetadata().getIsProfilePic();
                            writer = getFilePath(token, fileUploadRequest);
                        } else {
                            status = Status.FAILED;
                            this.onCompleted();
                        }
                    } else {
                        if (tokenIsValid) {
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
                Path fileLocation = Paths.get(SERVER_BASE_PATH.toString(), (request.getMetadata().getIsProfilePic() ? "/profilepictures" : "/chatfiles"),
                        "/", request.getMetadata().getFileName());

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
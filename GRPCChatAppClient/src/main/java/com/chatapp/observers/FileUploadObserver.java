/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.chatapp.observers;

import com.chatapp.dataobjects.Chat;
import com.chatapp.filetransfer.FileUploadResponse;
import com.chatapp.filetransfer.Status;
import com.chatapp.listeners.FileListener;
import io.grpc.stub.StreamObserver;
import java.util.ArrayList;
import java.util.logging.Logger;

/**
 *
 * @author Mariano
 */
public class FileUploadObserver implements StreamObserver<FileUploadResponse> {

    private final ArrayList<FileListener> fileListeners;
    private boolean success = false;
    private final String fileName;
    private final Chat chat;

    public FileUploadObserver(ArrayList<FileListener> fileListeners, String fileName, Chat chat) {
        this.fileListeners = fileListeners;
        this.fileName = fileName;
        this.chat = chat;
    }
    
    public FileUploadObserver(ArrayList<FileListener> fileListeners, String fileName) {
        this.fileListeners = fileListeners;
        this.fileName = fileName;
        this.chat = null;
    }

    @Override
    public void onNext(FileUploadResponse fileUploadResponse) {
        System.out.println(
                "File upload status :: " + fileUploadResponse.getStatus()
        );
        if (fileUploadResponse.getStatus() == Status.SUCCESS) {
            this.success = true;
        } else if (fileUploadResponse.getStatus() == Status.FAILED) {
            this.success = false;
        }
    }

    @Override
    public void onError(Throwable error) {
        Logger.getLogger(FileUploadObserver.class.getName()).info(error.getCause().toString());
    }

    @Override
    public void onCompleted() {
        for (FileListener listener : fileListeners) {
            listener.fileSent(chat, success, fileName);
        }
    }

}

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.chatapp.observers;

import com.chatapp.file.FileUploadResponse;
import io.grpc.stub.StreamObserver;
import java.util.logging.Logger;

/**
 *
 * @author Mariano
 */
public class FileUploadObserver implements StreamObserver<FileUploadResponse> {

    @Override
    public void onNext(FileUploadResponse fileUploadResponse) {
        System.out.println(
                "File upload status :: " + fileUploadResponse.getStatus()
        );
    }

    @Override
    public void onError(Throwable error) {
        Logger.getLogger(FileUploadObserver.class.getName()).info(error.getCause().toString());
    }

    @Override
    public void onCompleted() {

    }

}

package com.chatapp.chatappgui;

import com.chatapp.chatappgui.controllers.LoginController;
import com.chatapp.grpcchatappclient.GRPCChatAppClient;
import java.io.IOException;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Appgui extends Application {
    
    private static Scene scene;
    private static GRPCChatAppClient client;

    @Override
    public void start(Stage stage) throws IOException {

        FXMLLoader fxmlLoader = new FXMLLoader(Appgui.class.getResource("loginfxml.fxml"));
        scene = new Scene(fxmlLoader.load(), 640, 480);
        
        //send client to controller
        LoginController controller = fxmlLoader.getController();
        controller.setClient(client);
        
        scene.getStylesheets().add(getClass().getResource("fxml.css").toExternalForm());

        stage.setTitle("MacChat");
        stage.setScene(scene);

        stage.show();
    }


    public static void main(String[] args) {
        client = new GRPCChatAppClient("localhost", 8818);
        client.connect();
        launch(args);
        
    }
}
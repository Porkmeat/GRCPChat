package com.chatapp.chatappgui.controllers;

import com.chatapp.chatappgui.Appgui;
import com.chatapp.grpcchatappclient.GRPCChatAppClient;
import com.jfoenix.controls.JFXSpinner;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

/**
 * FXML Controller class for the Log in screen. UI is built with JavaFX. All
 * methods annotated with FXML reffer to UI interactions and all fields
 * annotated with FXML are UI components.
 *
 * @author Mariano Cuneo
 */
public class LoginController {

    private GRPCChatAppClient client;
    private Stage stage;
    private Scene scene;

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private TextField newUsernameField;

    @FXML
    private PasswordField newUserPasswordField;

    @FXML
    private PasswordField newUserPasswordConfirmField;

    @FXML
    private AnchorPane createAccountPane;

    @FXML
    private AnchorPane loginPane;

    @FXML
    private JFXSpinner loadingSpinner;

    @FXML
    private void login() {

        loginPane.setDisable(true);
        loadingSpinner.setVisible(true);
        new Thread(() -> {

            if (client.login(usernameField.getText(), passwordField.getText())) {
                System.out.println("Connection successful!");
                Platform.runLater(() -> {
                    try {
                        switchToMainScene();
                    } catch (IOException ex) {
                        Logger.getLogger(LoginController.class.getName()).log(Level.SEVERE, null, ex);
                    }
                });
            } else {
                System.out.println("Connection failed!");
                Platform.runLater(() -> {
                    loadingSpinner.setVisible(false);
                    loginPane.setDisable(false);
                });
            }
        }).start();
    }

    @FXML
    private void createUser() {

        createAccountPane.setDisable(true);
        loadingSpinner.setVisible(true);
        new Thread(() -> {

            String user = newUsernameField.getText();
            String pass = newUserPasswordField.getText();
            if (user.equals(newUserPasswordConfirmField.getText())) {
                if (client.createUser(user, pass)) {
                    System.out.println("Account created!");
                    usernameField.setText(user);
                    passwordField.setText(pass);
                    Platform.runLater(() -> {
                        login();
                    });
                } else {
                    System.out.println("Failed!");
                    Platform.runLater(() -> {

                        loadingSpinner.setVisible(false);
                        createAccountPane.setDisable(false);
                    });

                }
            }
        }).start();
    }

    @FXML
    private void closeCreateAccontPane() {
        createAccountPane.setVisible(false);
        createAccountPane.setDisable(true);
        loginPane.setDisable(false);
    }

    @FXML
    private void openCreateAccontPane() {
        createAccountPane.setVisible(true);
        createAccountPane.setDisable(false);
        loginPane.setDisable(true);
    }

    /**
     * Passes the client for the controller to be able to interact with.
     *
     * @param client the main client that handles non-UI logic and server
     * interaction.
     */
    public void setClient(GRPCChatAppClient client) {
        this.client = client;
    }

    private void switchToMainScene() throws IOException {

        String username = usernameField.getText();

        usernameField.clear();
        passwordField.clear();

        FXMLLoader fxmlLoader = new FXMLLoader(Appgui.class.getResource("mainscreenfxml.fxml"));
        scene = new Scene(fxmlLoader.load(), 640, 480);
        stage = (Stage) usernameField.getScene().getWindow();

        MainScreenController controller = fxmlLoader.getController();
        controller.setupController(client, username);

        stage.setOnCloseRequest(event -> {
            event.consume();
            controller.logoff(stage);
        });

        scene.getStylesheets().add(Appgui.class.getResource("fxml.css").toExternalForm());

        stage.setScene(scene);

        stage.setMinHeight(600);
        stage.setMinWidth(900);
        stage.show();
    }
}

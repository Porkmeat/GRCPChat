package com.chatapp.chatappgui;

import com.chatapp.grpcchatappclient.GRPCChatAppClient;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 * FXML Controller class
 *
 * @author Mariano
 */
public class LoginController {

    /**
     * Initializes the controller class.
     */
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
    private void login() {
        try {
            if (client.login(usernameField.getText(), passwordField.getText())) {
                System.out.println("Connection successful!");
                switchToMainScene();
            } else {
                System.out.println("Connection failed!");
            }
        } catch (IOException ex) {
            Logger.getLogger(LoginController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @FXML
    private void createUser() {
        String user = newUsernameField.getText();
        String pass = newUserPasswordField.getText();
        if (user.equals(newUserPasswordConfirmField.getText())) {
            try {
                if (client.createUser(user, pass)) {
                    System.out.println("Account created!");
                    usernameField.setText(user);
                    passwordField.setText(pass);
                    login();
                } else {
                    System.out.println("Failed!");
                }
            } catch (IOException ex) {
                Logger.getLogger(LoginController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

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

        //send client to controller
        MainScreenController controller = fxmlLoader.getController();
        controller.setupController(client, username);

        stage.setOnCloseRequest(event -> {
            event.consume();
            controller.logoff(stage);
        });

        scene.getStylesheets().add(getClass().getResource("fxml.css").toExternalForm());

        stage.setScene(scene);
        
        stage.setMinHeight(600);
        stage.setMinWidth(900);
        stage.show();
    }
}

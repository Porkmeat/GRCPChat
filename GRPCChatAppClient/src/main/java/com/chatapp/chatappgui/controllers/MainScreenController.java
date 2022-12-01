package com.chatapp.chatappgui.controllers;

import com.chatapp.dataobjects.Chat;
import com.chatapp.chatappgui.javafxelements.ChatListCell;
import com.chatapp.dataobjects.Friend;
import com.chatapp.chatappgui.javafxelements.FriendListCell;
import com.chatapp.chatappgui.javafxelements.RequestListCell;
import com.chatapp.listeners.FriendListener;
import com.chatapp.grpcchatappclient.GRPCChatAppClient;
import com.chatapp.listeners.FileListener;
import com.chatapp.listeners.MessageListener;
import com.chatapp.listeners.RequestListener;
import com.chatapp.listeners.StatusListener;
import com.jfoenix.controls.JFXSpinner;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class MainScreenController implements StatusListener, MessageListener, RequestListener, FriendListener, FileListener {

    private GRPCChatAppClient client;
    private int currentChat;
    private final HashMap<Integer, ListView> activeChats = new HashMap<>();
    private ListView<Chat> activeChat;
    private Friend requester;

    @FXML
    private Circle mainuserimg;
    @FXML
    private ToggleButton usercardtoggle;
    @FXML
    private ListView<Friend> userlist;
    @FXML
    private ListView<Friend> requestlist;
    @FXML
    private Label mainusername;
    @FXML
    private Label mainchatusername;
    @FXML
    private BorderPane chatscreen;
    @FXML
    private TextArea chatinput;
    @FXML
    private ScrollPane chatwindow;
    @FXML
    private TextField addFriendField;
    @FXML
    private Tab requestTab;
    @FXML
    private HBox requestButtons;
    @FXML
    private TabPane mainTabPane;
    @FXML
    private ImageView mainchatimg;
    @FXML
    private JFXSpinner profilePicSpinner;
    @FXML
    private Label profilePicLabel;

    public void setupController(GRPCChatAppClient client, String username) {
        mainusername.setText(username);
        mainuserimg.setStyle(username);
        this.client = client;
        this.client.addStatusListener(this);
        this.client.addMessageListener(this);
        this.client.addFriendListener(this);
        this.client.addFileListener(this);
        this.client.requestStreams();
        new Thread(() -> {
            String filePath = client.fetchFile(mainusername.getText(), "jpg", "", true, 0);
            if (!filePath.isEmpty()) {
                setProfilePicture(new Image(filePath));
            } else {
                profilePicLabel.setOpacity(1);
            }
        }).start();

        ObservableList<Friend> friends = FXCollections.observableArrayList(Friend.extractor());
        userlist.setCellFactory((ListView<Friend> userlist1) -> new FriendListCell(client.getTmpFolder()));
        userlist.setItems(friends);

        userlist.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Friend>() {
            @Override
            public void changed(ObservableValue<? extends Friend> ov, Friend t, Friend t1) {
                if (t1 != null) {
                    String friendUsername = userlist.getSelectionModel().getSelectedItem().getUsername();
                    currentChat = userlist.getSelectionModel().getSelectedItem().getUserId();
                    if (!activeChats.containsKey(currentChat)) {
                        ListView<Chat> newChat = new ListView<>();
                        newChat.setCellFactory((ListView<Chat> newChat1) -> new ChatListCell(MainScreenController.this));
                        activeChats.put(currentChat, newChat);
                        client.fetchMessages(friendUsername, currentChat);
                    }

                    if (mainchatusername.getText() != null && !mainchatusername.getText().equals(friendUsername)) {

                        Platform.runLater(() -> {
                            activeChat = activeChats.get(currentChat);
                            chatwindow.setContent(activeChat);
                            autoScroll();
                            mainchatusername.setText(friendUsername);
                            mainchatimg.setImage(t1.getProfilePicture());
                        });

                        if (!chatscreen.isVisible()) {
                            Platform.runLater(() -> {
                                chatscreen.setVisible(true);
                            });
                        }
                    }
                }
            }
        });

        requestlist.setCellFactory((ListView<Friend> requestlist1) -> new RequestListCell());

        requestlist.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Friend>() {
            @Override
            public void changed(ObservableValue<? extends Friend> ov, Friend t, Friend t1) {
                requester = requestlist.getSelectionModel().getSelectedItem();
                if (requester != null && requestButtons.isDisable()) {
                    requestButtons.setDisable(false);
                }
            }
        });

        chatinput.addEventFilter(KeyEvent.KEY_PRESSED, new EnterKeyHandler());

        this.client.getFriendsAndRequests();
    }

    public void logoff(Stage stage) {

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Logout");
        alert.setContentText("Are you sure you want to log out?");

        if (alert.showAndWait().get() == ButtonType.OK) {
            client.logoff();
            System.out.println("You logged out!");
            Platform.runLater(() -> {
                stage.close();
            });
        }
    }

    @Override
    public void online(String username) {
        for (Friend friend : userlist.getItems()) {
            if (friend.getUsername().equals(username)) {
                friend.setIsOnline(true);
            }
        }
    }

    @Override
    public void offline(String username) {

        for (Friend friend : userlist.getItems()) {
            if (friend.getUsername().equals(username)) {
                friend.setIsOnline(false);
            }
        }
    }

//    @FXML
//    public void activateUserToggle() {
//        usercardtoggle.fire();
//    }
//    @FXML
//    public void openUserCard() {
//        Duration cycleDuration = Duration.millis(500);
//        Timeline timeline;
//        if (usercardtoggle.isSelected()) {
//            timeline = new Timeline(
//                    new KeyFrame(cycleDuration,
//                            new KeyValue(usercard.prefHeightProperty(), 300, Interpolator.EASE_BOTH))
//            );
//        } else {
//            timeline = new Timeline(
//                    new KeyFrame(cycleDuration,
//                            new KeyValue(usercard.prefHeightProperty(), 60, Interpolator.EASE_BOTH))
//            );
//        }
//
//        timeline.play();
//    }
    @FXML
    public void openPictureChooser() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose new Profile Picture");
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.jpg", "*.jpeg", "*.png"));

        File picture = fileChooser.showOpenDialog(mainuserimg.getScene().getWindow());
        if (picture != null) {
            profilePicChange(picture);
        } else {
            System.out.println("Wrong file or no file.");
        }
    }

    private void profilePicChange(File picture) {
        profilePicSpinner.setVisible(true);
        profilePicLabel.setVisible(false);
        mainuserimg.setOpacity(0.5);
        new Thread(() -> {
            try {
                client.uploadProfilePicture(picture);
            } catch (IOException ex) {
                Logger.getLogger(MainScreenController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }).start();
    }

    @FXML
    public void openFileChooser() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose File");
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("All Files", "*.*"));
        Friend friend = userlist.getSelectionModel().getSelectedItem();

        File file = fileChooser.showOpenDialog(mainuserimg.getScene().getWindow());

        if (file != null) {
            if (file.length() / (1024 * 1024) < 20) {
                sendFile(file, friend);
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Chosen File is too big");
                alert.setContentText("Choose a file under 20Mb");
                alert.show();
            }
        } else {
            System.out.println("Wrong file or no file.");
        }
    }

    public void downloadFile(String fileName, String fileType, Chat chat) {
        String fileData = chat.getMessage();
        chat.setMessage("Downloading " + fileName + "." + fileType + "...");
        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialFileName(fileName);
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(".jpg", "*." + fileType));
        File file = fileChooser.showSaveDialog(mainuserimg.getScene().getWindow());
        if (file != null) {
            new Thread(() -> {
                String downloadDir = client.fetchFile(fileName, fileType, file.getParent(), false, userlist.getSelectionModel().getSelectedItem().getUserId());
                if (!downloadDir.isEmpty()) {
                    chat.setMessage( fileName + "." + fileType + " finished downloading.");
                } else {
                    chat.setMessage(fileName + "." + fileType + " - Download failed");
                }
            }).start();
        } else {
            chat.setMessage(fileData);
            chat.setIsFile(true);
        }
    }

    private void sendFile(File file, Friend reciever) {
        String message = "Sending File: " + file.getName() + "...";
        LocalDateTime now = LocalDateTime.now();
        Chat newMessage = new Chat(message, true, now);
        activeChat.getItems().add(newMessage);
        autoScroll();
        new Thread(() -> {
            try {
                client.uploadFile(file, reciever.getUserId(), reciever.getUsername(), newMessage);
            } catch (IOException ex) {
                Platform.runLater(() -> {
                    String failMessage = file.getName() + " upload failed.";
                    newMessage.setMessage(failMessage);
                    reciever.setLastMsg(failMessage);
                });
                System.out.println("Error uploading file");
                Logger.getLogger(MainScreenController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }).start();
    }

    @Override
    public void fileSent(Chat chat, boolean success, String fileName) {
        Platform.runLater(() -> {
            if (chat != null) {
                if (success) {
                    String successMessage = fileName + " sent.";
                    chat.setMessage(successMessage);
                } else {
                    String failMessage = fileName + " upload failed.";
                    chat.setMessage(failMessage);
                }
            } else {
                if (success) {
                    setProfilePicture(new Image(fileName));
                } else {
                    System.out.println("File upload Failed");
                }
                profilePicSpinner.setVisible(false);
                profilePicLabel.setVisible(true);
                mainuserimg.setOpacity(1);
            }
        });
    }

    @FXML
    public void sendMsg() throws IOException {
        if (chatinput.getText() != null && !chatinput.getText().isBlank()) {
            String message = chatinput.getText().trim();
            LocalDateTime now = LocalDateTime.now();
            int activeChatSize = activeChat.getItems().size();
            LocalDateTime lastMsgDate = activeChat.getItems().get(activeChatSize - 1).getTimestamp();

            if (activeChatSize == 0 || !now.toLocalDate().equals(lastMsgDate.toLocalDate())) {
                Platform.runLater(() -> {
                    activeChat.getItems().add(new Chat(now));
                });
            }

            Chat newMessage = new Chat(message, true, now);
            Platform.runLater(() -> {
                activeChat.getItems().add(newMessage);
                autoScroll();
                Friend friend = userlist.getSelectionModel().getSelectedItem();
                friend.setLastMsg(message);
                friend.setTimestamp(now);
                userlist.getItems().sort(Comparator.naturalOrder());
            });
            client.msg("", currentChat, message);
            Platform.runLater(() -> {
                chatinput.clear();
            });
        }
    }

    @Override
    public void messageGet(int fromUser, Chat message) {

        for (Friend friend : userlist.getItems()) {
            if (friend.getUserId() == fromUser) {
                Platform.runLater(() -> {
                    friend.setLastMsg(message.getMessage());
                    friend.setTimestamp(message.getTimestamp());
                    userlist.getItems().sort(Comparator.naturalOrder());
                });
            }
        }

        if (activeChats.containsKey(fromUser)) {
            ListView<Chat> chatWithUser = activeChats.get(fromUser);
            int chatWithUserSize = chatWithUser.getItems().size();
            if (chatWithUserSize == 0) {
                Platform.runLater(() -> {
                    chatWithUser.getItems().add(new Chat(message.getTimestamp()));
                });
            } else {
                LocalDateTime lastMsgDate = chatWithUser.getItems().get(chatWithUserSize - 1).getTimestamp();
                if (!message.getTimestamp().toLocalDate().equals(lastMsgDate.toLocalDate())) {
                    Platform.runLater(() -> {
                        chatWithUser.getItems().add(new Chat(message.getTimestamp()));
                    });
                }
            }
            Platform.runLater(() -> {
                chatWithUser.getItems().add(message);
                if (activeChat == chatWithUser) {
                    autoScroll();
                }
            });
        }
    }

    @FXML
    public void addFriend() throws IOException {
        String friendName = addFriendField.getText();
        client.addFriend(friendName);
        Platform.runLater(() -> {
            addFriendField.clear();
        });
    }

    private void autoScroll() {
        if (activeChat != null) {
            activeChat.scrollTo(activeChat.getItems().size());
        }
    }

    @Override
    public void request(Friend fromUser) {
        if (requestTab.isDisabled()) {
            requestTab.setDisable(false);
        }
        Platform.runLater(() -> {
            requestlist.getItems().add(fromUser);
        });
    }

    @FXML
    public void acceptRequest() {
        client.respondToRequest(requester, 1);
        System.out.println("Added friend " + requester.getUsername());
        Platform.runLater(() -> {
            if (requestlist.getItems().remove(requester)) {
                requester = null;
            }
        });
        closeRequestTab();
    }

    @FXML
    public void denyRequest() {
        client.respondToRequest(requester, 2);
        System.out.println("Request denied: " + requester.getUsername());
        Platform.runLater(() -> {
            if (requestlist.getItems().remove(requester)) {
                requester = null;
            }
        });
        closeRequestTab();
    }

    @FXML
    public void blockRequest() {
        client.respondToRequest(requester, 3);
        System.out.println("Blocked user " + requester.getUsername());
        Platform.runLater(() -> {
            if (requestlist.getItems().remove(requester)) {
                requester = null;
            }
        });
        closeRequestTab();
    }

    private void closeRequestTab() {
        Platform.runLater(() -> {
            if (requestlist.getItems().isEmpty()) {
                requestButtons.setDisable(true);
                mainTabPane.getSelectionModel().selectFirst();
                requestTab.setDisable(true);
            } else {
                requester = requestlist.getSelectionModel().getSelectedItem();
            }
        });
    }

    @Override
    public void addChat(Friend friend) {
        Platform.runLater(() -> {
            userlist.getItems().add(friend);
        });
    }

    @Override
    public void loadMessages(int fromUser, ObservableList messages) {
        ListView<Chat> chatWithUser = activeChats.get(fromUser);

        Platform.runLater(() -> {
            chatWithUser.setItems(messages);
            autoScroll();
        });
    }

    private void setProfilePicture(Image image) {

        double radius = mainuserimg.getRadius();
        final double hRad;   // horizontal "radius"
        final double vRad;   // vertical "radius"
        if (image.getWidth() != image.getHeight()) {
            double ratio = image.getWidth() / image.getHeight();
            if (ratio > 1) {
                // Width is longer, left anchor is outside
                hRad = radius * ratio;
                vRad = radius;
            } else {
                // Height is longer, top anchor is outside
                vRad = radius / ratio;
                hRad = radius;
            }
        } else {
            hRad = radius;
            vRad = radius;
        }
        Platform.runLater(() -> {
            profilePicLabel.setOpacity(0);
            mainuserimg.setFill(new ImagePattern(image, -hRad, -vRad, 2 * hRad, 2 * vRad, false));
        });

    }

    private class EnterKeyHandler implements EventHandler<KeyEvent> {

        private KeyEvent keypress;

        @Override
        public void handle(KeyEvent event) {
            if (keypress != null) {
                keypress = null;
                return;
            }

            Parent parent = chatinput.getParent();
            if (parent != null) {
                if (event.getCode() == KeyCode.ENTER) {
                    Event parentEvent = event.copyFor(parent, parent);
                    parent.fireEvent(parentEvent);
                    event.consume();
                }
            }
        }
    }

}

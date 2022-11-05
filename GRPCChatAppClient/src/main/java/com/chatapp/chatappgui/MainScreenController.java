package com.chatapp.chatappgui;

import com.chatapp.grpcchatappclient.FriendListener;
import com.chatapp.grpcchatappclient.GRPCChatAppClient;
import com.chatapp.grpcchatappclient.MessageListener;
import com.chatapp.grpcchatappclient.RequestListener;
import com.chatapp.grpcchatappclient.StatusListener;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
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
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.util.Duration;

public class MainScreenController implements StatusListener, MessageListener, RequestListener, FriendListener {

    private GRPCChatAppClient client;
    private String currentChat;
    private final HashMap<String, ListView> activeChats = new HashMap<>();
    private ListView<Chat> activeChat;
    private String requester;

    @FXML
    private Circle mainuserimg;
    @FXML
    private ToggleButton usercardtoggle;
    @FXML
    private AnchorPane usercard;
    @FXML
    private ListView<Friend> userlist;
    @FXML
    private ListView<String> requestlist;
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

    public void setupController(GRPCChatAppClient client, String username) throws IOException {
        mainusername.setText(username);
        mainuserimg.setStyle(username);
        this.client = client;
        this.client.addStatusListener(this);
        this.client.addMessageListener(this);
        this.client.addRequestListener(this);
        this.client.addFriendListener(this);
        
        
        ObservableList<Friend> friends = FXCollections.observableArrayList(Friend.extractor());
        userlist.setCellFactory((ListView<Friend> userlist1) -> new CustomListCell());
        userlist.setItems(friends);
        
//        this.client.fetchRequests();
//        this.client.fetchFriends();
        

        userlist.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Friend>() {
            @Override
            public void changed(ObservableValue<? extends Friend> ov, Friend t, Friend t1) {
                if (t1 != null) {
                    currentChat = userlist.getSelectionModel().getSelectedItem().getUsername();
                    if (!activeChats.containsKey(currentChat)) {
                        ListView<Chat> newChat = new ListView<>();
                        newChat.setCellFactory((ListView<Chat> newChat1) -> new ChatListCell());
                        activeChats.put(currentChat, newChat);

                        try {
                            client.fetchMessages(currentChat);
                        } catch (IOException ex) {
                            Logger.getLogger(MainScreenController.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                    if (mainchatusername.getText() != null && !mainchatusername.getText().equals(currentChat)) {

                        Platform.runLater(() -> {
                            activeChat = activeChats.get(currentChat);
                            chatwindow.setContent(activeChat);
                            autoScroll();
                            mainchatusername.setText(currentChat);

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

        requestlist.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> ov, String t, String t1) {
                requester = requestlist.getSelectionModel().getSelectedItem();
                System.out.println(requestlist.getItems().toString());
                if (requester != null && requestButtons.isDisable()) {
                    requestButtons.setDisable(false);
                }
            }
        });

        chatinput.addEventFilter(KeyEvent.KEY_PRESSED, new EnterKeyHandler());
    }

    public void logoff(Stage stage) {

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Logout");
        alert.setContentText("Are you sure you want to log out?");

        if (alert.showAndWait().get() == ButtonType.OK) {
            try {
                client.logoff();
                System.out.println("You logged out!");
                Platform.runLater(() -> {
                    stage.close();
                });
            } catch (IOException ex) {
                Logger.getLogger(MainScreenController.class.getName()).log(Level.SEVERE, null, ex);
            }
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

    @FXML
    public void activateUserToggle() {
        usercardtoggle.fire();
    }

    @FXML
    public void openUserCard() {
        Duration cycleDuration = Duration.millis(500);
        Timeline timeline;
        if (usercardtoggle.isSelected()) {
            timeline = new Timeline(
                    new KeyFrame(cycleDuration,
                            new KeyValue(usercard.prefHeightProperty(), 300, Interpolator.EASE_BOTH))
            );
        } else {
            timeline = new Timeline(
                    new KeyFrame(cycleDuration,
                            new KeyValue(usercard.prefHeightProperty(), 60, Interpolator.EASE_BOTH))
            );
        }

        timeline.play();
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
                userlist.getItems().remove(friend);
                friend.setLastMsg(message);
                friend.setTimestamp(now);
                userlist.getItems().add(0, friend);
                userlist.getSelectionModel().clearAndSelect(0);
            });
            client.msg(currentChat, message);
            Platform.runLater(() -> {
                chatinput.clear();
            });
        }
    }

    @Override
    public void messageGet(String fromUser, Chat message) {

        for (Friend friend : userlist.getItems()) {
            if (friend.getUsername().equals(fromUser)) {
                Platform.runLater(() -> {
                    boolean isSelected = userlist.getSelectionModel().getSelectedItem().equals(friend);
                    userlist.getItems().remove(friend);
                    friend.setLastMsg(message.getMessage());
                    friend.setTimestamp(message.getTimestamp());
                    userlist.getItems().add(0, friend);
                    if (isSelected) {
                        userlist.getSelectionModel().clearAndSelect(0);
                    }
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
            activeChat.scrollTo(activeChat.getItems().size() - 1);
        }
    }

    @Override
    public void request(String fromUser) {
        if (requestTab.isDisabled()) {
            requestTab.setDisable(false);
        }
        Platform.runLater(() -> {
            requestlist.getItems().add(fromUser);
        });
    }

    @FXML
    public void acceptRequest() {
        try {
            client.respondToRequest(requester, 1);
            System.out.println("Added friend " + requester);
            Platform.runLater(() -> {
                if (requestlist.getItems().remove(requester)) {
                    requester = null;
                }
            });
            closeRequestTab();
        } catch (IOException ex) {
            Logger.getLogger(MainScreenController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @FXML
    public void denyRequest() {
        try {
            client.respondToRequest(requester, 2);
            System.out.println("Request denied: " + requester);
            Platform.runLater(() -> {
                if (requestlist.getItems().remove(requester)) {
                    requester = null;
                }
            });
            closeRequestTab();
        } catch (IOException ex) {
            Logger.getLogger(MainScreenController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @FXML
    public void blockRequest() {
        try {
            client.respondToRequest(requester, 3);
            System.out.println("Blocked user " + requester);
            Platform.runLater(() -> {
                if (requestlist.getItems().remove(requester)) {
                    requester = null;
                }
            });
            closeRequestTab();
        } catch (IOException ex) {
            Logger.getLogger(MainScreenController.class.getName()).log(Level.SEVERE, null, ex);
        }
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
    public void loadMessages(String fromUser, ObservableList messages) {
        ListView<Chat> chatWithUser = activeChats.get(fromUser);

        Platform.runLater(() -> {
            chatWithUser.setItems(messages);
            autoScroll();
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

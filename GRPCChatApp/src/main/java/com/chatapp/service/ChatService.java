package com.chatapp.service;

import com.chatapp.chat.ChatMessage;
import com.chatapp.chat.ChatServiceGrpc;
import com.chatapp.chat.GetChatRequest;
import com.chatapp.chat.MessageList;
import com.chatapp.chat.SendMessageRequest;
import com.chatapp.common.GetRequest;
import com.chatapp.common.ServiceResponse;
import com.chatapp.database.MySqlConnection;
import com.chatapp.grpcchatapp.JWToken;
import com.chatapp.grpcchatapp.MessageData;
import io.grpc.stub.StreamObserver;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * gRPC service for handling users' chat messages.
 *
 * @author Mariano Cuneo
 */
public class ChatService extends ChatServiceGrpc.ChatServiceImplBase {

    private final ConcurrentHashMap<Integer, StreamObserver<ChatMessage>> messageObservers;

    /**
     * Class constructor.
     *
     * @param messageObservers contains all currently active
     * <code>ChatService</code> streams.
     */
    public ChatService(ConcurrentHashMap<Integer, StreamObserver<ChatMessage>> messageObservers) {
        this.messageObservers = messageObservers;
    }

    /**
     * RPC method to request all messages between two users.
     *
     * @param request client request message. Must contain a valid JWToken and
     * the other user's user ID.
     * @param responseObserver the call's stream observer.
     */
    @Override
    public void getMessages(GetChatRequest request, StreamObserver<MessageList> responseObserver) {
        JWToken token = new JWToken(request.getToken());
        MessageList.Builder response = MessageList.newBuilder();

        if (token.isValid()) {
            int userId = token.getUserId();
            int friendId = request.getFriend().getUserId();

            response.setFriendId(friendId);

            ChatMessage.Builder chatMessage = ChatMessage.newBuilder();

            MySqlConnection database = new MySqlConnection();

            try {
                ArrayList<MessageData> results = database.fetchMessages(userId, friendId);
                for (MessageData message : results) {

                    chatMessage.setSenderId(message.getSenderId())
                            .setMessage(message.getMessage())
                            .setTimestamp(message.getTimestamp())
                            .setSeen(message.isSeen())
                            .setIsFile(message.isFile());
                    response.addMessages(chatMessage.build());
                    chatMessage.clear();
                }

            } catch (SQLException ex) {
                Logger.getLogger(ChatService.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        responseObserver.onNext(response.build());
        responseObserver.onCompleted();
    }

    /**
     * RPC method to request a message to be sent. This method immediately sends
     * the message to the reciever (if they're online), and it also saves the
     * message to the database.
     *
     * @param request client request message. Must contain a valid JWToken and
     * the reciever's ID.
     * @param responseObserver the call's stream observer.
     */
    @Override
    public void sendMessage(SendMessageRequest request, StreamObserver<ServiceResponse> responseObserver) {
        JWToken token = new JWToken(request.getToken());
        ServiceResponse.Builder response = ServiceResponse.newBuilder();

        if (token.isValid()) {
            String message = request.getMessage();
            int userId = token.getUserId();
            int friendId = request.getReciever().getUserId();

            MySqlConnection database = new MySqlConnection();

            sendMessageIfOnline(userId, friendId, message);

            try {
                database.saveMsg(userId, friendId, message);

                response.setResponse("SUCCESS");
                response.setResponseCode(1);
            } catch (SQLException ex) {
                Logger.getLogger(ChatService.class.getName()).log(Level.SEVERE, null, ex);
                response.setResponse("INTERNAL_ERROR");
                response.setResponseCode(0);
            }
        } else {
            response.setResponse("INVALID_CREDENTIALS");
            response.setResponseCode(0);
        }
        responseObserver.onNext(response.build());
        responseObserver.onCompleted();
    }

    /**
     * RPC method to request a long-lived stream to be started. This method
     * starts a long-lived stream and adds it to <code>messageObservers</code>
     * for later access.
     *
     * @param request client request message. Must contain a valid JWToken.
     * @param responseObserver the call's stream observer for the long-lived stream.
     */
    @Override
    public void receiveMessage(GetRequest request, StreamObserver<ChatMessage> responseObserver) {

        JWToken token = new JWToken(request.getToken());
        if (token.isValid()) {
            int userId = token.getUserId();
            messageObservers.put(userId, responseObserver);
        }
    }

    private void sendMessageIfOnline(int userId, int friendId, String message) {
        if (messageObservers.containsKey(friendId)) {

            ChatMessage.Builder chatMessage = ChatMessage.newBuilder();
            chatMessage.setSenderId(userId)
                    .setMessage(message)
                    .setTimestamp(Instant.now().toString())
                    .setSeen(false);
            messageObservers.get(friendId).onNext(chatMessage.build());
        }
    }
}

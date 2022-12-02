package com.chatapp.database;

import com.chatapp.friends.UserFriend;
import com.chatapp.grpcchatapp.FriendData;
import com.chatapp.grpcchatapp.MessageData;
import com.chatapp.grpcchatapp.UserData;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import org.apache.commons.codec.digest.DigestUtils;

/**
 * Class to handle the connection and interaction with the MySQL Database
 *
 * @author Mariano Cuneo
 */
public class MySqlConnection {

    private Connection connect = null;
    private PreparedStatement preparedStatement = null;
    private ResultSet resultSet = null;
    private final String DATABASE_ADDRESS = "jdbc:mysql://localhost:3306/chatapp_schema";
    private final String DATABASE_ACCOUNT_NAME = "javatest";
    private final String DATABASE_ACCOUNT_PASSWORD = "Java1test2";

    /**
     * Query the database for the user ID of for a given username.
     *
     * @param username username to be searched against.
     * @return queried user ID, 0 if username doesn't exist.
     * @throws SQLException if connection with the database fails.
     */
    public int getUserId(String username) throws SQLException {
        try {
            System.out.println("connecting");

            connect();
            preparedStatement = connect
                    .prepareStatement("SELECT user_id FROM user WHERE user_login = ?;");
            preparedStatement.setString(1, username);

            resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getInt(1);
            } else {
                return 0;
            }

        } catch (SQLException ex) {
            throw ex;
        } finally {
            close();
        }
    }

    /**
     * Insert new user into database.
     *
     * @param username new user's username.
     * @param password new user's password.
     * @param salt randomly generated salt for password encryption.
     * @throws SQLException if username is already taken, is invalid, or if the
     * connection with database fails.
     */
    public void addNewUser(String username, String password, int salt) throws SQLException {
        try {
            connect();
            preparedStatement = connect
                    .prepareStatement("INSERT INTO user (user_login,user_password,salt) VALUES (?,?,?);");
            preparedStatement.setString(1, username);
            preparedStatement.setString(2, password);
            preparedStatement.setInt(3, salt);

            preparedStatement.executeUpdate();
        } catch (SQLException ex) {
            throw ex;
        } finally {
            close();
        }

    }

    /**
     * Inserts entries in the database for a new friend request. This method
     * inserts the entry for the requesting user with a pending status and an
     * entry for the requested user with a requested status. It also generates
     * unique ID for the Chat between both users.
     *
     * @param userId user ID of the requesting user.
     * @param username username of the requesting user.
     * @param friendId user ID of the requested user.
     * @param friendName username of the requested user.
     * @throws SQLException if the contact between the user already exists, if
     * either ID is wrong, or if the connection with the database fails.
     */
    public void addFriend(int userId, String username, int friendId, String friendName) throws SQLException {
        try {
            connect();
            // generate unique id for friends chat
            long chatUuid = generateChatUuid(userId, friendId);

            preparedStatement = connect
                    .prepareStatement("INSERT INTO user_contacts (contact_user_id,contact_friend_id,contact_alias,contact_status,chat_uuid) "
                            + "VALUES (?,?,?,1," + chatUuid + "),"
                            + "(?,?,?,2," + chatUuid + ");");

            preparedStatement.setInt(1, userId);
            preparedStatement.setInt(2, friendId);
            preparedStatement.setString(3, friendName);
            preparedStatement.setInt(4, friendId);
            preparedStatement.setInt(5, userId);
            preparedStatement.setString(6, username);

            preparedStatement.executeUpdate();
        } catch (SQLException ex) {
            throw ex;
        } finally {
            close();
        }

    }

    /**
     * Generates a unique number for any combination of IDs regardless of order.
     *
     * @param id1 any existing user ID.
     * @param id2 any other existing user ID.
     * @return unique number generated from both IDs regardless of order.
     */
    public static long generateChatUuid(int id1, int id2) {
        return (long) Math.max(id1, id2) << 32 + Math.min(id1, id2);
    }

    /**
     * Verifies the password inputted by the user. This method retrieves the
     * salt from the database and uses it to generate a hash that's then checked
     * against the hash stored in the database.
     *
     * @param username username to be verified.
     * @param password password to be verified.
     * @return <code>true</code> if hashed passwords patch, else
     * <code>false</code>
     * @throws SQLException if connection with the database fails.
     */
    public boolean checkPassword(String username, String password) throws SQLException {
        try {
            connect();
            int salt = getSalt(username);
            String saltedpass = password + String.valueOf(salt);
            String hashedpass = DigestUtils.sha256Hex(saltedpass);
            preparedStatement = connect
                    .prepareStatement("SELECT user_password FROM user WHERE user_login = ?;");
            preparedStatement.setString(1, username);

            resultSet = preparedStatement.executeQuery();

            resultSet.next();
            String userpass = resultSet.getString(1);

            return userpass.equals(hashedpass);

        } catch (SQLException ex) {
            throw ex;
        } finally {
            close();
        }
    }

    private int getSalt(String username) throws SQLException {
        try {
            // connect();
            preparedStatement = connect
                    .prepareStatement("SELECT salt FROM user WHERE user_login = ?;");
            preparedStatement.setString(1, username);

            resultSet = preparedStatement.executeQuery();

            resultSet.next();
            return resultSet.getInt(1);

        } catch (SQLException ex) {
            throw ex;
        }
    }

    /**
     * Queries the server for all accepted or requested user contacts for a
     * single user. This method generates <code>FriendData</code> objects from
     * the result set returned by the database.
     *
     * @param userid user ID to search against.
     * @return list with all generated <code>FriendData</code> objects.
     * @throws SQLException if connection with the database fails.
     */
    public ArrayList<FriendData> fetchFriends(int userid) throws SQLException {
        ArrayList<FriendData> friends = new ArrayList<>();
        try {
            connect();
            preparedStatement = connect
                    .prepareStatement("SELECT  u.user_login,"
                            + "uc.contact_friend_id,"
                            + "uc.contact_alias,"
                            + "c.chat_user_sender,"
                            + "c.last_message,"
                            + " DATE_FORMAT(c.last_message_time,'%Y-%m-%dT%H:%i:%s') "
                            + "AS last_message_time,"
                            + "c.unseen_chats,"
                            + "uc.contact_status "
                            + "FROM user_contacts uc LEFT JOIN chat c USING (chat_uuid) "
                            + "INNER JOIN user u ON u.user_id = uc.contact_friend_id "
                            + "WHERE uc.contact_user_id = ? AND (uc.contact_status = 3 OR uc.contact_status = 2);");
            preparedStatement.setInt(1, userid);

            resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {

                UserFriend.Type type = switch (resultSet.getInt(8)) {
                    case 2 ->
                        UserFriend.Type.REQUEST;
                    case 3 ->
                        UserFriend.Type.FRIEND;
                    default ->
                        UserFriend.Type.UNRECOGNIZED;
                };

                friends.add(new FriendData(new UserData(resultSet.getString(1), resultSet.getInt(2)),
                        resultSet.getString(3), resultSet.getInt(2) == resultSet.getInt(4), resultSet.getString(1) + ".jpg",
                        resultSet.getString(5), resultSet.getString(6), resultSet.getInt(7), type));
            }

            return friends;

        } catch (SQLException ex) {
            throw ex;
        } finally {
            close();
        }
    }

    /**
     * Queries the server for the IDs of all all accepted user contacts for a
     * single user.
     *
     * @param userId user ID to search against.
     * @return list with all IDs returned by the Query.
     * @throws SQLException if connection with the database fails.
     */
    public ArrayList<Integer> getFriendList(int userId) throws SQLException {

        ArrayList<Integer> friendList = new ArrayList<>();
        try {
            connect();
            preparedStatement = connect
                    .prepareStatement("SELECT contact_friend_id FROM user_contacts WHERE contact_user_id = ? AND contact_status = 3;");

            preparedStatement.setInt(1, userId);

            resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                friendList.add(resultSet.getInt(1));
            }
            return friendList;

        } catch (SQLException ex) {
            throw ex;
        } finally {
            close();
        }
    }

    /**
     * Queries the server for all messages between a user and a friend of the
     * user. This method generates <code>MessageData</code> objects from the
     * result set returned by the database.
     *
     * @param userid user ID to search against.
     * @param friendId user ID of the user's friend to search against.
     * @return list with all generated <code>MessageData</code> objects.
     * @throws SQLException if connection with the database fails.
     */
    public ArrayList<MessageData> fetchMessages(int userid, int friendId) throws SQLException {
        ArrayList<MessageData> messages = new ArrayList<>();
        try {
            connect();
            long chatUuid = generateChatUuid(userid, friendId);
            preparedStatement = connect
                    .prepareStatement("SELECT message_text, message_user_id,"
                            + "DATE_FORMAT(message_datetime,'%Y-%m-%dT%H:%i:%s') "
                            + "AS message_datetime, "
                            + "message_seen,"
                            + "is_file FROM message "
                            + "WHERE chat_uuid = ? ORDER BY message_datetime;");

            preparedStatement.setLong(1, chatUuid);

            resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                messages.add(new MessageData(resultSet.getString(1), resultSet.getInt(2), resultSet.getString(3), resultSet.getBoolean(4), resultSet.getBoolean(5)));
            }
            return messages;

        } catch (SQLException ex) {
            throw ex;
        } finally {
            close();
        }
    }

    private void connect() throws SQLException {
        System.out.println("try to connect");
        connect = DriverManager
                .getConnection(DATABASE_ADDRESS, DATABASE_ACCOUNT_NAME, DATABASE_ACCOUNT_PASSWORD);
        System.out.println("connected");
    }

    private void close() throws SQLException {
        try {
            if (resultSet != null) {
                resultSet.close();
            }

            if (connect != null) {
                connect.close();
            }
        } catch (SQLException e) {
            throw e;
        }
    }

    /**
     * Saves a chat message sent by a user into the database. This method also
     * updates all data related to the last message sent in the corresponding
     * chat table entry.
     *
     * @param userid user ID of the message's sender.
     * @param recipientid user ID of the message's recipient.
     * @param message text of the message.
     * @throws SQLException if either ID doesn't exist or if the connection with
     * the database fails.
     */
    public void saveMsg(int userid, int recipientid, String message) throws SQLException {
        try {
            connect();
            // generate unique id for friends chat
            long chatUuid = generateChatUuid(userid, recipientid);

            preparedStatement = connect
                    .prepareStatement("INSERT INTO message (message_datetime,message_text,chat_uuid,message_user_id,message_seen) "
                            + "VALUES (UTC_TIMESTAMP(),?," + chatUuid + ",?,0);");

            preparedStatement.setString(1, message);
            preparedStatement.setInt(2, userid);

            preparedStatement.executeUpdate();

            preparedStatement = connect
                    .prepareStatement("UPDATE chat SET chat_user_sender = ?, last_message = ?, last_message_time = UTC_TIMESTAMP(), last_message_seen = 0 WHERE chat_uuid = " + chatUuid + ";");

            preparedStatement.setInt(1, userid);
            preparedStatement.setString(2, message);

            preparedStatement.executeUpdate();
        } catch (SQLException ex) {
            throw ex;
        } finally {
            close();
        }
    }

    /**
     * Saves a chat message containing information on file sent by user. This
     * method also updates all data related to the last message sent in the
     * corresponding chat table entry.
     *
     * @param userid user ID of the file's sender.
     * @param recipientid user ID of the file's recipient.
     * @param fileName full name (with extension) of uploaded file.
     * @param fileSize size of the file (in MB).
     * @throws SQLException if either ID doesn't exist or if the connection with
     * the database fails.
     */
    public void saveFile(int userid, int recipientid, String fileName, double fileSize) throws SQLException {
        try {
            connect();

            long chatUuid = generateChatUuid(userid, recipientid);

            preparedStatement = connect
                    .prepareStatement("INSERT INTO message (message_datetime,message_text,chat_uuid,message_user_id,message_seen, is_file) "
                            + "VALUES (UTC_TIMESTAMP(),?," + chatUuid + ",?,0,1);");

            preparedStatement.setString(1, fileName + " " + fileSize);
            preparedStatement.setInt(2, userid);

            preparedStatement.executeUpdate();

            preparedStatement = connect
                    .prepareStatement("UPDATE chat SET chat_user_sender = ?, last_message = ?, last_message_time = UTC_TIMESTAMP(), last_message_seen = 0 WHERE chat_uuid = " + chatUuid + ";");

            preparedStatement.setInt(1, userid);
            preparedStatement.setString(2, "Sent file: " + fileName);

            preparedStatement.executeUpdate();
        } catch (SQLException ex) {
            throw ex;
        } finally {
            close();
        }
    }

    /**
     * Updates the database when a friend request is accepted. This method also
     * creates an entry in the chat table representing the chat conversation
     * between both users.
     *
     * @param userid user ID of accepting user.
     * @param requesterId user ID of requesting user.
     * @return a <code>FriendData</code> object containing information of the
     * accepting user.
     * @throws SQLException if either ID doesn't exist, if the chat conversation
     * between both users already exists or if the connection with the database
     * fails.
     */
    public FriendData acceptRequest(int userid, int requesterId) throws SQLException {
        try {
            connect();
            long chatUuid = generateChatUuid(userid, requesterId);

            preparedStatement = connect
                    .prepareStatement("UPDATE user_contacts SET contact_status = 3 WHERE chat_uuid = " + chatUuid + ";");
            preparedStatement.executeUpdate();

            preparedStatement = connect
                    .prepareStatement("INSERT INTO chat (chat_user_sender, last_message, last_message_time, chat_uuid) VALUES (?,?,UTC_TIMESTAMP()," + chatUuid + ");");

            preparedStatement.setInt(1, requesterId);
            preparedStatement.setString(2, "");
            preparedStatement.executeUpdate();

            preparedStatement = connect
                    .prepareStatement("SELECT  u.user_login,"
                            + "uc.contact_friend_id,"
                            + "uc.contact_alias,"
                            + "c.chat_user_sender,"
                            + "c.last_message,"
                            + " DATE_FORMAT(c.last_message_time,'%Y-%m-%dT%H:%i:%s') "
                            + "AS last_message_time,"
                            + "c.unseen_chats "
                            + "FROM user_contacts uc LEFT JOIN chat c USING (chat_uuid) "
                            + "INNER JOIN user u ON u.user_id = uc.contact_friend_id "
                            + "WHERE uc.contact_user_id = ? AND uc.contact_friend_id = ?;");
            preparedStatement.setInt(1, requesterId);
            preparedStatement.setInt(2, userid);

            resultSet = preparedStatement.executeQuery();

            resultSet.next();

            FriendData friend = new FriendData(new UserData(resultSet.getString(1), resultSet.getInt(2)),
                    resultSet.getString(3), resultSet.getInt(2) == resultSet.getInt(4), resultSet.getString(1) + ".jpg",
                    resultSet.getString(5), resultSet.getString(6), resultSet.getInt(7), UserFriend.Type.FRIEND);

            return friend;
        } catch (SQLException ex) {
            throw ex;
        } finally {
            close();
        }
    }

    /**
     * Updates the database when a friend request is denied.
     *
     * @param userid user ID of denying user.
     * @param requesterId user ID of requesting user.
     * @throws SQLException if either ID doesn't exist or if the connection with
     * the database fails.
     */
    public void denyRequest(int userid, int requesterId) throws SQLException {
        try {
            connect();
            preparedStatement = connect
                    .prepareStatement("UPDATE user_contacts SET contact_status = 4 WHERE contact_user_id = ? AND contact_friend_id = ?;");
            preparedStatement.setInt(1, userid);
            preparedStatement.setInt(2, requesterId);
            preparedStatement.executeUpdate();
        } catch (SQLException ex) {
            throw ex;
        } finally {
            close();
        }
    }

    /**
     * Updates the database when a friend request is blocked.
     *
     * @param userid user ID of blocking user.
     * @param requesterId user ID of requesting user.
     * @throws SQLException if either ID doesn't exist or if the connection with
     * the database fails.
     */
    public void blockRequest(int userid, int requesterId) throws SQLException {
        try {
            connect();
            preparedStatement = connect
                    .prepareStatement("UPDATE user_contacts SET contact_status = 6 WHERE contact_user_id = ? AND contact_friend_id = ?;");
            preparedStatement.setInt(1, userid);
            preparedStatement.setInt(2, requesterId);
            preparedStatement.executeUpdate();
        } catch (SQLException ex) {
            throw ex;
        } finally {
            close();
        }
    }
}

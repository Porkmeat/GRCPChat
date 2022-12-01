/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
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

public class MySqlConnection {

    private Connection connect = null;
    private PreparedStatement preparedStatement = null;
    private ResultSet resultSet = null;

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
    
    public void addFriend(int userId,String username,int friendId, String friendName) throws SQLException {
        try {
            connect();
            // generate unique id for friends chat
            long chatUuid = generateChatUuid(userId, friendId);
            
            preparedStatement = connect
                    .prepareStatement("INSERT INTO user_contacts (contact_user_id,contact_friend_id,contact_alias,contact_status,chat_uuid) "
                            + "VALUES (?,?,?,1,"+chatUuid+"),"
                            + "(?,?,?,2,"+chatUuid+");");
            
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
    
    public static long generateChatUuid(int id1, int id2) {
        return (long)Math.max(id1, id2) << 32 + Math.min(id1, id2);
    }

    
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
                    case 2 -> UserFriend.Type.REQUEST;
                    case 3 -> UserFriend.Type.FRIEND;
                    default -> UserFriend.Type.UNRECOGNIZED;
                };
                
                friends.add(new FriendData(new UserData(resultSet.getString(1), resultSet.getInt(2))
                        , resultSet.getString(3), resultSet.getInt(2) == resultSet.getInt(4), resultSet.getString(1) + ".jpg"
                        , resultSet.getString(5), resultSet.getString(6), resultSet.getInt(7), type));
            }
            
            return friends;            
            
        } catch (SQLException ex) {
            throw ex;
        } finally {
            close();
        }
    }
    
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
    
    public ArrayList<MessageData> fetchMessages(int userid, int friendId) throws SQLException {
        ArrayList<MessageData> messages = new ArrayList<>();
        try {
            connect();
            long chatUuid = generateChatUuid(userid,friendId);
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
    
//    public static JSONArray convertToJSONArray(ResultSet resultSet)
//            throws Exception {
//        JSONArray jsonArray = new JSONArray();
//        while (resultSet.next()) {
//            JSONObject obj = new JSONObject();
//            int total_rows = resultSet.getMetaData().getColumnCount();
//            for (int i = 0; i < total_rows; i++) {
//                obj.put(resultSet.getMetaData().getColumnLabel(i + 1)
//                        .toLowerCase(), resultSet.getObject(i + 1));
//
//            }
//            jsonArray.put(obj);
//        }
//        return jsonArray;
//    }


    private void connect() throws SQLException {
        System.out.println("try to connect");
        connect = DriverManager
                .getConnection("jdbc:mysql://localhost:3306/chatapp_schema", "javatest", "Java1test2");
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

    public void saveMsg(int userid, int recipientid, String message) throws SQLException {
        try {
            connect();
            // generate unique id for friends chat
            long chatUuid = generateChatUuid(userid, recipientid);
            
            preparedStatement = connect
                    .prepareStatement("INSERT INTO message (message_datetime,message_text,chat_uuid,message_user_id,message_seen) "
                            + "VALUES (UTC_TIMESTAMP(),?,"+chatUuid+",?,0);");
            
            preparedStatement.setString(1, message);
            preparedStatement.setInt(2, userid);

            preparedStatement.executeUpdate();
            
            preparedStatement = connect
                    .prepareStatement("UPDATE chat SET chat_user_sender = ?, last_message = ?, last_message_time = UTC_TIMESTAMP(), last_message_seen = 0 WHERE chat_uuid = "+chatUuid+";");
            
            preparedStatement.setInt(1, userid);
            preparedStatement.setString(2, message);

            preparedStatement.executeUpdate();
        } catch (SQLException ex) {
            throw ex;
        } finally {
            close();
        }
    }
    
    public void saveFile(int userid, int recipientid, String fileName, double fileSize) throws SQLException {
        try {
            connect();
            
            long chatUuid = generateChatUuid(userid, recipientid);
            
            preparedStatement = connect
                    .prepareStatement("INSERT INTO message (message_datetime,message_text,chat_uuid,message_user_id,message_seen, is_file) "
                            + "VALUES (UTC_TIMESTAMP(),?,"+chatUuid+",?,0,1);");
            
            preparedStatement.setString(1, fileName + " " + fileSize);
            preparedStatement.setInt(2, userid);

            preparedStatement.executeUpdate();
            
//            preparedStatement = connect
//                    .prepareStatement("UPDATE chat SET chat_user_sender = ?, last_message = ?, last_message_time = UTC_TIMESTAMP(), last_message_seen = 0 WHERE chat_uuid = "+chatUuid+";");
//            
//            preparedStatement.setInt(1, userid);
//            preparedStatement.setString(2, message);
//
//            preparedStatement.executeUpdate();
        } catch (SQLException ex) {
            throw ex;
        } finally {
            close();
        }
    }
    
    
//    next method is obsolete 
    
//    public ArrayList<UserData> getRequests(int userid) throws Exception {
//        ArrayList<UserData> requests = new ArrayList<>();
//        try {
//            connect();
//            preparedStatement = connect
//                    .prepareStatement("SELECT contact_alias, contact_friend_id FROM user_contacts"
//                            + " WHERE contact_user_id = ? AND contact_status = 2 ORDER BY contact_alias;");
//            preparedStatement.setInt(1, userid);
//
//            resultSet = preparedStatement.executeQuery();
//            
//            while (resultSet.next()) {
//                requests.add(new UserData(resultSet.getString(1),resultSet.getInt(2)));
//            }
//            return requests;
//            
//        } catch (Exception ex) {
//            throw ex;
//        } finally {
//            close();
//        }
//    }

    public FriendData acceptRequest(int userid, int requesterId) throws SQLException {
        try {
            connect();
            long chatUuid = generateChatUuid(userid, requesterId);
            
            preparedStatement = connect
                    .prepareStatement("UPDATE user_contacts SET contact_status = 3 WHERE chat_uuid = "+chatUuid+";");
            preparedStatement.executeUpdate();
            
            preparedStatement = connect
                    .prepareStatement("INSERT INTO chat (chat_user_sender, last_message, last_message_time, chat_uuid) VALUES (?,?,UTC_TIMESTAMP(),"+chatUuid+");");
            
            preparedStatement.setInt(1,requesterId);
            preparedStatement.setString(2,"");
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
            
            FriendData friend = new FriendData(new UserData(resultSet.getString(1), resultSet.getInt(2))
                        , resultSet.getString(3), resultSet.getInt(2) == resultSet.getInt(4), "profilepic"
                        , resultSet.getString(5), resultSet.getString(6), resultSet.getInt(7), UserFriend.Type.FRIEND);

            return friend;
        } catch (SQLException ex) {
            throw ex;
        } finally {
            close();
        }
    }
    
    
    public void denyRequest(int userid, int requesterId) throws SQLException {
        try {
            connect();
            preparedStatement = connect
                    .prepareStatement("UPDATE user_contacts SET contact_status = 4 WHERE contact_user_id = ? AND contact_friend_id = ?;");
            preparedStatement.setInt(1,userid);
            preparedStatement.setInt(2,requesterId);
            preparedStatement.executeUpdate();            
        } catch (SQLException ex) {
            throw ex;
        } finally {
            close();
        }
    }

    public void blockRequest(int userid, int requesterId) throws SQLException {
        try {
            connect();
            preparedStatement = connect
                    .prepareStatement("UPDATE user_contacts SET contact_status = 6 WHERE contact_user_id = ? AND contact_friend_id = ?;");
            preparedStatement.setInt(1,userid);
            preparedStatement.setInt(2,requesterId);
            preparedStatement.executeUpdate();            
        } catch (SQLException ex) {
            throw ex;
        } finally {
            close();
        }
    }
}

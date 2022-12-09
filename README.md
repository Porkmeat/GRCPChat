# GRCPChat

This is a schowcase project for a live and persistent messaging service including both a desktop client, the server and the database.
Both the client and the server are coded in Java and the DBMS is MySQL.

## Features

*Live Messages
*Live user online status
*Chat History
*Friend Management
   *Send Friend Requests
   *Accept/Block/Deny Requests
   *Persistent Friend List
*File transfer between users
*Profile picture upload

More features to be added in the future.

## Running the Project

1. Setting up the database:

First create the database schema using `chatapp_schema.sql`. Then create a user with `SELECT`, `UPDATE` and `INSERT` permissions.
Finally, add the username, password and database port to the `MySqlConnection.java` file found in `GRCPChat/GRPCChatApp/src/main/java/com/chatapp/database/`.

2. Compile and run the server:

From the `GRPCChatApp` directory:

   *Compile:
   
   ```bash
   mvn clean install
   ```
   
   *Run 
   
   ```bash
   mvn exec:java
   ```
   
3. Compile the client:

From the `GRPCChatAppClient` directory and from a different terminal:

   *Compile:
   
   ```bash
   mvn clean install
   ```

4. Run multiplie clients to test:

From the `GRPCChatAppClient`:
   
   *Run the client (use a different terminal for each instance):
   
   ```bash
   mvn exec:java
   ```
   
## Getting started:

Create 2 different users and create a friendship by using the 'Add Contact' tab and accepting the friend request.

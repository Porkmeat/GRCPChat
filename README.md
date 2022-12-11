# GRCPChat

This is a schowcase project for a live and persistent messaging service including both a desktop client, the server and the database.
Both the client and the server are coded in Java and the DBMS is MySQL. All client-server interaction is carried out using the gRPC framework.

## Features

* Live Messages
* Live user online status
* Chat History
* Friend Management
   * Send Friend Requests
   * Accept/Block/Deny Requests
   * Persistent Friend List
* File transfer between users
* Profile picture upload

More features to be added in the future.

## Running the Project

1. Setting up the database:

    First create the database schema using `chatapp_schema.sql`. Then create a user with `SELECT`,
    `UPDATE` and `INSERT` permissions.  Finally, add the username, password and database port to the
    `MySqlConnection.java` file found in `GRCPChat/GRPCChatApp/src/main/java/com/chatapp/database/`.

    [How to: run mysql-server in a container](#how_to_run_mysql_server_in_a_container).

2. Compile and run the server:

From the `GRPCChatApp` directory:

   * Compile:
   
   ```bash
   mvn clean install
   ```
   
   * Run 
   
   ```bash
   mvn exec:java
   ```
   
3. Compile the client:

From the `GRPCChatAppClient` directory and from a different terminal:

   * Compile:
   
   ```bash
   mvn clean install
   ```

4. Run multiplie clients to test:

From the `GRPCChatAppClient` directory:
   
   * Run the client (use a different terminal for each instance):
   
   ```bash
   mvn exec:java
   ```
   
## Getting started:

Create 2 different users and create a friendship by using the 'Add Contact' tab and accepting the friend request.

## How to: run mysql-server in a container

If you choose not to install MySQL on the host, you can run it in a Docker container.

1. Install Docker and make sure `docker run hello-world` works.
1. In a shell, `cd` to the root directory of this project
1. Start the MySQL server:

    ```bash
    docker run --name=mysql1 -v $PWD:/src -p 3306:3306 mysql/mysql-server
    ```

    Explanation:

    - `--name` creates a named container called `mysql1`, so we can refer to it by that name instead
      of its container ID.
    - `-v` bind-mounts the current directory to `/src`. We will import the schema file from there.
    - `-p` opens port 3306 so the chat server can talk to the database
    
1. Wait for the MySQL server to start.

    Eventually it prints:

    ```
    [Entrypoint] MySQL init process done.  Ready for start up.
    ```

    Scroll up a few lines, look for a line like this:

    ```
    [Entrypoint] GENERATED ROOT PASSWORD: ,i&xa&EQBl:71499,8%Ul:QC9R=h8yPa
    ```

    Copy this password somewhere, we'll need it in a moment.

1. Connect to the MySQL container and set it up:

    ```sh
    $ docker exec -it mysql1 bash
    ```

    Then, in the container:

    ```sh
    # mysql -uroot -p"GENERATED_ROOT_PASSWORD"
    ```

    (Replace `GENERATED_ROOT_PASSWORD` with the password from the previous step.)\

    Then, in the mysql command line:

    ```sql
    mysql> ALTER USER 'root'@'localhost' IDENTIFIED BY 'foo123';

    mysql> exit
    ```

    ```sh
    # mysql -uroot -pfoo123 < /src/chatapp_schema.sql
    
    # exit
    ```

When you're done with the database, stop it like so:

```sh
docker kill mysql1 && docker rm mysql1
```


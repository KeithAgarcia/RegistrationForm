package com.theironyard;

import jodd.json.JsonParser;
import jodd.json.JsonSerializer;
import org.h2.tools.Server;
import spark.Spark;

import java.sql.*;
import java.util.ArrayList;

public class Main {

    public static void createTables(Connection conn) throws SQLException { //creates table
        Statement stmt = conn.createStatement(); //create a statement to execute SQL
        stmt.execute("CREATE TABLE IF NOT EXISTS registry (id IDENTITY, username VARCHAR, address VARCHAR, email VARCHAR)"); //create methods table.
    }


    public static void main(String[] args) throws SQLException{
        Server.createWebServer().start();
        Connection conn = DriverManager.getConnection("jdbc:h2:./main");
        createTables(conn);

        Spark.staticFileLocation("/public");
        Spark.get(
                "/user",
                ((request, response) -> {
                    ArrayList<User> users = selectUser(conn);
                    JsonSerializer s = new JsonSerializer();
                    return s.serialize(users); //
                })
        );

        Spark.post(
                "/user",
                ((request, response) -> {
                    String body = request.body(); //body is data sent with the request. data is stored in body of the request.
                    JsonParser p = new JsonParser();
                    User user = p.parse(body, User.class);
                    insertUser(conn, user.username, user.address, user.email);
                    return "";
                })
        );

        Spark.put("/user", ((request, response) -> {
            /// / update the user (which is very similar to insert, except
            // you call updateUser instead of insertUser
            // do the same parsing as the post request
            String body = request.body();
            JsonParser p = new JsonParser();
            User user = p.parse(body, User.class);
            updateUser(conn, user.id, user.username, user.address, user.email);
            return "";
                })
        );

        Spark.delete(
                "/user/:id",
                ( request, response) -> {
                    int id = Integer.valueOf( request.params("id"));
                    deleteUser(conn, id);

                    return "";
                }
        );


    }
    public static void insertUser(Connection conn, String username, String address, String email) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("INSERT INTO registry VALUES (NULL, ?, ?, ?)"); //insert values into our messages table
        stmt.setString(1, username);
        stmt.setString(2, address);
        stmt.setString(3, email);
        stmt.execute(); //method change data on the server is execute.
    }
    public static ArrayList<User> selectUser(Connection conn) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("SELECT * FROM registry");
        ArrayList<User> users = new ArrayList<>();
        ResultSet results = stmt.executeQuery();
        while (results.next()) {
            int id = results.getInt("id");
            String username = results.getString("username");
            String address = results.getString("address");
            String email = results.getString("email");
            users.add(new User(id, username, address, email));//adding to arraylist.
        }
        return users;
    }
    public static void updateUser(Connection conn, int id, String username, String address, String email) throws SQLException{
        PreparedStatement stmt = conn.prepareStatement("UPDATE registry set username = ?, address =?, email =? WHERE id = ?");
        stmt.setString(1, username);
        stmt.setString(2, address);
        stmt.setString(3, email);
        stmt.setInt(4, id);
        stmt.execute();
    }
    public static  void  deleteUser(Connection conn, int id) throws SQLException{
        PreparedStatement stmt = conn.prepareStatement("DELETE FROM registry WHERE id = ?"); //username supposed to be ID?
        stmt.setInt(1, id);
        stmt.execute();
    }
}

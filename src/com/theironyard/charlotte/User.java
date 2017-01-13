package com.theironyard.charlotte;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class User {
    Integer userId;
    String userName;
    String userEmail;
    ArrayList<Order> userOrders;

    public User() {
    }

    public User(Integer userId) {
        this.userId = userId;
    }

    public User(String userName, String userEmail) {
        this.userName = userName;
        this.userEmail = userEmail;
    }

    public User(Integer userId, String userName, String userEmail) {
        this.userId = userId;
        this.userName = userName;
        this.userEmail = userEmail;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public ArrayList<Order> getUserOrders() {
        return userOrders;
    }

    public void setUserOrders(ArrayList<Order> userOrders) {
        this.userOrders = userOrders;
    }


    public static User selectUserById(Connection conn, User currentUser) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("SELECT * FROM users WHERE id = ?");
        stmt.setInt(1, currentUser.getUserId());

        ResultSet results = stmt.executeQuery();
        if (results.next()) {
            return makeUser(results);
        } else {
            return null;
        }
    }

    public static User makeUser(ResultSet results) throws SQLException{
        int id = results.getInt("id");
        String userName = results.getString("username");
        String email = results.getString("email");
        return new User(id, userName, email);
    }

    public static User selectUserByNameAndEmail(Connection conn, User currentUser) throws SQLException{
        PreparedStatement stmt = conn.prepareStatement("SELECT * FROM users WHERE upper(username) = ? and upper(email) = ?");
        stmt.setString(1, currentUser.getUserName().toUpperCase());
        stmt.setString(2, currentUser.getUserEmail().toUpperCase());
        ResultSet results = stmt.executeQuery();
        if(results.next()) {
            return makeUser(results);
        } else {
            return null;
        }
    }

    public static void insertUser(Connection conn, User newUser) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("INSERT INTO users VALUES (NULL, ?, ?)");
        stmt.setString(1, newUser.getUserName());
        stmt.setString(2, newUser.getUserEmail());
        stmt.execute();
    }
}


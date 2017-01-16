package com.theironyard.charlotte;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class User {
    private int id;
    private String name;
    private String email;
    private List<Order> orders;

    public User(int id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
    }

    public User(String name, String email) {
        this.name = name;
        this.email = email;
    }

    public List<Order> getOrders() {
        return orders;
    }

    public void setOrders(List<Order> orders) {
        this.orders = orders;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public static User selectUserById(Connection conn, Integer id) throws SQLException {
        User user = null;

        if (id != null) {
            PreparedStatement stmt = conn.prepareStatement("SELECT * FROM users WHERE id = ?");
            stmt.setInt(1, id);
            ResultSet results = stmt.executeQuery();
            if (results.next()) {
                user = makeUser(results);
                user.setOrders(Order.getOrdersForUser(conn, id));
            }
        }
        return user;
    }

    public static User makeUser(ResultSet results) throws SQLException{
        int id = results.getInt("id");
        String name = results.getString("username");
        String email = results.getString("email");
        return new User(id, name, email);
    }

    public static Integer selectUserByEmail(Connection conn, String email) throws SQLException {
        Integer userId = null;

        if (email != null) {
            PreparedStatement stmt = conn.prepareStatement("SELECT * FROM users WHERE upper(email) = ?");
            stmt.setString(1, email.toUpperCase());
            ResultSet results = stmt.executeQuery();
            if (results.next()) {
                userId = results.getInt("id");
            }
        }
        return userId;
    }

    public static void insertUser(Connection conn, User newUser) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("INSERT INTO users VALUES (NULL, ?, ?)");
        stmt.setString(1, newUser.getName());
        stmt.setString(2, newUser.getEmail());
        stmt.execute();
    }
}


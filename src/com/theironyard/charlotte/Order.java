package com.theironyard.charlotte;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by Ben on 1/15/17.
 */
public class Order {
    private int id;
    private int userId;
    private boolean complete;
    private List<Item> items;

    public Order(int userId, boolean complete) {
        this.userId = userId;
        this.complete = complete;
    }

    public Order(int id, int userId, boolean complete) {
        this.id = id;
        this.userId = userId;
        this.complete = complete;
    }

    public boolean isComplete() {
        return complete;
    }

    public void setComplete(boolean complete) {
        this.complete = complete;
    }

    public List<Item> getItems() {
        return items;
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public static List<Order> getOrdersForUser(Connection conn, Integer userId) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("SELECT * FROM orders WHERE user_id = ?");
        stmt.setInt(1, userId);
        ResultSet results = stmt.executeQuery();

        return new ArrayList<Order>((Collection<? extends Order>) results);
}

    public static Order getLatestCurrentOrder(Connection conn, Integer userId) throws SQLException {
        Order order = null;

        if (userId != null) {
            PreparedStatement stmt = conn.prepareStatement("SELECT TOP 1 * FROM orders Where user_id = ? and complete = false");

            ResultSet results = stmt.executeQuery();

            if (results.next()) {
                order = new Order(results.getInt("id"), results.getInt("user_id"), false);
            }
        }

        return order;
    }

    public static int insertOrder(Connection conn, int userId) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("INSERT INTO orders VALUES (NULL, ?)", Statement.RETURN_GENERATED_KEYS);
        stmt.setInt(1, userId);
        stmt.executeUpdate();

        ResultSet keys =  stmt.getGeneratedKeys();

        keys.next();

        return keys.getInt(1);
    }
}
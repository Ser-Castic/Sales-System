package com.theironyard.charlotte;

import java.util.ArrayList;

public class User {
    Integer userId;
    String userName;
    String userEmail;
    ArrayList<Order> userOrders;

    public User() {
    }

    public User(Integer userId, String userName, String userEmail, ArrayList<Order> userOrders) {
        this.userId = userId;
        this.userName = userName;
        this.userEmail = userEmail;
        this.userOrders = userOrders;
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
}


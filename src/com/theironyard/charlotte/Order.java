package com.theironyard.charlotte;

import java.util.ArrayList;

public class Order {
    Integer orderId;
    ArrayList<Item> orderItems;

    public Order() {
    }

    public Order(Integer orderId, ArrayList<Item> orderItems) {
        this.orderId = orderId;
        this.orderItems = orderItems;
    }

    public Integer getOrderId() {
        return orderId;
    }

    public void setOrderId(Integer orderId) {
        this.orderId = orderId;
    }

    public ArrayList<Item> getOrderItems() {
        return orderItems;
    }

    public void setOrderItems(ArrayList<Item> orderItems) {
        this.orderItems = orderItems;
    }
}

package com.theironyard.charlotte;

import org.h2.tools.Server;
import spark.ModelAndView;
import spark.Session;
import spark.Spark;
import spark.template.mustache.MustacheTemplateEngine;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Main {

    public static void createTables(Connection conn) throws SQLException {
        Statement stmt = conn.createStatement();
        stmt.execute("CREATE TABLE IF NOT EXISTS users (id IDENTITY, name VARCHAR, email VARCHAR)");
        stmt.execute("CREATE TABLE IF NOT EXISTS items (id IDENTITY, name VARCHAR, quantity INT, price DOUBLE, order_id INT)");
        stmt.execute("CREATE TABLE IF NOT EXISTS orders (id IDENTITY, complete BOOLEAN, user_id INT)");
    }

    public static List<Order> getOrdersForUser(Connection conn, Integer userId) throws SQLException {
        List<Order> orderList = new ArrayList<>();
        PreparedStatement stmt = conn.prepareStatement("SELECT * FROM orders WHERE user_id = ?");
        stmt.setInt(1, userId);
        ResultSet results = stmt.executeQuery();
        while(results.next()) {
            orderList.add(new Order(results.getInt("id"), results.getInt("user_id"), results.getBoolean("complete")));
        }
        return orderList;
    }

    public static List<Item> getItemsForOrder(Connection conn, Integer orderId) throws SQLException {
        List<Item> itemsList = new ArrayList<>();
        PreparedStatement stmt = conn.prepareStatement("SELECT * FROM items where order_id = ?");
        stmt.setInt(1, orderId);
        ResultSet result = stmt.executeQuery();
        while(result.next()) {
            itemsList.add(new Item(result.getString("name"), result.getInt("quantity"), result.getDouble("price")));
        }
        return itemsList;
    }

    public static Order getLatestCurrentOrder(Connection conn, Integer userId) throws SQLException {
        Order order = null;

        if (userId != null) {
            PreparedStatement stmt = conn.prepareStatement("SELECT * FROM orders Where user_id = ? and complete = false");
            stmt.setInt(1, userId);
            ResultSet results = stmt.executeQuery();

            if (results.next()) {
                order = new Order(results.getInt("id"), results.getInt("user_id"), results.getBoolean("complete"));
            }
        }

        return order;
    }

    public static int insertOrder(Connection conn, int userId, boolean complete) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("INSERT INTO orders VALUES (NULL, ?, ?)", Statement.RETURN_GENERATED_KEYS);
        stmt.setInt(1, userId);
        stmt.setBoolean(2, complete);
        stmt.executeUpdate();

        ResultSet keys =  stmt.getGeneratedKeys();

        keys.next();

        return keys.getInt(1);
    }

    public static User selectUserById(Connection conn, Integer id) throws SQLException {
        User user = null;

        if (id != null) {
            PreparedStatement stmt = conn.prepareStatement("SELECT * FROM users WHERE id = ?");
            stmt.setInt(1, id);
            ResultSet results = stmt.executeQuery();
            if (results.next()) {
                user = makeUser(results);
//                user.setOrders(Order.getOrdersForUser(conn, id));
            }
        }
        return user;
    }

    public static User makeUser(ResultSet results) throws SQLException{
        int id = results.getInt("id");
        String name = results.getString("name");
        String email = results.getString("email");
        return new User(id, name, email);
    }

    public static Integer selectUserByEmail(Connection conn, String email) throws SQLException {
        Integer userId = null;

        if (email != null) {
            PreparedStatement stmt = conn.prepareStatement("SELECT * FROM users WHERE email = ?");
            stmt.setString(1, email);
            ResultSet results = stmt.executeQuery();
            if (results.next()) {
                userId = results.getInt("id");
            }
        }
        return userId;
    }

    public static void insertUser(Connection conn, String name, String email) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("INSERT INTO users VALUES (NULL, ?, ?)");
        stmt.setString(1, name);
        stmt.setString(2, email);
        stmt.execute();
    }

    public static void insertItem(Connection conn, Item item) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("INSERT INTO items VALUES (NULL, ?, ?, ?, ?)");
        stmt.setString(1, item.getName());
        stmt.setInt(2, item.getQuantity());
        stmt.setDouble(3, item.getPrice());
        stmt.setInt(4, item.getOrderId());
        stmt.execute();
    }

    public static void main(String[] args) throws SQLException {
        try {
            Server.createWebServer().start();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        Connection conn = DriverManager.getConnection("jdbc:h2:./main");
        createTables(conn);

        Spark.get(
                "/",
                ((request, response) -> {
                    HashMap m = new HashMap();
                    Session session = request.session();

                    Integer userId = session.attribute("userId");
                    if (userId == null) {//if we don't have a User name lets ask them to log in
                        return new ModelAndView(m, "login.html");
                    } else {//we have one...yay! lets make a view
                        User currentUser = selectUserById(conn, userId);
                        List<Order> orders =  getOrdersForUser(conn, currentUser.getId());
                        List<Item> items =  getItemsForOrder(conn, currentUser.getId());

                        m.put("user", currentUser);
                        m.put("orders", orders);
                        m.put("items", items);
                        return new ModelAndView(m, "home.html");
                    }
                }),
                new MustacheTemplateEngine()
        );

        Spark.post(
                "/login",
                ((request, response) -> {
                    String email = request.queryParams("email");
                    String name = request.queryParams("name");
                    Integer userId = selectUserByEmail(conn, email);
                    if(userId != null) {
                        User currentUser = selectUserById(conn, userId);
                        Session session = request.session();
                        session.attribute("userId", currentUser.getId());
                        response.redirect("/");
                    } else {
                        insertUser(conn, name, email);
                        Integer newUserId = selectUserByEmail(conn, email);
                        User currentUser = selectUserById(conn, newUserId);
                        Session session = request.session();
                        session.attribute("userId", currentUser.getId());
                        response.redirect("/");
                    }
                    return "";
                })
        );

        Spark.post("/items", (request, response) -> {
            Session session = request.session();
            User current = selectUserById(conn, session.attribute("userId"));

            if (current != null) {
                // see if there is a current order
                Order currentOrder = getLatestCurrentOrder(conn, current.getId());

                if (currentOrder == null) {
                    // if not, make a new one
                    insertOrder(conn, current.getId(), false);

                    // get item from post data
                    Item postedItem = new Item(request.queryParams("itemName"),
                            Integer.valueOf(request.queryParams("quantity")),
                            Double.valueOf(request.queryParams("price")));
                    // add item to order
                    insertItem(conn, postedItem);
                }

            }
            // redirect
            response.redirect("/");
            return "";
        });

        Spark.post(
                "/logout",
                ((request, response) -> {
                    Session session = request.session();
                    session.invalidate();
                    response.redirect("/");
                    return "";
                })
        );
    }
}
package com.theironyard.charlotte;

import org.h2.tools.Server;
import spark.ModelAndView;
import spark.Session;
import spark.Spark;
import spark.template.mustache.MustacheTemplateEngine;

import java.sql.*;
import java.util.HashMap;

public class Main {

    public static void createTables(Connection conn) throws SQLException {
        Statement stmt = conn.createStatement();
        stmt.execute("CREATE TABLE IF NOT EXISTS users (id IDENTITY, name VARCHAR, email VARCHAR)");
        stmt.execute("CREATE TABLE IF NOT EXISTS items (id IDENTITY, name VARCHAR, quantity INT, price DOUBLE, order_id INT)");
        stmt.execute("CREATE TABLE IF NOT EXISTS orders (id IDENTITY, user_id INT)");
    }

    public static void main(String[] args) throws SQLException {
        try {
            Server.createWebServer().start();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        Connection conn = DriverManager.getConnection("jdbc:h2:./main");
        createTables(conn);

        Spark.get("/", (request, response) -> {
            HashMap model = new HashMap();
            Session session = request.session();

            User current = User.selectUserById(conn, session.attribute("the_user"));

            if (current != null) {
                // pass user into model
                model.put("user", current);
                model.put("order_id", Order.insertOrder(conn, current.getId()));

//                session.attribute("orderid", insertOrder(current.getId()));

                return new ModelAndView(model, "home.html");
            } else {
                return new ModelAndView(model, "login.html");
            }
        }, new MustacheTemplateEngine());

        Spark.post("/login", (request, response) -> {
            String email = request.queryParams("email");

            // look up the user by email address
            Integer userId = User.selectUserByEmail(conn, email);

            // if the user exists, save the id in session.
            if (userId != null) {
                Session session = request.session();
                session.attribute("the_user", userId);
            }
            response.redirect("/");
            return "";
        });

        Spark.post("/items", (request, response) -> {
            Session session = request.session();

            User current = User.selectUserById(conn, session.attribute("the_user"));

            if (current != null) {
                // see if there is a current order
                Order currentOrder = Order.getLatestCurrentOrder(conn, current.getId());

                if (currentOrder == null) {
                    // if not, make a new one
                    int orderId = Order.insertOrder(conn, current.getId());

                    // get item from post data
                    Item postedItem = new Item(request.queryParams("name"),
                            Integer.valueOf(request.queryParams("quantity")),
                            Double.valueOf("price"), orderId);

                    // add item to order
                    Item.insertItem(conn, postedItem);
                }
            }
            // redirect
            response.redirect("/");
            return "";
        });
    }
}
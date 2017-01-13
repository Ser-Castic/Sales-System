package com.theironyard.charlotte;

import jodd.json.JsonSerializer;
import org.h2.tools.Server;
import spark.ModelAndView;
import spark.Session;
import spark.Spark;
import spark.template.mustache.MustacheTemplateEngine;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;

public class Main {

    public static void createTables(Connection conn) throws SQLException {
        Statement stmt = conn.createStatement();
        stmt.execute("CREATE TABLE IF NOT EXISTS users (id IDENTITY, username VARCHAR, email VARCHAR)");
        stmt.execute("CREATE TABLE IF NOT EXISTS items (id IDENTITY, item_name VARCHAR, item_quantity INT, item_price DOUBLE, order_id INT)");
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

//        Spark.externalStaticFileLocation("public");
        Spark.init();

        Spark.get(
                "/",
                ((request, response) -> {
                    HashMap m = new HashMap();

                    Session session = request.session();
                    Integer userId = session.attribute("userId");

                    User currentUser = User.selectUserById(conn, new User(userId));

                    if (currentUser == null) {
                        return new ModelAndView(m, "login.html");
                    } else {
                        m.put("name", currentUser.userName);
                        m.put("email", currentUser.userEmail);
                        return new ModelAndView(m, "home.html");
                    }
                }),
                new MustacheTemplateEngine()
        );

        Spark.post(
                "/login",
                (request, response) -> {
                    String name = request.queryParams("loginName");
                    String email = request.queryParams("loginEmail");
                    User.insertUser(conn, new User(name, email));
                    User user = User.selectUserByNameAndEmail(conn, new User(name, email));

                    Session session = request.session();
                    session.attribute("id", user.getUserId());
                    session.attribute("userName", user.getUserName());
                    session.attribute("userEmail", user.getUserEmail());

                    response.redirect("/");
                    return "";
                });
    }
}

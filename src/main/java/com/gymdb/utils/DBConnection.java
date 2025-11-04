package com.gymdb.utils;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DBConnection {
    private static final String URL;
    private static final String USER;
    private static final String PASSWORD;
    private static Connection connection = null;

    // static block: load db.properties once
    static {
        try (InputStream input = DBConnection.class.getResourceAsStream("/db.properties")) {
            Properties p = new Properties();
            p.load(input);
            URL = p.getProperty("db.url");
            USER = p.getProperty("db.user");
            PASSWORD = p.getProperty("db.password");
            System.out.println("Loaded DB config from properties file.");
        } catch (Exception e) {
            throw new RuntimeException("Failed to load database configuration.", e);
        }
    }

    public static Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
                System.out.println("Database connection established!");
            }
        } catch (SQLException e) {
            System.err.println("Failed to connect to the database. Check URL, username, and password.");
            e.printStackTrace();
        }
        return connection;
    }

    public static void main(String[] args) {
        System.out.println("Testing DB Connection...");
        getConnection();
    }
}

package com.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.event.ActionEvent;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.utils.DBconnection;

public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private TextField passwordField;

    @FXML
    private Button loginButton;

    @FXML
    private Button staffLoginButton;

    @FXML
    private Button customerLoginButton;

    // Called when the login button is clicked
    @FXML
    private void handleLogin(ActionEvent event) {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showAlert("Error", "Please enter both username and password.");
            return;
        }

        // Check for default admin login first
        if (username.equals("admin") && password.equals("ccinfom124!")) {
            showAlert("Success", "Admin login successful! Welcome " + username + ".");
            // TODO: load admin dashboard here
            return;
        }

        // Check the database if not admin
        try (Connection conn = DBconnection.getConnection()) {
            String query = "SELECT * FROM users WHERE username = ? AND password = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, username);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                showAlert("Success", "Login successful! Welcome " + username + ".");
                // TODO: load next scene here
            } else {
                showAlert("Error", "Invalid username or password.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Database Error", "Failed to connect or query database.");
        }
    }

    @FXML
    private void handleStaffLogin(ActionEvent event) {
        showAlert("Staff Login", "Redirecting to Staff Login...");
        // TODO: Add staff login behavior (e.g., load staff dashboard)
    }

    @FXML
    private void handleCustomerLogin(ActionEvent event) {
        showAlert("Customer Login", "Redirecting to Customer Login...");
        // TODO: Add customer login behavior
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}


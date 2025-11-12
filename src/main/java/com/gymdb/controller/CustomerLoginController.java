package com.gymdb.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.io.FileWriter;
import java.io.IOException;

public class CustomerLoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private TextField passwordField;

    private boolean validateUser(String user, String pass) {
        try {
            java.util.List<String> lines = java.nio.file.Files.readAllLines(java.nio.file.Paths.get("users.txt"));

            for (String line : lines) {
                String[] data = line.split(",");

                // data[0] = username, data[1] = password
                if (data.length >= 2 && data[0].equals(user) && data[1].equals(pass)) {
                    return true;
                }
            }
        } catch (IOException e) {
            // If file doesn't exist or can't be read
            return false;
        }
        return false;
    }

    @FXML
    private void handleLogin(ActionEvent event) {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showAlert("Missing Information", "Please enter both username and password.");
            return;
        }

        if (validateUser(username, password)) {
            showAlert("Login Successful", "Welcome, " + username + "!");
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxmls/CustomersDashboard.fxml"));
                Parent root = loader.load();

                // Pass username to CustomersDashboardController
                CustomersDashboardController controller = loader.getController();
                controller.setCurrentUsername(username);

                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            showAlert("Login Failed", "Invalid username or password.");
        }
    }

    @FXML
    private void handleAdmin(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxmls/AdminLogin.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleStaff(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxmls/StaffLogin.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleSignUp(ActionEvent event) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Create Account");

        // Buttons
        ButtonType createButton = new ButtonType("Create Account", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(createButton, ButtonType.CANCEL);

        // FORM FIELDS
        TextField fullName = new TextField();
        fullName.setPromptText("Full Name");

        TextField username = new TextField();
        username.setPromptText("Username");

        PasswordField password = new PasswordField();
        password.setPromptText("Password");

        TextField phone = new TextField();
        phone.setPromptText("Phone Number");

        TextField address = new TextField();
        address.setPromptText("Address");

        ComboBox<String> planBox = new ComboBox<>();
        planBox.getItems().addAll("Monthly", "Yearly");
        planBox.setPromptText("Plan");

        ComboBox<String> serviceBox = new ComboBox<>();
        serviceBox.getItems().addAll("Yoga", "Strength Training", "HIIT", "Zumba");
        serviceBox.setPromptText("Service");

        ComboBox<String> classBox = new ComboBox<>();
        classBox.setPromptText("Class");

        // AUTO-UPDATE CLASS OPTIONS
        serviceBox.setOnAction(e -> {
            classBox.getItems().clear();

            switch (serviceBox.getValue()) {
                case "Yoga":
                    classBox.getItems().addAll("Morning Yoga Flow", "Stretch & Relax", "Power Up");
                    break;
                case "Strength Training":
                    classBox.getItems().addAll("Body Pump Burn", "Core & Stability", "Upper Body Blast");
                    break;
                case "HIIT":
                    classBox.getItems().addAll("HIIT Express", "Total Body Inferno", "Cardio Crush");
                    break;
                case "Zumba":
                    classBox.getItems().addAll("Zumba Dance Party", "Latin Groove", "Pop & Sweat");
                    break;
            }
        });

        // LAYOUT
        GridPane grid = new GridPane();
        grid.setVgap(10);
        grid.setHgap(10);

        grid.add(new Label("Full Name:"), 0, 0);  grid.add(fullName, 1, 0);
        grid.add(new Label("Username:"), 0, 1);   grid.add(username, 1, 1);
        grid.add(new Label("Password:"), 0, 2);   grid.add(password, 1, 2);
        grid.add(new Label("Phone:"), 0, 3);      grid.add(phone, 1, 3);
        grid.add(new Label("Address:"), 0, 4);    grid.add(address, 1, 4);
        grid.add(new Label("Plan:"), 0, 5);       grid.add(planBox, 1, 5);
        grid.add(new Label("Service:"), 0, 6);    grid.add(serviceBox, 1, 6);
        grid.add(new Label("Class:"), 0, 7);      grid.add(classBox, 1, 7);

        dialog.getDialogPane().setContent(grid);

        // HANDLE BUTTON PRESS
        dialog.setResultConverter(button -> {
            if (button == createButton) {

                if (fullName.getText().isEmpty() || username.getText().isEmpty() || password.getText().isEmpty() ||
                        phone.getText().isEmpty() || address.getText().isEmpty() ||
                        planBox.getValue() == null || serviceBox.getValue() == null || classBox.getValue() == null) {

                    showAlert("Missing Information", "Please fill out all fields.");
                    return null;
                }

                // SAVE USER TO FILE
                try (FileWriter writer = new FileWriter("users.txt", true)) {
                    writer.write(username.getText() + "," +
                            password.getText() + "," +
                            fullName.getText() + "," +
                            phone.getText() + "," +
                            address.getText() + "," +
                            planBox.getValue() + "," +
                            serviceBox.getValue() + "," +
                            classBox.getValue() + "\n");
                } catch (Exception e) {
                    showAlert("Error", "Failed to save user.");
                }

                showAlert("Success", "Account created successfully!");
            }
            return null;
        });

        dialog.showAndWait();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

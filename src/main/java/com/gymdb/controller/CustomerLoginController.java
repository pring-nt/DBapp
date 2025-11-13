package com.gymdb.controller;

import com.gymdb.model.Member;
import com.gymdb.model.MemberCRUD;
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
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class CustomerLoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private TextField passwordField;

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

                // Pass username to dashboard
                CustomersDashboardController dashboardController = loader.getController();
                dashboardController.setCurrentUsername(username);

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

    private boolean validateUser(String user, String pass) {
        try {
            List<String> lines = Files.readAllLines(Paths.get("users.txt"));

            for (String line : lines) {
                String[] data = line.split(",");
                if (data.length >= 2 && data[0].equals(user) && data[1].equals(pass)) {
                    return true;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    @FXML
    private void handleAdmin(ActionEvent event) {
        loadScreen(event, "/fxmls/AdminLogin.fxml");
    }

    @FXML
    private void handleStaff(ActionEvent event) {
        loadScreen(event, "/fxmls/StaffLogin.fxml");
    }

    @FXML
    private void handleSignUp(ActionEvent event) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Create Account");

        ButtonType createButton = new ButtonType("Create Account", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(createButton, ButtonType.CANCEL);

        TextField fullName = new TextField(); fullName.setPromptText("Full Name");
        TextField username = new TextField(); username.setPromptText("Username");
        PasswordField password = new PasswordField(); password.setPromptText("Password");
        TextField phone = new TextField(); phone.setPromptText("Phone Number");
        TextField address = new TextField(); address.setPromptText("Address");
        TextField emailField = new TextField(); emailField.setPromptText("Email");

        ComboBox<String> planBox = new ComboBox<>();
        planBox.getItems().addAll("Monthly", "Yearly");
        planBox.setPromptText("Plan");

        DatePicker startPicker = new DatePicker();
        startPicker.setPromptText("Start Date");
        DatePicker endPicker = new DatePicker();
        endPicker.setPromptText("End Date");

        GridPane grid = new GridPane();
        grid.setVgap(10);
        grid.setHgap(10);

        grid.add(new Label("Full Name:"), 0, 0);    grid.add(fullName, 1, 0);
        grid.add(new Label("Username:"), 0, 1);     grid.add(username, 1, 1);
        grid.add(new Label("Password:"), 0, 2);     grid.add(password, 1, 2);
        grid.add(new Label("Phone:"), 0, 3);        grid.add(phone, 1, 3);
        grid.add(new Label("Address:"), 0, 4);      grid.add(address, 1, 4);
        grid.add(new Label("Plan:"), 0, 5);         grid.add(planBox, 1, 5);
        grid.add(new Label("Email:"), 0, 6);        grid.add(emailField, 1, 6);
        grid.add(new Label("Start Date:"), 0, 7);   grid.add(startPicker, 1, 7);
        grid.add(new Label("End Date:"), 0, 8);     grid.add(endPicker, 1, 8);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(button -> {
            if (button == createButton) {
                // Validate inputs
                if (fullName.getText().isEmpty() || username.getText().isEmpty() || password.getText().isEmpty()
                        || phone.getText().isEmpty() || address.getText().isEmpty()
                        || planBox.getValue() == null || emailField.getText().isEmpty()
                        || startPicker.getValue() == null || endPicker.getValue() == null) {
                    showAlert("Missing Information", "Please fill out all fields.");
                    return null;
                }

                LocalDate start = startPicker.getValue();
                LocalDate end = endPicker.getValue();
                if (end.isBefore(start)) {
                    showAlert("Invalid Dates", "End date must be the same or after start date.");
                    return null;
                }

                // Append to users.txt (keeps backward compatibility)
                try (FileWriter writer = new FileWriter("users.txt", true)) {
                    // Format: username,password,fullname,phone,address,plan,email,startDate,endDate
                    writer.write(username.getText().trim() + "," +
                            password.getText().trim() + "," +
                            fullName.getText().trim() + "," +
                            phone.getText().trim() + "," +
                            address.getText().trim() + "," +
                            planBox.getValue() + "," +
                            emailField.getText().trim() + "," +
                            start.toString() + "," +
                            end.toString() + "\n");
                } catch (Exception e) {
                    showAlert("Error", "Failed to save user file.");
                    return null;
                }

                // Insert member into DB so it shows in ListMembers
                try {
                    // split full name into first and last (simple heuristic)
                    String fn = fullName.getText().trim();
                    String first = fn;
                    String last = "";
                    int idx = fn.lastIndexOf(' ');
                    if (idx > 0) {
                        first = fn.substring(0, idx).trim();
                        last = fn.substring(idx + 1).trim();
                    }

                    Member m = new Member(
                            0,                          // memberID (auto)
                            first,                      // firstName
                            last,                       // lastName
                            emailField.getText().trim(),// email (now explicit)
                            phone.getText().trim(),     // contactNo
                            planBox.getValue(),         // membershipType
                            start,                      // startDate
                            end,                        // endDate
                            "",                         // healthGoal
                            null,                       // initialWeight
                            null,                       // goalWeight
                            null,                       // startBMI
                            null,                       // updatedBMI
                            null,                       // classID
                            null,                       // trainerID
                            null                        // lockerID
                    );

                    MemberCRUD crud = new MemberCRUD();
                    boolean added = crud.addRecord(m);
                    if (!added) {
                        showAlert("DB Error", "Account saved to file but failed to insert member into database (email may already exist).");
                    } else {
                        showAlert("Success", "Account created and member added to database.");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    showAlert("Error", "Failed to create member in database.");
                }
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

    private void loadScreen(ActionEvent event, String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

//package com.gymdb.controller;
//
//import javafx.event.ActionEvent;
//import javafx.fxml.FXML;
//import javafx.fxml.FXMLLoader;
//import javafx.scene.Node;
//import javafx.scene.Parent;
//import javafx.scene.Scene;
//import javafx.scene.control.*;
//import javafx.scene.layout.GridPane;
//import javafx.stage.Stage;
//
//import java.io.FileWriter;
//import java.io.IOException;
//import java.nio.file.Files;
//import java.nio.file.Paths;
//import java.util.List;
//
//public class CustomerLoginController {
//
//    @FXML
//    private TextField usernameField;
//
//    @FXML
//    private TextField passwordField;
//
//    @FXML
//    private void handleLogin(ActionEvent event) {
//        String username = usernameField.getText();
//        String password = passwordField.getText();
//
//        if (username.isEmpty() || password.isEmpty()) {
//            showAlert("Missing Information", "Please enter both username and password.");
//            return;
//        }
//
//        if (validateUser(username, password)) {
//            showAlert("Login Successful", "Welcome, " + username + "!");
//            try {
//                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxmls/CustomersDashboard.fxml"));
//                Parent root = loader.load();
//
//                // Pass username to dashboard
//                CustomersDashboardController dashboardController = loader.getController();
//                dashboardController.setCurrentUsername(username);
//
//                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
//                stage.setScene(new Scene(root));
//                stage.show();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        } else {
//            showAlert("Login Failed", "Invalid username or password.");
//        }
//    }
//
//    private boolean validateUser(String user, String pass) {
//        try {
//            List<String> lines = Files.readAllLines(Paths.get("users.txt"));
//
//            for (String line : lines) {
//                String[] data = line.split(",");
//                if (data.length >= 2 && data[0].equals(user) && data[1].equals(pass)) {
//                    return true;
//                }
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return false;
//    }
//
//    @FXML
//    private void handleAdmin(ActionEvent event) {
//        loadScreen(event, "/fxmls/AdminLogin.fxml");
//    }
//
//    @FXML
//    private void handleStaff(ActionEvent event) {
//        loadScreen(event, "/fxmls/StaffLogin.fxml");
//    }
//
//    @FXML
//    private void handleSignUp(ActionEvent event) {
//        Dialog<Void> dialog = new Dialog<>();
//        dialog.setTitle("Create Account");
//
//        ButtonType createButton = new ButtonType("Create Account", ButtonBar.ButtonData.OK_DONE);
//        dialog.getDialogPane().getButtonTypes().addAll(createButton, ButtonType.CANCEL);
//
//        TextField fullName = new TextField(); fullName.setPromptText("Full Name");
//        TextField username = new TextField(); username.setPromptText("Username");
//        PasswordField password = new PasswordField(); password.setPromptText("Password");
//        TextField phone = new TextField(); phone.setPromptText("Phone Number");
//        TextField address = new TextField(); address.setPromptText("Address");
//
//        ComboBox<String> planBox = new ComboBox<>();
//        planBox.getItems().addAll("Monthly", "Yearly");
//        planBox.setPromptText("Plan");
//
//        ComboBox<String> serviceBox = new ComboBox<>();
//        serviceBox.getItems().addAll("Yoga", "Strength Training", "HIIT", "Zumba");
//        serviceBox.setPromptText("Service");
//
//        ComboBox<String> classBox = new ComboBox<>();
//        classBox.setPromptText("Class");
//
//        serviceBox.setOnAction(e -> {
//            classBox.getItems().clear();
//            switch (serviceBox.getValue()) {
//                case "Yoga" -> classBox.getItems().addAll("Morning Yoga Flow", "Stretch & Relax", "Power Up");
//                case "Strength Training" -> classBox.getItems().addAll("Body Pump Burn", "Core & Stability", "Upper Body Blast");
//                case "HIIT" -> classBox.getItems().addAll("HIIT Express", "Total Body Inferno", "Cardio Crush");
//                case "Zumba" -> classBox.getItems().addAll("Zumba Dance Party", "Latin Groove", "Pop & Sweat");
//            }
//        });
//
//        GridPane grid = new GridPane();
//        grid.setVgap(10);
//        grid.setHgap(10);
//
//        grid.add(new Label("Full Name:"), 0, 0);  grid.add(fullName, 1, 0);
//        grid.add(new Label("Username:"), 0, 1);   grid.add(username, 1, 1);
//        grid.add(new Label("Password:"), 0, 2);   grid.add(password, 1, 2);
//        grid.add(new Label("Phone:"), 0, 3);      grid.add(phone, 1, 3);
//        grid.add(new Label("Address:"), 0, 4);    grid.add(address, 1, 4);
//        grid.add(new Label("Plan:"), 0, 5);       grid.add(planBox, 1, 5);
//        grid.add(new Label("Service:"), 0, 6);    grid.add(serviceBox, 1, 6);
//        grid.add(new Label("Class:"), 0, 7);      grid.add(classBox, 1, 7);
//
//        dialog.getDialogPane().setContent(grid);
//
//        dialog.setResultConverter(button -> {
//            if (button == createButton) {
//                if (fullName.getText().isEmpty() || username.getText().isEmpty() || password.getText().isEmpty() ||
//                        phone.getText().isEmpty() || address.getText().isEmpty() ||
//                        planBox.getValue() == null || serviceBox.getValue() == null || classBox.getValue() == null) {
//                    showAlert("Missing Information", "Please fill out all fields.");
//                    return null;
//                }
//
//                try (FileWriter writer = new FileWriter("users.txt", true)) {
//                    writer.write(username.getText() + "," +
//                            password.getText() + "," +
//                            fullName.getText() + "," +
//                            phone.getText() + "," +
//                            address.getText() + "," +
//                            planBox.getValue() + "," +
//                            serviceBox.getValue() + "," +
//                            classBox.getValue() + "\n");
//                } catch (Exception e) {
//                    showAlert("Error", "Failed to save user.");
//                }
//
//                showAlert("Success", "Account created successfully!");
//            }
//            return null;
//        });
//
//        dialog.showAndWait();
//    }
//
//    private void showAlert(String title, String message) {
//        Alert alert = new Alert(Alert.AlertType.INFORMATION);
//        alert.setTitle(title);
//        alert.setHeaderText(null);
//        alert.setContentText(message);
//        alert.showAndWait();
//    }
//
//    private void loadScreen(ActionEvent event, String fxmlPath) {
//        try {
//            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
//            Parent root = loader.load();
//            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
//            stage.setScene(new Scene(root));
//            stage.show();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//}

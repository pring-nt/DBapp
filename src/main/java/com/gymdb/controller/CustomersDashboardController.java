package com.gymdb.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class CustomersDashboardController {

    private String currentUsername;

    public void setCurrentUsername(String username) {
        this.currentUsername = username;
    }

    @FXML
    private void handleToDo(ActionEvent event) throws IOException {
        loadScreen(event, "/fxmls/ToDoList.fxml");
    }

    @FXML
    private void handleProduct(ActionEvent event) throws IOException {
        loadScreen(event, "/fxmls/BuyProduct.fxml");
    }

    @FXML
    private void handleLocker(ActionEvent event) throws IOException {
        loadScreen(event, "/fxmls/Locker.fxml");
    }

    @FXML
    private void handleYoga(ActionEvent event) throws IOException {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxmls/Yoga.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleST(ActionEvent event) throws IOException {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxmls/StrengthTraining.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleZumba(ActionEvent event) throws IOException {
        loadScreen(event, "/fxmls/Zumba.fxml");
    }

    @FXML
    private void handleReports(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxmls/Reports.fxml"));
            Parent root = loader.load();

            ReportsController reportsController = loader.getController();
            reportsController.setCurrentUsername(currentUsername);

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleBack(ActionEvent event) throws IOException {
        loadScreen(event, "/fxmls/CustomerLogin.fxml");
    }

    private void loadScreen(ActionEvent event, String fxmlPath) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
        Parent root = loader.load();

        // Pass current username if the next screen is Dashboard or Reports
        Object controller = loader.getController();
        try {
            controller.getClass().getMethod("setCurrentUsername", String.class)
                    .invoke(controller, currentUsername);
        } catch (Exception ignored) {}

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }
}

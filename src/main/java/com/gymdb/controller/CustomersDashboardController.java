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
    private void handleToDo(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxmls/ToDoList.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @FXML
    private void handleProduct(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxmls/BuyProduct.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @FXML
    private void handleLocker(ActionEvent event) {
        System.out.println("Locker clicked");
        // loadNextScreen(event, "/path/to/announcements.fxml");
    }

    @FXML
    private void handleReports(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxmls/Reports.fxml"));
            Parent root = loader.load();

            // Get the controller and pass the current username
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
    private void handleBack(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxmls/CustomerLogin.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Optional reusable screen switcher
    private void loadNextScreen(ActionEvent event, String fxmlPath) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

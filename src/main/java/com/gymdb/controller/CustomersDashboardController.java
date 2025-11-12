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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxmls/ToDoList.fxml"));
            Parent root = loader.load();

            // Pass currentUsername to ToDo controller
            ToDoListController controller = loader.getController();
            controller.setCurrentUsername(currentUsername);

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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxmls/BuyProduct.fxml"));
            Parent root = loader.load();

            // Pass currentUsername to BuyProduct controller if needed
            BuyProductController controller = loader.getController();
            controller.setCurrentUsername(currentUsername);

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
        // If you have a locker controller, you can pass currentUsername similarly
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
}

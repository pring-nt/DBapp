package com.gymdb.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

public class ReportsMenuController {

    private Stage stage;
    private Scene scene;
    private Parent root;

    @FXML
    private void handleLockerUsage(ActionEvent event) throws IOException {
        // TODO: Load Locker Usage Report page
        System.out.println("Locker Usage clicked");
        // navigate(event, "/com/gymdb/reports/LockerUsage.fxml");
    }

    @FXML
    private void handleMemberActivity(ActionEvent event) throws IOException {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxmls/MembersReport.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handlePerformanceReward(ActionEvent event) throws IOException {
        // TODO: Load Performance Reward Report page
        System.out.println("Performance Reward clicked");
        // navigate(event, "/com/gymdb/reports/PerformanceReward.fxml");
    }

    @FXML
    private void handleRetailSales(ActionEvent event) throws IOException {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxmls/RetailSalesReport.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleBack(ActionEvent event) throws IOException {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxmls/MainMenu.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

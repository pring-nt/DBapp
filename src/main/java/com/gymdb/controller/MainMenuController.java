package com.gymdb.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;

import java.io.IOException;

public class MainMenuController {

    @FXML private Button membersTab;
    @FXML private Button staffTab;
    @FXML private Button equipmentsTab;
    @FXML private Button classTab;
    @FXML private Button progressTab;
    @FXML private Button paymentsTab;
    @FXML private Button attendanceTab;
    @FXML private Button lockerTab;
    @FXML private Button productsTab;
    @FXML private Button reportsTab;
    @FXML private Button backBtn;

    @FXML
    public void initialize() {
        // This runs after the FXML loads. You can initialize values here.
    }

    // Event handlers (you’ll add navigation later)
    @FXML
    private void handleMembersTab(ActionEvent event) {

    }
    @FXML
    private void handleStaffTab(ActionEvent event) {

    }
    @FXML
    private void handleEquipmentsTab(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxmls/Equipment.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @FXML
    private void handleClassTab(ActionEvent event) {

    }
    @FXML
    private void handleProgressTab(ActionEvent event) {

    }
    @FXML
    private void handlePaymentsTab(ActionEvent event) {

    }
    @FXML
    private void handleAttendanceTab(ActionEvent event) {

    }
    @FXML
    private void handleLockerTab(ActionEvent event) {

    }
    @FXML
    private void handleProductsTab(ActionEvent event) {

    }
    @FXML
    private void handleReportsTab(ActionEvent event) {

    }
    @FXML
    private void handleBack(ActionEvent event) {
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

}

package com.gymdb.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

public class PaymentFormController {

    @FXML
    private TextField fullName;

    @FXML
    private TextField service;

    @FXML
    private TextField amountPerMonth;

    @FXML
    private TextField plan;

    @FXML
    private TextField memberStatus;

    @FXML
    private Button makepaymentBtn;

    @FXML
    private Button backBtn;

    // Called when the "Make Payment" button is pressed
    @FXML
    private void makePayment(ActionEvent event) {
        String name = fullName.getText().trim();
        String selectedService = service.getText().trim();
        String amount = amountPerMonth.getText().trim();
        String selectedPlan = plan.getText().trim();
        String status = memberStatus.getText().trim();

        // Simple validation
        if (name.isEmpty() || selectedService.isEmpty() || amount.isEmpty() || selectedPlan.isEmpty() || status.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Missing Information", "Please fill out all fields before making payment.");
            return;
        }

        // (Optional) Validate that amount is numeric
        try {
            Double.parseDouble(amount);
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Invalid Amount", "Please enter a valid numeric amount.");
            return;
        }

        // Simulate a successful payment
        showAlert(Alert.AlertType.INFORMATION, "Payment Successful",
                "Payment has been successfully processed for " + name + " (" + selectedPlan + " Plan).");

        // Clear fields after payment
        clearFields();
    }

    // Called when the "Back" button is pressed

    @FXML
    private void handleBack(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/fxmls/MainMenu.fxml"));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }
    // Utility method for showing alerts
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Clears all text fields
    private void clearFields() {
        fullName.clear();
        service.clear();
        amountPerMonth.clear();
        plan.clear();
        memberStatus.clear();
    }
}

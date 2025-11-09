package com.gymdb.controller;

import com.gymdb.model.Equipment;
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
import java.time.LocalDateTime;

public class AddEquipmentController {

    @FXML private TextField equipment_info;
    @FXML private TextField description_info;
    @FXML private TextField quantity_info;
    @FXML private TextField cpi_info;
    @FXML private TextField vendo_info;
    @FXML private TextField contact_info;
    @FXML private Button saveBtn;
    @FXML private Button backBtn;

    @FXML
    private void handleSave(ActionEvent event) throws IOException {
        try {
            String name = equipment_info.getText();
            String desc = description_info.getText();
            int qty = Integer.parseInt(quantity_info.getText());
            double price = Double.parseDouble(cpi_info.getText());
            String vendor = vendo_info.getText();
            String contact = contact_info.getText();
            LocalDateTime purchaseDate = LocalDateTime.now();

            Equipment newEquipment = new Equipment(0, name, desc, qty, price, vendor, contact, purchaseDate);

            // Open ShowEquipment window and add new equipment dynamically
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxmls/ShowEquipment.fxml"));
            Parent root = loader.load();

            ShowEquipmentController controller = loader.getController();
            controller.addNewEquipment(newEquipment);

            Stage stage = new Stage();
            stage.setTitle("Equipment List");
            stage.setScene(new Scene(root));
            stage.show();

            // Close AddEquipment window
            ((Stage) saveBtn.getScene().getWindow()).close();

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Input Error", "Please enter valid numbers for quantity and price.");
        }
    }

    @FXML
    private void handleBack(ActionEvent event) throws IOException {
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

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}

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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.gymdb.utils.DBConnection;

public class AddEquipmentController {

    @FXML private TextField equipment_info;
    @FXML private TextField description_info;
    @FXML private TextField quantity_info;
    @FXML private TextField dop_info;
    @FXML private TextField vendo_info;
    @FXML private TextField address_info;
    @FXML private TextField contact_info;
    @FXML private TextField cpi_info;
    @FXML private Button saveBtn;
    @FXML private Button backBtn;

    @FXML
    private void handleSave(ActionEvent event) {
        String query = "INSERT INTO equipment (equipment_name, description, quantity, amount, vendor_name, contact_number, purchase_date) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, equipment_info.getText());
            stmt.setString(2, description_info.getText());
            stmt.setInt(3, Integer.parseInt(quantity_info.getText()));
            stmt.setDouble(4, Double.parseDouble(cpi_info.getText()));
            stmt.setString(5, vendo_info.getText());
            stmt.setString(6, contact_info.getText());
            stmt.setString(7, dop_info.getText());

            stmt.executeUpdate();
            showAlert(Alert.AlertType.INFORMATION, "Success", "Equipment added successfully!");
            clearFields();

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", e.getMessage());
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Input Error", "Please enter valid numbers for quantity and amount.");
        }
    }

    @FXML
    private void handleBack(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/fxmls/Equipment.fxml"));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void clearFields() {
        equipment_info.clear();
        description_info.clear();
        quantity_info.clear();
        dop_info.clear();
        vendo_info.clear();
        address_info.clear();
        contact_info.clear();
        cpi_info.clear();
    }
}

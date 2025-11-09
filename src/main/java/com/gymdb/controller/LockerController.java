package com.gymdb.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.stage.Stage;

import java.io.IOException;

public class LockerController {

    @FXML private Button one;
    @FXML private Button two;
    @FXML private Button three;
    @FXML private Button four;
    @FXML private Button five;
    @FXML private Button sex; // assuming typo from "six"
    @FXML private Button seven;
    @FXML private Button eight;
    @FXML private Button nine;
    @FXML private Button ten;
    @FXML private Button eleven;
    @FXML private Button twelve;
    @FXML private Button thirteen;
    @FXML private Button forten; // likely meant "fourteen"
    @FXML private Button fifteen;
    @FXML private Button sixteen;
    @FXML private Button seventeen;
    @FXML private Button eighteen;
    @FXML private Button backBtn;

    // Handles all locker button clicks
    @FXML
    private void handleLockerClick(ActionEvent event) {
        Button clicked = (Button) event.getSource();
        String lockerNumber = clicked.getId().replaceAll("[^0-9]", ""); // extract number from ID
        showAlert(Alert.AlertType.INFORMATION, "Locker Selected",
                "You selected locker number: " + lockerNumber);
    }

    // Called when the "Back" button is pressed
    @FXML
    private void handleBack(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/fxmls/MainMenu.fxml"));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }

    // Helper for showing alerts
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}


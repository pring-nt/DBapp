package com.gymdb.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.event.ActionEvent;
import javafx.stage.Stage;
import javafx.scene.Node;

import java.io.IOException;
    public class ProductInventoryController {

        @FXML
        private Button backbtn;

        // Called when the "Back" button is pressed
        @FXML
        private void handleBack(ActionEvent event) throws IOException {
            Parent root = FXMLLoader.load(getClass().getResource("/fxmls/MainMenu.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        }

}

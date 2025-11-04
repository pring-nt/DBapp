package com.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

public class MainController {

    @FXML
    private void handleAllMembers(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/list_members.fxml"));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }

    @FXML
    private void handleEntryForm(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/entry_form.fxml"));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }


    @FXML
    private void handleRemove(ActionEvent event) {
        System.out.println("Remove clicked!");
    }

    @FXML
    private void handleUpdate(ActionEvent event) {
        System.out.println("Update clicked!");
    }
}

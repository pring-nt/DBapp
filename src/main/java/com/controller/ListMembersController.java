package com.controller;

import com.model.Member;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;

public class ListMembersController {

    @FXML
    private TableView<Member> membersTable;

    // Note: types must match your Member property types.
    @FXML private TableColumn<Member, Integer> colId;
    @FXML private TableColumn<Member, String> colFullName;
    @FXML private TableColumn<Member, String> colUsername;
    @FXML private TableColumn<Member, String> colGender;
    @FXML private TableColumn<Member, String> colContact;
    @FXML private TableColumn<Member, String> colDob;
    @FXML private TableColumn<Member, String> colAmount;   // amount is String in your model
    @FXML private TableColumn<Member, String> colService;
    @FXML private TableColumn<Member, String> colPlan;

    @FXML
    private void initialize() {
        // wire columns to Member properties (property names from Member.java)
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colFullName.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        colUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
        colGender.setCellValueFactory(new PropertyValueFactory<>("gender"));
        colContact.setCellValueFactory(new PropertyValueFactory<>("contactNumber"));
        colDob.setCellValueFactory(new PropertyValueFactory<>("dateOfBirth"));
        colAmount.setCellValueFactory(new PropertyValueFactory<>("amount"));
        colService.setCellValueFactory(new PropertyValueFactory<>("chosenService"));
        colPlan.setCellValueFactory(new PropertyValueFactory<>("plan"));

        // start empty (table headers visible)
        ObservableList<Member> empty = FXCollections.observableArrayList();
        membersTable.setItems(empty);

        // Optional: uncomment to test with sample data
        // empty.add(new Member(1, "Jane Doe", "jane", "Female", "09171234567", "2000-01-01", "0.00", "Service A", "Basic"));
    }

    @FXML
    private void handleBack(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/main-view.fxml"));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }
}

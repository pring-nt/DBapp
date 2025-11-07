package com.gymdb.controller;

import com.gymdb.model.Member;
import com.gymdb.model.MemberCRUD;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
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
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;

public class ListMembersController {

    @FXML
    private TableView<Member> membersTable;

    @FXML private TableColumn<Member, Integer> colId;
    @FXML private TableColumn<Member, String>  colFirstName;
    @FXML private TableColumn<Member, String>  colLastName;
    @FXML private TableColumn<Member, String>  colEmail;
    @FXML private TableColumn<Member, String>  colContact;
    @FXML private TableColumn<Member, String>  colMembership;
    @FXML private TableColumn<Member, String>  colStartDate;
    @FXML private TableColumn<Member, String>  colEndDate;

    @FXML
    private void initialize() {
        // Map record fields from your Member record class to table columns
        colId.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().memberID()));
        colFirstName.setCellValueFactory(c -> new ReadOnlyStringWrapper(
                c.getValue().firstName() == null ? "" : c.getValue().firstName()));
        colLastName.setCellValueFactory(c -> new ReadOnlyStringWrapper(
                c.getValue().lastName() == null ? "" : c.getValue().lastName()));
        colEmail.setCellValueFactory(c -> new ReadOnlyStringWrapper(
                c.getValue().email() == null ? "" : c.getValue().email()));
        colContact.setCellValueFactory(c -> new ReadOnlyStringWrapper(
                c.getValue().contactNo() == null ? "" : c.getValue().contactNo()));
        colMembership.setCellValueFactory(c -> new ReadOnlyStringWrapper(
                c.getValue().membershipType() == null ? "" : c.getValue().membershipType()));
        colStartDate.setCellValueFactory(c -> new ReadOnlyStringWrapper(
                c.getValue().startDate() == null ? "" : c.getValue().startDate().toString()));
        colEndDate.setCellValueFactory(c -> new ReadOnlyStringWrapper(
                c.getValue().endDate() == null ? "" : c.getValue().endDate().toString()));

        // Load data from the database
        loadMembersFromDB();
    }

    private void loadMembersFromDB() {
        MemberCRUD crud = new MemberCRUD();
        List<Member> members = crud.getAllRecords();
        ObservableList<Member> observableList = FXCollections.observableArrayList(members);
        membersTable.setItems(observableList);
    }

    @FXML
    private void handleBack(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/main-view.fxml"));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }
}

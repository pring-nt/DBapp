package com.gymdb.controller;

import com.gymdb.model.Member;
import com.gymdb.model.MemberCRUD;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class MainController {

    // For the Remove Member scene
    @FXML
    private ComboBox<Member> cmbMembers;

    private final MemberCRUD crud = new MemberCRUD();
    private final ObservableList<Member> members = FXCollections.observableArrayList();
    private boolean ignoreSelection = true; // prevents unwanted triggers during load

    @FXML
    private void handleAllMembers(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/fxmls/list_members.fxml"));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }

    @FXML
    private void handleEntryForm(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/fxmls/entry_form.fxml"));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }

    @FXML
    private void handleBack(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxmls/main-view.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @FXML
    private void handleBackMain(ActionEvent event) {
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

    // Handles going to the Remove Member page
    @FXML
    private void handleRemove(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxmls/remove.fxml"));
        Parent root = loader.load();

        // Get controller instance from remove.fxml (this same class handles it)
        MainController controller = loader.getController();
        controller.initializeRemoveComboBox(); // manually initialize comboBox

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }

    // Called when remove.fxml is opened
    private void initializeRemoveComboBox() {
        if (cmbMembers == null) return;

        // Set display format for each member in ComboBox
        cmbMembers.setConverter(new javafx.util.StringConverter<>() {
            @Override
            public String toString(Member m) {
                if (m == null) return "";
                String first = m.firstName() == null ? "" : m.firstName();
                String last = m.lastName() == null ? "" : m.lastName();
                return (first + " " + last).trim() + " (ID: " + m.memberID() + ")";
            }
            @Override
            public Member fromString(String s) { return null; }
        });

        // Load members from database
        loadMembersIntoComboBox();

        ignoreSelection = false;

        // Listener to trigger confirmation popup when user selects an item
        cmbMembers.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (ignoreSelection || newVal == null) return;
            Platform.runLater(() -> confirmAndDelete(newVal));
        });
    }

    private void loadMembersIntoComboBox() {
        try {
            List<Member> list = crud.getAllRecords();
            members.setAll(list);
            cmbMembers.setItems(members);
            ignoreSelection = true;
            cmbMembers.getSelectionModel().clearSelection();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            ignoreSelection = false;
        }
    }

    private void confirmAndDelete(Member member) {
        String name = (member.firstName() == null ? "" : member.firstName()) +
                (member.lastName() == null ? "" : " " + member.lastName());
        name = name.trim().isEmpty() ? "ID: " + member.memberID() : name + " (ID: " + member.memberID() + ")";

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Delete");
        confirm.setHeaderText(null);
        confirm.setContentText("Delete member " + name + "?");

        Optional<ButtonType> result = confirm.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            boolean success = crud.delRecord(member.memberID());
            if (success) {
                new Alert(Alert.AlertType.INFORMATION, "Member deleted successfully.").showAndWait();
                ignoreSelection = true;
                members.remove(member);
                cmbMembers.getSelectionModel().clearSelection();
                ignoreSelection = false;
            } else {
                new Alert(Alert.AlertType.ERROR, "Failed to delete member.").showAndWait();
            }
        } else {
            ignoreSelection = true;
            cmbMembers.getSelectionModel().clearSelection();
            ignoreSelection = false;
        }
    }

    @FXML
    private void handleUpdate(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/fxmls/update.fxml"));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }
}

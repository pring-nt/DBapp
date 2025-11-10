package com.gymdb.controller;

import com.gymdb.model.GymPersonnel;
import com.gymdb.model.GymPersonnelCRUD;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Optional;

public class TrainerEntryController {

    @FXML private TextField txtFirstName;
    @FXML private TextField txtLastName;
    @FXML private TextField txtRecord;
    @FXML private TextField txtSpeciality;
    @FXML private ComboBox<String> cmbType;
    @FXML private TextField txtSchedule;

    private final GymPersonnelCRUD crud = new GymPersonnelCRUD();

    @FXML
    private void initialize() {
        // Populate personnel type options and default to Trainer
        cmbType.getItems().addAll("Trainer", "Staff");
        cmbType.setValue("Trainer");
    }

    @FXML
    private void handleSave(ActionEvent event) {
        // Basic validation
        String first = txtFirstName.getText() == null ? "" : txtFirstName.getText().trim();
        String last  = txtLastName.getText() == null ? "" : txtLastName.getText().trim();
        String type  = cmbType.getValue();
        String schedule = txtSchedule.getText() == null ? "" : txtSchedule.getText().trim();
        String record = txtRecord.getText() == null ? "" : txtRecord.getText().trim();
        String speciality = txtSpeciality.getText() == null ? "" : txtSpeciality.getText().trim();

        if (first.isEmpty() || last.isEmpty()) {
            Alert a = new Alert(Alert.AlertType.WARNING, "First name and last name are required.");
            a.setHeaderText(null);
            a.showAndWait();
            return;
        }

        // Confirm before saving
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Add");
        confirm.setHeaderText(null);
        confirm.setContentText("Are you sure you want to add " + type.toLowerCase() + "?");
        Optional<ButtonType> res = confirm.showAndWait();

        if (res.isEmpty() || res.get() != ButtonType.OK) {
            // user cancelled
            return;
        }

        // Build GymPersonnel record (ID is handled by DB)
        GymPersonnel gp = new GymPersonnel(
                0,
                first,
                last,
                type == null ? "Trainer" : type,
                schedule,
                record,
                speciality
        );

        boolean ok = crud.addRecord(gp);
        if (ok) {
            Alert info = new Alert(Alert.AlertType.INFORMATION, type + " added successfully.");
            info.setHeaderText(null);
            info.showAndWait();
            clearForm();
        } else {
            Alert err = new Alert(Alert.AlertType.ERROR, "Failed to add " + type.toLowerCase() + ". Check console for details.");
            err.setHeaderText(null);
            err.showAndWait();
        }
    }

    private void clearForm() {
        txtFirstName.clear();
        txtLastName.clear();
        txtRecord.clear();
        txtSpeciality.clear();
        txtSchedule.clear();
        cmbType.setValue("Trainer");
    }

    @FXML
    private void handleBack(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxmls/trainer_menu.fxml")); // adjust path if needed
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            Alert err = new Alert(Alert.AlertType.ERROR, "Failed to load Trainer Menu.");
            err.setHeaderText(null);
            err.showAndWait();
        }
    }
}

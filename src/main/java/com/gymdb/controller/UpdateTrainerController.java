package com.gymdb.controller;

import com.gymdb.model.GymPersonnel;
import com.gymdb.model.GymPersonnelCRUD;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class UpdateTrainerController {

    @FXML private ComboBox<GymPersonnel> cmbTrainers;
    @FXML private Button btnBack;

    private final GymPersonnelCRUD crud = new GymPersonnelCRUD();
    private final ObservableList<GymPersonnel> trainers = FXCollections.observableArrayList();

    // guard to avoid handling selection events during initialization/clearing
    private boolean ignoreSelection = true;

    @FXML
    private void initialize() {
        // Show "First Last (ID: #)" in the combo
        cmbTrainers.setConverter(new StringConverter<>() {
            @Override
            public String toString(GymPersonnel p) {
                if (p == null) return "";
                String f = p.firstName() == null ? "" : p.firstName();
                String l = p.lastName() == null ? "" : p.lastName();
                return (f + " " + l).trim() + " (ID: " + p.personnelID() + ")";
            }

            @Override
            public GymPersonnel fromString(String string) { return null; }
        });

        loadTrainers();

        // allow user selection events to trigger after initial load
        ignoreSelection = false;

        // listen for selection changes
        cmbTrainers.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (ignoreSelection || newVal == null) return;
            Platform.runLater(() -> startUpdateFlow(newVal));
        });
    }

    private void loadTrainers() {
        try {
            List<GymPersonnel> all = crud.getAllRecords();
            List<GymPersonnel> onlyTrainers = all.stream()
                    .filter(p -> p.personnelType() != null && p.personnelType().equalsIgnoreCase("trainer"))
                    .collect(Collectors.toList());
            trainers.setAll(onlyTrainers);
            cmbTrainers.setItems(trainers);

            // clear selection safely
            ignoreSelection = true;
            cmbTrainers.getSelectionModel().clearSelection();
        } catch (Exception e) {
            e.printStackTrace();
            trainers.clear();
            cmbTrainers.setItems(trainers);
        } finally {
            ignoreSelection = false;
        }
    }

    /**
     * The full update flow: choose field -> input new value -> confirm -> update DB -> refresh UI.
     */
    private void startUpdateFlow(GymPersonnel selected) {
        if (selected == null) return;

        List<String> choices = Arrays.asList(
                "First name",
                "Last name",
                "Schedule",
                "Speciality",
                "Instructor record"
        );

        ChoiceDialog<String> choiceDialog = new ChoiceDialog<>(choices.get(0), choices);
        choiceDialog.setTitle("Choose field");
        choiceDialog.setHeaderText(null);
        choiceDialog.setContentText("What do you want to edit?");
        Optional<String> choiceRes = choiceDialog.showAndWait();
        if (choiceRes.isEmpty()) {
            resetSelection();
            return;
        }

        String field = choiceRes.get();

        TextInputDialog inputDialog = new TextInputDialog();
        inputDialog.setTitle("Enter new value");
        inputDialog.setHeaderText(null);
        inputDialog.setContentText("New " + field + ":");
        Optional<String> inputRes = inputDialog.showAndWait();
        if (inputRes.isEmpty()) {
            resetSelection();
            return;
        }

        String newValue = inputRes.get().trim();
        if (newValue.isEmpty()) {
            Alert warn = new Alert(Alert.AlertType.WARNING, "Value cannot be empty.");
            warn.setHeaderText(null);
            warn.showAndWait();
            resetSelection();
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm change");
        confirm.setHeaderText(null);
        confirm.setContentText("Change " + field + " to:\n\n" + newValue + "\n\nConfirm?");
        Optional<ButtonType> conf = confirm.showAndWait();
        if (conf.isEmpty() || conf.get() != ButtonType.OK) {
            resetSelection();
            return;
        }

        // Build updated GymPersonnel record (copy old, replace chosen field)
        GymPersonnel updated;
        try {
            updated = buildUpdatedPersonnel(selected, field, newValue);
        } catch (IllegalArgumentException ex) {
            Alert err = new Alert(Alert.AlertType.ERROR, ex.getMessage());
            err.setHeaderText(null);
            err.showAndWait();
            resetSelection();
            return;
        }

        boolean ok = crud.modRecord(updated);
        if (ok) {
            Alert info = new Alert(Alert.AlertType.INFORMATION, "Trainer updated successfully.");
            info.setHeaderText(null);
            info.showAndWait();
            loadTrainers(); // refresh list from DB
        } else {
            Alert err = new Alert(Alert.AlertType.ERROR, "Failed to update trainer (DB error).");
            err.setHeaderText(null);
            err.showAndWait();
        }

        resetSelection();
    }

    private GymPersonnel buildUpdatedPersonnel(GymPersonnel old, String field, String newVal) {
        try {
            return switch (field) {
                case "First name" -> new GymPersonnel(
                        old.personnelID(),
                        newVal,
                        old.lastName(),
                        old.personnelType(),
                        old.schedule(),
                        old.instructorRecord(),
                        old.speciality()
                );
                case "Last name" -> new GymPersonnel(
                        old.personnelID(),
                        old.firstName(),
                        newVal,
                        old.personnelType(),
                        old.schedule(),
                        old.instructorRecord(),
                        old.speciality()
                );
                case "Schedule" -> new GymPersonnel(
                        old.personnelID(),
                        old.firstName(),
                        old.lastName(),
                        old.personnelType(),
                        newVal,
                        old.instructorRecord(),
                        old.speciality()
                );
                case "Speciality" -> new GymPersonnel(
                        old.personnelID(),
                        old.firstName(),
                        old.lastName(),
                        old.personnelType(),
                        old.schedule(),
                        old.instructorRecord(),
                        newVal
                );
                case "Instructor record" -> new GymPersonnel(
                        old.personnelID(),
                        old.firstName(),
                        old.lastName(),
                        old.personnelType(),
                        old.schedule(),
                        newVal,
                        old.speciality()
                );
                default -> throw new IllegalArgumentException("Unknown field: " + field);
            };
        } catch (NullPointerException npe) {
            throw new IllegalArgumentException("Member data incomplete — cannot update.");
        }
    }

    private void resetSelection() {
        ignoreSelection = true;
        if (cmbTrainers != null) cmbTrainers.getSelectionModel().clearSelection();
        ignoreSelection = false;
    }

    @FXML
    private void handleBack(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/fxmls/trainer_menu.fxml")); // adjust path if needed
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }
}

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
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.io.IOException;
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
            // ensure dialog is shown after selection settles
            Platform.runLater(() -> showEditDialog(newVal));
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
     * Single pop-up "Edit Trainer" dialog with editable fields and Save button.
     */
    private void showEditDialog(GymPersonnel selected) {
        if (selected == null) return;

        Dialog<GymPersonnel> dialog = new Dialog<>();
        dialog.setTitle("Edit Trainer");
        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        // Fields prefilled with selected trainer values
        TextField firstField = new TextField(selected.firstName());
        TextField lastField = new TextField(selected.lastName());

        ComboBox<String> scheduleBox = new ComboBox<>();
        scheduleBox.getItems().addAll("AM", "PM");
        scheduleBox.setValue(selected.schedule() == null || selected.schedule().isEmpty() ? "AM" : selected.schedule());

        ComboBox<String> specialityBox = new ComboBox<>();
        specialityBox.getItems().addAll("Yoga", "Strength Training", "HIIT", "Zumba");
        specialityBox.setValue(selected.speciality() == null || selected.speciality().isEmpty() ? "Yoga" : selected.speciality());

        TextField recordField = new TextField(selected.instructorRecord() == null ? "" : selected.instructorRecord());

        // Layout
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.addRow(0, new Label("First name:"), firstField);
        grid.addRow(1, new Label("Last name:"), lastField);
        grid.addRow(2, new Label("Schedule:"), scheduleBox);
        grid.addRow(3, new Label("Speciality:"), specialityBox);
        grid.addRow(4, new Label("Instructor record:"), recordField);

        dialog.getDialogPane().setContent(grid);

        // Enable/disable Save when required fields are empty
        Node saveButton = dialog.getDialogPane().lookupButton(saveButtonType);
        saveButton.setDisable(firstField.getText().trim().isEmpty() || lastField.getText().trim().isEmpty());
        firstField.textProperty().addListener((obs, o, n) ->
                saveButton.setDisable(n.trim().isEmpty() || lastField.getText().trim().isEmpty()));
        lastField.textProperty().addListener((obs, o, n) ->
                saveButton.setDisable(n.trim().isEmpty() || firstField.getText().trim().isEmpty()));

        // Set dialog owner if possible (avoids some focus issues)
        if (cmbTrainers.getScene() != null && cmbTrainers.getScene().getWindow() != null) {
            dialog.initOwner(cmbTrainers.getScene().getWindow());
        }

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                // Build updated GymPersonnel record (copy old, replace chosen fields)
                return new GymPersonnel(
                        selected.personnelID(),
                        firstField.getText().trim(),
                        lastField.getText().trim(),
                        selected.personnelType(),
                        scheduleBox.getValue(),
                        recordField.getText().trim(),
                        specialityBox.getValue()
                );
            }
            return null;
        });

        Optional<GymPersonnel> result = dialog.showAndWait();

        if (result.isPresent()) {
            GymPersonnel updated = result.get();
            boolean ok = crud.modRecord(updated);
            if (ok) {
                Alert info = new Alert(Alert.AlertType.INFORMATION, "Trainer updated successfully.");
                info.setHeaderText(null);
                info.showAndWait();
                // refresh list and keep no selection (so user can pick another)
                loadTrainers();
            } else {
                Alert err = new Alert(Alert.AlertType.ERROR, "Failed to update trainer (DB error).");
                err.setHeaderText(null);
                err.showAndWait();
                resetSelection();
            }
        } else {
            // user cancelled -> clear selection so they can choose again if desired
            resetSelection();
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

//package com.gymdb.controller;
//
//import com.gymdb.model.GymPersonnel;
//import com.gymdb.model.GymPersonnelCRUD;
//import javafx.application.Platform;
//import javafx.collections.FXCollections;
//import javafx.collections.ObservableList;
//import javafx.event.ActionEvent;
//import javafx.fxml.FXML;
//import javafx.fxml.FXMLLoader;
//import javafx.scene.Node;
//import javafx.scene.Parent;
//import javafx.scene.Scene;
//import javafx.scene.control.*;
//import javafx.stage.Stage;
//import javafx.util.StringConverter;
//
//import java.io.IOException;
//import java.util.Arrays;
//import java.util.List;
//import java.util.Optional;
//import java.util.stream.Collectors;
//
//public class UpdateTrainerController {
//
//    @FXML private ComboBox<GymPersonnel> cmbTrainers;
//    @FXML private Button btnBack;
//
//    private final GymPersonnelCRUD crud = new GymPersonnelCRUD();
//    private final ObservableList<GymPersonnel> trainers = FXCollections.observableArrayList();
//
//    // guard to avoid handling selection events during initialization/clearing
//    private boolean ignoreSelection = true;
//
//    @FXML
//    private void initialize() {
//        // Show "First Last (ID: #)" in the combo
//        cmbTrainers.setConverter(new StringConverter<>() {
//            @Override
//            public String toString(GymPersonnel p) {
//                if (p == null) return "";
//                String f = p.firstName() == null ? "" : p.firstName();
//                String l = p.lastName() == null ? "" : p.lastName();
//                return (f + " " + l).trim() + " (ID: " + p.personnelID() + ")";
//            }
//
//            @Override
//            public GymPersonnel fromString(String string) { return null; }
//        });
//
//        loadTrainers();
//
//        // allow user selection events to trigger after initial load
//        ignoreSelection = false;
//
//        // listen for selection changes
//        cmbTrainers.valueProperty().addListener((obs, oldVal, newVal) -> {
//            if (ignoreSelection || newVal == null) return;
//            Platform.runLater(() -> startUpdateFlow(newVal));
//        });
//    }
//
//    private void loadTrainers() {
//        try {
//            List<GymPersonnel> all = crud.getAllRecords();
//            List<GymPersonnel> onlyTrainers = all.stream()
//                    .filter(p -> p.personnelType() != null && p.personnelType().equalsIgnoreCase("trainer"))
//                    .collect(Collectors.toList());
//            trainers.setAll(onlyTrainers);
//            cmbTrainers.setItems(trainers);
//
//            // clear selection safely
//            ignoreSelection = true;
//            cmbTrainers.getSelectionModel().clearSelection();
//        } catch (Exception e) {
//            e.printStackTrace();
//            trainers.clear();
//            cmbTrainers.setItems(trainers);
//        } finally {
//            ignoreSelection = false;
//        }
//    }
//
//    /**
//     * The full update flow: choose field -> input new value -> confirm -> update DB -> refresh UI.
//     */
//    private void startUpdateFlow(GymPersonnel selected) {
//        if (selected == null) return;
//
//        List<String> choices = Arrays.asList(
//                "First name",
//                "Last name",
//                "Schedule",
//                "Speciality",
//                "Instructor record"
//        );
//
//        ChoiceDialog<String> choiceDialog = new ChoiceDialog<>(choices.get(0), choices);
//        choiceDialog.setTitle("Choose field");
//        choiceDialog.setHeaderText(null);
//        choiceDialog.setContentText("What do you want to edit?");
//        Optional<String> choiceRes = choiceDialog.showAndWait();
//        if (choiceRes.isEmpty()) {
//            resetSelection();
//            return;
//        }
//
//        String field = choiceRes.get();
//
//        TextInputDialog inputDialog = new TextInputDialog();
//        inputDialog.setTitle("Enter new value");
//        inputDialog.setHeaderText(null);
//        inputDialog.setContentText("New " + field + ":");
//        Optional<String> inputRes = inputDialog.showAndWait();
//        if (inputRes.isEmpty()) {
//            resetSelection();
//            return;
//        }
//
//        String newValue = inputRes.get().trim();
//        if (newValue.isEmpty()) {
//            Alert warn = new Alert(Alert.AlertType.WARNING, "Value cannot be empty.");
//            warn.setHeaderText(null);
//            warn.showAndWait();
//            resetSelection();
//            return;
//        }
//
//        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
//        confirm.setTitle("Confirm change");
//        confirm.setHeaderText(null);
//        confirm.setContentText("Change " + field + " to:\n\n" + newValue + "\n\nConfirm?");
//        Optional<ButtonType> conf = confirm.showAndWait();
//        if (conf.isEmpty() || conf.get() != ButtonType.OK) {
//            resetSelection();
//            return;
//        }
//
//        // Build updated GymPersonnel record (copy old, replace chosen field)
//        GymPersonnel updated;
//        try {
//            updated = buildUpdatedPersonnel(selected, field, newValue);
//        } catch (IllegalArgumentException ex) {
//            Alert err = new Alert(Alert.AlertType.ERROR, ex.getMessage());
//            err.setHeaderText(null);
//            err.showAndWait();
//            resetSelection();
//            return;
//        }
//
//        boolean ok = crud.modRecord(updated);
//        if (ok) {
//            Alert info = new Alert(Alert.AlertType.INFORMATION, "Trainer updated successfully.");
//            info.setHeaderText(null);
//            info.showAndWait();
//            loadTrainers(); // refresh list from DB
//        } else {
//            Alert err = new Alert(Alert.AlertType.ERROR, "Failed to update trainer (DB error).");
//            err.setHeaderText(null);
//            err.showAndWait();
//        }
//
//        resetSelection();
//    }
//
//    private GymPersonnel buildUpdatedPersonnel(GymPersonnel old, String field, String newVal) {
//        try {
//            return switch (field) {
//                case "First name" -> new GymPersonnel(
//                        old.personnelID(),
//                        newVal,
//                        old.lastName(),
//                        old.personnelType(),
//                        old.schedule(),
//                        old.instructorRecord(),
//                        old.speciality()
//                );
//                case "Last name" -> new GymPersonnel(
//                        old.personnelID(),
//                        old.firstName(),
//                        newVal,
//                        old.personnelType(),
//                        old.schedule(),
//                        old.instructorRecord(),
//                        old.speciality()
//                );
//                case "Schedule" -> new GymPersonnel(
//                        old.personnelID(),
//                        old.firstName(),
//                        old.lastName(),
//                        old.personnelType(),
//                        newVal,
//                        old.instructorRecord(),
//                        old.speciality()
//                );
//                case "Speciality" -> new GymPersonnel(
//                        old.personnelID(),
//                        old.firstName(),
//                        old.lastName(),
//                        old.personnelType(),
//                        old.schedule(),
//                        old.instructorRecord(),
//                        newVal
//                );
//                case "Instructor record" -> new GymPersonnel(
//                        old.personnelID(),
//                        old.firstName(),
//                        old.lastName(),
//                        old.personnelType(),
//                        old.schedule(),
//                        newVal,
//                        old.speciality()
//                );
//                default -> throw new IllegalArgumentException("Unknown field: " + field);
//            };
//        } catch (NullPointerException npe) {
//            throw new IllegalArgumentException("Member data incomplete — cannot update.");
//        }
//    }
//
//    private void resetSelection() {
//        ignoreSelection = true;
//        if (cmbTrainers != null) cmbTrainers.getSelectionModel().clearSelection();
//        ignoreSelection = false;
//    }
//
//    @FXML
//    private void handleBack(ActionEvent event) throws IOException {
//        Parent root = FXMLLoader.load(getClass().getResource("/fxmls/trainer_menu.fxml")); // adjust path if needed
//        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
//        stage.setScene(new Scene(root));
//        stage.show();
//    }
//}

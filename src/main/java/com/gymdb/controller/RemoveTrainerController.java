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
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Controller for remove_trainer.fxml
 * Selecting a trainer in the ComboBox immediately asks for confirmation and deletes on OK.
 */
public class RemoveTrainerController {

    @FXML
    private ComboBox<GymPersonnel> cmbTrainers;

    private final GymPersonnelCRUD crud = new GymPersonnelCRUD();
    private final ObservableList<GymPersonnel> trainers = FXCollections.observableArrayList();

    // guard to ignore selection events while initializing or programmatically clearing selection
    private boolean ignoreSelection = true;

    @FXML
    private void initialize() {
        // format each trainer shown in the ComboBox
        cmbTrainers.setConverter(new javafx.util.StringConverter<>() {
            @Override
            public String toString(GymPersonnel p) {
                if (p == null) return "";
                String f = p.firstName() == null ? "" : p.firstName();
                String l = p.lastName() == null ? "" : p.lastName();
                return (f + " " + l).trim() + " (ID: " + p.personnelID() + ")";
            }

            @Override
            public GymPersonnel fromString(String string) {
                return null; // not used
            }
        });

        loadTrainers();

        // after loading, allow selection events
        ignoreSelection = false;

        // listen for user selection: confirm and possibly delete
        cmbTrainers.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (ignoreSelection || newVal == null) return;
            Platform.runLater(() -> confirmAndDelete(newVal));
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

            // clear any selection safely
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

    private void confirmAndDelete(GymPersonnel selected) {
        if (selected == null) return;

        String name = ((selected.firstName() == null ? "" : selected.firstName())
                + " " + (selected.lastName() == null ? "" : selected.lastName())).trim();
        if (name.isEmpty()) name = "ID: " + selected.personnelID();

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm delete");
        confirm.setHeaderText(null);
        confirm.setContentText("Are you sure you want to delete trainer " + name + "?");

        Optional<ButtonType> res = confirm.showAndWait();
        if (res.isPresent() && res.get() == ButtonType.OK) {
            boolean ok = crud.delRecord(selected.personnelID());
            if (ok) {
                Alert info = new Alert(Alert.AlertType.INFORMATION);
                info.setHeaderText(null);
                info.setTitle("Deleted");
                info.setContentText("Trainer deleted successfully.");
                info.showAndWait();

                // remove from list and clear selection
                ignoreSelection = true;
                trainers.remove(selected);
                cmbTrainers.getSelectionModel().clearSelection();
                ignoreSelection = false;
            } else {
                Alert err = new Alert(Alert.AlertType.ERROR);
                err.setHeaderText(null);
                err.setTitle("Delete failed");
                err.setContentText("Failed to delete trainer. There may be related records or a DB error.");
                err.showAndWait();

                // clear selection so user can choose again
                ignoreSelection = true;
                cmbTrainers.getSelectionModel().clearSelection();
                ignoreSelection = false;
            }
        } else {
            // user cancelled -> clear selection so they may pick again or press Back
            ignoreSelection = true;
            cmbTrainers.getSelectionModel().clearSelection();
            ignoreSelection = false;
        }
    }

    @FXML
    private void handleBack(ActionEvent event) {
        try {
            // adjust path if your trainer menu FXML has a different filename/location
            Parent root = FXMLLoader.load(getClass().getResource("/fxmls/trainer_menu.fxml"));
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

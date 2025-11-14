package com.gymdb.controller;

import com.gymdb.model.GymPersonnel;
import com.gymdb.model.GymPersonnelCRUD;
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
import java.util.stream.Collectors;

public class ListTrainersController {

    @FXML private TableView<GymPersonnel> trainersTable;

    @FXML private TableColumn<GymPersonnel, Integer> colId;
    @FXML private TableColumn<GymPersonnel, String>  colFirstName;
    @FXML private TableColumn<GymPersonnel, String>  colLastName;
    @FXML private TableColumn<GymPersonnel, String>  colType;
    @FXML private TableColumn<GymPersonnel, String>  colSchedule;
    @FXML private TableColumn<GymPersonnel, String>  colRecord;
    @FXML private TableColumn<GymPersonnel, String>  colSpeciality;

    @FXML private GymPersonnelCRUD crud = new GymPersonnelCRUD();
    private final ObservableList<GymPersonnel> trainers = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        // Bind columns to GymPersonnel record fields
        colId.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().personnelID()));
        colFirstName.setCellValueFactory(c -> new ReadOnlyStringWrapper(
                c.getValue().firstName() == null ? "" : c.getValue().firstName()));
        colLastName.setCellValueFactory(c -> new ReadOnlyStringWrapper(
                c.getValue().lastName() == null ? "" : c.getValue().lastName()));
        colType.setCellValueFactory(c -> new ReadOnlyStringWrapper(
                c.getValue().personnelType() == null ? "" : c.getValue().personnelType()));
        colSchedule.setCellValueFactory(c -> new ReadOnlyStringWrapper(
                c.getValue().schedule() == null ? "" : c.getValue().schedule()));
        colRecord.setCellValueFactory(c -> new ReadOnlyStringWrapper(
                c.getValue().instructorRecord() == null ? "" : c.getValue().instructorRecord()));
        colSpeciality.setCellValueFactory(c -> new ReadOnlyStringWrapper(
                c.getValue().speciality() == null ? "" : c.getValue().speciality()));

        // Load trainers from DB
        loadTrainers();
        trainersTable.setItems(trainers);
    }

    private void loadTrainers() {
        try {
            List<GymPersonnel> all = crud.getAllRecords();
            // filter to only personnelType "Trainer" (case-insensitive)
            List<GymPersonnel> onlyTrainers = all.stream()
                    .filter(p -> p.personnelType() != null && p.personnelType().equalsIgnoreCase("trainer"))
                    .collect(Collectors.toList());
            trainers.setAll(onlyTrainers);
        } catch (Exception e) {
            e.printStackTrace();
            trainers.clear();
        }
    }

    @FXML
    private void handleRemove(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxmls/RemoveTrainer.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleUpdate(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxmls/UpdateTrainer.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @FXML
    private void handleEntry(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxmls/TrainerEntry.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleBack(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxmls/trainer_menu.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}

package com.gymdb.controller;

import com.gymdb.model.Equipment;
import com.gymdb.model.EquipmentCRUD;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

public class RemoveEquipmentController {

    @FXML private TableView<Equipment> equipmentTable;
    @FXML private TableColumn<Equipment, String> colName;
    @FXML private TableColumn<Equipment, String> colDescription;
    @FXML private TableColumn<Equipment, Integer> colQuantity;
    @FXML private TableColumn<Equipment, Double> colUnitPrice;
    @FXML private TableColumn<Equipment, String> colVendor;
    @FXML private TableColumn<Equipment, String> colContact;
    @FXML private TableColumn<Equipment, LocalDateTime> colPurchaseDate;
    @FXML private TableColumn<Equipment, Void> colAction; // For Remove button
    @FXML private Button backBtn;

    private ObservableList<Equipment> equipmentList = FXCollections.observableArrayList();
    private EquipmentCRUD crud = new EquipmentCRUD();

    @FXML
    private void initialize() {
        // Bind columns
        colName.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().equipmentName()));
        colDescription.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().equipmentDescription()));
        colQuantity.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().quantity()).asObject());
        colUnitPrice.setCellValueFactory(data -> new SimpleDoubleProperty(data.getValue().unitPrice()).asObject());
        colVendor.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().vendor()));
        colContact.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().contactNo()));
        colPurchaseDate.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().purchaseDate()));

        addRemoveButtonToTable();
        loadEquipment();
    }

    private void loadEquipment() {
        equipmentList.clear();
        List<Equipment> allRecords = crud.getAllRecords();

        if (allRecords.isEmpty()) {
            // Optionally add defaults if empty
            LocalDateTime now = LocalDateTime.now();
            Equipment[] defaults = {
                    new Equipment(0, "Treadmill", "High-speed motorized treadmill", 5, 45000, "FitnessPro Supplies", "09171234567", now),
                    new Equipment(0, "Dumbbell Set", "Adjustable steel dumbbells 5kg–25kg", 10, 12000, "IronWorks Co.", "09982345678", now),
                    new Equipment(0, "Exercise Bike", "Indoor stationary exercise bike", 4, 38000, "CycleFit", "09223456789", now),
                    new Equipment(0, "Yoga Mat", "Non-slip mats for yoga and stretching", 15, 800, "ZenFlex", "09183456712", now)
            };
            for (Equipment e : defaults) {
                crud.addRecord(e);
            }
            allRecords = crud.getAllRecords();
        }

        equipmentList.addAll(allRecords);
        equipmentTable.setItems(equipmentList);
    }

    private void addRemoveButtonToTable() {
        colAction.setCellFactory(param -> new TableCell<>() {
            private final Button removeBtn = new Button("Remove");

            {
                removeBtn.setOnAction(event -> {
                    Equipment e = getTableView().getItems().get(getIndex());
                    boolean success = crud.delRecord(e.equipmentID());
                    if (success) {
                        equipmentList.remove(e);
                    } else {
                        Alert alert = new Alert(Alert.AlertType.ERROR, "Failed to remove equipment.");
                        alert.showAndWait();
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(removeBtn);
                }
            }
        });
    }

    @FXML
    private void handleBack(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/fxmls/Equipment.fxml"));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }
}

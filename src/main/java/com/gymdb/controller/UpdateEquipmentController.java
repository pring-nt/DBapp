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
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.scene.Node;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

public class UpdateEquipmentController {

    @FXML private TableView<Equipment> equipmentTable;
    @FXML private TableColumn<Equipment, String> colName;
    @FXML private TableColumn<Equipment, String> colDescription;
    @FXML private TableColumn<Equipment, Integer> colQuantity;
    @FXML private TableColumn<Equipment, Double> colUnitPrice;
    @FXML private TableColumn<Equipment, String> colVendor;
    @FXML private TableColumn<Equipment, String> colContact;
    @FXML private TableColumn<Equipment, LocalDateTime> colPurchaseDate;
    @FXML private TableColumn<Equipment, Void> colAction;
    @FXML private Button backBtn;

    private ObservableList<Equipment> equipmentList = FXCollections.observableArrayList();
    private EquipmentCRUD crud = new EquipmentCRUD();

    @FXML
    private void initialize() {
        colName.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().equipmentName()));
        colDescription.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().equipmentDescription()));
        colQuantity.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().quantity()).asObject());
        colUnitPrice.setCellValueFactory(data -> new SimpleDoubleProperty(data.getValue().unitPrice()).asObject());
        colVendor.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().vendor()));
        colContact.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().contactNo()));
        colPurchaseDate.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().purchaseDate()));

        loadEquipment();            // populate table
        addUpdateButtonToTable();   // add update buttons
    }


    private void loadEquipment() {
        equipmentList.clear();
        List<Equipment> allRecords = crud.getAllRecords();

        if (allRecords.isEmpty()) {
            addDefaultEquipment();
            allRecords = crud.getAllRecords();
        }

        equipmentList.addAll(allRecords);
        equipmentTable.setItems(equipmentList);
    }

    private void addDefaultEquipment() {
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
    }

    private void addUpdateButtonToTable() {
        colAction.setCellFactory(param -> new TableCell<>() {
            private final Button btn = new Button("Update");

            {
                btn.setOnAction(event -> {
                    Equipment selected = getTableView().getItems().get(getIndex());
                    showEditDialog(selected);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(btn);
                }
            }
        });
    }

    private void showEditDialog(Equipment e) {
        Dialog<Equipment> dialog = new Dialog<>();
        dialog.setTitle("Edit Equipment");
        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        TextField nameField = new TextField(e.equipmentName());
        TextField descField = new TextField(e.equipmentDescription());
        TextField qtyField = new TextField(String.valueOf(e.quantity()));
        TextField priceField = new TextField(String.valueOf(e.unitPrice()));
        TextField vendorField = new TextField(e.vendor());
        TextField contactField = new TextField(e.contactNo());

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.addRow(0, new Label("Name:"), nameField);
        grid.addRow(1, new Label("Description:"), descField);
        grid.addRow(2, new Label("Quantity:"), qtyField);
        grid.addRow(3, new Label("Unit Price:"), priceField);
        grid.addRow(4, new Label("Vendor:"), vendorField);
        grid.addRow(5, new Label("Contact No:"), contactField);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                try {
                    Equipment updated = new Equipment(
                            e.equipmentID(),
                            nameField.getText(),
                            descField.getText(),
                            Integer.parseInt(qtyField.getText()),
                            Double.parseDouble(priceField.getText()),
                            vendorField.getText(),
                            contactField.getText(),
                            e.purchaseDate()
                    );
                    crud.modRecord(updated);
                    loadEquipment();
                    return updated;
                } catch (NumberFormatException ex) {
                    showAlert("Invalid Input", "Please enter valid numeric values for quantity and price.");
                }
            }
            return null;
        });

        dialog.showAndWait();
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    private void handleBack(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/fxmls/Equipment.fxml"));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }
}

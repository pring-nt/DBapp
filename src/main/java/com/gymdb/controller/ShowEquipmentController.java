package com.gymdb.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.event.ActionEvent;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;

import com.gymdb.utils.DBConnection;

public class ShowEquipmentController {

    @FXML private TableView<Equipment> equipmentTable;
    @FXML private TableColumn<Equipment, String> colName;
    @FXML private TableColumn<Equipment, String> colDescription;
    @FXML private TableColumn<Equipment, Integer> colQuantity;
    @FXML private TableColumn<Equipment, Double> colAmount;
    @FXML private TableColumn<Equipment, String> colVendor;
    @FXML private TableColumn<Equipment, String> colContact;
    @FXML private TableColumn<Equipment, LocalDate> colPurchaseDate;
    @FXML private Button backBtn;

    private ObservableList<Equipment> equipmentList = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        // Bind table columns to Equipment properties
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        colQuantity.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        colAmount.setCellValueFactory(new PropertyValueFactory<>("amount"));
        colVendor.setCellValueFactory(new PropertyValueFactory<>("vendor"));
        colContact.setCellValueFactory(new PropertyValueFactory<>("contact"));
        colPurchaseDate.setCellValueFactory(new PropertyValueFactory<>("purchaseDate"));

        loadEquipmentData();
    }

    private void loadEquipmentData() {
        equipmentList.clear();
        String query = "SELECT * FROM equipment";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            boolean isEmpty = true;
            while (rs.next()) {
                isEmpty = false;
                equipmentList.add(new Equipment(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("description"),
                        rs.getInt("quantity"),
                        rs.getDouble("amount"),
                        rs.getString("vendor"),
                        rs.getString("contact"),
                        rs.getDate("purchase_date").toLocalDate()
                ));
            }

            // If database is empty, add hardcoded defaults
            if (isEmpty) addDefaultEquipments(conn);

            equipmentTable.setItems(equipmentList);

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", e.getMessage());
        }
    }

    private void addDefaultEquipments(Connection conn) throws SQLException {
        String insertQuery = "INSERT INTO equipment (name, description, quantity, amount, vendor, contact, purchase_date) VALUES (?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement ps = conn.prepareStatement(insertQuery);

        Object[][] defaults = {
                {"Treadmill", "High-speed motorized treadmill", 5, 45000.0, "FitnessPro Supplies", "09171234567", LocalDate.of(2024,3,10)},
                {"Dumbbell Set", "Adjustable steel dumbbells 5kg–25kg", 10, 12000.0, "IronWorks Co.", "09982345678", LocalDate.of(2024,4,5)},
                {"Exercise Bike", "Indoor stationary exercise bike", 4, 38000.0, "CycleFit", "09223456789", LocalDate.of(2024,5,15)},
                {"Yoga Mat", "Non-slip mats for yoga and stretching", 15, 800.0, "ZenFlex", "09183456712", LocalDate.of(2024,2,22)},
                {"Barbell Rack", "Heavy-duty steel rack for barbells", 2, 25000.0, "PowerRack Inc.", "09561234567", LocalDate.of(2024,6,30)},
                {"Bench Press", "Adjustable incline bench press", 3, 27000.0, "StrongMan Equipment", "09781234567", LocalDate.of(2024,7,18)},
                {"Kettlebell Set", "Cast iron kettlebells from 5kg–30kg", 6, 15000.0, "FlexZone", "09391234567", LocalDate.of(2024,3,25)}
        };

        for (Object[] eq : defaults) {
            ps.setString(1, (String) eq[0]);
            ps.setString(2, (String) eq[1]);
            ps.setInt(3, (int) eq[2]);
            ps.setDouble(4, (double) eq[3]);
            ps.setString(5, (String) eq[4]);
            ps.setString(6, (String) eq[5]);
            ps.setDate(7, Date.valueOf((LocalDate) eq[6]));
            ps.executeUpdate();

            // Also add to the ObservableList to display immediately
            equipmentList.add(new Equipment(
                    0, // id will be auto-incremented
                    (String) eq[0],
                    (String) eq[1],
                    (int) eq[2],
                    (double) eq[3],
                    (String) eq[4],
                    (String) eq[5],
                    (LocalDate) eq[6]
            ));
        }
    }

    @FXML
    private void handleBack(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/fxmls/Equipment.fxml"));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // Equipment model
    public static class Equipment {
        private int id;
        private String name;
        private String description;
        private int quantity;
        private double amount;
        private String vendor;
        private String contact;
        private LocalDate purchaseDate;

        public Equipment(int id, String name, String description, int quantity, double amount, String vendor, String contact, LocalDate purchaseDate) {
            this.id = id;
            this.name = name;
            this.description = description;
            this.quantity = quantity;
            this.amount = amount;
            this.vendor = vendor;
            this.contact = contact;
            this.purchaseDate = purchaseDate;
        }

        public int getId() { return id; }
        public String getName() { return name; }
        public String getDescription() { return description; }
        public int getQuantity() { return quantity; }
        public double getAmount() { return amount; }
        public String getVendor() { return vendor; }
        public String getContact() { return contact; }
        public LocalDate getPurchaseDate() { return purchaseDate; }
    }
}

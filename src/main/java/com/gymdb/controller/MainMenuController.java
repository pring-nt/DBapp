package com.gymdb.controller;

import com.gymdb.model.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.IOException;

public class MainMenuController {

    @FXML private Button membersTab;
    @FXML private Button staffTab;
    @FXML private Button equipmentsTab;
    @FXML private Button classTab;
    @FXML private Button progressTab;
    @FXML private Button paymentsTab;
    @FXML private Button attendanceTab;
    @FXML private Button lockerTab;
    @FXML private Button productsTab;
    @FXML private Button reportsTab;
    @FXML private Button backBtn;

    // TextAreas for stats
    @FXML private TextArea totalMembersField;
    @FXML private TextArea staffUsersField;
    @FXML private TextArea availableEquipmentsField;
    @FXML private TextArea totalEarningsField;
    @FXML private TextArea activeTrainersField;
    @FXML private TextArea activeMembersField;

    private MemberCRUD memberCRUD = new MemberCRUD();
    private GymPersonnelCRUD gymPersonnelCRUD = new GymPersonnelCRUD();
    private EquipmentCRUD equipmentCRUD = new EquipmentCRUD();
    private PaymentCRUD paymentCRUD = new PaymentCRUD();

    @FXML
    public void initialize() {
        // Make stats non-editable
        totalMembersField.setEditable(false);
        staffUsersField.setEditable(false);
        availableEquipmentsField.setEditable(false);
        totalEarningsField.setEditable(false);
        activeTrainersField.setEditable(false);
        activeMembersField.setEditable(false);

        // Delay refresh until after scene is loaded
        javafx.application.Platform.runLater(this::refreshStats);
    }



    public void refreshStats() {
        try {
            int totalMembers = memberCRUD.getAllRecords().size();
            int activeMembers = memberCRUD.getActiveMembers().size();  // implement this method
            int staffUsers = gymPersonnelCRUD.getAllRecords().size();
            int activeTrainers = gymPersonnelCRUD.getActiveTrainers().size(); // implement this method
            int availableEquipments = equipmentCRUD.getAvailableEquipments().size(); // implement this
            double totalEarnings = paymentCRUD.getTotalEarnings(); // sum all payment amounts

            totalMembersField.setText(String.valueOf(totalMembers));
            activeMembersField.setText(String.valueOf(activeMembers));
            staffUsersField.setText(String.valueOf(staffUsers));
            activeTrainersField.setText(String.valueOf(activeTrainers));
            availableEquipmentsField.setText(String.valueOf(availableEquipments));
            totalEarningsField.setText("₱" + totalEarnings);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    // -------------------- Button Navigation Handlers --------------------
    @FXML private void handleMembersTab(ActionEvent event) { navigate(event, "/fxmls/main-view.fxml"); }
    @FXML private void handleStaffTab(ActionEvent event) { navigate(event, "/fxmls/trainer_menu.fxml"); }
    @FXML private void handleEquipmentsTab(ActionEvent event) { navigate(event, "/fxmls/Equipment.fxml"); }
    @FXML private void handleClassTab(ActionEvent event) { navigate(event, "/fxmls/Class.fxml"); }
    @FXML private void handleProgressTab(ActionEvent event) { navigate(event, "/fxmls/CustomersProgress.fxml"); }
    @FXML private void handlePaymentsTab(ActionEvent event) { navigate(event, "/fxmls/PaymentForm.fxml"); }
    @FXML private void handleAttendanceTab(ActionEvent event) { navigate(event, "/fxmls/AttendanceMenu.fxml"); }
    @FXML private void handleLockerTab(ActionEvent event) { navigate(event, "/fxmls/Locker.fxml"); }
    @FXML private void handleProductsTab(ActionEvent event) { navigate(event, "/fxmls/ProductInventory.fxml"); }
    @FXML private void handleReportsTab(ActionEvent event) { navigate(event, "/fxmls/members_report.fxml"); }
    @FXML private void handleBack(ActionEvent event) { navigate(event, "/fxmls/AdminLogin.fxml"); }

    private void navigate(ActionEvent event, String fxmlPath) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

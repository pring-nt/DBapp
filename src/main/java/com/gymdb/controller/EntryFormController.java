package com.gymdb.controller;

import com.gymdb.model.Member;
import com.gymdb.model.MemberCRUD;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDate;

public class EntryFormController {

    @FXML private TextField txtFirstName;
    @FXML private TextField txtLastName;
    @FXML private TextField txtEmail;
    @FXML private TextField txtContact;
    @FXML private TextField txtMembership;
    @FXML private TextField txtHealthGoal;
    @FXML private TextField txtInitialWeight;
    @FXML private TextField txtGoalWeight;
    @FXML private DatePicker dpStartDate;
    @FXML private DatePicker dpEndDate;

    @FXML
    private void handleSubmit(ActionEvent event) {
        try {
            // Collect data from the form
            String firstName = txtFirstName.getText();
            String lastName = txtLastName.getText();
            String email = txtEmail.getText();
            String contactNo = txtContact.getText();
            String membershipType = txtMembership.getText();
            String healthGoal = txtHealthGoal.getText();
            LocalDate startDate = dpStartDate.getValue();
            LocalDate endDate = dpEndDate.getValue();

            Double initialWeight = txtInitialWeight.getText().isEmpty()
                    ? null : Double.parseDouble(txtInitialWeight.getText());
            Double goalWeight = txtGoalWeight.getText().isEmpty()
                    ? null : Double.parseDouble(txtGoalWeight.getText());

            // Create a Member object — IDs handled by DB (AUTO_INCREMENT)
            Member member = new Member(
                    0,
                    firstName,
                    lastName,
                    email,
                    contactNo,
                    membershipType,
                    startDate,
                    endDate,
                    healthGoal,
                    initialWeight,
                    goalWeight,
                    null, null, // startBMI, updatedBMI
                    null, null, null // classID, trainerID, lockerID
            );

            // Save to DB using your groupmate's MemberCRUD
            MemberCRUD crud = new MemberCRUD();
            boolean success = crud.addRecord(member);

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            if (success) {
                alert.setTitle("Success");
                alert.setHeaderText(null);
                alert.setContentText("Member successfully registered!");
                alert.showAndWait();
                clearForm();
            } else {
                alert.setTitle("Error");
                alert.setHeaderText(null);
                alert.setContentText("Failed to register member.");
                alert.showAndWait();
            }

        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("Please fill in all required fields correctly.");
            alert.showAndWait();
        }
    }

    private void clearForm() {
        txtFirstName.clear();
        txtLastName.clear();
        txtEmail.clear();
        txtContact.clear();
        txtMembership.clear();
        txtHealthGoal.clear();
        txtInitialWeight.clear();
        txtGoalWeight.clear();
        dpStartDate.setValue(null);
        dpEndDate.setValue(null);
    }

    @FXML
    private void handleBack(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/fxmls/main-view.fxml"));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }
}

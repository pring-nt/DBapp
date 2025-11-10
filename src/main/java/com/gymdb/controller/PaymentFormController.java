package com.gymdb.controller;

import com.gymdb.model.Member;
import com.gymdb.model.MemberCRUD;
import com.gymdb.model.Payment;
import com.gymdb.model.PaymentCRUD;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.event.ActionEvent;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.Node;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class PaymentFormController {

    @FXML private ComboBox<String> fullName;
    @FXML private TextField amountPerMonth;

    @FXML private ComboBox<String> service;         // Single ComboBox
    @FXML private ComboBox<String> plan;
    @FXML private ComboBox<String> memberStatus;

    private MemberCRUD memberCRUD = new MemberCRUD();
    private PaymentCRUD paymentCRUD = new PaymentCRUD();

    // Store mapping fullName → memberID
    private HashMap<String, Integer> memberIds = new HashMap<>();

    @FXML
    public void initialize() {
        loadMembersToDropdown();
        loadServiceOptions();
        loadPlanAndStatusOptions();
    }

    // Load members from DB into dropdown
    private void loadMembersToDropdown() {
        List<Member> members = memberCRUD.getAllRecords();
        for (Member m : members) {
            String full = m.firstName() + " " + m.lastName();
            fullName.getItems().add(full);
            memberIds.put(full, m.memberID());
        }
    }

    // Load services directly into single ComboBox
    private void loadServiceOptions() {
        service.setItems(FXCollections.observableArrayList(
                "Yoga - Morning Yoga Flow", "Yoga - Stretch & Relax", "Yoga - Power Up",
                "Strength Training - Body Pump Burn", "Strength Training - Core & Stability",
                "Strength Training - Upper Body Blast",
                "HIIT - HIIT Express", "HIIT - Total Body Inferno", "HIIT - Cardio Crush",
                "Zumba - Zumba Dance Party", "Zumba - Latin Groove", "Zumba - Pop & Sweat"
        ));
    }

    // Load plan + status
    private void loadPlanAndStatusOptions() {
        plan.setItems(FXCollections.observableArrayList("Monthly", "Yearly"));
        memberStatus.setItems(FXCollections.observableArrayList("Active", "Expired"));
    }

    // Generate RCPT number
    private String generatePaymentNumber() {
        int num = new Random().nextInt(9000) + 1000;
        return "RCPT-" + num;
    }

    @FXML
    private void makePayment(ActionEvent event) {
        if (fullName.getValue() == null ||
                service.getValue() == null ||
                plan.getValue() == null ||
                memberStatus.getValue() == null ||
                amountPerMonth.getText().isEmpty()) {

            System.out.println("Missing fields.");
            return;
        }

        int memberID = memberIds.get(fullName.getValue());

        Payment payment = new Payment(
                0,
                generatePaymentNumber(),
                new Timestamp(System.currentTimeMillis()),
                plan.getValue(),          // transaction_type
                Double.parseDouble(amountPerMonth.getText()),
                service.getValue(),       // payment_method
                memberID
        );

        boolean success = paymentCRUD.addRecord(payment);
        System.out.println(success ? "Payment saved." : "Failed to save payment.");
    }

    @FXML
    private void handleBack(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxmls/MainMenu.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

package com.gymdb.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

public class PaymentFormController {

    @FXML
    private Button makepaymentBtn;

    @FXML
    private TextField fullName;

    @FXML
    private TextField fullName1;

    @FXML
    private TextField fullName11;

    @FXML
    private TextField fullName111;

    @FXML
    private TextField amountPerMonth;

    @FXML
    private void makePayment() {
        // Example: retrieve field values
        String name = fullName.getText();
        String field1 = fullName1.getText();
        String field2 = fullName11.getText();
        String field3 = fullName111.getText();
        String amount = amountPerMonth.getText();

        // For now, just print them
        System.out.println("=== Payment Form Data ===");
        System.out.println("Full Name: " + name);
        System.out.println("Field 1: " + field1);
        System.out.println("Field 2: " + field2);
        System.out.println("Field 3: " + field3);
        System.out.println("Amount per month: " + amount);

        // TODO: Add your actual payment logic here
    }
}


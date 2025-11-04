package com.gymdb.utils;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class ListMembersUtil {
    private final IntegerProperty id = new SimpleIntegerProperty();
    private final StringProperty fullName = new SimpleStringProperty();
    private final StringProperty username = new SimpleStringProperty();
    private final StringProperty gender = new SimpleStringProperty();
    private final StringProperty contactNumber = new SimpleStringProperty();
    private final StringProperty dateOfBirth = new SimpleStringProperty();
    private final StringProperty amount = new SimpleStringProperty();
    private final StringProperty chosenService = new SimpleStringProperty();
    private final StringProperty plan = new SimpleStringProperty();

    public ListMembersUtil(int id, String fullName, String username, String gender,
                           String contactNumber, String dateOfBirth, String amount,
                           String chosenService, String plan) {
        this.id.set(id);
        this.fullName.set(fullName);
        this.username.set(username);
        this.gender.set(gender);
        this.contactNumber.set(contactNumber);
        this.dateOfBirth.set(dateOfBirth);
        this.amount.set(amount);
        this.chosenService.set(chosenService);
        this.plan.set(plan);
    }

    // id
    public int getId() { return id.get(); }
    public IntegerProperty idProperty() { return id; }

    // fullName
    public String getFullName() { return fullName.get(); }
    public StringProperty fullNameProperty() { return fullName; }

    // username
    public String getUsername() { return username.get(); }
    public StringProperty usernameProperty() { return username; }

    // gender
    public String getGender() { return gender.get(); }
    public StringProperty genderProperty() { return gender; }

    // contactNumber
    public String getContactNumber() { return contactNumber.get(); }
    public StringProperty contactNumberProperty() { return contactNumber; }

    // dateOfBirth
    public String getDateOfBirth() { return dateOfBirth.get(); }
    public StringProperty dateOfBirthProperty() { return dateOfBirth; }

    // amount
    public String getAmount() { return amount.get(); }
    public StringProperty amountProperty() { return amount; }

    // chosenService
    public String getChosenService() { return chosenService.get(); }
    public StringProperty chosenServiceProperty() { return chosenService; }

    // plan
    public String getPlan() { return plan.get(); }
    public StringProperty planProperty() { return plan; }
}

package com.gymdb.controller;

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
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class ReportsController {

    @FXML
    private TableView<User> reportsTable;

    @FXML
    private TableColumn<User, String> fullNameCol;

    @FXML
    private TableColumn<User, String> contactCol;

    @FXML
    private TableColumn<User, String> locationCol;

    @FXML
    private TableColumn<User, String> planCol;

    @FXML
    private TableColumn<User, String> serviceTypeCol;

    @FXML
    private TableColumn<User, String> serviceNameCol;

    private ObservableList<User> usersList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Set up columns
        fullNameCol.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        contactCol.setCellValueFactory(new PropertyValueFactory<>("contact"));
        locationCol.setCellValueFactory(new PropertyValueFactory<>("location"));
        planCol.setCellValueFactory(new PropertyValueFactory<>("plan"));
        serviceTypeCol.setCellValueFactory(new PropertyValueFactory<>("serviceType"));
        serviceNameCol.setCellValueFactory(new PropertyValueFactory<>("serviceName"));

        loadUsers();
    }

    private void loadUsers() {
        usersList.clear();
        try (BufferedReader br = new BufferedReader(new FileReader("users.txt"))) {
            String line;
            while ((line = br.readLine()) != null) {
                // Split CSV: username,password,fullName,contact,location,plan,serviceType,serviceName
                String[] data = line.split(",");
                if (data.length >= 8) {
                    String fullName = data[2];
                    String contact = data[3];
                    String location = data[4];
                    String plan = data[5];
                    String serviceType = data[6];
                    String serviceName = data[7];

                    usersList.add(new User(fullName, contact, location, plan, serviceType, serviceName));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        reportsTable.setItems(usersList);
    }

    // User class for TableView
    public static class User {
        private final String fullName;
        private final String contact;
        private final String location;
        private final String plan;
        private final String serviceType;
        private final String serviceName;

        public User(String fullName, String contact, String location, String plan, String serviceType, String serviceName) {
            this.fullName = fullName;
            this.contact = contact;
            this.location = location;
            this.plan = plan;
            this.serviceType = serviceType;
            this.serviceName = serviceName;
        }

        public String getFullName() { return fullName; }
        public String getContact() { return contact; }
        public String getLocation() { return location; }
        public String getPlan() { return plan; }
        public String getServiceType() { return serviceType; }
        public String getServiceName() { return serviceName; }
    }

    @FXML
    private void handleBack(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxmls/CustomersDashboard.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

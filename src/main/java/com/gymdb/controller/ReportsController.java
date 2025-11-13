package com.gymdb.controller;

import com.gymdb.model.Member;
import com.gymdb.model.MemberCRUD;
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

public class ReportsController {

    @FXML
    private TableView<Member> reportsTable;

    @FXML
    private TableColumn<Member, String> firstNameCol, lastNameCol, healthGoalCol;
    @FXML
    private TableColumn<Member, Double> initialWeightCol, goalWeightCol;

    private final MemberCRUD memberCRUD = new MemberCRUD();
    private ObservableList<Member> membersList = FXCollections.observableArrayList();
    private String currentUsername;

    @FXML
    private void initialize() {
        firstNameCol.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().firstName()));
        lastNameCol.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().lastName()));
        healthGoalCol.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().healthGoal()));
        initialWeightCol.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().initialWeight()));
        goalWeightCol.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().goalWeight()));
    }

    private void loadMembers() {
        if (currentUsername == null || currentUsername.isBlank()) {
            System.out.println("No username set, cannot load members.");
            membersList.clear();
            reportsTable.setItems(membersList);
            return;
        }

        List<Member> members = memberCRUD.getAllRecords();

        String fullName = getFullNameFromUsername(currentUsername);
        System.out.println("Current username: [" + currentUsername + "]");
        System.out.println("Full name from file: [" + fullName + "]");

        if (fullName != null && !fullName.isBlank()) {
            membersList = FXCollections.observableArrayList(
                    members.stream()
                            .filter(m -> {
                                String memberFullName = (m.firstName() + " " + m.lastName()).replaceAll("\\s+", " ").trim();
                                return memberFullName.equalsIgnoreCase(fullName.replaceAll("\\s+", " ").trim());
                            })
                            .toList()
            );
        } else {
            membersList = FXCollections.observableArrayList();
            System.out.println("No matching full name found in users.txt");
        }

        System.out.println("Members loaded: " + membersList.size());
        reportsTable.setItems(membersList);
    }

    private String getFullNameFromUsername(String username) {
        if (username == null || username.isBlank()) return null;

        File file = new File("users.txt"); // make sure this path is correct
        if (!file.exists()) {
            System.out.println("users.txt not found at " + file.getAbsolutePath());
            return null;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 3) {
                    String fileUsername = parts[0].trim();
                    String fileFullName = parts[2].trim();
                    if (fileUsername.equalsIgnoreCase(username.trim())) {
                        return fileFullName;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public void setCurrentUsername(String username) {
        this.currentUsername = username;
        System.out.println("setCurrentUsername called: " + username);
        loadMembers();
    }

    @FXML
    private void handleBack(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxmls/CustomersDashboard.fxml"));
            Parent root = loader.load();

            CustomersDashboardController controller = loader.getController();
            controller.setCurrentUsername(currentUsername);

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

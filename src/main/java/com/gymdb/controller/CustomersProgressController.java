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
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.IOException;
import java.util.List;

public class CustomersProgressController {

    @FXML
    private TableView<Member> membersTable;

    @FXML
    private TableColumn<Member, String> colFirstName, colLastName, colHealthGoal;
    @FXML
    private TableColumn<Member, Double> colInitialWeight, colGoalWeight;
    @FXML
    private TableColumn<Member, Void> colUpdate;

    private final MemberCRUD memberCRUD = new MemberCRUD();

    @FXML
    private void initialize() {
        // Set columns
        colFirstName.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().firstName()));
        colLastName.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().lastName()));
        colHealthGoal.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().healthGoal()));
        colInitialWeight.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().initialWeight()));
        colGoalWeight.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().goalWeight()));

        addUpdateButtonToTable();
        loadMembers();
    }

    private void loadMembers() {
        List<Member> members = memberCRUD.getAllRecords();
        ObservableList<Member> list = FXCollections.observableArrayList(members);
        membersTable.setItems(list);
    }

    private void addUpdateButtonToTable() {
        colUpdate.setCellFactory(new Callback<>() {
            @Override
            public TableCell<Member, Void> call(final TableColumn<Member, Void> param) {
                return new TableCell<>() {
                    private final Button btn = new Button("Update");

                    {
                        btn.setOnAction((ActionEvent event) -> {
                            Member member = getTableView().getItems().get(getIndex());

                            // Single pop-up with only the required fields
                            Dialog<Member> dialog = new Dialog<>();
                            dialog.setTitle("Update Member");
                            dialog.setHeaderText("Update Fitness Details for " + member.firstName() + " " + member.lastName());

                            // Buttons
                            ButtonType updateButtonType = new ButtonType("Update", ButtonBar.ButtonData.OK_DONE);
                            dialog.getDialogPane().getButtonTypes().addAll(updateButtonType, ButtonType.CANCEL);

                            // Form fields
                            TextField healthGoalField = new TextField(member.healthGoal());
                            TextField initWeightField = new TextField(member.initialWeight() == null ? "" : member.initialWeight().toString());
                            TextField goalWeightField = new TextField(member.goalWeight() == null ? "" : member.goalWeight().toString());

                            GridPane grid = new GridPane();
                            grid.setHgap(10);
                            grid.setVgap(10);

                            grid.addRow(0, new Label("Health Goal:"), healthGoalField);
                            grid.addRow(1, new Label("Initial Weight:"), initWeightField);
                            grid.addRow(2, new Label("Goal Weight:"), goalWeightField);

                            dialog.getDialogPane().setContent(grid);

                            // Convert result
                            dialog.setResultConverter(dialogButton -> {
                                if (dialogButton == updateButtonType) {
                                    return new Member(
                                            member.memberID(),
                                            member.firstName(),
                                            member.lastName(),
                                            member.email(),
                                            member.contactNo(),
                                            member.membershipType(),
                                            member.startDate(),
                                            member.endDate(),
                                            healthGoalField.getText(),
                                            parseDoubleOrNull(initWeightField.getText()),
                                            parseDoubleOrNull(goalWeightField.getText()),
                                            null, // startBMI
                                            null, // updatedBMI
                                            member.classID(),
                                            member.trainerID(),
                                            member.lockerID()
                                    );
                                }
                                return null;
                            });

                            dialog.showAndWait().ifPresent(updated -> {
                                boolean success = memberCRUD.modRecord(updated);
                                if (success) {
                                    getTableView().getItems().set(getIndex(), updated);
                                } else {
                                    Alert alert = new Alert(Alert.AlertType.ERROR, "Update failed!");
                                    alert.showAndWait();
                                }
                            });
                        });
                    }

                    @Override
                    public void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) setGraphic(null);
                        else setGraphic(btn);
                    }
                };
            }
        });
    }

    @FXML
    private void handleBack(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/fxmls/MainMenu.fxml"));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }

    private Double parseDoubleOrNull(String text) {
        try {
            return text == null || text.isEmpty() ? null : Double.parseDouble(text);
        } catch (NumberFormatException e) {
            return null;
        }


    }
}

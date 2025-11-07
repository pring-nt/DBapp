package com.gymdb.controller;

import com.gymdb.model.Member;
import com.gymdb.model.MemberCRUD;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class UpdateMemberController {

    @FXML private ComboBox<Member> cmbMembers;
    @FXML private Button btnBack;

    private final MemberCRUD crud = new MemberCRUD();
    private final ObservableList<Member> members = FXCollections.observableArrayList();

    // ignoreSelection prevents reacting to programmatic selection changes (like during load)
    private boolean ignoreSelection = true;

    @FXML
    private void initialize() {
        // configure how Member appears in ComboBox
        cmbMembers.setConverter(new StringConverter<>() {
            @Override
            public String toString(Member m) {
                if (m == null) return "";
                String first = m.firstName() == null ? "" : m.firstName();
                String last  = m.lastName()  == null ? "" : m.lastName();
                return (first + " " + last).trim() + " (ID: " + m.memberID() + ")";
            }
            @Override
            public Member fromString(String s) { return null; }
        });

        loadMembers();

        // allow user selection to trigger dialogs
        ignoreSelection = false;

        // listen for user selection
        cmbMembers.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (ignoreSelection || newVal == null) return;
            // ensure the UI has finished selecting before showing dialogs
            Platform.runLater(() -> startEditFlow(newVal));
        });
    }

    // loads members from DB into ComboBox
    private void loadMembers() {
        try {
            List<Member> list = crud.getAllRecords();
            members.setAll(list);
            cmbMembers.setItems(members);
            // clear selection safely
            ignoreSelection = true;
            cmbMembers.getSelectionModel().clearSelection();
        } catch (Exception e) {
            e.printStackTrace();
            members.clear();
            cmbMembers.setItems(members);
        } finally {
            ignoreSelection = false;
        }
    }

    // start the sequence of popups to choose field, input new value, confirm, then update
    private void startEditFlow(Member selected) {
        if (selected == null) return;

        // 1) ask what to edit (choice dialog)
        List<String> choices = Arrays.asList(
                "First name",
                "Last name",
                "Email",
                "Contact number",
                "Membership type",
                "Health goal",
                "Initial weight",
                "Goal weight"
        );

        ChoiceDialog<String> choice = new ChoiceDialog<>(choices.get(0), choices);
        choice.setTitle("Choose field");
        choice.setHeaderText(null);
        choice.setContentText("What do you want to edit?");
        Optional<String> choiceRes = choice.showAndWait();

        if (choiceRes.isEmpty()) {
            // user cancelled choosing field -> clear selection
            resetSelection();
            return;
        }

        String field = choiceRes.get();

        // 2) ask for new value with a TextInputDialog
        TextInputDialog input = new TextInputDialog();
        input.setTitle("Enter new value");
        input.setHeaderText(null);
        input.setContentText("New " + field + ":");

        Optional<String> inputRes = input.showAndWait();
        if (inputRes.isEmpty()) {
            // user cancelled entering new value
            resetSelection();
            return;
        }

        String newValueRaw = inputRes.get().trim();
        if (newValueRaw.isEmpty()) {
            Alert a = new Alert(Alert.AlertType.WARNING, "Value cannot be empty.");
            a.setHeaderText(null);
            a.showAndWait();
            resetSelection();
            return;
        }

        // 3) confirm change
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm change");
        confirm.setHeaderText(null);
        confirm.setContentText("Change " + field + " to:\n\n" + newValueRaw + "\n\nConfirm?");
        Optional<ButtonType> conf = confirm.showAndWait();
        if (conf.isEmpty() || conf.get() != ButtonType.OK) {
            // user cancelled confirmation
            resetSelection();
            return;
        }

        // 4) apply change to a new Member object (Member is a record -> create new instance)
        Member updated;
        try {
            updated = buildUpdatedMember(selected, field, newValueRaw);
        } catch (IllegalArgumentException ex) {
            Alert a = new Alert(Alert.AlertType.ERROR, ex.getMessage());
            a.setHeaderText(null);
            a.showAndWait();
            resetSelection();
            return;
        }

        // 5) call CRUD to update DB
        boolean ok = crud.modRecord(updated);
        if (ok) {
            new Alert(Alert.AlertType.INFORMATION, "Member updated successfully.").showAndWait();
            // refresh combo box list (re-load from DB)
            loadMembers();
        } else {
            new Alert(Alert.AlertType.ERROR, "Failed to update member (DB error).").showAndWait();
        }

        // clear selection after action so user can pick another member
        resetSelection();
    }

    // construct a new Member record by copying existing fields and replacing the selected field
    private Member buildUpdatedMember(Member old, String field, String newVal) {
        // copy everything from old, replace only the chosen field.
        // Note: the Member constructor signature in your project:
        // Member(int memberID, String firstName, String lastName, String email, String contactNo,
        //        String membershipType, LocalDate startDate, LocalDate endDate,
        //        String healthGoal, Double initialWeight, Double goalWeight,
        //        Double startBMI, Double updatedBMI, Integer classID, Integer trainerID, Integer lockerID)
        try {
            return switch (field) {
                case "First name" ->
                        new Member(old.memberID(),
                                newVal,
                                old.lastName(),
                                old.email(),
                                old.contactNo(),
                                old.membershipType(),
                                old.startDate(),
                                old.endDate(),
                                old.healthGoal(),
                                old.initialWeight(),
                                old.goalWeight(),
                                old.startBMI(),
                                old.updatedBMI(),
                                old.classID(),
                                old.trainerID(),
                                old.lockerID()
                        );
                case "Last name" ->
                        new Member(old.memberID(),
                                old.firstName(),
                                newVal,
                                old.email(),
                                old.contactNo(),
                                old.membershipType(),
                                old.startDate(),
                                old.endDate(),
                                old.healthGoal(),
                                old.initialWeight(),
                                old.goalWeight(),
                                old.startBMI(),
                                old.updatedBMI(),
                                old.classID(),
                                old.trainerID(),
                                old.lockerID()
                        );
                case "Email" ->
                        new Member(old.memberID(),
                                old.firstName(),
                                old.lastName(),
                                newVal,
                                old.contactNo(),
                                old.membershipType(),
                                old.startDate(),
                                old.endDate(),
                                old.healthGoal(),
                                old.initialWeight(),
                                old.goalWeight(),
                                old.startBMI(),
                                old.updatedBMI(),
                                old.classID(),
                                old.trainerID(),
                                old.lockerID()
                        );
                case "Contact number" ->
                        new Member(old.memberID(),
                                old.firstName(),
                                old.lastName(),
                                old.email(),
                                newVal,
                                old.membershipType(),
                                old.startDate(),
                                old.endDate(),
                                old.healthGoal(),
                                old.initialWeight(),
                                old.goalWeight(),
                                old.startBMI(),
                                old.updatedBMI(),
                                old.classID(),
                                old.trainerID(),
                                old.lockerID()
                        );
                case "Membership type" ->
                        new Member(old.memberID(),
                                old.firstName(),
                                old.lastName(),
                                old.email(),
                                old.contactNo(),
                                newVal,
                                old.startDate(),
                                old.endDate(),
                                old.healthGoal(),
                                old.initialWeight(),
                                old.goalWeight(),
                                old.startBMI(),
                                old.updatedBMI(),
                                old.classID(),
                                old.trainerID(),
                                old.lockerID()
                        );
                case "Health goal" ->
                        new Member(old.memberID(),
                                old.firstName(),
                                old.lastName(),
                                old.email(),
                                old.contactNo(),
                                old.membershipType(),
                                old.startDate(),
                                old.endDate(),
                                newVal,
                                old.initialWeight(),
                                old.goalWeight(),
                                old.startBMI(),
                                old.updatedBMI(),
                                old.classID(),
                                old.trainerID(),
                                old.lockerID()
                        );
                case "Initial weight" -> {
                    Double d;
                    try {
                        d = Double.parseDouble(newVal);
                    } catch (NumberFormatException e) {
                        throw new IllegalArgumentException("Initial weight must be a number.");
                    }
                    yield new Member(old.memberID(),
                            old.firstName(),
                            old.lastName(),
                            old.email(),
                            old.contactNo(),
                            old.membershipType(),
                            old.startDate(),
                            old.endDate(),
                            old.healthGoal(),
                            d,
                            old.goalWeight(),
                            old.startBMI(),
                            old.updatedBMI(),
                            old.classID(),
                            old.trainerID(),
                            old.lockerID()
                    );
                }
                case "Goal weight" -> {
                    Double d;
                    try {
                        d = Double.parseDouble(newVal);
                    } catch (NumberFormatException e) {
                        throw new IllegalArgumentException("Goal weight must be a number.");
                    }
                    yield new Member(old.memberID(),
                            old.firstName(),
                            old.lastName(),
                            old.email(),
                            old.contactNo(),
                            old.membershipType(),
                            old.startDate(),
                            old.endDate(),
                            old.healthGoal(),
                            old.initialWeight(),
                            d,
                            old.startBMI(),
                            old.updatedBMI(),
                            old.classID(),
                            old.trainerID(),
                            old.lockerID()
                    );
                }
                default -> throw new IllegalArgumentException("Unknown field: " + field);
            };
        } catch (NullPointerException npe) {
            throw new IllegalArgumentException("Member data is incomplete — cannot update.");
        }
    }

    private void resetSelection() {
        ignoreSelection = true;
        cmbMembers.getSelectionModel().clearSelection();
        ignoreSelection = false;
    }

    // Back button handler
    @FXML
    private void handleBack(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/fxmls/main-view.fxml")); // adjust path to your main menu fxml
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }
}

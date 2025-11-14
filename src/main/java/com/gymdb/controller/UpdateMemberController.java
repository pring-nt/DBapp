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
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class UpdateMemberController {

    @FXML private ComboBox<Member> cmbMembers;
    @FXML private Button btnBack;

    private final MemberCRUD crud = new MemberCRUD();
    private final ObservableList<Member> members = FXCollections.observableArrayList();

    // used to avoid reacting while programmatically changing selection
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

        // allow user selection to trigger the edit dialog
        ignoreSelection = false;

        cmbMembers.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (ignoreSelection || newVal == null) return;
            // make sure selection finished and UI isn't in layout phase
            Platform.runLater(() -> showEditDialog(newVal));
        });
    }

    // loads members from DB into ComboBox
    private void loadMembers() {
        try {
            List<Member> list = crud.getAllRecords();
            members.setAll(list);
            cmbMembers.setItems(members);

            // clear selection safely (avoid firing listener)
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

    /**
     * Show a single dialog that allows editing of several member fields.
     */
    private void showEditDialog(Member member) {
        if (member == null) return;

        Dialog<Member> dialog = new Dialog<>();
        dialog.setTitle("Edit Member");
        dialog.setHeaderText(null);

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        // Create fields pre-filled with current values
        TextField firstField = new TextField(member.firstName() == null ? "" : member.firstName());
        TextField lastField  = new TextField(member.lastName() == null ? "" : member.lastName());
        TextField emailField = new TextField(member.email() == null ? "" : member.email());
        TextField contactField = new TextField(member.contactNo() == null ? "" : member.contactNo());
        TextField membershipField = new TextField(member.membershipType() == null ? "" : member.membershipType());
        TextField healthGoalField = new TextField(member.healthGoal() == null ? "" : member.healthGoal());
        TextField initialWeightField = new TextField(member.initialWeight() == null ? "" : String.valueOf(member.initialWeight()));
        TextField goalWeightField = new TextField(member.goalWeight() == null ? "" : String.valueOf(member.goalWeight()));

        // layout
        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(10);

        grid.addRow(0, new Label("First name:"), firstField);
        grid.addRow(1, new Label("Last name:"), lastField);
        grid.addRow(2, new Label("Email:"), emailField);
        grid.addRow(3, new Label("Contact number:"), contactField);
        grid.addRow(4, new Label("Membership type:"), membershipField);
        grid.addRow(5, new Label("Health goal:"), healthGoalField);
        grid.addRow(6, new Label("Initial weight:"), initialWeightField);
        grid.addRow(7, new Label("Goal weight:"), goalWeightField);

        dialog.getDialogPane().setContent(grid);

        // enable/disable Save button based on basic validation (e.g., first/last/email not empty)
        Node saveButton = dialog.getDialogPane().lookupButton(saveButtonType);
        saveButton.setDisable(false); // optional: further validation can be added

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                // Basic validation examples
                String fn = firstField.getText().trim();
                String ln = lastField.getText().trim();
                String em = emailField.getText().trim();

                if (fn.isEmpty() || ln.isEmpty() || em.isEmpty()) {
                    showAlert(Alert.AlertType.WARNING, "Please fill first name, last name and email.");
                    return null;
                }

                // parse numeric weight fields (allow empty -> null)
                Double initW = null;
                Double goalW = null;
                try {
                    if (!initialWeightField.getText().trim().isEmpty()) {
                        initW = Double.parseDouble(initialWeightField.getText().trim());
                    }
                    if (!goalWeightField.getText().trim().isEmpty()) {
                        goalW = Double.parseDouble(goalWeightField.getText().trim());
                    }
                } catch (NumberFormatException ex) {
                    showAlert(Alert.AlertType.ERROR, "Initial/Goal weight must be numeric (e.g. 72.5).");
                    return null;
                }

                // build updated Member record (copy other fields unchanged)
                Member updated = new Member(
                        member.memberID(),
                        fn,
                        ln,
                        em,
                        contactField.getText().trim().isEmpty() ? null : contactField.getText().trim(),
                        membershipField.getText().trim().isEmpty() ? null : membershipField.getText().trim(),
                        member.startDate(),    // keep existing startDate
                        member.endDate(),      // keep existing endDate
                        healthGoalField.getText().trim().isEmpty() ? null : healthGoalField.getText().trim(),
                        initW,
                        goalW,
                        member.startBMI(),
                        member.updatedBMI(),
                        member.classID(),
                        member.trainerID(),
                        member.lockerID()
                );

                // Attempt DB update
                boolean ok = crud.modRecord(updated);
                if (ok) {
                    showAlert(Alert.AlertType.INFORMATION, "Member updated successfully.");
                    // refresh list
                    loadMembers();
                    return updated;
                } else {
                    showAlert(Alert.AlertType.ERROR, "Failed to update member. See console for details.");
                    return null;
                }
            }
            return null;
        });

        // show and wait for user action
        Optional<Member> result = dialog.showAndWait();

        // clear selection so user can select again
        ignoreSelection = true;
        cmbMembers.getSelectionModel().clearSelection();
        ignoreSelection = false;
    }

    private void showAlert(Alert.AlertType type, String message) {
        Alert a = new Alert(type, message);
        a.setHeaderText(null);
        a.showAndWait();
    }

    @FXML
    private void handleAllMembers(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/fxmls/list_members.fxml"));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }

    @FXML
    private void handleEntryForm(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/fxmls/entry_form.fxml"));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }

    @FXML
    private void handleRemove(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxmls/remove.fxml"));
        Parent root = loader.load();

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }

    // Back button handler
    @FXML
    private void handleBack(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/fxmls/main-view.fxml")); // adjust path as needed
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }
}

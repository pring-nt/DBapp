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

    // Back button handler
    @FXML
    private void handleBack(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/fxmls/main-view.fxml")); // adjust path as needed
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }
}

//package com.gymdb.controller;
//
//import com.gymdb.model.Member;
//import com.gymdb.model.MemberCRUD;
//import javafx.application.Platform;
//import javafx.collections.FXCollections;
//import javafx.collections.ObservableList;
//import javafx.event.ActionEvent;
//import javafx.fxml.FXML;
//import javafx.fxml.FXMLLoader;
//import javafx.scene.Node;
//import javafx.scene.Parent;
//import javafx.scene.Scene;
//import javafx.scene.control.*;
//import javafx.stage.Stage;
//import javafx.util.StringConverter;
//
//import java.io.IOException;
//import java.util.Arrays;
//import java.util.List;
//import java.util.Optional;
//
//public class UpdateMemberController {
//
//    @FXML private ComboBox<Member> cmbMembers;
//    @FXML private Button btnBack;
//
//    private final MemberCRUD crud = new MemberCRUD();
//    private final ObservableList<Member> members = FXCollections.observableArrayList();
//
//    // ignoreSelection prevents reacting to programmatic selection changes (like during load)
//    private boolean ignoreSelection = true;
//
//    @FXML
//    private void initialize() {
//        // configure how Member appears in ComboBox
//        cmbMembers.setConverter(new StringConverter<>() {
//            @Override
//            public String toString(Member m) {
//                if (m == null) return "";
//                String first = m.firstName() == null ? "" : m.firstName();
//                String last  = m.lastName()  == null ? "" : m.lastName();
//                return (first + " " + last).trim() + " (ID: " + m.memberID() + ")";
//            }
//            @Override
//            public Member fromString(String s) { return null; }
//        });
//
//        loadMembers();
//
//        // allow user selection to trigger dialogs
//        ignoreSelection = false;
//
//        // listen for user selection
//        cmbMembers.valueProperty().addListener((obs, oldVal, newVal) -> {
//            if (ignoreSelection || newVal == null) return;
//            // ensure the UI has finished selecting before showing dialogs
//            Platform.runLater(() -> startEditFlow(newVal));
//        });
//    }
//
//    // loads members from DB into ComboBox
//    private void loadMembers() {
//        try {
//            List<Member> list = crud.getAllRecords();
//            members.setAll(list);
//            cmbMembers.setItems(members);
//            // clear selection safely
//            ignoreSelection = true;
//            cmbMembers.getSelectionModel().clearSelection();
//        } catch (Exception e) {
//            e.printStackTrace();
//            members.clear();
//            cmbMembers.setItems(members);
//        } finally {
//            ignoreSelection = false;
//        }
//    }
//
//    // start the sequence of popups to choose field, input new value, confirm, then update
//    private void startEditFlow(Member selected) {
//        if (selected == null) return;
//
//        // 1) ask what to edit (choice dialog)
//        List<String> choices = Arrays.asList(
//                "First name",
//                "Last name",
//                "Email",
//                "Contact number",
//                "Membership type",
//                "Health goal",
//                "Initial weight",
//                "Goal weight"
//        );
//
//        ChoiceDialog<String> choice = new ChoiceDialog<>(choices.get(0), choices);
//        choice.setTitle("Choose field");
//        choice.setHeaderText(null);
//        choice.setContentText("What do you want to edit?");
//        Optional<String> choiceRes = choice.showAndWait();
//
//        if (choiceRes.isEmpty()) {
//            // user cancelled choosing field -> clear selection
//            resetSelection();
//            return;
//        }
//
//        String field = choiceRes.get();
//
//        // 2) ask for new value with a TextInputDialog
//        TextInputDialog input = new TextInputDialog();
//        input.setTitle("Enter new value");
//        input.setHeaderText(null);
//        input.setContentText("New " + field + ":");
//
//        Optional<String> inputRes = input.showAndWait();
//        if (inputRes.isEmpty()) {
//            // user cancelled entering new value
//            resetSelection();
//            return;
//        }
//
//        String newValueRaw = inputRes.get().trim();
//        if (newValueRaw.isEmpty()) {
//            Alert a = new Alert(Alert.AlertType.WARNING, "Value cannot be empty.");
//            a.setHeaderText(null);
//            a.showAndWait();
//            resetSelection();
//            return;
//        }
//
//        // 3) confirm change
//        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
//        confirm.setTitle("Confirm change");
//        confirm.setHeaderText(null);
//        confirm.setContentText("Change " + field + " to:\n\n" + newValueRaw + "\n\nConfirm?");
//        Optional<ButtonType> conf = confirm.showAndWait();
//        if (conf.isEmpty() || conf.get() != ButtonType.OK) {
//            // user cancelled confirmation
//            resetSelection();
//            return;
//        }
//
//        // 4) apply change to a new Member object (Member is a record -> create new instance)
//        Member updated;
//        try {
//            updated = buildUpdatedMember(selected, field, newValueRaw);
//        } catch (IllegalArgumentException ex) {
//            Alert a = new Alert(Alert.AlertType.ERROR, ex.getMessage());
//            a.setHeaderText(null);
//            a.showAndWait();
//            resetSelection();
//            return;
//        }
//
//        // 5) call CRUD to update DB
//        boolean ok = crud.modRecord(updated);
//        if (ok) {
//            new Alert(Alert.AlertType.INFORMATION, "Member updated successfully.").showAndWait();
//            // refresh combo box list (re-load from DB)
//            loadMembers();
//        } else {
//            new Alert(Alert.AlertType.ERROR, "Failed to update member (DB error).").showAndWait();
//        }
//
//        // clear selection after action so user can pick another member
//        resetSelection();
//    }
//
//    // construct a new Member record by copying existing fields and replacing the selected field
//    private Member buildUpdatedMember(Member old, String field, String newVal) {
//        // copy everything from old, replace only the chosen field.
//        // Note: the Member constructor signature in your project:
//        // Member(int memberID, String firstName, String lastName, String email, String contactNo,
//        //        String membershipType, LocalDate startDate, LocalDate endDate,
//        //        String healthGoal, Double initialWeight, Double goalWeight,
//        //        Double startBMI, Double updatedBMI, Integer classID, Integer trainerID, Integer lockerID)
//        try {
//            return switch (field) {
//                case "First name" ->
//                        new Member(old.memberID(),
//                                newVal,
//                                old.lastName(),
//                                old.email(),
//                                old.contactNo(),
//                                old.membershipType(),
//                                old.startDate(),
//                                old.endDate(),
//                                old.healthGoal(),
//                                old.initialWeight(),
//                                old.goalWeight(),
//                                old.startBMI(),
//                                old.updatedBMI(),
//                                old.classID(),
//                                old.trainerID(),
//                                old.lockerID()
//                        );
//                case "Last name" ->
//                        new Member(old.memberID(),
//                                old.firstName(),
//                                newVal,
//                                old.email(),
//                                old.contactNo(),
//                                old.membershipType(),
//                                old.startDate(),
//                                old.endDate(),
//                                old.healthGoal(),
//                                old.initialWeight(),
//                                old.goalWeight(),
//                                old.startBMI(),
//                                old.updatedBMI(),
//                                old.classID(),
//                                old.trainerID(),
//                                old.lockerID()
//                        );
//                case "Email" ->
//                        new Member(old.memberID(),
//                                old.firstName(),
//                                old.lastName(),
//                                newVal,
//                                old.contactNo(),
//                                old.membershipType(),
//                                old.startDate(),
//                                old.endDate(),
//                                old.healthGoal(),
//                                old.initialWeight(),
//                                old.goalWeight(),
//                                old.startBMI(),
//                                old.updatedBMI(),
//                                old.classID(),
//                                old.trainerID(),
//                                old.lockerID()
//                        );
//                case "Contact number" ->
//                        new Member(old.memberID(),
//                                old.firstName(),
//                                old.lastName(),
//                                old.email(),
//                                newVal,
//                                old.membershipType(),
//                                old.startDate(),
//                                old.endDate(),
//                                old.healthGoal(),
//                                old.initialWeight(),
//                                old.goalWeight(),
//                                old.startBMI(),
//                                old.updatedBMI(),
//                                old.classID(),
//                                old.trainerID(),
//                                old.lockerID()
//                        );
//                case "Membership type" ->
//                        new Member(old.memberID(),
//                                old.firstName(),
//                                old.lastName(),
//                                old.email(),
//                                old.contactNo(),
//                                newVal,
//                                old.startDate(),
//                                old.endDate(),
//                                old.healthGoal(),
//                                old.initialWeight(),
//                                old.goalWeight(),
//                                old.startBMI(),
//                                old.updatedBMI(),
//                                old.classID(),
//                                old.trainerID(),
//                                old.lockerID()
//                        );
//                case "Health goal" ->
//                        new Member(old.memberID(),
//                                old.firstName(),
//                                old.lastName(),
//                                old.email(),
//                                old.contactNo(),
//                                old.membershipType(),
//                                old.startDate(),
//                                old.endDate(),
//                                newVal,
//                                old.initialWeight(),
//                                old.goalWeight(),
//                                old.startBMI(),
//                                old.updatedBMI(),
//                                old.classID(),
//                                old.trainerID(),
//                                old.lockerID()
//                        );
//                case "Initial weight" -> {
//                    Double d;
//                    try {
//                        d = Double.parseDouble(newVal);
//                    } catch (NumberFormatException e) {
//                        throw new IllegalArgumentException("Initial weight must be a number.");
//                    }
//                    yield new Member(old.memberID(),
//                            old.firstName(),
//                            old.lastName(),
//                            old.email(),
//                            old.contactNo(),
//                            old.membershipType(),
//                            old.startDate(),
//                            old.endDate(),
//                            old.healthGoal(),
//                            d,
//                            old.goalWeight(),
//                            old.startBMI(),
//                            old.updatedBMI(),
//                            old.classID(),
//                            old.trainerID(),
//                            old.lockerID()
//                    );
//                }
//                case "Goal weight" -> {
//                    Double d;
//                    try {
//                        d = Double.parseDouble(newVal);
//                    } catch (NumberFormatException e) {
//                        throw new IllegalArgumentException("Goal weight must be a number.");
//                    }
//                    yield new Member(old.memberID(),
//                            old.firstName(),
//                            old.lastName(),
//                            old.email(),
//                            old.contactNo(),
//                            old.membershipType(),
//                            old.startDate(),
//                            old.endDate(),
//                            old.healthGoal(),
//                            old.initialWeight(),
//                            d,
//                            old.startBMI(),
//                            old.updatedBMI(),
//                            old.classID(),
//                            old.trainerID(),
//                            old.lockerID()
//                    );
//                }
//                default -> throw new IllegalArgumentException("Unknown field: " + field);
//            };
//        } catch (NullPointerException npe) {
//            throw new IllegalArgumentException("Member data is incomplete — cannot update.");
//        }
//    }
//
//    private void resetSelection() {
//        ignoreSelection = true;
//        cmbMembers.getSelectionModel().clearSelection();
//        ignoreSelection = false;
//    }
//
//    // Back button handler
//    @FXML
//    private void handleBack(ActionEvent event) throws IOException {
//        Parent root = FXMLLoader.load(getClass().getResource("/fxmls/main-view.fxml")); // adjust path to your main menu fxml
//        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
//        stage.setScene(new Scene(root));
//        stage.show();
//    }
//}

package com.gymdb.controller;

import com.gymdb.model.Attendance;
import com.gymdb.model.AttendanceCRUD;
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
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class AttendanceController {

    @FXML private ComboBox<Member> cmbMember;
    @FXML private ComboBox<String> cmbClassType;
    @FXML private Button btnCheckIn;
    @FXML private Button btnBack;

    private final MemberCRUD memberCrud = new MemberCRUD();
    private final AttendanceCRUD attendanceCrud = new AttendanceCRUD();

    private final ObservableList<Member> members = FXCollections.observableArrayList();

    // selectedClassId will hold the fixed mapping ID (1-4) or 0 if none chosen
    private int selectedClassId = 0;
    private String selectedClassName = null;
    private String selectedClassType = null;

    @FXML
    private void initialize() {
        // Member display: "First Last (ID: #)"
        cmbMember.setConverter(new StringConverter<>() {
            @Override
            public String toString(Member m) {
                if (m == null) return "";
                String f = m.firstName() == null ? "" : m.firstName();
                String l = m.lastName() == null ? "" : m.lastName();
                return (f + " " + l).trim() + " (ID: " + m.memberID() + ")";
            }
            @Override
            public Member fromString(String s) { return null; }
        });

        // fixed class type options (exactly these four)
        cmbClassType.setItems(FXCollections.observableArrayList(
                "Yoga", "Strength Training", "HIIT", "Zumba"
        ));
        cmbClassType.setPromptText("Select class type");

        // load members from DB
        try {
            List<Member> list = memberCrud.getAllRecords();
            members.setAll(list);
            cmbMember.setItems(members);
        } catch (Exception e) {
            e.printStackTrace();
            members.clear();
        }

        // when user picks a class type, show popup to choose a class name for that type
        cmbClassType.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null || newVal.isBlank()) {
                clearSelectedClass();
                return;
            }
            // show the choice dialog after selection event finishes
            Platform.runLater(() -> showClassNameChoices(newVal));
        });
    }

    private void showClassNameChoices(String classType) {
        // mapping classType -> options
        Map<String, List<String>> choices = Map.of(
                "Yoga", List.of("Morning Yoga Flow", "Stretch & Relax", "Power Up"),
                "Strength Training", List.of("Body Pump Burn", "Core & Stability", "Upper Body Blast"),
                "HIIT", List.of("HIIT Express", "Total Body Inferno", "Cardio Crush"),
                "Zumba", List.of("Zumba Dance Party", "Latin Groove", "Pop & Sweat")
        );

        List<String> options = choices.getOrDefault(classType, List.of());

        ChoiceDialog<String> dialog = new ChoiceDialog<>(options.get(0), options);
        dialog.setTitle("Choose class");
        dialog.setHeaderText(null);
        dialog.setContentText("Choose a " + classType + " class:");

        Optional<String> opt = dialog.showAndWait();
        if (opt.isEmpty()) {
            // user cancelled -> clear selection so they can pick again
            clearSelectedClass();
            // also clear the combobox selection visually
            cmbClassType.getSelectionModel().clearSelection();
            return;
        }

        String className = opt.get();

        // **MAP class type to fixed IDs (Option 2)**
        int cid = mapClassTypeToId(classType);
        if (cid <= 0) {
            Alert err = new Alert(Alert.AlertType.ERROR, "Invalid class type mapping.");
            err.setHeaderText(null);
            err.showAndWait();
            clearSelectedClass();
            cmbClassType.getSelectionModel().clearSelection();
            return;
        }

        // success — store chosen class info for check-in
        selectedClassId = cid;
        selectedClassName = className;
        selectedClassType = classType;

        Alert info = new Alert(Alert.AlertType.INFORMATION, "Selected class: " + className + " (" + classType + ")");
        info.setHeaderText(null);
        info.showAndWait();
    }

    /**
     * Fixed mapping: Yoga=1, Strength Training=2, HIIT=3, Zumba=4
     */
    private int mapClassTypeToId(String type) {
        if (type == null) return 0;
        return switch (type) {
            case "Yoga" -> 1;
            case "Strength Training" -> 2;
            case "HIIT" -> 3;
            case "Zumba" -> 4;
            default -> 0;
        };
    }

    @FXML
    private void handleCheckIn(ActionEvent event) {
        Member selectedMember = cmbMember.getValue();
        if (selectedMember == null) {
            new Alert(Alert.AlertType.WARNING, "Please select a member.").showAndWait();
            return;
        }

        // if no class chosen (selectedClassId==0), ask if they want to proceed without class
        if (selectedClassId == 0) {
            Alert a = new Alert(Alert.AlertType.CONFIRMATION,
                    "No class selected. Record attendance without a class?");
            a.setHeaderText(null);
            Optional<ButtonType> res = a.showAndWait();
            if (res.isEmpty() || res.get() != ButtonType.OK) return;
        }

        String memberName = (selectedMember.firstName() == null ? "" : selectedMember.firstName())
                + " " + (selectedMember.lastName() == null ? "" : selectedMember.lastName());

        String classInfo = (selectedClassId == 0) ? "No class" : (selectedClassName + " (" + selectedClassType + ")");

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm check-in");
        confirm.setHeaderText(null);
        confirm.setContentText("Check in " + memberName.trim() + " for: " + classInfo + " ?");
        Optional<ButtonType> res = confirm.showAndWait();
        if (res.isEmpty() || res.get() != ButtonType.OK) return;

        Attendance attendance = new Attendance(0, null, selectedMember.memberID(), selectedClassId);
        boolean ok = attendanceCrud.addRecord(attendance);

        if (ok) {
            new Alert(Alert.AlertType.INFORMATION, "Check-in recorded.").showAndWait();
            // clear selections
            cmbMember.getSelectionModel().clearSelection();
            cmbClassType.getSelectionModel().clearSelection();
            clearSelectedClass();
        } else {
            new Alert(Alert.AlertType.ERROR, "Failed to record check-in.").showAndWait();
        }
    }

    private void clearSelectedClass() {
        selectedClassId = 0;
        selectedClassName = null;
        selectedClassType = null;
    }

    @FXML
    private void handleBack(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/fxmls/AttendanceMenu.fxml")); // adjust path if different
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }
}


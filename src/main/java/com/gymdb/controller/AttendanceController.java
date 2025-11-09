package com.gymdb.controller;

import com.gymdb.model.Attendance;
import com.gymdb.model.AttendanceCRUD;
import com.gymdb.model.GymClass;
import com.gymdb.model.GymClassCRUD;
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
import java.util.stream.Collectors;

public class AttendanceController {

    @FXML private ComboBox<Member> cmbMember;
    @FXML private ComboBox<String> cmbClassType;
    @FXML private Button btnCheckIn;
    @FXML private Button btnBack;

    private final MemberCRUD memberCrud = new MemberCRUD();
    private final GymClassCRUD classCrud = new GymClassCRUD();
    private final AttendanceCRUD attendanceCrud = new AttendanceCRUD();

    private final ObservableList<Member> members = FXCollections.observableArrayList();

    // after user picks a class type and class name popup, this will hold resolved classID (>0),
    // or 0 to indicate "no class", or -1 on failure.
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
        // resolve classID: find existing or create it
        int cid = findOrCreateClassId(classType, className);
        if (cid <= 0) {
            // failed to create/find class — show error and clear selection
            Alert err = new Alert(Alert.AlertType.ERROR, "Failed to locate or create class '" + className + "'.");
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
        // optionally show small confirmation
        Tooltip t = new Tooltip("Selected: " + className + " (" + classType + ")");
        // (can't attach a Tooltip to ComboBox directly via code easily here; instead show information box)
        Alert info = new Alert(Alert.AlertType.INFORMATION, "Selected class: " + className + " (" + classType + ")");
        info.setHeaderText(null);
        info.showAndWait();
    }

    /**
     * Find a GymClass with the given type+name. If not found, try to insert it (GymClassCRUD.addRecord)
     * and then re-query to find the new record and return its classID.
     * Returns:
     *  - classID (>0) on success
     *  - -1 on failure
     */
    private int findOrCreateClassId(String classType, String className) {
        try {
            List<GymClass> all = classCrud.getAllRecords();
            Optional<GymClass> found = all.stream()
                    .filter(g -> g.classType() != null && g.classType().equalsIgnoreCase(classType)
                            && g.className() != null && g.className().equalsIgnoreCase(className))
                    .findFirst();
            if (found.isPresent()) return found.get().classID();

            // not found -> create a new class record with null date/time and no personnel
            GymClass newClass = new GymClass(0, className, classType, null, null, null, null);
            boolean added = classCrud.addRecord(newClass);
            if (!added) return -1;

            // re-query to find the inserted record (matching name+type)
            all = classCrud.getAllRecords();
            Optional<GymClass> created = all.stream()
                    .filter(g -> g.classType() != null && g.classType().equalsIgnoreCase(classType)
                            && g.className() != null && g.className().equalsIgnoreCase(className))
                    .findFirst();
            return created.map(GymClass::classID).orElse(-1);
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    @FXML
    private void handleCheckIn(ActionEvent event) {
        Member selectedMember = cmbMember.getValue();
        if (selectedMember == null) {
            new Alert(Alert.AlertType.WARNING, "Please select a member.").showAndWait();
            return;
        }

        // selectedClassId contains the resolved class id chosen earlier, or 0 if none
        // If user didn't pick a className yet (selectedClassId==0), confirm with user to proceed without class
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

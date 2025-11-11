package com.gymdb.controller;

import com.gymdb.model.Attendance;
import com.gymdb.model.AttendanceCRUD;
import com.gymdb.model.Member;
import com.gymdb.model.MemberCRUD;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class AttendanceController {

    @FXML private ComboBox<Member> cmbMember;
    @FXML private ComboBox<String> cmbClassType;
    @FXML private Button btnCheckIn;
    @FXML private Button btnBack;

    private final MemberCRUD memberCrud = new MemberCRUD();
    private final AttendanceCRUD attendanceCrud = new AttendanceCRUD();
    private final ObservableList<Member> members = FXCollections.observableArrayList();

    private int selectedClassId = 0;
    private String selectedClassName = null;
    private String selectedClassType = null;

    @FXML
    private void initialize() {
        System.out.println("[DEBUG] initialize() start");

        // debug injections
        System.out.println("[DEBUG] cmbMember is " + (cmbMember == null ? "NULL" : "OK"));
        System.out.println("[DEBUG] cmbClassType is " + (cmbClassType == null ? "NULL" : "OK"));
        System.out.println("[DEBUG] btnCheckIn is " + (btnCheckIn == null ? "NULL" : "OK"));
        System.out.println("[DEBUG] btnBack is " + (btnBack == null ? "NULL" : "OK"));

        if (cmbMember == null || cmbClassType == null || btnCheckIn == null) {
            System.err.println("[DEBUG] One or more FXML controls are NULL. Check fx:id and fx:controller in FXML.");
            return;
        }

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

        cmbClassType.setItems(FXCollections.observableArrayList(
                "Yoga", "Strength Training", "HIIT", "Zumba"
        ));

        try {
            List<Member> list = memberCrud.getAllRecords();
            members.setAll(list);
            cmbMember.setItems(members);
            System.out.println("[DEBUG] loaded members: " + list.size());
        } catch (Exception e) {
            e.printStackTrace();
            members.clear();
        }

        // When the user picks a class type, show a dialog to choose specific class name
        cmbClassType.valueProperty().addListener((obs, oldVal, newVal) -> {
            System.out.println("[DEBUG] cmbClassType listener: newVal=" + newVal);
            if (newVal == null || newVal.isBlank()) {
                clearSelectedClass();
                return;
            }
            showClassSelectionDialog(newVal);
        });

        System.out.println("[DEBUG] initialize() end");
    }

    /**
     * Non-blocking class-name chooser: uses dialog.show() and processes result in setOnHidden.
     */
    private void showClassSelectionDialog(String classType) {
        Map<String, List<String>> choices = Map.of(
                "Yoga", List.of("Morning Yoga Flow", "Stretch & Relax", "Power Up"),
                "Strength Training", List.of("Body Pump Burn", "Core & Stability", "Upper Body Blast"),
                "HIIT", List.of("HIIT Express", "Total Body Inferno", "Cardio Crush"),
                "Zumba", List.of("Zumba Dance Party", "Latin Groove", "Pop & Sweat")
        );

        List<String> options = choices.getOrDefault(classType, List.of());
        if (options.isEmpty()) {
            clearSelectedClass();
            return;
        }

        ChoiceBox<String> box = new ChoiceBox<>(FXCollections.observableArrayList(options));
        box.getSelectionModel().selectFirst();

        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Choose Class");
        dialog.setHeaderText("Choose a " + classType + " class");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.getDialogPane().setContent(box);
        dialog.setResultConverter(btn -> btn == ButtonType.OK ? box.getValue() : null);
        dialog.initModality(Modality.WINDOW_MODAL);
        if (cmbClassType.getScene() != null && cmbClassType.getScene().getWindow() != null) {
            dialog.initOwner(cmbClassType.getScene().getWindow());
        }

        // non-blocking show
        dialog.show();
        dialog.setOnHidden(ev -> {
            String result = dialog.getResult(); // null if cancelled
            if (result == null) {
                clearSelectedClass();
                if (cmbClassType != null && cmbClassType.getSelectionModel() != null) {
                    cmbClassType.getSelectionModel().clearSelection();
                }
            } else {
                selectedClassName = result;
                selectedClassType = classType;
                selectedClassId = mapClassTypeToId(classType);
                if (cmbClassType != null) cmbClassType.setValue(classType);
                // optional feedback
                Alert info = new Alert(Alert.AlertType.INFORMATION, "Selected class: " + selectedClassName + " (" + selectedClassType + ")");
                setOwner(info);
                info.setHeaderText(null);
                info.show();
            }
        });
    }

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
            showAlertNonBlocking(Alert.AlertType.WARNING, "Please select a member.");
            return;
        }

        // If no class selected, ask user first (non-blocking)
        if (selectedClassId == 0) {
            Alert a = new Alert(Alert.AlertType.CONFIRMATION, "No class selected. Record attendance without a class?");
            a.setHeaderText(null);
            setOwner(a);
            a.show();
            a.setOnHidden(ev -> {
                ButtonType r = a.getResult();
                if (r == null || r != ButtonType.OK) return;
                showConfirmAndSaveNonBlocking(selectedMember);
            });
            return;
        }

        // class selected -> confirm & save (non-blocking)
        showConfirmAndSaveNonBlocking(selectedMember);
    }

    /** Non-blocking confirmation + save using show() and setOnHidden() */
    private void showConfirmAndSaveNonBlocking(Member selectedMember) {
        String memberName = (selectedMember.firstName() == null ? "" : selectedMember.firstName())
                + " " + (selectedMember.lastName() == null ? "" : selectedMember.lastName());
        String classInfo = (selectedClassId == 0)
                ? "No class"
                : (selectedClassName + " (" + selectedClassType + ")");

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm check-in");
        confirm.setHeaderText(null);
        confirm.setContentText("Check in " + memberName.trim() + " for: " + classInfo + " ?");
        setOwner(confirm);

        confirm.show();
        confirm.setOnHidden(ev -> {
            ButtonType res = confirm.getResult();
            if (res == null || res != ButtonType.OK) return;

            LocalDateTime checkInTime = LocalDateTime.now();
            Attendance attendance = new Attendance(0, checkInTime, selectedMember.memberID(), selectedClassId);
            boolean ok = attendanceCrud.addRecord(attendance);

            if (ok) {
                showAlertNonBlocking(Alert.AlertType.INFORMATION, "Check-in recorded at " + checkInTime.toLocalTime());
                if (cmbMember != null) cmbMember.getSelectionModel().clearSelection();
                if (cmbClassType != null) cmbClassType.getSelectionModel().clearSelection();
                clearSelectedClass();
            } else {
                showAlertNonBlocking(Alert.AlertType.ERROR, "Failed to record check-in.");
            }
        });
    }

    @FXML
    private void handleBack(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/fxmls/AttendanceMenu.fxml"));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }

    private void setOwner(Dialog<?> dialog) {
        if (btnCheckIn != null && btnCheckIn.getScene() != null && btnCheckIn.getScene().getWindow() != null) {
            dialog.initOwner(btnCheckIn.getScene().getWindow());
        }
    }

    /**
     * showNonBlocking alert (use this for small messages). Avoids showAndWait() during layout.
     */
    private void showAlertNonBlocking(Alert.AlertType type, String message) {
        Alert a = new Alert(type, message);
        setOwner(a);
        a.setHeaderText(null);
        a.show();
    }

    private void clearSelectedClass() {
        selectedClassId = 0;
        selectedClassName = null;
        selectedClassType = null;
    }
}


//package com.gymdb.controller;
//
//import com.gymdb.model.Attendance;
//import com.gymdb.model.AttendanceCRUD;
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
//import java.time.LocalDateTime;
//import java.util.List;
//import java.util.Map;
//import java.util.Optional;
//
//public class AttendanceController {
//
//    @FXML private ComboBox<Member> cmbMember;
//    @FXML private ComboBox<String> cmbClassType;
//    @FXML private Button btnCheckIn;
//    @FXML private Button btnBack;
//
//    private final MemberCRUD memberCrud = new MemberCRUD();
//    private final AttendanceCRUD attendanceCrud = new AttendanceCRUD();
//    private final ObservableList<Member> members = FXCollections.observableArrayList();
//
//    private int selectedClassId = 0;
//    private String selectedClassName = null;
//    private String selectedClassType = null;
//
//    @FXML
//    private void initialize() {
//        cmbMember.setConverter(new StringConverter<>() {
//            @Override
//            public String toString(Member m) {
//                if (m == null) return "";
//                String f = m.firstName() == null ? "" : m.firstName();
//                String l = m.lastName() == null ? "" : m.lastName();
//                return (f + " " + l).trim() + " (ID: " + m.memberID() + ")";
//            }
//            @Override
//            public Member fromString(String s) { return null; }
//        });
//
//        cmbClassType.setItems(FXCollections.observableArrayList(
//                "Yoga", "Strength Training", "HIIT", "Zumba"
//        ));
//        cmbClassType.setPromptText("Select class type");
//
//        try {
//            List<Member> list = memberCrud.getAllRecords();
//            members.setAll(list);
//            cmbMember.setItems(members);
//        } catch (Exception e) {
//            e.printStackTrace();
//            members.clear();
//        }
//
//        cmbClassType.valueProperty().addListener((obs, oldVal, newVal) -> {
//            if (newVal == null || newVal.isBlank()) {
//                clearSelectedClass();
//                return;
//            }
//            Platform.runLater(() -> showClassNameChoices(newVal));
//        });
//    }
//
//    private void showClassNameChoices(String classType) {
//        Map<String, List<String>> choices = Map.of(
//                "Yoga", List.of("Morning Yoga Flow", "Stretch & Relax", "Power Up"),
//                "Strength Training", List.of("Body Pump Burn", "Core & Stability", "Upper Body Blast"),
//                "HIIT", List.of("HIIT Express", "Total Body Inferno", "Cardio Crush"),
//                "Zumba", List.of("Zumba Dance Party", "Latin Groove", "Pop & Sweat")
//        );
//
//        List<String> options = choices.getOrDefault(classType, List.of());
//
//        ChoiceDialog<String> dialog = new ChoiceDialog<>(options.get(0), options);
//        dialog.setTitle("Choose class");
//        dialog.setHeaderText(null);
//        dialog.setContentText("Choose a " + classType + " class:");
//
//        Optional<String> opt = dialog.showAndWait();
//        if (opt.isEmpty()) {
//            clearSelectedClass();
//            cmbClassType.getSelectionModel().clearSelection();
//            return;
//        }
//
//        selectedClassName = opt.get();
//        selectedClassId = mapClassTypeToId(classType);
//        selectedClassType = classType;
//
//        Alert info = new Alert(Alert.AlertType.INFORMATION, "Selected class: " + selectedClassName + " (" + selectedClassType + ")");
//        info.setHeaderText(null);
//        info.showAndWait();
//    }
//
//    private int mapClassTypeToId(String type) {
//        if (type == null) return 0;
//        return switch (type) {
//            case "Yoga" -> 1;
//            case "Strength Training" -> 2;
//            case "HIIT" -> 3;
//            case "Zumba" -> 4;
//            default -> 0;
//        };
//    }
//
//    @FXML
//    private void handleCheckIn(ActionEvent event) {
//        Member selectedMember = cmbMember.getValue();
//        if (selectedMember == null) {
//            new Alert(Alert.AlertType.WARNING, "Please select a member.").showAndWait();
//            return;
//        }
//
//        if (selectedClassId == 0) {
//            Alert a = new Alert(Alert.AlertType.CONFIRMATION,
//                    "No class selected. Record attendance without a class?");
//            a.setHeaderText(null);
//            Optional<ButtonType> res = a.showAndWait();
//            if (res.isEmpty() || res.get() != ButtonType.OK) return;
//        }
//
//        String memberName = (selectedMember.firstName() == null ? "" : selectedMember.firstName())
//                + " " + (selectedMember.lastName() == null ? "" : selectedMember.lastName());
//        String classInfo = (selectedClassId == 0) ? "No class" : (selectedClassName + " (" + selectedClassType + ")");
//
//        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
//        confirm.setTitle("Confirm check-in");
//        confirm.setHeaderText(null);
//        confirm.setContentText("Check in " + memberName.trim() + " for: " + classInfo + " ?");
//        Optional<ButtonType> res = confirm.showAndWait();
//        if (res.isEmpty() || res.get() != ButtonType.OK) return;
//
//        // Store current date-time as check-in time
//        LocalDateTime checkInTime = LocalDateTime.now();
//
//        // Create Attendance record with actual check-in time
//        Attendance attendance = new Attendance(0, checkInTime, selectedMember.memberID(), selectedClassId);
//
//        boolean ok = attendanceCrud.addRecord(attendance);
//
//        if (ok) {
//            new Alert(Alert.AlertType.INFORMATION, "Check-in recorded at " + checkInTime.toLocalTime()).showAndWait();
//            cmbMember.getSelectionModel().clearSelection();
//            cmbClassType.getSelectionModel().clearSelection();
//            clearSelectedClass();
//        } else {
//            new Alert(Alert.AlertType.ERROR, "Failed to record check-in.").showAndWait();
//        }
//    }
//
//    private void clearSelectedClass() {
//        selectedClassId = 0;
//        selectedClassName = null;
//        selectedClassType = null;
//    }
//
//    @FXML
//    private void handleBack(ActionEvent event) throws IOException {
//        Parent root = FXMLLoader.load(getClass().getResource("/fxmls/AttendanceMenu.fxml"));
//        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
//        stage.setScene(new Scene(root));
//        stage.show();
//    }
//}

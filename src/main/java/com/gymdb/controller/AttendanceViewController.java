package com.gymdb.controller;

import com.gymdb.model.Attendance;
import com.gymdb.model.AttendanceCRUD;
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

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AttendanceViewController {

    @FXML private TableView<Attendance> attendanceTable;
    @FXML private TableColumn<Attendance, Integer> colId;
    @FXML private TableColumn<Attendance, String> colDatetime;
    @FXML private TableColumn<Attendance, String> colMember;
    @FXML private TableColumn<Attendance, String> colClass;

    private final AttendanceCRUD attendanceCrud = new AttendanceCRUD();
    private final MemberCRUD memberCrud = new MemberCRUD();
    private final ObservableList<Attendance> data = FXCollections.observableArrayList();
    private Map<Integer, String> memberNames;

    private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @FXML
    private void initialize() {
        // Preload member names to display
        List<Member> allMembers = memberCrud.getAllRecords();
        memberNames = allMembers.stream()
                .collect(Collectors.toMap(Member::memberID,
                        m -> ((m.firstName()==null?"":m.firstName()) + " " + (m.lastName()==null?"":m.lastName())).trim()));

        // Column factories
        colId.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().attendanceID()));
        colDatetime.setCellValueFactory(c -> new ReadOnlyStringWrapper(
                c.getValue().datetime() == null ? "" : c.getValue().datetime().format(fmt)));
        colMember.setCellValueFactory(c -> new ReadOnlyStringWrapper(
                memberNames.getOrDefault(c.getValue().memberID(), "ID:" + c.getValue().memberID())));
        colClass.setCellValueFactory(c -> new ReadOnlyStringWrapper(
                String.valueOf(c.getValue().classID())));

        // Load records
        loadAttendance();
    }

    private void loadAttendance() {
        List<Attendance> list = attendanceCrud.getAllRecords();
        data.setAll(list);
        attendanceTable.setItems(data);
    }

    @FXML
    private void handleBack(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/fxmls/AttendanceMenu.fxml")); // adjust path
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }
}

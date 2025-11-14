package com.gymdb.controller;

import com.gymdb.model.Member;
import com.gymdb.model.MemberCRUD;
import com.gymdb.services.ClassAttendance;
import com.gymdb.services.ClassAttendanceService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;
import javafx.beans.property.ReadOnlyStringWrapper;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AttendanceViewController {

    @FXML private TableView<ClassAttendance> attendanceTable;
    @FXML private TableColumn<ClassAttendance, Integer> colId;
    @FXML private TableColumn<ClassAttendance, String> colDatetime;
    @FXML private TableColumn<ClassAttendance, String> colMember;
    @FXML private TableColumn<ClassAttendance, String> colClassType;

    private final MemberCRUD memberCrud = new MemberCRUD();

    // cache memberID -> "First Last" for fast lookup
    private Map<Integer, String> memberNames;

    private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @FXML
    public void initialize() {
        // prepare member lookup map
        List<Member> members = memberCrud.getAllRecords();
        memberNames = members.stream()
                .collect(Collectors.toMap(Member::memberID,
                        m -> ((m.firstName()==null ? "" : m.firstName()) + " " + (m.lastName()==null ? "" : m.lastName())).trim()));

        // wire columns
        colId.setCellValueFactory(cell -> new javafx.beans.property.ReadOnlyObjectWrapper<>(cell.getValue().attendanceID()));

        colDatetime.setCellValueFactory(cell -> {
            var dt = cell.getValue().attendanceDateTime();
            String s = (dt == null) ? "" : fmt.format(dt);
            return new ReadOnlyStringWrapper(s);
        });

        colMember.setCellValueFactory(cell -> {
            int memberId = cell.getValue().memberID();
            String name = memberNames.getOrDefault(memberId, "ID:" + memberId);
            return new ReadOnlyStringWrapper(name);
        });

        colClassType.setCellValueFactory(cell ->
                new ReadOnlyStringWrapper(cell.getValue().classType() == null ? "" : cell.getValue().classType())
        );

        loadAttendance();
    }

    private void loadAttendance() {
        List<ClassAttendance> list = ClassAttendanceService.getClassAttendances();

        // sort by attendanceDateTime descending (latest -> oldest)
        // ensure null datetimes end up last
        Comparator<ClassAttendance> cmp = Comparator.comparing(
                ClassAttendance::attendanceDateTime,
                Comparator.nullsFirst(Comparator.naturalOrder()) // nullsFirst, then reversed so nulls become last after reverse
        ).reversed();

        list.sort(cmp);

        ObservableList<ClassAttendance> data = FXCollections.observableArrayList(list);
        attendanceTable.setItems(data);

        // optional: ensure the table view shows from top (latest)
        if (!data.isEmpty()) attendanceTable.scrollTo(0);
    }

    @FXML
    private void handleCheck(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxmls/Attendance.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            Alert err = new Alert(Alert.AlertType.ERROR, "Failed to open Check-In screen.");
            err.setHeaderText(null);
            err.showAndWait();
        }
    }

    @FXML
    private void handleBack(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/fxmls/AttendanceMenu.fxml")); // adjust path if different
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }
}

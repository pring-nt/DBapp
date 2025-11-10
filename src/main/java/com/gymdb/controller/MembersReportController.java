package com.gymdb.controller;

import com.gymdb.model.Member;
import com.gymdb.model.MemberCRUD;
import com.gymdb.utils.DBConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.Region;

import java.io.IOException;
import java.sql.*;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MembersReportController {

    @FXML private ComboBox<Member> cmbMembers;
    @FXML private Button btnBack;

    private final MemberCRUD memberCrud = new MemberCRUD();

    @FXML
    public void initialize() {
        // load members from DB
        try {
            List<Member> list = memberCrud.getAllRecords();
            ObservableList<Member> obs = FXCollections.observableArrayList(list);
            cmbMembers.setItems(obs);
        } catch (Exception ex) {
            ex.printStackTrace();
            cmbMembers.setItems(FXCollections.observableArrayList());
        }

        // show "First Last (ID:#)" in combo box
        cmbMembers.setConverter(new StringConverter<>() {
            @Override
            public String toString(Member m) {
                if (m == null) return "";
                String fn = m.firstName() == null ? "" : m.firstName();
                String ln = m.lastName() == null ? "" : m.lastName();
                return (fn + " " + ln).trim() + " (ID: " + m.memberID() + ")";
            }
            @Override
            public Member fromString(String s) { return null; }
        });

        cmbMembers.setPromptText("Select a member");

        // when a member is selected, show the popup
        cmbMembers.setOnAction(evt -> {
            Member selected = cmbMembers.getValue();
            if (selected != null) {
                showMemberPopup(selected);
            }
        });
    }

    private void showMemberPopup(Member m) {
        int memberId = m.memberID();

        // membership type from Member record
        String membershipType = m.membershipType() == null ? "N/A" : m.membershipType();

        // total paid
        double totalPaid = 0.0;
        // total attendance count
        int attendanceCount = 0;
        // classes by classType => count (preserve order by count desc using LinkedHashMap later)
        Map<String, Integer> classesByType = new LinkedHashMap<>();

        try (Connection conn = DBConnection.getConnection()) {
            // total paid
            try (PreparedStatement p = conn.prepareStatement(
                    "SELECT IFNULL(SUM(amount),0) FROM Payment WHERE memberID = ?")) {
                p.setInt(1, memberId);
                try (ResultSet rs = p.executeQuery()) {
                    if (rs.next()) totalPaid = rs.getDouble(1);
                }
            }

            // attendance total
            try (PreparedStatement p = conn.prepareStatement(
                    "SELECT COUNT(*) FROM Attendance WHERE memberID = ?")) {
                p.setInt(1, memberId);
                try (ResultSet rs = p.executeQuery()) {
                    if (rs.next()) attendanceCount = rs.getInt(1);
                }
            }

            // classes grouped by classType with counts (e.g., HIIT (2), Yoga (1))
            // exclude Attendance rows where classID IS NULL
            try (PreparedStatement p = conn.prepareStatement(
                    "SELECT c.classType AS type, COUNT(*) AS cnt " +
                            "FROM Attendance a JOIN Class c ON a.classID = c.classID " +
                            "WHERE a.memberID = ? AND a.classID IS NOT NULL " +
                            "GROUP BY c.classType " +
                            "ORDER BY cnt DESC")) {
                p.setInt(1, memberId);
                try (ResultSet rs = p.executeQuery()) {
                    while (rs.next()) {
                        String type = rs.getString("type");
                        int cnt = rs.getInt("cnt");
                        if (type == null) type = "Unknown";
                        classesByType.put(type, cnt);
                    }
                }
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
            Alert err = new Alert(Alert.AlertType.ERROR, "Failed to load member report details.");
            err.setHeaderText(null);
            err.showAndWait();
            return;
        }

        // build the message
        StringBuilder content = new StringBuilder();
        content.append("Member ID: ").append(memberId).append("\n");
        content.append("Name: ").append(
                (m.firstName() == null ? "" : m.firstName()) + " " + (m.lastName() == null ? "" : m.lastName())
        ).append("\n");
        content.append("Membership Type: ").append(membershipType).append("\n");
        content.append("Total Paid: ₱").append(String.format("%.2f", totalPaid)).append("\n");
        content.append("Attendance Count: ").append(attendanceCount).append("\n\n");

        content.append("Classes Taken (by type):\n");
        if (classesByType.isEmpty()) {
            content.append("  None\n");
        } else {
            for (Map.Entry<String,Integer> e : classesByType.entrySet()) {
                content.append("  ").append(e.getKey()).append(" (").append(e.getValue()).append(")\n");
            }
        }

        // Show in an information dialog (multi-line)
        Alert info = new Alert(Alert.AlertType.INFORMATION);
        info.setTitle("Member Report");
        info.setHeaderText((m.firstName()==null?"":m.firstName()) + " " + (m.lastName()==null?"":m.lastName()));
        info.setContentText(content.toString());
        // ensure dialog grows if content is long
        info.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        info.showAndWait();
    }

    @FXML
    private void handleBack(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxmls/MainMenu.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException ex) {
            ex.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Failed to return to Main Menu.").showAndWait();
        }
    }
}

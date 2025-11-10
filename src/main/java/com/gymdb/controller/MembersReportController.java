package com.gymdb.controller;

import com.gymdb.utils.DBConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.Node;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MembersReportController {

    @FXML private TableView<SummaryRow> tblSummary;
    @FXML private TableColumn<SummaryRow, Integer> colTotalMembers;
    @FXML private TableColumn<SummaryRow, String>  colPlanDist;
    @FXML private TableColumn<SummaryRow, String>  colTopServices;
    @FXML private TableColumn<SummaryRow, String>  colExpired;
    @FXML private Button btnBack;

    private final ObservableList<SummaryRow> rows = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // wire columns
        colTotalMembers.setCellValueFactory(new PropertyValueFactory<>("totalMembers"));
        colPlanDist.setCellValueFactory(new PropertyValueFactory<>("planDist"));
        colTopServices.setCellValueFactory(new PropertyValueFactory<>("topServices"));
        colExpired.setCellValueFactory(new PropertyValueFactory<>("expired"));

        // enable text wrap for long String columns
        setWrapCellFactory(colPlanDist);
        setWrapCellFactory(colTopServices);
        setWrapCellFactory(colExpired);

        tblSummary.setItems(rows);

        // load data now
        loadSummaryRow();
    }

    private void setWrapCellFactory(TableColumn<SummaryRow, String> col) {
        col.setCellFactory(tc -> {
            TableCell<SummaryRow, String> cell = new TableCell<>() {
                private final TextArea ta = new TextArea();
                {
                    ta.setWrapText(true);
                    ta.setEditable(false);
                    ta.setPrefRowCount(3);
                    ta.setPrefWidth(col.getPrefWidth());
                    ta.setStyle("-fx-background-color:transparent; -fx-control-inner-background: transparent;");
                }
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setGraphic(null);
                    } else {
                        ta.setText(item);
                        ta.setMinHeight(Region.USE_PREF_SIZE);
                        setGraphic(ta);
                    }
                }
            };
            return cell;
        });
    }

    private void loadSummaryRow() {
        int totalMembers = 0;
        String planDist = "";
        String topServices = "";
        String expired = "";

        try (Connection conn = DBConnection.getConnection()) {
            // total members
            try (PreparedStatement p = conn.prepareStatement("SELECT COUNT(*) FROM Member")) {
                ResultSet rs = p.executeQuery();
                if (rs.next()) totalMembers = rs.getInt(1);
            }

            // membership plan distribution: membership_type -> count
            try (PreparedStatement p = conn.prepareStatement(
                    "SELECT membership_type, COUNT(*) AS cnt FROM Member GROUP BY membership_type")) {
                ResultSet rs = p.executeQuery();
                List<String> parts = new ArrayList<>();
                while (rs.next()) {
                    String plan = rs.getString("membership_type");
                    int cnt = rs.getInt("cnt");
                    parts.add((plan == null ? "Unknown" : plan) + ": " + cnt);
                }
                planDist = String.join("\n", parts);
            }

            // top "services" chosen: we use Member.classID joined with Class.className
            // if Class table missing entries, those members will be skipped here
            try (PreparedStatement p = conn.prepareStatement(
                    "SELECT c.className AS name, COUNT(*) AS cnt " +
                            "FROM Member m JOIN Class c ON m.classID = c.classID " +
                            "GROUP BY c.className " +
                            "ORDER BY cnt DESC " +
                            "LIMIT 5")) {
                ResultSet rs = p.executeQuery();
                List<String> parts = new ArrayList<>();
                while (rs.next()) {
                    String name = rs.getString("name");
                    int cnt = rs.getInt("cnt");
                    parts.add((name == null ? "Unknown" : name) + " (" + cnt + ")");
                }
                topServices = parts.isEmpty() ? "No class selections recorded." : String.join("\n", parts);
            }

            // expired memberships: show count and up to 5 names (end_date < today)
            try (PreparedStatement p = conn.prepareStatement(
                    "SELECT memberID, first_name, last_name, end_date FROM Member WHERE end_date < CURDATE() ORDER BY end_date DESC")) {
                ResultSet rs = p.executeQuery();
                List<String> names = new ArrayList<>();
                int expiredCount = 0;
                while (rs.next()) {
                    expiredCount++;
                    if (names.size() < 5) {
                        String name = (rs.getString("first_name")==null? "" : rs.getString("first_name"))
                                + " " + (rs.getString("last_name")==null? "" : rs.getString("last_name"));
                        Date d = rs.getDate("end_date");
                        names.add(name.trim() + " (" + (d==null? "?" : d.toString()) + ")");
                    }
                }
                expired = "Count: " + expiredCount;
                if (!names.isEmpty()) expired += "\n" + String.join("\n", names);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            // fallback strings
            planDist = planDist.isEmpty() ? "N/A" : planDist;
            topServices = topServices.isEmpty() ? "N/A" : topServices;
            expired = expired.isEmpty() ? "N/A" : expired;
        }

        rows.clear();
        rows.add(new SummaryRow(totalMembers, planDist, topServices, expired));
    }

    @FXML
    private void handleBack(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/fxmls/MainMenu.fxml"));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }

    // simple data holder
    public static class SummaryRow {
        private final Integer totalMembers;
        private final String planDist;
        private final String topServices;
        private final String expired;
        public SummaryRow(Integer totalMembers, String planDist, String topServices, String expired) {
            this.totalMembers = totalMembers;
            this.planDist = planDist;
            this.topServices = topServices;
            this.expired = expired;
        }
        public Integer getTotalMembers() { return totalMembers; }
        public String getPlanDist() { return planDist; }
        public String getTopServices() { return topServices; }
        public String getExpired() { return expired; }
    }
}

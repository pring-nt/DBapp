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
import javafx.scene.paint.Color;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Combined table + chart controller for member activity reports.
 * Make sure the FXML has a TableView with fx:id="reportTable" and matching columns,
 * and (optionally) the BarChart + CategoryAxis+NumberAxis used here.
 */
public class MembersActivityReportController {

    // --- Table refs (make sure these fx:id values exist in FXML) ---
    @FXML private TableView<MemberActivityReport> reportTable;
    @FXML private TableColumn<MemberActivityReport, String> colMemberName;
    @FXML private TableColumn<MemberActivityReport, Integer> colSessions;
    @FXML private TableColumn<MemberActivityReport, String> colInitialWeight;
    @FXML private TableColumn<MemberActivityReport, String> colGoalWeight;
    @FXML private TableColumn<MemberActivityReport, String> colTargetChange;
    @FXML private TableColumn<MemberActivityReport, String> colBMIChange;
    @FXML private TableColumn<MemberActivityReport, String> colTrend;
    @FXML private TableColumn<MemberActivityReport, String> colHealthGoal;

    // --- Chart + optional combobox ---
    @FXML private BarChart<String, Number> attendanceBarChart;
    @FXML private CategoryAxis attendanceXAxis;
    @FXML private NumberAxis attendanceYAxis;
    @FXML private ComboBox<Member> cmbMemberReport; // optional: single-member popup behavior

    // CRUD helpers
    private final AttendanceCRUD attendanceCrud = new AttendanceCRUD();
    private final MemberCRUD memberCrud = new MemberCRUD();
    private final GymClassCRUD classCrud = new GymClassCRUD();

    private final ObservableList<MemberActivityReport> tableData = FXCollections.observableArrayList();


    @FXML
    public void initialize() {
        // inside initialize()
        if (attendanceXAxis != null) attendanceXAxis.setTickLabelFill(Color.WHITE);
        if (attendanceYAxis != null) attendanceYAxis.setTickLabelFill(Color.WHITE);

        // optional: make axis lines / tick marks more visible with a small delay
        Platform.runLater(() -> {
            // axis line and tick mark CSS via lookup (requires scene to be rendered)
            if (attendanceXAxis.getScene() != null) {
                var xLine = attendanceXAxis.lookup(".axis-line");
                if (xLine != null) xLine.setStyle("-fx-stroke: white;");
                var yLine = attendanceYAxis.lookup(".axis-line");
                if (yLine != null) yLine.setStyle("-fx-stroke: white;");
            }
        });
        // 1) wire table columns (only if present in FXML)
        if (colMemberName != null) {
            colMemberName.setCellValueFactory(c ->
                    new javafx.beans.property.ReadOnlyStringWrapper(
                            c.getValue().memberName == null ? ("ID:" + c.getValue().memberID) : c.getValue().memberName));
        }
        if (colSessions != null) {
            colSessions.setCellValueFactory(c ->
                    new javafx.beans.property.ReadOnlyObjectWrapper<>(c.getValue().sessionsAttended));
        }
        if (colInitialWeight != null) {
            colInitialWeight.setCellValueFactory(c ->
                    new javafx.beans.property.ReadOnlyStringWrapper(
                            c.getValue().initialWeight == null ? "" : String.format("%.1f", c.getValue().initialWeight)));
        }
        if (colGoalWeight != null) {
            colGoalWeight.setCellValueFactory(c ->
                    new javafx.beans.property.ReadOnlyStringWrapper(
                            c.getValue().goalWeight == null ? "" : String.format("%.1f", c.getValue().goalWeight)));
        }
        if (colTargetChange != null) {
            colTargetChange.setCellValueFactory(c ->
                    new javafx.beans.property.ReadOnlyStringWrapper(
                            c.getValue().targetChange == null ? "" : String.format("%.1f", c.getValue().targetChange)));
        }
        if (colBMIChange != null) {
            colBMIChange.setCellValueFactory(c ->
                    new javafx.beans.property.ReadOnlyStringWrapper(
                            c.getValue().bmiChange == null ? "" : String.format("%.2f", c.getValue().bmiChange)));
        }
        if (colTrend != null) {
            colTrend.setCellValueFactory(c ->
                    new javafx.beans.property.ReadOnlyStringWrapper(
                            c.getValue().bmiTrend == null ? "" : c.getValue().bmiTrend));
        }
        if (colHealthGoal != null) {
            colHealthGoal.setCellValueFactory(c ->
                    new javafx.beans.property.ReadOnlyStringWrapper(
                            c.getValue().healthGoal == null ? "" : c.getValue().healthGoal));
        }

        // attach list to table (if exists)
        if (reportTable != null) reportTable.setItems(tableData);

        // 2) populate member combobox (if present)
        if (cmbMemberReport != null) {
            List<Member> members = memberCrud.getAllRecords();
            cmbMemberReport.setItems(FXCollections.observableArrayList(members));
            cmbMemberReport.setConverter(new StringConverter<>() {
                @Override
                public String toString(Member m) {
                    if (m == null) return "";
                    String f = m.firstName() == null ? "" : m.firstName();
                    String l = m.lastName() == null ? "" : m.lastName();
                    return (f + " " + l).trim() + " (ID: " + m.memberID() + ")";
                }
                @Override
                public Member fromString(String string) { return null; }
            });

            cmbMemberReport.setOnAction(evt -> {
                Member sel = cmbMemberReport.getValue();
                if (sel != null) {
                    // show same popup info you requested previously
                    showMemberReport(sel);
                    cmbMemberReport.getSelectionModel().clearSelection();
                }
            });
        }

        // 3) initial load
        loadReportsIntoTableAndChart();
    }

    /**
     * Builds the table data and populates the BarChart.
     */
    private void loadReportsIntoTableAndChart() {
        // load all members and attendances
        List<Member> members = memberCrud.getAllRecords();
        List<Attendance> attendances = attendanceCrud.getAllRecords();

        // map memberID -> attendance count
        Map<Integer, Long> counts = attendances.stream()
                .collect(Collectors.groupingBy(Attendance::memberID, Collectors.counting()));

        // build a list of MemberActivityReport rows
        List<MemberActivityReport> rows = new ArrayList<>();
        for (Member m : members) {
            int mid = m.memberID();
            long sessions = counts.getOrDefault(mid, 0L);

            Double initial = m.initialWeight();
            Double goal = m.goalWeight();
            Double targetChange = null;
            if (initial != null && goal != null) targetChange = goal - initial;

            Double startBMI = m.startBMI();
            Double updatedBMI = m.updatedBMI();
            Double bmiChange = null;
            String bmiTrend = null;
            if (startBMI != null && updatedBMI != null) {
                bmiChange = updatedBMI - startBMI;
                bmiTrend = bmiChange < 0 ? "Down" : (bmiChange > 0 ? "Up" : "Stable");
            }

            String healthGoal = m.healthGoal();
            String name = ((m.firstName() == null ? "" : m.firstName()) + " " + (m.lastName() == null ? "" : m.lastName())).trim();

            rows.add(new MemberActivityReport(mid, name.isEmpty() ? "ID:" + mid : name,
                    (int) sessions,
                    initial, goal, targetChange, bmiChange, bmiTrend, healthGoal));
        }

        // update table UI on FX thread
        Platform.runLater(() -> {
            tableData.setAll(rows);

            // Update BarChart (top attendees)
            if (attendanceBarChart != null) {
                attendanceBarChart.getData().clear();
                XYChart.Series<String, Number> series = new XYChart.Series<>();
                series.setName("Sessions attended");

                rows.stream()
                        .sorted(Comparator.comparingInt((MemberActivityReport r) -> r.sessionsAttended).reversed())
                        .forEach(r -> {
                            XYChart.Data<String, Number> d = new XYChart.Data<>(r.memberName, r.sessionsAttended);
                            series.getData().add(d);
                        });

                attendanceBarChart.getData().add(series);

                // add tooltips once nodes are rendered
                Platform.runLater(() -> {
                    for (XYChart.Data<String, Number> d : series.getData()) {
                        if (d.getNode() != null) {
                            Tooltip.install(d.getNode(), new Tooltip(d.getXValue() + ": " + d.getYValue()));
                        }
                    }
                });
            }
        });
    }

    private void showMemberReport(Member m) {
        if (m == null) return;

        int memberId = m.memberID();

        // attendance count
        long attendanceCount = attendanceCrud.getAllRecords().stream()
                .filter(a -> a.memberID() == memberId).count();

        // classes taken grouped by classType
        Map<Integer, GymClass> classMap = classCrud.getAllRecords().stream()
                .collect(Collectors.toMap(GymClass::classID, gc -> gc, (a,b)->a));
        Map<String, Long> classCounts = attendanceCrud.getAllRecords().stream()
                .filter(a -> a.memberID() == memberId && a.classID() != 0)
                .map(a -> {
                    GymClass g = classMap.get(a.classID());
                    return g == null ? "Unassigned" : (g.classType() == null ? g.className() : g.classType());
                })
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(x -> x, Collectors.counting()));

        String classesStr = classCounts.isEmpty() ? "No class selections recorded." :
                classCounts.entrySet().stream()
                        .map(e -> e.getKey() + " (" + e.getValue() + ")")
                        .collect(Collectors.joining(", "));

        String memType = m.membershipType() == null ? "N/A" : m.membershipType();

        double totalPaid = 0.0;
        try {
            // PaymentCRUD does not have a helper; fallback to summing all payment rows for this member
            com.gymdb.model.PaymentCRUD paymentCrud = new com.gymdb.model.PaymentCRUD();
            totalPaid = paymentCrud.getAllRecords().stream()
                    .filter(p -> p.memberID() == memberId)
                    .mapToDouble(p -> p.amount())
                    .sum();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Member ID: ").append(memberId).append("\n");
        sb.append("Membership: ").append(memType).append("\n");
        sb.append("Total paid: ").append(String.format("%.2f", totalPaid)).append("\n");
        sb.append("Attendance count: ").append(attendanceCount).append("\n");
        sb.append("Classes taken: ").append(classesStr).append("\n");

        Alert info = new Alert(Alert.AlertType.INFORMATION);
        info.setTitle("Member Report: " + (m.firstName()==null ? "" : m.firstName()) + " " + (m.lastName()==null ? "" : m.lastName()));
        info.setHeaderText(null);
        info.setContentText(sb.toString());
        info.showAndWait();
    }

    @FXML
    private void handleBack(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/fxmls/CustomersDashboard.fxml"));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }

    // small DTO used for table rows
    private static final class MemberActivityReport {
        final int memberID;
        final String memberName;
        final int sessionsAttended;
        final Double initialWeight;
        final Double goalWeight;
        final Double targetChange;
        final Double bmiChange;
        final String bmiTrend;
        final String healthGoal;

        MemberActivityReport(int memberID, String memberName, int sessionsAttended,
                             Double initialWeight, Double goalWeight, Double targetChange,
                             Double bmiChange, String bmiTrend, String healthGoal) {
            this.memberID = memberID;
            this.memberName = memberName;
            this.sessionsAttended = sessionsAttended;
            this.initialWeight = initialWeight;
            this.goalWeight = goalWeight;
            this.targetChange = targetChange;
            this.bmiChange = bmiChange;
            this.bmiTrend = bmiTrend;
            this.healthGoal = healthGoal;
        }
    }
}

//package com.gymdb.controller;
//
//import com.gymdb.reports.MemberActivityReport;
//import com.gymdb.services.ReportService;
//import com.gymdb.services.AttendanceEventBus; // add this helper (see notes below)
//import javafx.application.Platform;
//import javafx.collections.FXCollections;
//import javafx.collections.ObservableList;
//import javafx.event.ActionEvent;
//import javafx.fxml.FXML;
//import javafx.fxml.FXMLLoader;
//import javafx.scene.Node;
//import javafx.scene.Parent;
//import javafx.scene.Scene;
//import javafx.scene.chart.BarChart;
//import javafx.scene.chart.CategoryAxis;
//import javafx.scene.chart.NumberAxis;
//import javafx.scene.chart.XYChart;
//import javafx.scene.control.*;
//import javafx.scene.control.cell.PropertyValueFactory;
//import javafx.stage.Stage;
//
//import java.io.IOException;
//import java.util.List;
//
//public class MembersActivityReportController {
//
//    // Table and columns
//    @FXML private TableView<MemberActivityReport> reportTable;
//    @FXML private TableColumn<MemberActivityReport, String> colMemberName;
//    @FXML private TableColumn<MemberActivityReport, Integer> colSessions;
//    @FXML private TableColumn<MemberActivityReport, String> colInitialWeight;
//    @FXML private TableColumn<MemberActivityReport, String> colGoalWeight;
//    @FXML private TableColumn<MemberActivityReport, String> colTargetChange;
//    @FXML private TableColumn<MemberActivityReport, String> colBMIChange;
//    @FXML private TableColumn<MemberActivityReport, String> colTrend;
//    @FXML private TableColumn<MemberActivityReport, String> colHealthGoal;
//
//    // Optional chart
//    @FXML private BarChart<String, Number> attendanceBarChart;
//    @FXML private CategoryAxis attendanceXAxis;
//    @FXML private NumberAxis attendanceYAxis;
//
//    private final ObservableList<MemberActivityReport> tableData = FXCollections.observableArrayList();
//
//    @FXML
//    public void initialize() {
//        // Configure table columns
//        if (colMemberName != null) {
//            colMemberName.setCellValueFactory(c -> new javafx.beans.property.ReadOnlyStringWrapper(c.getValue().memberName()));
//        }
//        if (colSessions != null) {
//            colSessions.setCellValueFactory(c -> new javafx.beans.property.ReadOnlyObjectWrapper<>(c.getValue().sessionsAttended()));
//        }
//        if (colInitialWeight != null) {
//            colInitialWeight.setCellValueFactory(c ->
//                    new javafx.beans.property.ReadOnlyStringWrapper(String.format("%.1f", c.getValue().initialWeight())));
//        }
//        if (colGoalWeight != null) {
//            colGoalWeight.setCellValueFactory(c ->
//                    new javafx.beans.property.ReadOnlyStringWrapper(String.format("%.1f", c.getValue().goalWeight())));
//        }
//        if (colTargetChange != null) {
//            colTargetChange.setCellValueFactory(c ->
//                    new javafx.beans.property.ReadOnlyStringWrapper(String.format("%.1f", c.getValue().targetWeightChange())));
//        }
//        if (colBMIChange != null) {
//            colBMIChange.setCellValueFactory(c ->
//                    new javafx.beans.property.ReadOnlyStringWrapper(String.format("%.2f", c.getValue().bmiChange())));
//        }
//        if (colTrend != null) {
//            colTrend.setCellValueFactory(c -> new javafx.beans.property.ReadOnlyStringWrapper(c.getValue().bmiTrend()));
//        }
//        if (colHealthGoal != null) {
//            colHealthGoal.setCellValueFactory(c -> new javafx.beans.property.ReadOnlyStringWrapper(
//                    c.getValue().healthGoal() == null ? "" : c.getValue().healthGoal()
//            ));
//        }
//
//        // attach list to table
//        if (reportTable != null) {
//            reportTable.setItems(tableData);
//        }
//
//        // initial load
//        loadReports();
//
//        // Listen for attendance changes so UI refreshes automatically
//        // AttendanceEventBus is a tiny helper (see notes).
//        try {
//            AttendanceEventBus.addListener((obs, oldVal, newVal) -> Platform.runLater(this::loadReports));
//        } catch (Throwable ignored) {
//            // If AttendanceEventBus isn't present yet, no problem — refresh can be manual.
//        }
//    }
//
//    private void loadReports() {
//        // fetch from DB using ReportService
//        List<MemberActivityReport> reports = ReportService.getMemberProgressReports();
//
//        Platform.runLater(() -> {
//            tableData.setAll(reports);
//
//            // refresh bar chart if present
//            if (attendanceBarChart != null) {
//                attendanceBarChart.getData().clear();
//                XYChart.Series<String, Number> series = new XYChart.Series<>();
//                series.setName("Sessions attended");
//
//                for (MemberActivityReport r : reports) {
//                    String name = r.memberName();
//                    // keep labels short if necessary
//                    String label = name == null ? "ID:" + r.memberID() : name;
//                    XYChart.Data<String, Number> d = new XYChart.Data<>(label, r.sessionsAttended());
//                    series.getData().add(d);
//                }
//                attendanceBarChart.getData().add(series);
//            }
//        });
//    }
//    @FXML
//    private void handleBack(ActionEvent event) throws IOException {
//        Parent root = FXMLLoader.load(getClass().getResource("/fxmls/CustomersDashboard.fxml"));
//        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
//        stage.setScene(new Scene(root));
//        stage.show();
//    }
//
//    // Optional: call this publicly from outside to force refresh
//    public void forceRefresh() {
//        loadReports();
//    }
//}

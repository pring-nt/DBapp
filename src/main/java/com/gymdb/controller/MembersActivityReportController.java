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
import javafx.scene.chart.*;
import javafx.scene.paint.Color;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import javafx.application.Platform;
import javafx.scene.chart.XYChart;

import javafx.animation.PauseTransition;
import javafx.scene.Node;
import javafx.util.Duration;
import java.util.Set;

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

    // add these near your other @FXML fields
    @FXML private LineChart<String, Number> bmiLineChart;
    @FXML private CategoryAxis bmiXAxis;
    @FXML private NumberAxis bmiYAxis;
    @FXML private PieChart planPieChart;

    // CRUD helpers
    private final AttendanceCRUD attendanceCrud = new AttendanceCRUD();
    private final MemberCRUD memberCrud = new MemberCRUD();
    private final GymClassCRUD classCrud = new GymClassCRUD();

    private final ObservableList<MemberActivityReport> tableData = FXCollections.observableArrayList();


    @FXML
    public void initialize() {
        // inside initialize()
        System.out.println("DEBUG: bmiChart=" + bmiLineChart);
        System.out.println("DEBUG: xAxis=" + bmiXAxis + ", yAxis=" + bmiYAxis);
        System.out.println("DEBUG: report rows size = " + tableData.size()); // if you have tableData
        installAxisStylingForBmi();
        if (planPieChart != null) {
            loadPlanDistribution();
        }
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
            if (bmiXAxis != null) {
                // tick labels (numbers/text along axis)
                bmiXAxis.setTickLabelFill(Color.WHITE);

                // axis title (the axis label node) — style it if present
                var xAxisLabelNode = bmiXAxis.lookup(".axis-label");
                if (xAxisLabelNode != null) xAxisLabelNode.setStyle("-fx-text-fill: white;");

                // axis line color
                var xLine = bmiXAxis.lookup(".axis-line");
                if (xLine != null) xLine.setStyle("-fx-stroke: white;");
            }

            if (bmiYAxis != null) {
                bmiYAxis.setTickLabelFill(Color.WHITE);

                var yAxisLabelNode = bmiYAxis.lookup(".axis-label");
                if (yAxisLabelNode != null) yAxisLabelNode.setStyle("-fx-text-fill: white;");

                var yLine = bmiYAxis.lookup(".axis-line");
                if (yLine != null) yLine.setStyle("-fx-stroke: white;");
            }

            // Optional: change chart title color if you use one
            if (bmiLineChart != null) {
                var title = bmiLineChart.lookup(".chart-title");
                if (title != null) title.setStyle("-fx-text-fill: white;");
            }
            if (bmiXAxis != null) {
                var xLine = bmiXAxis.lookup(".axis-line");
                if (xLine != null) xLine.setStyle("-fx-stroke: white;");
            }
            if (bmiYAxis != null) {
                var yLine = bmiYAxis.lookup(".axis-line");
                if (yLine != null) yLine.setStyle("-fx-stroke: white;");
            }
            var legend = planPieChart.lookup(".chart-legend");
            if (legend != null) legend.setStyle("-fx-text-fill: white;");

            // iterate slice nodes and set their label text color
            planPieChart.lookupAll(".chart-pie-label").forEach(node -> node.setStyle("-fx-fill: white;"));

            // optionally also set the legend label nodes explicitly
            planPieChart.lookupAll(".chart-legend .label").forEach(node -> node.setStyle("-fx-text-fill: white;"));

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

        // selection listener: draw BMI trend for selected row
        if (reportTable != null) {
            reportTable.getSelectionModel().selectedItemProperty().addListener((obs, oldRow, newRow) -> {
                if (newRow != null) drawBMITrend(newRow);
                else if (bmiLineChart != null) bmiLineChart.getData().clear();
            });
            Platform.runLater(this::stylePieLegend);

        }

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
    public void refreshAllCharts() {
        if (planPieChart != null) loadPlanDistribution();
        // if you later add other charts: loadTopClasses(); loadWeightTrend();
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

            rows.add(new MemberActivityReport(
                    mid,
                    name.isEmpty() ? "ID:" + mid : name,
                    (int) sessions,
                    initial,
                    goal,
                    targetChange,
                    startBMI,            // NEW
                    updatedBMI,          // NEW
                    bmiChange,
                    bmiTrend,
                    healthGoal));

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
    private void drawBMITrend(MemberActivityReport rpt) {
        if (bmiLineChart == null) return;

        bmiLineChart.getData().clear();

        // if we don't have numeric BMI values, show empty chart
        if (rpt.startBMI == null && rpt.updatedBMI == null) return;

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName(rpt.memberName);

        // Use two labeled categories: Start and Latest (or only the one that exists)
        if (rpt.startBMI != null) series.getData().add(new XYChart.Data<>("Start", rpt.startBMI));
        if (rpt.updatedBMI != null) series.getData().add(new XYChart.Data<>("Latest", rpt.updatedBMI));

        bmiLineChart.getData().add(series);

        // optional: tooltip on points
        Platform.runLater(() -> {
            for (XYChart.Data<String, Number> d : series.getData()) {
                if (d.getNode() != null) {
                    Tooltip.install(d.getNode(), new Tooltip(d.getXValue() + ": " + String.format("%.2f", d.getYValue().doubleValue())));
                }
            }
        });
    }
    // imports needed


    // Call this from initialize() (after any other setup)

    private void loadPlanDistribution() {
            try {
                List<Member> members = memberCrud.getAllRecords();
                Map<String, Long> counts = members.stream()
                        .collect(Collectors.groupingBy(
                                m -> m.membershipType() == null || m.membershipType().isBlank() ? "Unspecified" : m.membershipType(),
                                Collectors.counting()
                        ));

                ObservableList<PieChart.Data> data = FXCollections.observableArrayList();
                counts.forEach((plan, cnt) -> data.add(new PieChart.Data(plan + " (" + cnt + ")", cnt)));

                // update chart on FX thread
                Platform.runLater(() -> {
                    planPieChart.setData(data);
                    planPieChart.setLegendVisible(true);
                    planPieChart.setLabelsVisible(true);

                    // style pie labels & legend text using runtime-only approach
                    stylePieChartLabels();
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    if (planPieChart != null) planPieChart.setData(FXCollections.observableArrayList());
                });
            }
        }
    /** Robust runtime-only styling of PieChart labels and legend text to white. */
    /** Robust runtime-only styling of PieChart labels and legend text to white. */
    private void stylePieChartLabels() {
        if (planPieChart == null) return;

        // Helper to apply style to a Node
        java.util.function.Consumer<javafx.scene.Node> applyStyle = node -> {
            if (node == null) return;
            if (node instanceof javafx.scene.text.Text text) {
                text.setFill(javafx.scene.paint.Color.WHITE);
            } else {
                String s = node.getStyle();
                if (s == null) s = "";
                if (!s.contains("-fx-text-fill")) {
                    node.setStyle(s + (s.isEmpty() ? "" : ";") + "-fx-text-fill: white;");
                } else {
                    node.setStyle(s.replaceAll("-fx-text-fill:[^;]+;", "-fx-text-fill: white;"));
                }
            }
        };

        // Recursive walker using a Consumer<Node>
        java.util.function.Consumer<javafx.scene.Node> walk = new java.util.function.Consumer<>() {
            @Override
            public void accept(javafx.scene.Node n) {
                if (n == null) return;
                applyStyle.accept(n);
                if (n instanceof javafx.scene.Parent p) {
                    for (javafx.scene.Node child : p.getChildrenUnmodifiable()) {
                        accept(child); // recursive call
                    }
                }
            }
        };

        // Run styling after layout; do two attempts (immediate and short delayed) to catch late-created nodes
        Platform.runLater(() -> {
            planPieChart.lookupAll(".chart-legend .label")
                    .forEach(node -> node.setStyle("-fx-text-fill: black;"));
            try {
                planPieChart.lookupAll(".chart-pie-label").forEach(n -> walk.accept(n));
                planPieChart.lookupAll(".chart-legend .label").forEach(n -> walk.accept(n));
                planPieChart.lookupAll(".chart-legend-item").forEach(n -> walk.accept(n));
                // fallback: traverse whole chart subtree
                walk.accept(planPieChart);
            } catch (Exception ignored) {}

            // retry shortly after to catch any nodes created after initial skin/layout
            javafx.animation.PauseTransition retry = new javafx.animation.PauseTransition(javafx.util.Duration.millis(180));
            retry.setOnFinished(ev -> {
                try {
                    planPieChart.lookupAll(".chart-pie-label").forEach(n -> walk.accept(n));
                    planPieChart.lookupAll(".chart-legend .label").forEach(n -> walk.accept(n));
                    walk.accept(planPieChart);
                } catch (Exception ignored) {}
            });
            retry.play();
        });
    }
    private void stylePieLegend() {
        Runnable applyStyle = () -> {
            planPieChart.lookupAll(".chart-legend .label")
                    .forEach(node -> node.setStyle("-fx-text-fill: black;"));
        };

        // First pass
        Platform.runLater(applyStyle);

        // Second pass (catches labels created late, like "Monthly")
        Platform.runLater(() -> {
            PauseTransition delay = new PauseTransition(Duration.millis(200));
            delay.setOnFinished(e -> applyStyle.run());
            delay.play();
        });
    }







    private void installAxisStylingForBmi() {
        // ensure bmiLineChart reference exists
        if (bmiLineChart == null) return;

        // run when the chart is attached to a scene (safe moment to lookup CSS nodes)
        bmiLineChart.sceneProperty().addListener((obsScene, oldScene, newScene) -> {
            if (newScene == null) return;

            // schedule styling on next pulse (and an extra retry shortly after)
            Runnable styleRun = () -> {
                try {
                    // tick label colors
                    if (bmiXAxis != null) bmiXAxis.setTickLabelFill(Color.WHITE);
                    if (bmiYAxis != null) bmiYAxis.setTickLabelFill(Color.WHITE);

                    // axis label (title) nodes
                    if (bmiXAxis != null) {
                        Node xAxisLabel = bmiXAxis.lookup(".axis-label");
                        if (xAxisLabel != null) xAxisLabel.setStyle("-fx-text-fill: white;");
                    }
                    if (bmiYAxis != null) {
                        Node yAxisLabel = bmiYAxis.lookup(".axis-label");
                        if (yAxisLabel != null) yAxisLabel.setStyle("-fx-text-fill: white;");
                    }

                    // style any axis line nodes found under the chart
                    Set<Node> axisLines = bmiLineChart.lookupAll(".axis-line");
                    for (Node n : axisLines) {
                        n.setStyle("-fx-stroke: white;");
                    }

                    // optionally style tick marks and zero lines as well
                    Set<Node> tickMarks = bmiLineChart.lookupAll(".tick-mark");
                    for (Node n : tickMarks) n.setStyle("-fx-stroke: white;");

                    // chart title (if any)
                    Node title = bmiLineChart.lookup(".chart-title");
                    if (title != null) title.setStyle("-fx-text-fill: white;");

                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            };

            // run once on next pulse
            Platform.runLater(styleRun);

            // also run a second time after a short delay to catch late layout/skin changes
            PauseTransition retry = new PauseTransition(Duration.millis(200));
            retry.setOnFinished(e -> Platform.runLater(styleRun));
            retry.play();
        });

        // If already attached to a scene (chart created earlier), kick off immediately
        if (bmiLineChart.getScene() != null) {
            Platform.runLater(() -> {
                // same small style block (duplicate to avoid code repetition)
                Set<Node> axisLines = bmiLineChart.lookupAll(".axis-line");
                for (Node n : axisLines) n.setStyle("-fx-stroke: white;");
                if (bmiXAxis != null) bmiXAxis.setTickLabelFill(Color.WHITE);
                if (bmiYAxis != null) bmiYAxis.setTickLabelFill(Color.WHITE);
                Node xAxisLabel = bmiXAxis != null ? bmiXAxis.lookup(".axis-label") : null;
                if (xAxisLabel != null) xAxisLabel.setStyle("-fx-text-fill: white;");
                Node yAxisLabel = bmiYAxis != null ? bmiYAxis.lookup(".axis-label") : null;
                if (yAxisLabel != null) yAxisLabel.setStyle("-fx-text-fill: white;");
            });
        }
    }

    private void populateBmiChart(com.gymdb.reports.MemberActivityReport report) {
        if (bmiLineChart == null) {
            System.out.println("DEBUG: bmiLineChart is null - check fx:id in FXML");
            return;
        }
        // ensure axes tick labels are visible on your background
        if (bmiXAxis != null) bmiXAxis.setTickLabelFill(Color.WHITE); // change color as needed
        if (bmiYAxis != null) bmiYAxis.setTickLabelFill(Color.WHITE);

        // disable animations to avoid startup races (optional while debugging)
        bmiLineChart.setAnimated(false);

        // Create series (String x-axis, Number y-axis)
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        String memberName = report == null ? "Member" : (report.memberName() == null ? "Member" : report.memberName());
        series.setName(memberName + " BMI");

        // Use safe values (if startBMI or updatedBMI are 0 or missing, still show placeholders)
        Double startBMI = report == null ? null : report.startBMI();
        Double updatedBMI = report == null ? null : report.updatedBMI();

        if (startBMI == null && updatedBMI == null) {
            // nothing to plot — show placeholder point(s)
            series.getData().add(new XYChart.Data<>("Start", 0));
            series.getData().add(new XYChart.Data<>("Latest", 0));
        } else {
            // add both points (if one missing, use same value or 0)
            series.getData().add(new XYChart.Data<>("Start", startBMI == null ? (updatedBMI == null ? 0 : updatedBMI) : startBMI));
            series.getData().add(new XYChart.Data<>("Latest", updatedBMI == null ? (startBMI == null ? 0 : startBMI) : updatedBMI));
        }

        // Put on FX thread after layout
        Platform.runLater(() -> {
            bmiLineChart.getData().setAll(series);

            // add node tooltips (line nodes may not be available immediately; schedule again)
            Platform.runLater(() -> {
                for (XYChart.Data<String, Number> d : series.getData()) {
                    if (d.getNode() != null) {
                        Tooltip.install(d.getNode(), new Tooltip(d.getXValue() + " : " + d.getYValue()));
                        // style the data node so it becomes visible on image background
                        d.getNode().setStyle("-fx-background-color: white, white; -fx-background-radius: 4px; -fx-padding: 4px;");
                    }
                }
                // style the series line (optional)
                if (!series.getData().isEmpty() && series.getNode() != null) {
                    series.getNode().setStyle("-fx-stroke-width: 2px; -fx-stroke: #00FFCC;"); // change color if needed
                }
            });
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
    private void handleRetailSales(ActionEvent event) throws IOException {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxmls/RetailSalesReport.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleLockerUsage(ActionEvent event) throws IOException {
        // TODO: Load Locker Usage Report page
        System.out.println("Locker Usage clicked");
        // navigate(event, "/com/gymdb/reports/LockerUsage.fxml");
    }

    @FXML
    private void handlePerformanceReward(ActionEvent event) throws IOException {
        // TODO: Load Performance Reward Report page
        System.out.println("Performance Reward clicked");
        // navigate(event, "/com/gymdb/reports/PerformanceReward.fxml");
    }


    @FXML
    private void handleBack(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/fxmls/ReportsMenu.fxml"));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }

    // small DTO used for table rows (now includes startBMI and updatedBMI)
    private static final class MemberActivityReport {
        final int memberID;
        final String memberName;
        final int sessionsAttended;
        final Double initialWeight;
        final Double goalWeight;
        final Double targetChange;
        final Double startBMI;      // NEW
        final Double updatedBMI;    // NEW
        final Double bmiChange;
        final String bmiTrend;
        final String healthGoal;

        MemberActivityReport(int memberID, String memberName, int sessionsAttended,
                             Double initialWeight, Double goalWeight, Double targetChange,
                             Double startBMI, Double updatedBMI,
                             Double bmiChange, String bmiTrend, String healthGoal) {
            this.memberID = memberID;
            this.memberName = memberName;
            this.sessionsAttended = sessionsAttended;
            this.initialWeight = initialWeight;
            this.goalWeight = goalWeight;
            this.targetChange = targetChange;
            this.startBMI = startBMI;
            this.updatedBMI = updatedBMI;
            this.bmiChange = bmiChange;
            this.bmiTrend = bmiTrend;
            this.healthGoal = healthGoal;
        }
    }

}
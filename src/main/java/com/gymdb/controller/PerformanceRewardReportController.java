package com.gymdb.controller;

import com.gymdb.reports.PerformanceRewardReport;
import com.gymdb.services.ReportService;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class PerformanceRewardReportController {

    @FXML private TableView<PerformanceRewardReport> tblRewards;
    @FXML private TableColumn<PerformanceRewardReport, String> colMemberName;
    @FXML private TableColumn<PerformanceRewardReport, Integer> colTotalSessions;
    @FXML private TableColumn<PerformanceRewardReport, String> colMembershipType;
    @FXML private TableColumn<PerformanceRewardReport, String> colEndDate;
    @FXML private TableColumn<PerformanceRewardReport, String> colStatus;

    @FXML private ComboBox<String> cmbFilter;
    @FXML private Button btnRefresh;
    @FXML private Button btnBack;

    @FXML private PieChart pieQualificationChart;

    private final ObservableList<PerformanceRewardReport> data = FXCollections.observableArrayList();
    private final DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @FXML
    public void initialize() {
        // configure table columns
        colMemberName.setCellValueFactory(c ->
                new ReadOnlyStringWrapper(Objects.toString(c.getValue().memberName(), "Unknown")));

        colTotalSessions.setCellValueFactory(c ->
                new ReadOnlyObjectWrapper<>(c.getValue().totalSessions()));

        colMembershipType.setCellValueFactory(c ->
                new ReadOnlyStringWrapper(Objects.toString(c.getValue().membershipType(), "")));

        colEndDate.setCellValueFactory(c -> {
            var d = c.getValue().endDate();
            return new ReadOnlyStringWrapper(d == null ? "" : dateFmt.format(d));
        });

        colStatus.setCellValueFactory(c ->
                new ReadOnlyStringWrapper(Objects.toString(c.getValue().qualificationStatus(), "")));

        // attach data to table
        if (tblRewards != null) tblRewards.setItems(data);

        // setup filter combobox
        if (cmbFilter != null) {
            cmbFilter.getItems().addAll("All", "Qualified", "Not Qualified");
            cmbFilter.setValue("All"); // default to All
            cmbFilter.setOnAction(e -> applyFilter());
        }

        // wire buttons
        if (btnRefresh != null) btnRefresh.setOnAction(e -> loadData());
        if (btnBack != null) btnBack.setOnAction(this::handleBack);

        // Setup listeners to reapply white styles when JavaFX rebuilds the chart
        if (pieQualificationChart != null) {
            // when chart data list changes (pie skin rebuilds items)
            pieQualificationChart.getData().addListener((ListChangeListener<PieChart.Data>) change -> {
                Platform.runLater(() -> {
                    forceWhiteOnChart();
                    Platform.runLater(this::forceWhiteOnChart); // double-run to catch late repaint
                });
            });

            // when scene is attached (scene stylesheets or later CSS may affect nodes)
            pieQualificationChart.sceneProperty().addListener((obs, oldS, newS) -> {
                if (newS != null) {
                    // react to scene-level stylesheet additions/removals
                    newS.getStylesheets().addListener((ListChangeListener<String>) sc -> {
                        Platform.runLater(() -> {
                            forceWhiteOnChart();
                            Platform.runLater(this::forceWhiteOnChart);
                        });
                    });

                    Platform.runLater(() -> {
                        forceWhiteOnChart();
                        Platform.runLater(this::forceWhiteOnChart);
                    });
                }
            });
        }

        // initial load
        loadData();
    }

    private void loadData() {
        data.clear();
        List<PerformanceRewardReport> rows = ReportService.getPerformanceRewardReports();
        data.addAll(rows);

        // apply filter (updates table); then update pie using master data (all rows)
        applyFilter();
        updatePieChart(data);
    }

    private void applyFilter() {
        String filter = cmbFilter == null ? "All" : cmbFilter.getValue();
        if (filter == null || filter.equals("All")) {
            tblRewards.setItems(data);
        } else {
            ObservableList<PerformanceRewardReport> filtered = FXCollections.observableArrayList(
                    data.stream()
                            .filter(r -> {
                                if (filter.equals("Qualified")) return "Qualified".equalsIgnoreCase(r.qualificationStatus());
                                if (filter.equals("Not Qualified")) return "Not Qualified".equalsIgnoreCase(r.qualificationStatus());
                                return true;
                            })
                            .collect(Collectors.toList())
            );
            tblRewards.setItems(filtered);
        }
        // Also refresh pie to reflect totals from the master dataset (data)
        updatePieChart(data);
    }

    /**
     * Build and apply pie chart data (Qualified vs Not Qualified)
     * The method also installs tooltips and forces legend/slice label color to white for readability.
     */
    private void updatePieChart(List<PerformanceRewardReport> rows) {
        if (pieQualificationChart == null) return;

        long qualified = rows.stream()
                .filter(r -> "Qualified".equalsIgnoreCase(r.qualificationStatus()))
                .count();

        long total = rows.size();
        long notQualified = total - qualified;

        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
        pieData.add(new PieChart.Data("Qualified (" + qualified + ")", qualified));
        pieData.add(new PieChart.Data("Not Qualified (" + notQualified + ")", notQualified));

        Platform.runLater(() -> {
            pieQualificationChart.setData(pieData);
            pieQualificationChart.setLegendVisible(true);
            pieQualificationChart.setLabelsVisible(true);

            // small delay to allow internal nodes creation, then install tooltips and force white
            Platform.runLater(() -> {
                // install tooltips + click filtering
                for (PieChart.Data d : pieQualificationChart.getData()) {
                    if (d.getNode() != null) {
                        Tooltip.install(d.getNode(), new Tooltip(d.getName() + " — " + (int) d.getPieValue()));
                        d.getNode().setOnMouseClicked(ev -> {
                            if (d.getName().startsWith("Qualified")) {
                                cmbFilter.setValue("Qualified");
                            } else {
                                cmbFilter.setValue("Not Qualified");
                            }
                            applyFilter();
                        });
                    }
                }

                // ensure CSS/layout built and then force white styling (multiple attempts)
                pieQualificationChart.applyCss();
                pieQualificationChart.layout();

                // immediate force
                forceWhiteOnChart();

                // schedule repeated attempts to catch late skin/style rewrites
                Timeline reapply = new Timeline(
                        new KeyFrame(Duration.millis(50), e -> forceWhiteOnChart()),
                        new KeyFrame(Duration.millis(200), e -> forceWhiteOnChart()),
                        new KeyFrame(Duration.millis(500), e -> forceWhiteOnChart())
                );
                reapply.play();
            });
        });
    }

    /**
     * Walks likely nodes and forces text/label colors to white.
     * It attempts to cover Label, Text, and generic nodes by setting inline styles and Text.fill.
     */
    private void forceWhiteOnChart() {
        if (pieQualificationChart == null) return;

        // ensure CSS/layout done
        pieQualificationChart.applyCss();
        pieQualificationChart.layout();

        java.util.function.Consumer<javafx.scene.Node> makeWhite = new java.util.function.Consumer<javafx.scene.Node>() {
            @Override
            public void accept(javafx.scene.Node node) {
                if (node == null) return;
                try {
                    // Inline fallback style for generic nodes.
                    // Append !important so it is harder for later CSS rules to override.
                    String prev = node.getStyle();
                    String append = " -fx-text-fill: white !important; -fx-fill: white !important;";
                    if (prev == null || prev.isEmpty()) node.setStyle(append);
                    else if (!prev.contains("-fx-text-fill")) node.setStyle(prev + append);
                    else if (!prev.contains("!important")) node.setStyle(prev + append);

                    // Specific typed handling
                    if (node instanceof Label lbl) {
                        lbl.setStyle("-fx-text-fill: white !important;");
                    } else if (node instanceof Text txt) {
                        txt.setFill(Color.WHITE);
                    }

                    // Recurse into children if Parent
                    if (node instanceof Parent parent) {
                        for (javafx.scene.Node child : parent.getChildrenUnmodifiable()) {
                            accept(child);
                        }
                    }
                } catch (Exception ignored) {
                    // ignore actions on exotic nodes
                }
            }
        };

        // Target common selectors
        pieQualificationChart.lookupAll(".chart-legend-item").forEach(n -> makeWhite.accept(n));
        pieQualificationChart.lookupAll(".chart-pie-label").forEach(n -> makeWhite.accept(n));
        pieQualificationChart.lookupAll(".label").forEach(n -> makeWhite.accept(n));
        pieQualificationChart.lookupAll(".text").forEach(n -> makeWhite.accept(n));

        // Also process each slice node and its parent/siblings
        for (PieChart.Data d : pieQualificationChart.getData()) {
            javafx.scene.Node sliceNode = d.getNode();
            if (sliceNode != null) {
                makeWhite.accept(sliceNode);
                javafx.scene.Parent parent = sliceNode.getParent();
                if (parent != null) {
                    makeWhite.accept(parent);
                    for (javafx.scene.Node sibling : parent.getChildrenUnmodifiable()) {
                        makeWhite.accept(sibling);
                    }
                }
            }
        }

        // final traversal of the chart node tree
        makeWhite.accept(pieQualificationChart);
    }

    private void handleBack(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxmls/ReportsMenu.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException ex) {
            ex.printStackTrace();
            // fallback: nothing
        }
    }
}


//package com.gymdb.controller;
//
//import com.gymdb.reports.PerformanceRewardReport;
//import com.gymdb.services.ReportService;
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
//import javafx.beans.property.ReadOnlyObjectWrapper;
//import javafx.beans.property.ReadOnlyStringWrapper;
//
//import java.io.IOException;
//import java.time.format.DateTimeFormatter;
//import java.util.List;
//import java.util.Objects;
//import java.util.stream.Collectors;
//
//public class PerformanceRewardReportController {
//
//    @FXML private TableView<PerformanceRewardReport> tblRewards;
//    @FXML private TableColumn<PerformanceRewardReport, String> colMemberName;
//    @FXML private TableColumn<PerformanceRewardReport, Integer> colTotalSessions;
//    @FXML private TableColumn<PerformanceRewardReport, String> colMembershipType;
//    @FXML private TableColumn<PerformanceRewardReport, String> colEndDate;
//    @FXML private TableColumn<PerformanceRewardReport, String> colStatus;
//
//    @FXML private ComboBox<String> cmbFilter;
//    @FXML private Button btnRefresh;
//    @FXML private Button btnBack;
//
//    private final ObservableList<PerformanceRewardReport> data = FXCollections.observableArrayList();
//    private final DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
//
//    @FXML
//    public void initialize() {
//        // configure columns
//        colMemberName.setCellValueFactory(c ->
//                new ReadOnlyStringWrapper(
//                        Objects.toString(c.getValue().memberName(), "Unknown")));
//
//        colTotalSessions.setCellValueFactory(c ->
//                new ReadOnlyObjectWrapper<>(c.getValue().totalSessions()));
//
//        colMembershipType.setCellValueFactory(c ->
//                new ReadOnlyStringWrapper(Objects.toString(c.getValue().membershipType(), "")));
//
//        colEndDate.setCellValueFactory(c -> {
//            var d = c.getValue().endDate();
//            return new ReadOnlyStringWrapper(d == null ? "" : dateFmt.format(d));
//        });
//
//        colStatus.setCellValueFactory(c ->
//                new ReadOnlyStringWrapper(Objects.toString(c.getValue().qualificationStatus(), "")));
//
//        // attach data
//        if (tblRewards != null) tblRewards.setItems(data);
//
//        // filter combobox
//        cmbFilter.getItems().addAll("All", "Qualified", "Not Qualified");
//        cmbFilter.setValue("Qualified"); // default: show qualified
//        cmbFilter.setOnAction(e -> applyFilter());
//
//        // refresh button
//        btnRefresh.setOnAction(e -> loadData());
//
//        // back button
//        btnBack.setOnAction(this::handleBack);
//
//        // initial load
//        loadData();
//    }
//
//    private void loadData() {
//        data.clear();
//        List<PerformanceRewardReport> rows = ReportService.getPerformanceRewardReports();
//        data.addAll(rows);
//        applyFilter(); // apply the current filter immediately
//    }
//
//    private void applyFilter() {
//        String filter = cmbFilter.getValue();
//        if (filter == null || filter.equals("All")) {
//            tblRewards.setItems(data);
//            return;
//        }
//
//        ObservableList<PerformanceRewardReport> filtered = FXCollections.observableArrayList(
//                data.stream()
//                        .filter(r -> {
//                            if (filter.equals("Qualified")) return "Qualified".equalsIgnoreCase(r.qualificationStatus());
//                            if (filter.equals("Not Qualified")) return "Not Qualified".equalsIgnoreCase(r.qualificationStatus());
//                            return true;
//                        })
//                        .collect(Collectors.toList())
//        );
//        tblRewards.setItems(filtered);
//    }
//
//    private void handleBack(ActionEvent event) {
//        try {
//            Parent root = FXMLLoader.load(getClass().getResource("/fxmls/ReportsMenu.fxml"));
//            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
//            stage.setScene(new Scene(root));
//            stage.show();
//        } catch (IOException ex) {
//            ex.printStackTrace();
//            // fallback: do nothing
//        }
//    }
//}

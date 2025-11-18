package com.gymdb.controller;

import com.gymdb.reports.PerformanceRewardReport;
import com.gymdb.services.ReportService;
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

    private final ObservableList<PerformanceRewardReport> data = FXCollections.observableArrayList();
    private final DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @FXML
    public void initialize() {
        // configure columns
        colMemberName.setCellValueFactory(c ->
                new ReadOnlyStringWrapper(
                        Objects.toString(c.getValue().memberName(), "Unknown")));

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

        // attach data
        if (tblRewards != null) tblRewards.setItems(data);

        // filter combobox
        cmbFilter.getItems().addAll("All", "Qualified", "Not Qualified");
        cmbFilter.setValue("Qualified"); // default: show qualified
        cmbFilter.setOnAction(e -> applyFilter());

        // refresh button
        btnRefresh.setOnAction(e -> loadData());

        // back button
        btnBack.setOnAction(this::handleBack);

        // initial load
        loadData();
    }

    private void loadData() {
        data.clear();
        List<PerformanceRewardReport> rows = ReportService.getPerformanceRewardReports();
        data.addAll(rows);
        applyFilter(); // apply the current filter immediately
    }

    private void applyFilter() {
        String filter = cmbFilter.getValue();
        if (filter == null || filter.equals("All")) {
            tblRewards.setItems(data);
            return;
        }

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

    private void handleBack(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxmls/ReportsMenu.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException ex) {
            ex.printStackTrace();
            // fallback: do nothing
        }
    }
}

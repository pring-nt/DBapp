package com.gymdb.controller;

import com.gymdb.model.Locker;
import com.gymdb.model.LockerCRUD;
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
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class LockerAdminController {

    private final LockerCRUD lockerCRUD = new LockerCRUD();
    private final MemberCRUD memberCRUD = new MemberCRUD();

    @FXML
    private void handleLocker(ActionEvent event) {
        Button clicked = (Button) event.getSource();
        int lockerID;
        try {
            lockerID = Integer.parseInt(clicked.getId().replaceAll("[^0-9]", ""));
        } catch (NumberFormatException ex) {
            showAlert("Error", "Invalid locker id on button.");
            return;
        }
        showLockerPopup(lockerID);
    }

    private void showLockerPopup(int lockerID) {
        Locker locker = lockerCRUD.getRecord(lockerID);

        ObservableList<LockerDetails> list = FXCollections.observableArrayList();
        if (locker != null) {
            String memberName = findMemberNameByLocker(lockerID);
            String status = computeStatus(locker, memberName);
            list.add(new LockerDetails(
                    locker.lockerID(),
                    status,
                    locker.rentalStartDate(),
                    locker.rentalEndDate(),
                    memberName
            ));
        } else {
            list.add(new LockerDetails(lockerID, "N/A", null, null, ""));
        }

        Stage popupStage = new Stage();
        popupStage.setTitle("Locker Info - " + lockerID);
        popupStage.initModality(Modality.APPLICATION_MODAL);

        TableView<LockerDetails> table = new TableView<>(list);
        table.setEditable(true);

        TableColumn<LockerDetails, Integer> colID = new TableColumn<>("Locker ID");
        colID.setCellValueFactory(new PropertyValueFactory<>("lockerID"));
        colID.setPrefWidth(80);

        TableColumn<LockerDetails, String> colStatus = new TableColumn<>("Status");
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colStatus.setPrefWidth(120);

        TableColumn<LockerDetails, LocalDate> colStart = new TableColumn<>("Start Date");
        colStart.setCellValueFactory(new PropertyValueFactory<>("rentalStartDate"));
        colStart.setPrefWidth(120);

        TableColumn<LockerDetails, LocalDate> colEnd = new TableColumn<>("End Date");
        colEnd.setCellValueFactory(new PropertyValueFactory<>("rentalEndDate"));
        colEnd.setPrefWidth(120);

        TableColumn<LockerDetails, String> colMember = new TableColumn<>("Member");
        colMember.setCellValueFactory(new PropertyValueFactory<>("memberName"));
        colMember.setPrefWidth(160);

        TableColumn<LockerDetails, Void> colEdit = new TableColumn<>("Edit");
        colEdit.setPrefWidth(80);
        colEdit.setCellFactory(param -> new TableCell<>() {
            private final Button editBtn = new Button("Edit");

            {
                editBtn.setOnAction(e -> {
                    LockerDetails ld = getTableView().getItems().get(getIndex());
                    boolean saved = showEditDialogAndSave(ld, popupStage);
                    if (saved) {
                        // Reload locker & member info from DB and update the row
                        Locker reloaded = lockerCRUD.getRecord(ld.getLockerID());
                        String memberName = findMemberNameByLocker(ld.getLockerID());
                        String status = (reloaded == null) ? "N/A" : computeStatus(reloaded, memberName);

                        if (reloaded != null) {
                            int idx = list.indexOf(ld);
                            if (idx >= 0) {
                                list.set(idx, new LockerDetails(
                                        reloaded.lockerID(),
                                        status,
                                        reloaded.rentalStartDate(),
                                        reloaded.rentalEndDate(),
                                        memberName
                                ));
                            } else {
                                // in case not found, refresh whole list
                                list.clear();
                                list.add(new LockerDetails(
                                        reloaded.lockerID(),
                                        status,
                                        reloaded.rentalStartDate(),
                                        reloaded.rentalEndDate(),
                                        memberName
                                ));
                            }
                        } else {
                            // locker removed from DB -> mark as N/A
                            int idx = list.indexOf(ld);
                            if (idx >= 0) {
                                list.set(idx, new LockerDetails(ld.getLockerID(), "N/A", null, null, ""));
                            }
                        }
                        table.refresh();
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : editBtn);
            }
        });

        table.getColumns().addAll(colID, colStatus, colStart, colEnd, colMember, colEdit);

        VBox layout = new VBox(10, table);
        layout.setPrefSize(700, 250);

        Scene scene = new Scene(layout);
        popupStage.setScene(scene);
        popupStage.showAndWait();
    }

    /**
     * Compute a friendly status string using locker.status() and whether a member is assigned.
     */
    private String computeStatus(Locker locker, String memberName) {
        if (locker == null) return "N/A";
        // if locker.status explicitly says "occupied"/"available", use that (normalized)
        String st = locker.status();
        if (st != null) {
            if (st.equalsIgnoreCase("occupied")) return "Occupied";
            if (st.equalsIgnoreCase("available") || st.equalsIgnoreCase("vacant")) return "Vacant";
        }
        // otherwise infer from member assignment / rental dates
        if (memberName != null && !memberName.isBlank()) return "Occupied";
        if (locker.rentalStartDate() != null || locker.rentalEndDate() != null) return "Occupied";
        return "Vacant";
    }

    /**
     * Look up the member (first + last) who currently has this lockerID (if any).
     * Returns empty string if none.
     */
    private String findMemberNameByLocker(int lockerID) {
        try {
            List<Member> all = memberCRUD.getAllRecords();
            for (Member m : all) {
                if (m.lockerID() != null && m.lockerID().equals(lockerID)) {
                    String fn = m.firstName() == null ? "" : m.firstName();
                    String ln = m.lastName()  == null ? "" : m.lastName();
                    return (fn + " " + ln).trim() + " (ID: " + m.memberID() + ")";
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    private boolean showEditDialogAndSave(LockerDetails ld, Stage popupStage) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Edit Locker " + ld.getLockerID());

        ButtonType saveBtn = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveBtn, ButtonType.CANCEL);

        TextField statusField = new TextField(ld.getStatus());
        DatePicker startDatePicker = new DatePicker(ld.getRentalStartDate());
        DatePicker endDatePicker = new DatePicker(ld.getRentalEndDate());

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.addRow(0, new Label("Status:"), statusField);
        grid.addRow(1, new Label("Start Date:"), startDatePicker);
        grid.addRow(2, new Label("End Date:"), endDatePicker);

        dialog.getDialogPane().setContent(grid);

        Optional<ButtonType> result = dialog.showAndWait();

        if (result.isPresent() && result.get() == saveBtn) {
            ld.setStatus(statusField.getText());
            ld.setRentalStartDate(startDatePicker.getValue());
            ld.setRentalEndDate(endDatePicker.getValue());

            Locker l = new Locker(ld.getLockerID(), ld.getStatus(), ld.getRentalStartDate(), ld.getRentalEndDate());

            try {
                // Check if locker exists
                if (lockerCRUD.getRecord(l.lockerID()) != null) {
                    lockerCRUD.modRecord(l);  // UPDATE
                } else {
                    lockerCRUD.addRecord(l);  // INSERT
                }
                popupStage.close(); // auto-close
                return true;
            } catch (Exception ex) {
                showAlert("Save failed", "Could not save locker to database: " + ex.getMessage());
                return false;
            }
        }
        return false;
    }

    private void showAlert(String title, String message) {
        Alert a = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
        a.setTitle(title);
        a.setHeaderText(null);
        a.showAndWait();
    }

    public static class LockerDetails {
        private Integer lockerID;
        private String status;
        private LocalDate rentalStartDate;
        private LocalDate rentalEndDate;
        private String memberName;

        public LockerDetails(Integer lockerID, String status, LocalDate rentalStartDate, LocalDate rentalEndDate, String memberName) {
            this.lockerID = lockerID;
            this.status = status;
            this.rentalStartDate = rentalStartDate;
            this.rentalEndDate = rentalEndDate;
            this.memberName = memberName;
        }

        public Integer getLockerID() { return lockerID; }
        public String getStatus() { return status; }
        public LocalDate getRentalStartDate() { return rentalStartDate; }
        public LocalDate getRentalEndDate() { return rentalEndDate; }
        public String getMemberName() { return memberName; }

        public void setStatus(String status) { this.status = status; }
        public void setRentalStartDate(LocalDate rentalStartDate) { this.rentalStartDate = rentalStartDate; }
        public void setRentalEndDate(LocalDate rentalEndDate) { this.rentalEndDate = rentalEndDate; }
        public void setMemberName(String memberName) { this.memberName = memberName; }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof LockerDetails)) return false;
            LockerDetails that = (LockerDetails) o;
            return lockerID != null && lockerID.equals(that.lockerID);
        }

        @Override
        public int hashCode() {
            return lockerID != null ? lockerID.hashCode() : 0;
        }
    }

    @FXML
    private void handleBack(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/fxmls/MainMenu.fxml"));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }
}

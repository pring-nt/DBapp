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

public class LockerController {

    private final LockerCRUD lockerCRUD = new LockerCRUD();
    private final MemberCRUD memberCRUD = new MemberCRUD();

    @FXML
    private void handleLocker(ActionEvent event) {
        Button clicked = (Button) event.getSource();
        // Expect button id like "locker1", "btnLocker5" etc. Extract digits.
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

        // If locker record exists, check occupied-ness
        if (locker != null) {
            boolean occupied = isOccupied(locker);
            if (occupied) {
                // find member who currently has this locker (if any)
                Member occupant = findMemberByLocker(lockerID);
                String who = occupant == null ? "Unknown member" :
                        (occupant.firstName() == null ? "" : occupant.firstName()) + " " +
                                (occupant.lastName() == null ? "" : occupant.lastName()) + " (ID: " + occupant.memberID() + ")";

                Alert info = new Alert(Alert.AlertType.INFORMATION);
                info.setTitle("Locker occupied");
                info.setHeaderText(null);
                info.setContentText("Locker " + lockerID + " is already occupied.\nRented by: " + who +
                        "\nStart: " + (locker.rentalStartDate() == null ? "N/A" : locker.rentalStartDate()) +
                        "\nEnd: " + (locker.rentalEndDate() == null ? "N/A" : locker.rentalEndDate()));
                info.showAndWait();
                return;
            }
        }

        // Locker is available (or doesn't exist) -> ask if user wants to rent
        Alert confirmRent = new Alert(Alert.AlertType.CONFIRMATION, "Do you want to rent locker " + lockerID + "?", ButtonType.YES, ButtonType.NO);
        confirmRent.setHeaderText(null);
        Optional<ButtonType> ans = confirmRent.showAndWait();
        if (ans.isEmpty() || ans.get() != ButtonType.YES) return;

        // Show rent dialog (choose member + start/end)
        boolean saved = showRentDialogAndSave(lockerID);
        if (saved) {
            Alert ok = new Alert(Alert.AlertType.INFORMATION, "Locker " + lockerID + " successfully rented.");
            ok.setHeaderText(null);
            ok.showAndWait();
        }
    }

    /**
     * Returns true if locker appears occupied.
     * Heuristic: status == "occupied" OR rentalEndDate != null and rentalEndDate >= today
     */
    private boolean isOccupied(Locker locker) {
        if (locker == null) return false;
        String st = locker.status();
        if (st != null && st.equalsIgnoreCase("occupied")) return true;
        LocalDate end = locker.rentalEndDate();
        if (end != null) {
            return !end.isBefore(LocalDate.now()); // occupied if end >= today
        }
        return false;
    }

    private Member findMemberByLocker(int lockerID) {
        try {
            List<Member> all = memberCRUD.getAllRecords();
            for (Member m : all) {
                Integer lid = m.lockerID();
                if (lid != null && lid == lockerID) return m;
            }
        } catch (Exception e) {
            // ignore
        }
        return null;
    }

    /**
     * Shows a dialog for choosing member + start + end, then saves Locker and Member.
     * returns true if saved.
     */
    private boolean showRentDialogAndSave(int lockerID) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Rent Locker " + lockerID);

        ButtonType saveBtn = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveBtn, ButtonType.CANCEL);

        // Member list for selection
        List<Member> allMembers = memberCRUD.getAllRecords();
        ObservableList<Member> membersObs = FXCollections.observableArrayList(allMembers);
        ComboBox<Member> cmbMembers = new ComboBox<>(membersObs);
        cmbMembers.setPrefWidth(300);
        // show friendly member name
        cmbMembers.setConverter(new javafx.util.StringConverter<>() {
            @Override
            public String toString(Member m) {
                if (m == null) return "";
                String first = m.firstName() == null ? "" : m.firstName();
                String last = m.lastName() == null ? "" : m.lastName();
                return (first + " " + last).trim() + " (ID: " + m.memberID() + ")";
            }
            @Override
            public Member fromString(String string) { return null; }
        });

        DatePicker startPicker = new DatePicker(LocalDate.now());
        DatePicker endPicker = new DatePicker(LocalDate.now().plusMonths(1));

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.addRow(0, new Label("Member:"), cmbMembers);
        grid.addRow(1, new Label("Start date:"), startPicker);
        grid.addRow(2, new Label("End date:"), endPicker);

        dialog.getDialogPane().setContent(grid);
        // block until user interacts
        Optional<ButtonType> res = dialog.showAndWait();
        if (res.isPresent() && res.get() == saveBtn) {
            Member chosen = cmbMembers.getValue();
            LocalDate start = startPicker.getValue();
            LocalDate end = endPicker.getValue();

            if (chosen == null) {
                showAlert("Missing", "Please choose a member.");
                return false;
            }
            if (start == null || end == null) {
                showAlert("Missing", "Please set both start and end dates.");
                return false;
            }
            if (end.isBefore(start)) {
                showAlert("Invalid dates", "End date must be same or after start date.");
                return false;
            }

            // ensure chosen member doesn't already have a locker
            if (chosen.lockerID() != null && chosen.lockerID() > 0) {
                showAlert("Already has locker", "Selected member already has locker ID: " + chosen.lockerID());
                return false;
            }

            // Prepare Locker object to save
            Locker l = new Locker(lockerID, "occupied", start, end);

            try {
                // If locker exists -> update, else add
                if (lockerCRUD.getRecord(lockerID) != null) {
                    lockerCRUD.modRecord(l);
                } else {
                    lockerCRUD.addRecord(l);
                }

                // update member record to set lockerID
                Member original = memberCRUD.getRecord(chosen.memberID());
                if (original == null) {
                    showAlert("Error", "Selected member not found in DB.");
                    return false;
                }
                // create updated Member (copy fields, set lockerID)
                Member updated = new Member(
                        original.memberID(),
                        original.firstName(),
                        original.lastName(),
                        original.email(),
                        original.contactNo(),
                        original.membershipType(),
                        original.startDate(),
                        original.endDate(),
                        original.healthGoal(),
                        original.initialWeight(),
                        original.goalWeight(),
                        original.startBMI(),
                        original.updatedBMI(),
                        original.classID(),
                        original.trainerID(),
                        lockerID // set locker
                );
                boolean ok = memberCRUD.modRecord(updated);
                if (!ok) {
                    showAlert("DB Error", "Could not update member to assign locker.");
                    return false;
                }

                return true;
            } catch (Exception ex) {
                ex.printStackTrace();
                showAlert("Save failed", "Failed to save locker or update member: " + ex.getMessage());
                return false;
            }
        }

        return false;
    }

    // existing edit dialog flow retained (you had this earlier)
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
                    System.out.println("Updated locker " + l.lockerID() + " in DB.");
                } else {
                    lockerCRUD.addRecord(l);  // INSERT
                    System.out.println("Added locker " + l.lockerID() + " to DB.");
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
        Alert a = new Alert(Alert.AlertType.INFORMATION, message, ButtonType.OK);
        a.setTitle(title);
        a.setHeaderText(null);
        a.showAndWait();
    }

    public static class LockerDetails {
        private Integer lockerID;
        private String status;
        private LocalDate rentalStartDate;
        private LocalDate rentalEndDate;

        public LockerDetails(Integer lockerID, String status, LocalDate rentalStartDate, LocalDate rentalEndDate) {
            this.lockerID = lockerID;
            this.status = status;
            this.rentalStartDate = rentalStartDate;
            this.rentalEndDate = rentalEndDate;
        }

        public Integer getLockerID() { return lockerID; }
        public String getStatus() { return status; }
        public LocalDate getRentalStartDate() { return rentalStartDate; }
        public LocalDate getRentalEndDate() { return rentalEndDate; }

        public void setStatus(String status) { this.status = status; }
        public void setRentalStartDate(LocalDate rentalStartDate) { this.rentalStartDate = rentalStartDate; }
        public void setRentalEndDate(LocalDate rentalEndDate) { this.rentalEndDate = rentalEndDate; }

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
        Parent root = FXMLLoader.load(getClass().getResource("/fxmls/CustomersDashboard.fxml"));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }
}

//package com.gymdb.controller;

//
//import com.gymdb.model.Locker;
//import com.gymdb.model.LockerCRUD;
//import javafx.collections.FXCollections;
//import javafx.collections.ObservableList;
//import javafx.event.ActionEvent;
//import javafx.fxml.FXML;
//import javafx.fxml.FXMLLoader;
//import javafx.scene.Node;
//import javafx.scene.Parent;
//import javafx.scene.Scene;
//import javafx.scene.control.*;
//import javafx.scene.control.cell.PropertyValueFactory;
//import javafx.scene.layout.GridPane;
//import javafx.scene.layout.VBox;
//import javafx.stage.Modality;
//import javafx.stage.Stage;
//
//import java.io.IOException;
//import java.time.LocalDate;
//import java.util.Optional;
//
//public class LockerController {
//
//    private final LockerCRUD lockerCRUD = new LockerCRUD();
//
//    @FXML
//    private void handleLocker(ActionEvent event) {
//        Button clicked = (Button) event.getSource();
//        int lockerID = Integer.parseInt(clicked.getId().replaceAll("[^0-9]", ""));
//        showLockerPopup(lockerID);
//    }
//
//    private void showLockerPopup(int lockerID) {
//        Locker locker = lockerCRUD.getRecord(lockerID);
//
//        ObservableList<LockerDetails> list = FXCollections.observableArrayList();
//        if (locker != null) {
//            list.add(new LockerDetails(
//                    locker.lockerID(),
//                    locker.status(),
//                    locker.rentalStartDate(),
//                    locker.rentalEndDate()
//            ));
//        } else {
//            list.add(new LockerDetails(lockerID, "N/A", null, null));
//        }
//
//        Stage popupStage = new Stage();
//        popupStage.setTitle("Locker Info - " + lockerID);
//        popupStage.initModality(Modality.APPLICATION_MODAL);
//
//        TableView<LockerDetails> table = new TableView<>(list);
//        table.setEditable(true);
//
//        TableColumn<LockerDetails, Integer> colID = new TableColumn<>("Locker ID");
//        colID.setCellValueFactory(new PropertyValueFactory<>("lockerID"));
//        colID.setPrefWidth(80);
//
//        TableColumn<LockerDetails, String> colStatus = new TableColumn<>("Status");
//        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
//        colStatus.setPrefWidth(120);
//
//        TableColumn<LockerDetails, LocalDate> colStart = new TableColumn<>("Start Date");
//        colStart.setCellValueFactory(new PropertyValueFactory<>("rentalStartDate"));
//        colStart.setPrefWidth(120);
//
//        TableColumn<LockerDetails, LocalDate> colEnd = new TableColumn<>("End Date");
//        colEnd.setCellValueFactory(new PropertyValueFactory<>("rentalEndDate"));
//        colEnd.setPrefWidth(120);
//
//        TableColumn<LockerDetails, Void> colEdit = new TableColumn<>("Edit");
//        colEdit.setPrefWidth(80);
//        colEdit.setCellFactory(param -> new TableCell<>() {
//            private final Button editBtn = new Button("Edit");
//
//            {
//                editBtn.setOnAction(e -> {
//                    LockerDetails ld = getTableView().getItems().get(getIndex());
//                    boolean saved = showEditDialogAndSave(ld, popupStage);
//                    if (saved) {
//                        // Reload table from DB
//                        Locker reloaded = lockerCRUD.getRecord(ld.getLockerID());
//                        if (reloaded != null) {
//                            int idx = list.indexOf(ld);
//                            if (idx >= 0) {
//                                list.set(idx, new LockerDetails(
//                                        reloaded.lockerID(),
//                                        reloaded.status(),
//                                        reloaded.rentalStartDate(),
//                                        reloaded.rentalEndDate()
//                                ));
//                            }
//                        }
//                        table.refresh();
//                    }
//                });
//            }
//
//            @Override
//            protected void updateItem(Void item, boolean empty) {
//                super.updateItem(item, empty);
//                setGraphic(empty ? null : editBtn);
//            }
//        });
//
//        table.getColumns().addAll(colID, colStatus, colStart, colEnd, colEdit);
//
//        VBox layout = new VBox(10, table);
//        layout.setPrefSize(550, 250);
//
//        Scene scene = new Scene(layout);
//        popupStage.setScene(scene);
//        popupStage.showAndWait();
//    }
//
//    private boolean showEditDialogAndSave(LockerDetails ld, Stage popupStage) {
//        Dialog<ButtonType> dialog = new Dialog<>();
//        dialog.setTitle("Edit Locker " + ld.getLockerID());
//
//        ButtonType saveBtn = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
//        dialog.getDialogPane().getButtonTypes().addAll(saveBtn, ButtonType.CANCEL);
//
//        TextField statusField = new TextField(ld.getStatus());
//        DatePicker startDatePicker = new DatePicker(ld.getRentalStartDate());
//        DatePicker endDatePicker = new DatePicker(ld.getRentalEndDate());
//
//        GridPane grid = new GridPane();
//        grid.setHgap(10);
//        grid.setVgap(10);
//        grid.addRow(0, new Label("Status:"), statusField);
//        grid.addRow(1, new Label("Start Date:"), startDatePicker);
//        grid.addRow(2, new Label("End Date:"), endDatePicker);
//
//        dialog.getDialogPane().setContent(grid);
//
//        Optional<ButtonType> result = dialog.showAndWait();
//
//        if (result.isPresent() && result.get() == saveBtn) {
//            ld.setStatus(statusField.getText());
//            ld.setRentalStartDate(startDatePicker.getValue());
//            ld.setRentalEndDate(endDatePicker.getValue());
//
//            Locker l = new Locker(ld.getLockerID(), ld.getStatus(), ld.getRentalStartDate(), ld.getRentalEndDate());
//
//            try {
//                // Check if locker exists
//                if (lockerCRUD.getRecord(l.lockerID()) != null) {
//                    lockerCRUD.modRecord(l);  // UPDATE
//                    System.out.println("Updated locker " + l.lockerID() + " in DB.");
//                } else {
//                    lockerCRUD.addRecord(l);  // INSERT
//                    System.out.println("Added locker " + l.lockerID() + " to DB.");
//                }
//                popupStage.close(); // auto-close
//                return true;
//            } catch (Exception ex) {
//                showAlert("Save failed", "Could not save locker to database: " + ex.getMessage());
//                return false;
//            }
//        }
//        return false;
//    }
//
//
//    private void showAlert(String title, String message) {
//        Alert a = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
//        a.setTitle(title);
//        a.showAndWait();
//    }
//
//    public static class LockerDetails {
//        private Integer lockerID;
//        private String status;
//        private LocalDate rentalStartDate;
//        private LocalDate rentalEndDate;
//
//        public LockerDetails(Integer lockerID, String status, LocalDate rentalStartDate, LocalDate rentalEndDate) {
//            this.lockerID = lockerID;
//            this.status = status;
//            this.rentalStartDate = rentalStartDate;
//            this.rentalEndDate = rentalEndDate;
//        }
//
//        public Integer getLockerID() { return lockerID; }
//        public String getStatus() { return status; }
//        public LocalDate getRentalStartDate() { return rentalStartDate; }
//        public LocalDate getRentalEndDate() { return rentalEndDate; }
//
//        public void setStatus(String status) { this.status = status; }
//        public void setRentalStartDate(LocalDate rentalStartDate) { this.rentalStartDate = rentalStartDate; }
//        public void setRentalEndDate(LocalDate rentalEndDate) { this.rentalEndDate = rentalEndDate; }
//
//        @Override
//        public boolean equals(Object o) {
//            if (this == o) return true;
//            if (!(o instanceof LockerDetails)) return false;
//            LockerDetails that = (LockerDetails) o;
//            return lockerID != null && lockerID.equals(that.lockerID);
//        }
//
//        @Override
//        public int hashCode() {
//            return lockerID != null ? lockerID.hashCode() : 0;
//        }
//    }
//
//    @FXML
//    private void handleBack(ActionEvent event) throws IOException {
//        Parent root = FXMLLoader.load(getClass().getResource("/fxmls/MainMenu.fxml"));
//        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
//        stage.setScene(new Scene(root));
//        stage.show();
//    }
//}

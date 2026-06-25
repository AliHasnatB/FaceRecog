package com.backex.faceox.controller;

import com.backex.faceox.model.LeaveRecord;
import com.backex.faceox.service.ApiService;
import com.backex.faceox.service.SessionManager;
import com.fasterxml.jackson.databind.JsonNode;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;

import java.util.Optional;

public class LeavesViewController {

    @FXML private TableView<LeaveRecord>             leavesTable;
    @FXML private TableColumn<LeaveRecord, String>   colStudent, colFrom, colTo, colReason, colStatus;
    @FXML private TableColumn<LeaveRecord, Void>     colActions;
    @FXML private ComboBox<String>                   statusFilter;
    @FXML private Button                             applyLeaveBtn;
    @FXML private Label approvedCount, pendingCount, rejectedCount;

    private final ApiService api = new ApiService();
    private final ObservableList<LeaveRecord> allRows = FXCollections.observableArrayList();
    private FilteredList<LeaveRecord> filteredRows;

    @FXML
    public void initialize() {
        setupColumns();
        filteredRows = new FilteredList<>(allRows, l -> true);
        leavesTable.setItems(filteredRows);

        statusFilter.setItems(FXCollections.observableArrayList("All", "PENDING", "APPROVED", "REJECTED"));
        statusFilter.setValue("All");

        // Students can apply; teachers/admins can approve
        SessionManager s = SessionManager.get();
        applyLeaveBtn.setVisible(s.isStudent());
        applyLeaveBtn.setManaged(s.isStudent());

        loadLeaves();
    }

    private void setupColumns() {
        colStudent.setCellValueFactory(new PropertyValueFactory<>("studentName"));
        colFrom.setCellValueFactory(new PropertyValueFactory<>("fromDate"));
        colTo.setCellValueFactory(new PropertyValueFactory<>("toDate"));
        colReason.setCellValueFactory(new PropertyValueFactory<>("reason"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); return; }
                Label chip = new Label(item);
                chip.getStyleClass().addAll("chip", item.toLowerCase());
                setGraphic(chip); setText(null);
            }
        });

        colActions.setCellFactory(col -> new TableCell<>() {
            private final Button approveBtn = new Button("Approve");
            private final Button rejectBtn  = new Button("Reject");
            {
                approveBtn.getStyleClass().add("btn-success");
                approveBtn.setStyle("-fx-font-size: 11px; -fx-padding: 4 10;");
                rejectBtn.getStyleClass().add("btn-danger");
                rejectBtn.setStyle("-fx-font-size: 11px; -fx-padding: 4 10;");

                approveBtn.setOnAction(e -> reviewLeave(getTableView().getItems().get(getIndex()), "APPROVED"));
                rejectBtn.setOnAction(e  -> reviewLeave(getTableView().getItems().get(getIndex()), "REJECTED"));
            }

            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setGraphic(null); return; }
                LeaveRecord leave = getTableView().getItems().get(getIndex());
                boolean canReview = (SessionManager.get().isAdmin() || SessionManager.get().isTeacher())
                        && "PENDING".equals(leave.getStatus());
                if (canReview) {
                    setGraphic(new HBox(6, approveBtn, rejectBtn));
                } else {
                    setGraphic(null);
                }
            }
        });
    }

    private void loadLeaves() {
        SessionManager s = SessionManager.get();
        var future = s.isStudent() && s.getProfileId() != null
                ? api.getLeavesByStudent(s.getProfileId())
                : api.getAllLeaves();

        future.thenAccept(json -> Platform.runLater(() -> {
            allRows.clear();
            int approved = 0, pending = 0, rejected = 0;
            if (json.isArray()) {
                for (JsonNode n : json) {
                    String status = n.path("status").asText("PENDING");
                    allRows.add(new LeaveRecord(
                            n.path("id").asLong(),
                            n.path("student").path("fullName").asText("—"),
                            n.path("fromDate").asText(),
                            n.path("toDate").asText(),
                            n.path("reason").asText(),
                            status
                    ));
                    if ("APPROVED".equals(status)) approved++;
                    else if ("PENDING".equals(status)) pending++;
                    else if ("REJECTED".equals(status)) rejected++;
                }
            }
            approvedCount.setText(String.valueOf(approved));
            pendingCount.setText(String.valueOf(pending));
            rejectedCount.setText(String.valueOf(rejected));
        }));
    }

    @FXML
    public void handleStatusFilter() {
        String filter = statusFilter.getValue();
        filteredRows.setPredicate(l -> "All".equals(filter) || l.getStatus().equals(filter));
    }

    @FXML
    public void showApplyDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Apply for Leave");
        dialog.setHeaderText("Submit a leave application");

        GridPane grid = new GridPane();
        grid.setHgap(12); grid.setVgap(10);
        grid.setStyle("-fx-padding: 20;");

        DatePicker fromDate = new DatePicker();
        DatePicker toDate   = new DatePicker();
        TextArea   reason   = new TextArea();
        reason.setPromptText("Reason for leave…");
        reason.setPrefRowCount(3);
        reason.setMaxWidth(250);
        reason.getStyleClass().add("text-area");

        grid.add(lbl("From Date"), 0, 0); grid.add(fromDate, 1, 0);
        grid.add(lbl("To Date"),   0, 1); grid.add(toDate,   1, 1);
        grid.add(lbl("Reason"),    0, 2); grid.add(reason,   1, 2);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.getDialogPane().getStylesheets().add(
                getClass().getResource("/styles/main.css").toExternalForm());

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            SessionManager s = SessionManager.get();
            api.applyLeave(
                    s.getProfileId(),
                    fromDate.getValue().toString(),
                    toDate.getValue().toString(),
                    reason.getText()
            ).thenAccept(j -> Platform.runLater(() -> {
                if (j != null && j.has("id")) {
                    loadLeaves(); info("Leave application submitted.");
                }else {
                    String serverMessage = j != null ? j.path("message").asText("Invalid Request data.") : "Unkown error";
                    error("Application Rejected: " + serverMessage);
                }
            })).exceptionally(ex -> {
                Platform.runLater(() -> error("Could not connect to server. Please check your internet connection."));
                return null;
            });
        }
    }

    private void reviewLeave(LeaveRecord leave, String status) {
        api.reviewLeave(leave.getId(), status).thenAccept(j -> Platform.runLater(this::loadLeaves));
    }

    private Label lbl(String t) { Label l = new Label(t); l.getStyleClass().add("form-label"); return l; }
    private void info(String msg)  { new Alert(Alert.AlertType.INFORMATION, msg).showAndWait(); }
    private void error(String msg) { new Alert(Alert.AlertType.ERROR, msg).showAndWait(); }
}

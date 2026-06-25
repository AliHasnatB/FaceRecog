package com.backex.faceox.controller;

import com.backex.faceox.model.AttendanceRecord;
import com.backex.faceox.service.ApiService;
import com.backex.faceox.service.SessionManager;
import com.fasterxml.jackson.databind.JsonNode;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.time.LocalDate;

public class HomeController {

    @FXML private Label welcomeLabel;
    @FXML private Label totalStudentsLabel;
    @FXML private Label presentTodayLabel;
    @FXML private Label pendingLeavesLabel;
    @FXML private Label activeClassesLabel;
    @FXML private Label attendanceDateLabel;
    @FXML private Label attendancePctLabel;
    @FXML private Label presentDaysLabel;
    @FXML private Label absentDaysLabel;
    @FXML private Label leaveDaysLabel;
    @FXML private Label totalFeesLabel;
    @FXML private Label paidFeesLabel;
    @FXML private Label pendingFeesLabel;

    @FXML private TableView<AttendanceRecord>      todayTable;
    @FXML private TableColumn<AttendanceRecord, String>  colName;
    @FXML private TableColumn<AttendanceRecord, String>  colSubject;
    @FXML private TableColumn<AttendanceRecord, String>  colTime;
    @FXML private TableColumn<AttendanceRecord, Double>  colSim;
    @FXML private TableColumn<AttendanceRecord, String>  colStatus;

    private final ApiService api = new ApiService();

    @FXML
    public void initialize() {
        SessionManager s = SessionManager.get();
        String hour = java.time.LocalTime.now().getHour() < 12 ? "morning"
                    : java.time.LocalTime.now().getHour() < 17 ? "afternoon" : "evening";
        welcomeLabel.setText("Good " + hour + ", " + s.getFullName() + "!");
        attendanceDateLabel.setText(LocalDate.now().toString());

        setupTable();
        loadAll();
    }

    private void setupTable() {
        colName.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        colSubject.setCellValueFactory(new PropertyValueFactory<>("subjectName"));
        colTime.setCellValueFactory(new PropertyValueFactory<>("time"));
        colSim.setCellValueFactory(new PropertyValueFactory<>("similarity"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        // Status chip styling
        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); setText(null); return; }
                Label chip = new Label(item);
                chip.getStyleClass().addAll("chip", item.toLowerCase());
                setGraphic(chip); setText(null);
            }
        });

        // Similarity as percentage
        colSim.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); return; }
                setText(String.format("%.1f%%", item * 100));
            }
        });
    }

    private void loadAll() {
        SessionManager s = SessionManager.get();

        // Today's attendance
        api.getTodayAttendance().thenAccept(json -> Platform.runLater(() -> {
            ObservableList<AttendanceRecord> rows = FXCollections.observableArrayList();
            if (json.isArray()) {
                json.forEach(n -> rows.add(new AttendanceRecord(
                        n.path("fullName").asText(),
                        n.path("subjectName").asText(),
                        n.path("similarity").asDouble(),
                        n.path("attendanceDate").asText(),
                        n.path("markedAt").asText("").substring(11, 19),
                        n.path("status").asText("PRESENT")
                )));
            }
            todayTable.setItems(rows);
            presentTodayLabel.setText(String.valueOf(rows.size()));
        })).exceptionally(e -> { Platform.runLater(() -> presentTodayLabel.setText("—")); return null; });

        // Students count
        api.getAllStudents().thenAccept(json -> Platform.runLater(() ->
                totalStudentsLabel.setText(json.isArray() ? String.valueOf(json.size()) : "—")
        )).exceptionally(e -> { Platform.runLater(() -> totalStudentsLabel.setText("—")); return null; });

        // Pending leaves
        api.getPendingLeaves().thenAccept(json -> Platform.runLater(() ->
                pendingLeavesLabel.setText(json.isArray() ? String.valueOf(json.size()) : "—")
        )).exceptionally(e -> { Platform.runLater(() -> pendingLeavesLabel.setText("—")); return null; });

        // Classes
        api.getAllClasses().thenAccept(json -> Platform.runLater(() ->
                activeClassesLabel.setText(json.isArray() ? String.valueOf(json.size()) : "—")
        )).exceptionally(e -> { Platform.runLater(() -> activeClassesLabel.setText("—")); return null; });

        // Student-specific stats
        if (s.isStudent() && s.getSubjectName() != null) {
            loadStudentStats(s);
        } else {
            attendancePctLabel.setText("N/A");
        }
    }

    private void loadStudentStats(SessionManager s) {
        api.getAttendanceStats(s.getSubjectName()).thenAccept(json -> Platform.runLater(() -> {
            long present = json.path("presentDays").asLong(0);
            presentDaysLabel.setText(String.valueOf(present));
            absentDaysLabel.setText("—");
            attendancePctLabel.setText(present > 0 ? present + " days" : "0");
        })).exceptionally(e -> null);

        if (s.getProfileId() != null) {
            api.getLeavesByStudent(s.getProfileId()).thenAccept(json -> Platform.runLater(() ->
                    leaveDaysLabel.setText(json.isArray() ? String.valueOf(json.size()) : "0")
            )).exceptionally(e -> null);

            api.getFeeSummary(s.getProfileId()).thenAccept(json -> Platform.runLater(() -> {
                totalFeesLabel.setText(String.format("PKR %.0f", json.path("totalFees").asDouble()));
                paidFeesLabel.setText(String.format("PKR %.0f",  json.path("paidFees").asDouble()));
                pendingFeesLabel.setText(String.format("PKR %.0f", json.path("pendingFees").asDouble()));
            })).exceptionally(e -> null);
        }
    }
}

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

public class AttendanceViewController {

    @FXML private TableView<AttendanceRecord>           attendanceTable;
    @FXML private TableColumn<AttendanceRecord, String> colFull, colSubj, colDate, colTime, colStatus;
    @FXML private TableColumn<AttendanceRecord, Double> colMatch;
    @FXML private DatePicker datePicker;
    @FXML private Label      summaryLabel;
    @FXML private Button     exportBtn;

    private final ApiService api = new ApiService();
    private final ObservableList<AttendanceRecord> rows = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupColumns();
        attendanceTable.setItems(rows);
        loadAll();
    }

    private void setupColumns() {
        colFull.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        colSubj.setCellValueFactory(new PropertyValueFactory<>("subjectName"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        colTime.setCellValueFactory(new PropertyValueFactory<>("time"));
        colMatch.setCellValueFactory(new PropertyValueFactory<>("similarity"));
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

        colMatch.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : String.format("%.1f%%", item * 100));
            }
        });
    }

    @FXML
    public void loadAll() {
        SessionManager s = SessionManager.get();
        if (s.isStudent() && s.getSubjectName() != null) {
            api.getStudentAttendance(s.getSubjectName()).thenAccept(this::populate);
        } else {
            api.getTodayAttendance().thenAccept(this::populate);
        }
    }

    @FXML
    public void handleFilter() {
        LocalDate date = datePicker.getValue();
        if (date == null) { loadAll(); return; }
        // Use the range API for a single day
        api.getAttendanceRange(date.toString(), date.toString()).thenAccept(this::populate);
    }

    @FXML
    public void handleExport() {
        // Build CSV in memory and show save dialog
        StringBuilder sb = new StringBuilder("Full Name,Subject,Date,Time,Match %,Status\n");
        for (AttendanceRecord r : rows) {
            sb.append(String.format("%s,%s,%s,%s,%.1f%%,%s\n",
                    r.getFullName(), r.getSubjectName(), r.getDate(),
                    r.getTime(), r.getSimilarity() * 100, r.getStatus()));
        }
        javafx.stage.FileChooser fc = new javafx.stage.FileChooser();
        fc.setTitle("Save CSV");
        fc.getExtensionFilters().add(new javafx.stage.FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        fc.setInitialFileName("attendance_" + LocalDate.now() + ".csv");
        java.io.File file = fc.showSaveDialog(exportBtn.getScene().getWindow());
        if (file != null) {
            try (java.io.FileWriter fw = new java.io.FileWriter(file)) {
                fw.write(sb.toString());
            } catch (Exception e) {
                new Alert(Alert.AlertType.ERROR, "Export failed: " + e.getMessage()).showAndWait();
            }
        }
    }

    private void populate(JsonNode json) {
        Platform.runLater(() -> {
            rows.clear();
            if (json.isArray()) {
                json.forEach(n -> {
                    String markedAt = n.path("markedAt").asText("");
                    String time = markedAt.length() >= 19 ? markedAt.substring(11, 19) : markedAt;
                    rows.add(new AttendanceRecord(
                            n.path("fullName").asText(),
                            n.path("subjectName").asText(),
                            n.path("similarity").asDouble(),
                            n.path("attendanceDate").asText(),
                            time,
                            n.path("status").asText("PRESENT")
                    ));
                });
            }
            summaryLabel.setText("Showing " + rows.size() + " record(s)");
        });
    }
}

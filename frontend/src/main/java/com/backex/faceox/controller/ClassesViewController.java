package com.backex.faceox.controller;

import com.backex.faceox.model.ClassRecord;
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

import java.util.Optional;

public class ClassesViewController {

    @FXML private TableView<ClassRecord>              classesTable;
    @FXML private TableColumn<ClassRecord, String>    colSubjectName, colSubjectCode, colTeacher,
                                                       colDept, colDay, colStart, colEnd, colRoom, colSem, colActive;
    @FXML private ComboBox<String> dayFilter;
    @FXML private Button           addClassBtn;

    private final ApiService api = new ApiService();
    private final ObservableList<ClassRecord> allRows = FXCollections.observableArrayList();
    private FilteredList<ClassRecord> filteredRows;

    @FXML
    public void initialize() {
        setupColumns();
        filteredRows = new FilteredList<>(allRows, c -> true);
        classesTable.setItems(filteredRows);

        dayFilter.setItems(FXCollections.observableArrayList(
                "All", "MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY"));
        dayFilter.setValue("All");

        boolean canAdd = SessionManager.get().isAdmin() || SessionManager.get().isTeacher();
        addClassBtn.setVisible(canAdd);
        addClassBtn.setManaged(canAdd);

        loadClasses();
    }

    private void setupColumns() {
        colSubjectName.setCellValueFactory(new PropertyValueFactory<>("subjectName"));
        colSubjectCode.setCellValueFactory(new PropertyValueFactory<>("subjectCode"));
        colTeacher.setCellValueFactory(new PropertyValueFactory<>("teacherName"));
        colDept.setCellValueFactory(new PropertyValueFactory<>("department"));
        colDay.setCellValueFactory(new PropertyValueFactory<>("dayOfWeek"));
        colStart.setCellValueFactory(new PropertyValueFactory<>("startTime"));
        colEnd.setCellValueFactory(new PropertyValueFactory<>("endTime"));
        colRoom.setCellValueFactory(new PropertyValueFactory<>("room"));
        colSem.setCellValueFactory(new PropertyValueFactory<>("semester"));
        colActive.setCellValueFactory(new PropertyValueFactory<>("semester")); // placeholder
    }

    private void loadClasses() {
        SessionManager s = SessionManager.get();
        var future = s.isTeacher() && s.getProfileId() != null
                ? api.getClassesByTeacher(s.getProfileId())
                : api.getAllClasses();

        future.thenAccept(json -> Platform.runLater(() -> {
            allRows.clear();
            if (json.isArray()) {
                json.forEach(n -> allRows.add(new ClassRecord(
                        n.path("id").asLong(),
                        n.path("subjectName").asText(),
                        n.path("subjectCode").asText(),
                        n.path("teacher").path("fullName").asText("—"),
                        n.path("department").path("name").asText("—"),
                        n.path("dayOfWeek").asText(),
                        n.path("startTime").asText(),
                        n.path("endTime").asText(),
                        n.path("roomNumber").asText("—"),
                        n.path("semester").asText("—")
                )));
            }
        }));
    }

    @FXML
    public void handleDayFilter() {
        String day = dayFilter.getValue();
        filteredRows.setPredicate(c -> "All".equals(day) || c.getDayOfWeek().equals(day));
    }

    @FXML
    public void showCreateDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Create Class Schedule");
        dialog.setHeaderText("New class schedule");

        GridPane grid = new GridPane();
        grid.setHgap(12); grid.setVgap(10);
        grid.setStyle("-fx-padding: 20;");

        TextField tfSubjectName = field("Subject Name");
        TextField tfSubjectCode = field("Subject Code");
        TextField tfTeacherId   = field("Teacher ID");
        TextField tfDeptId      = field("Department ID");
        ComboBox<String> cbDay  = new ComboBox<>(FXCollections.observableArrayList(
                "MONDAY","TUESDAY","WEDNESDAY","THURSDAY","FRIDAY","SATURDAY"));
        cbDay.setPromptText("Day of Week");
        cbDay.getStyleClass().add("combo-box");
        TextField tfStart  = field("Start Time (HH:mm)");
        TextField tfEnd    = field("End Time (HH:mm)");
        TextField tfRoom   = field("Room Number");
        TextField tfSem    = field("Semester");

        int r = 0;
        grid.add(lbl("Subject Name"), 0, r); grid.add(tfSubjectName, 1, r++);
        grid.add(lbl("Subject Code"), 0, r); grid.add(tfSubjectCode, 1, r++);
        grid.add(lbl("Teacher ID"),   0, r); grid.add(tfTeacherId,   1, r++);
        grid.add(lbl("Department ID"),0, r); grid.add(tfDeptId,      1, r++);
        grid.add(lbl("Day"),          0, r); grid.add(cbDay,         1, r++);
        grid.add(lbl("Start Time"),   0, r); grid.add(tfStart,       1, r++);
        grid.add(lbl("End Time"),     0, r); grid.add(tfEnd,         1, r++);
        grid.add(lbl("Room"),         0, r); grid.add(tfRoom,        1, r++);
        grid.add(lbl("Semester"),     0, r); grid.add(tfSem,         1, r);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.getDialogPane().getStylesheets().add(
                getClass().getResource("/styles/main.css").toExternalForm());

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            String json = String.format(
                    "{\"subjectName\":\"%s\",\"subjectCode\":\"%s\"," +
                    "\"teacherId\":%s,\"departmentId\":%s," +
                    "\"dayOfWeek\":\"%s\",\"startTime\":\"%s\",\"endTime\":\"%s\"," +
                    "\"roomNumber\":\"%s\",\"semester\":\"%s\"}",
                    tfSubjectName.getText(), tfSubjectCode.getText(),
                    tfTeacherId.getText(), tfDeptId.getText(),
                    cbDay.getValue(), tfStart.getText(), tfEnd.getText(),
                    tfRoom.getText(), tfSem.getText()
            );
            api.createClass(json).thenAccept(j -> Platform.runLater(() -> {
                if (j.has("id")) loadClasses();
                else new Alert(Alert.AlertType.ERROR, j.path("message").asText("Failed")).showAndWait();
            }));
        }
    }

    private TextField field(String p) {
        TextField tf = new TextField(); tf.setPromptText(p);
        tf.getStyleClass().add("text-field"); tf.setPrefWidth(220); return tf;
    }
    private Label lbl(String t) {
        Label l = new Label(t); l.getStyleClass().add("form-label"); return l;
    }
}

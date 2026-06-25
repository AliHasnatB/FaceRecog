package com.backex.faceox.controller;

import com.backex.faceox.model.StudentRecord;
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
import javafx.stage.FileChooser;

import java.io.File;
import java.util.Optional;

public class StudentsViewController {

    @FXML private TableView<StudentRecord>           studentsTable;
    @FXML private TableColumn<StudentRecord, Long>   colId;
    @FXML private TableColumn<StudentRecord, String> colName, colRoll, colEmail, colDept, colSem;
    @FXML private TableColumn<StudentRecord, Void>   colActions;
    @FXML private TextField searchField;
    @FXML private Button    addStudentBtn;

    private final ApiService api = new ApiService();
    private final ObservableList<StudentRecord> allRows = FXCollections.observableArrayList();
    private FilteredList<StudentRecord> filteredRows;

    @FXML
    public void initialize() {
        setupColumns();
        filteredRows = new FilteredList<>(allRows, s -> true);
        studentsTable.setItems(filteredRows);

        // Hide add button for non-admins
        boolean canAdd = SessionManager.get().isAdmin();
        addStudentBtn.setVisible(canAdd);
        addStudentBtn.setManaged(canAdd);

        loadStudents();
    }

    private void setupColumns() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colName.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        colRoll.setCellValueFactory(new PropertyValueFactory<>("rollNumber"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colDept.setCellValueFactory(new PropertyValueFactory<>("department"));
        colSem.setCellValueFactory(new PropertyValueFactory<>("semester"));

        colActions.setCellFactory(col -> new TableCell<>() {
            private final Button viewBtn   = new Button("View");
            private final Button deleteBtn = new Button("Delete");
            {
                viewBtn.getStyleClass().add("btn-ghost");
                viewBtn.setStyle("-fx-font-size: 11px; -fx-padding: 4 10;");
                deleteBtn.getStyleClass().add("btn-danger");
                deleteBtn.setStyle("-fx-font-size: 11px; -fx-padding: 4 10;");
                deleteBtn.setVisible(SessionManager.get().isAdmin());

                viewBtn.setOnAction(e -> {
                    StudentRecord s = getTableView().getItems().get(getIndex());
                    showStudentDetail(s);
                });
                deleteBtn.setOnAction(e -> {
                    StudentRecord s = getTableView().getItems().get(getIndex());
                    confirmDelete(s);
                });
            }

            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setGraphic(null); return; }
                HBox box = new HBox(6, viewBtn, deleteBtn);
                setGraphic(box);
            }
        });
    }

    private void loadStudents() {
        api.getAllStudents().thenAccept(json -> Platform.runLater(() -> {
            allRows.clear();
            if (json.isArray()) {
                json.forEach(n -> allRows.add(new StudentRecord(
                        n.path("id").asLong(),
                        n.path("fullName").asText(),
                        n.path("rollNumber").asText(),
                        n.path("email").asText(),
                        n.path("department").path("name").asText("—"),
                        n.path("semester").asText("—")
                )));
            }
        }));
    }

    @FXML
    public void handleSearch() {
        String q = searchField.getText().toLowerCase();
        filteredRows.setPredicate(s ->
                q.isEmpty() ||
                s.getFullName().toLowerCase().contains(q) ||
                s.getRollNumber().toLowerCase().contains(q) ||
                s.getDepartment().toLowerCase().contains(q)
        );
    }

    @FXML
    public void showRegisterDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Register New Student");
        dialog.setHeaderText("Fill in student details");
        DashboardController.styleDialog((Alert) null);

        GridPane grid = new GridPane();
        grid.setHgap(12); grid.setVgap(10);
        grid.setStyle("-fx-padding: 20;");

        TextField tfFull = field("Full Name");
        TextField tfRoll = field("Roll Number");
        TextField tfEmail = field("Email");
        TextField tfDept = field("Department");
        TextField tfSem = field("Semester");
        TextField tfUser = field("Username");
        PasswordField tfPass = new PasswordField();
        tfPass.setPromptText("Password");
        tfPass.getStyleClass().add("password-field");

        Label faceLabel = new Label("No image selected");
        faceLabel.setStyle("-fx-text-fill: #8b949e; -fx-font-size: 11px;");
        Button chooseImg = new Button("Choose Face Image");
        chooseImg.getStyleClass().add("btn-ghost");
        chooseImg.setStyle("-fx-font-size: 11px;");
        final File[] selectedFile = {null};
        chooseImg.setOnAction(e -> {
            FileChooser fc = new FileChooser();
            fc.setTitle("Select Face Image");
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Images", "*.jpg", "*.jpeg", "*.png"));
            File f = fc.showOpenDialog(chooseImg.getScene().getWindow());
            if (f != null) { selectedFile[0] = f; faceLabel.setText(f.getName()); }
        });

        int r = 0;
        grid.add(lbl("Full Name"),   0, r); grid.add(tfFull,  1, r++);
        grid.add(lbl("Roll Number"), 0, r); grid.add(tfRoll,  1, r++);
        grid.add(lbl("Email"),       0, r); grid.add(tfEmail, 1, r++);
        grid.add(lbl("Department"),  0, r); grid.add(tfDept,  1, r++);
        grid.add(lbl("Semester"),    0, r); grid.add(tfSem,   1, r++);
        grid.add(lbl("Username"),    0, r); grid.add(tfUser,  1, r++);
        grid.add(lbl("Password"),    0, r); grid.add(tfPass,  1, r++);
        grid.add(lbl("Face Image"),  0, r);
        HBox imgBox = new HBox(8, chooseImg, faceLabel);
        imgBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        grid.add(imgBox, 1, r);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.getDialogPane().getStylesheets().add(
                getClass().getResource("/styles/main.css").toExternalForm());

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (selectedFile[0] == null) {
                new Alert(Alert.AlertType.ERROR, "Please select a face image.").showAndWait();
                return;
            }
            api.registerStudent(
                    tfFull.getText(), tfFull.getText(), tfRoll.getText(),
                    tfEmail.getText(), tfDept.getText(), tfUser.getText(),
                    tfPass.getText(), selectedFile[0]
            ).thenAccept(json -> Platform.runLater(() -> {
                if (json.has("id")) {
                    loadStudents();
                    info("Student registered successfully.");
                } else {
                    error(json.path("message").asText("Registration failed."));
                }
            })).exceptionally(e -> { Platform.runLater(() -> error(e.getMessage())); return null; });
        }
    }

    private void showStudentDetail(StudentRecord s) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("Student Details");
        a.setHeaderText(s.getFullName());
        a.setContentText(
                "Roll Number : " + s.getRollNumber() + "\n" +
                "Email       : " + s.getEmail()      + "\n" +
                "Department  : " + s.getDepartment() + "\n" +
                "Semester    : " + s.getSemester()
        );
        a.getDialogPane().getStylesheets().add(
                getClass().getResource("/styles/main.css").toExternalForm());
        a.showAndWait();
    }

    private void confirmDelete(StudentRecord s) {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION,
                "Delete " + s.getFullName() + "? This also removes their face data.",
                ButtonType.YES, ButtonType.NO);
        a.getDialogPane().getStylesheets().add(
                getClass().getResource("/styles/main.css").toExternalForm());
        a.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.YES) {
                api.deleteStudent(s.getId()).thenAccept(r -> Platform.runLater(this::loadStudents));
            }
        });
    }

    private TextField field(String prompt) {
        TextField tf = new TextField();
        tf.setPromptText(prompt);
        tf.getStyleClass().add("text-field");
        tf.setPrefWidth(220);
        return tf;
    }
    private Label lbl(String text) {
        Label l = new Label(text);
        l.getStyleClass().add("form-label");
        return l;
    }
    private void info(String msg) { new Alert(Alert.AlertType.INFORMATION, msg).showAndWait(); }
    private void error(String msg) { new Alert(Alert.AlertType.ERROR, msg).showAndWait(); }
}

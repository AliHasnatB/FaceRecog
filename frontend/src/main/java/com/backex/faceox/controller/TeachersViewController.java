package com.backex.faceox.controller;

import com.backex.faceox.service.ApiService;
import com.backex.faceox.service.SessionManager;
import com.fasterxml.jackson.databind.JsonNode;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class TeachersViewController {

    @FXML private TableView<JsonNode>                 teachersTable;
    @FXML private TableColumn<JsonNode, String>       colName, colEmpId, colEmail, colDept, colDesg, colSubjs;
    @FXML private TextField searchField;
    @FXML private Button    addTeacherBtn;

    private final ApiService api = new ApiService();
    private final ObservableList<JsonNode> allRows = FXCollections.observableArrayList();
    private FilteredList<JsonNode> filteredRows;

    @FXML
    public void initialize() {
        setupColumns();
        filteredRows = new FilteredList<>(allRows, t -> true);
        teachersTable.setItems(filteredRows);

        boolean canAdd = SessionManager.get().isAdmin();
        addTeacherBtn.setVisible(canAdd);
        addTeacherBtn.setManaged(canAdd);

        loadTeachers();
    }

    private void setupColumns() {
        colName.setCellValueFactory(d  -> new SimpleStringProperty(d.getValue().path("fullName").asText()));
        colEmpId.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().path("employeeId").asText()));
        colEmail.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().path("email").asText()));
        colDept.setCellValueFactory(d  -> new SimpleStringProperty(d.getValue().path("department").path("name").asText("—")));
        colDesg.setCellValueFactory(d  -> new SimpleStringProperty(d.getValue().path("designation").asText("—")));
        colSubjs.setCellValueFactory(d -> new SimpleStringProperty("View classes →"));
    }

    private void loadTeachers() {
        api.getAllTeachers().thenAccept(json -> Platform.runLater(() -> {
            allRows.clear();
            if (json.isArray()) json.forEach(allRows::add);
        }));
    }

    @FXML
    public void handleSearch() {
        String q = searchField.getText().toLowerCase();
        filteredRows.setPredicate(n -> q.isEmpty() ||
                n.path("fullName").asText().toLowerCase().contains(q) ||
                n.path("employeeId").asText().toLowerCase().contains(q) ||
                n.path("department").path("name").asText().toLowerCase().contains(q));
    }

    @FXML
    public void showRegisterDialog() {
        Alert a = new Alert(Alert.AlertType.INFORMATION,
                "Teacher registration form — to be implemented with full multipart form dialog.");
        a.getDialogPane().getStylesheets().add(
                getClass().getResource("/styles/main.css").toExternalForm());
        a.showAndWait();
    }
}

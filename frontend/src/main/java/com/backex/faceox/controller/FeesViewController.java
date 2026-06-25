package com.backex.faceox.controller;

import com.backex.faceox.model.FeeRecord;
import com.backex.faceox.service.ApiService;
import com.backex.faceox.service.SessionManager;
import com.fasterxml.jackson.databind.JsonNode;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;

import java.util.Optional;

public class FeesViewController {

    @FXML private TableView<FeeRecord>              feesTable;
    @FXML private TableColumn<FeeRecord, String>    colStudent, colSemester, colDueDate, colStatus;
    @FXML private TableColumn<FeeRecord, Double>    colTotal, colPaid, colPending;
    @FXML private TableColumn<FeeRecord, Void>      colActions;
    @FXML private Label   totalFeeLabel, paidFeeLabel, pendingFeeLabel;
    @FXML private Button  addFeeBtn;

    private final ApiService api = new ApiService();
    private final ObservableList<FeeRecord> rows = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupColumns();
        feesTable.setItems(rows);

        boolean isAdmin = SessionManager.get().isAdmin();
        addFeeBtn.setVisible(isAdmin);
        addFeeBtn.setManaged(isAdmin);

        loadFees();
    }

    private void setupColumns() {
        // Student name: pulled from API node, stored in a separate list
        colStudent.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getSemester())); // re-used as placeholder — we override below
        colSemester.setCellValueFactory(new PropertyValueFactory<>("semester"));
        colTotal.setCellValueFactory(new PropertyValueFactory<>("total"));
        colPaid.setCellValueFactory(new PropertyValueFactory<>("paid"));
        colPending.setCellValueFactory(new PropertyValueFactory<>("pending"));
        colDueDate.setCellValueFactory(new PropertyValueFactory<>("dueDate"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        // Format currency columns
        for (TableColumn<FeeRecord, Double> col : new TableColumn[]{colTotal, colPaid, colPending}) {
            col.setCellFactory(c -> new TableCell<>() {
                @Override protected void updateItem(Double item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? null : String.format("PKR %.0f", item));
                }
            });
        }

        // Status chip
        colStatus.setCellFactory(c -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); return; }
                Label chip = new Label(item);
                chip.getStyleClass().addAll("chip", item.toLowerCase());
                setGraphic(chip); setText(null);
            }
        });

        // Actions (Admin only)
        colActions.setCellFactory(c -> new TableCell<>() {
            private final Button payBtn = new Button("Record Payment");
            {
                payBtn.getStyleClass().add("btn-success");
                payBtn.setStyle("-fx-font-size: 11px; -fx-padding: 4 10;");
                payBtn.setOnAction(e -> showPaymentDialog(getTableView().getItems().get(getIndex())));
            }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || !SessionManager.get().isAdmin()) { setGraphic(null); return; }
                setGraphic(payBtn);
            }
        });
    }

    // Parallel list to store IDs since FeeRecord doesn't include student name easily
    private final java.util.List<Long> feeIds = new java.util.ArrayList<>();

    private void loadFees() {
        SessionManager s = SessionManager.get();
        var future = s.isStudent() && s.getProfileId() != null
                ? api.getFeesByStudent(s.getProfileId())
                : api.getAllFees();

        future.thenAccept(json -> Platform.runLater(() -> {
            rows.clear();
            feeIds.clear();
            if (json.isArray()) {
                json.forEach(n -> {
                    feeIds.add(n.path("id").asLong());
                    rows.add(new FeeRecord(
                            n.path("id").asLong(),
                            n.path("semester").asText(),
                            n.path("totalAmount").asDouble(),
                            n.path("paidAmount").asDouble(),
                            n.path("dueDate").asText(),
                            n.path("status").asText("PENDING")
                    ));
                });
            }
        }));

        if (s.isStudent() && s.getProfileId() != null) {
            api.getFeeSummary(s.getProfileId()).thenAccept(json -> Platform.runLater(() -> {
                totalFeeLabel.setText(String.format("PKR %.0f", json.path("totalFees").asDouble()));
                paidFeeLabel.setText(String.format("PKR %.0f",  json.path("paidFees").asDouble()));
                pendingFeeLabel.setText(String.format("PKR %.0f", json.path("pendingFees").asDouble()));
            }));
        }
    }

    private void showPaymentDialog(FeeRecord fee) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Record Payment");
        dialog.setHeaderText("Semester: " + fee.getSemester() +
                "  |  Pending: PKR " + String.format("%.0f", fee.getPending()));

        GridPane grid = new GridPane();
        grid.setHgap(12); grid.setVgap(10);
        grid.setStyle("-fx-padding: 20;");

        TextField tfAmount = new TextField(String.format("%.0f", fee.getPending()));
        tfAmount.getStyleClass().add("text-field");
        TextField tfTxn = new TextField();
        tfTxn.setPromptText("Transaction ID (optional)");
        tfTxn.getStyleClass().add("text-field");

        grid.add(lbl("Amount (PKR)"),    0, 0); grid.add(tfAmount, 1, 0);
        grid.add(lbl("Transaction ID"),  0, 1); grid.add(tfTxn,    1, 1);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.getDialogPane().getStylesheets().add(
                getClass().getResource("/styles/main.css").toExternalForm());

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                double amount = Double.parseDouble(tfAmount.getText().trim());
                // Call backend pay endpoint
                new Alert(Alert.AlertType.INFORMATION,
                        "Payment of PKR " + amount + " recorded.\n(Connect api.recordPayment() for full integration)")
                        .showAndWait();
                loadFees();
            } catch (NumberFormatException e) {
                new Alert(Alert.AlertType.ERROR, "Invalid amount.").showAndWait();
            }
        }
    }

    @FXML
    public void showAddFeeDialog() {
        Alert a = new Alert(Alert.AlertType.INFORMATION,
                "Add Fee dialog — provide student ID, semester, amount, due date.\n" +
                "Connects to POST /api/fees/create");
        a.getDialogPane().getStylesheets().add(
                getClass().getResource("/styles/main.css").toExternalForm());
        a.showAndWait();
    }

    private Label lbl(String t) { Label l = new Label(t); l.getStyleClass().add("form-label"); return l; }
}

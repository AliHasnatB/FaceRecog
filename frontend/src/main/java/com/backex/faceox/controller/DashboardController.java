package com.backex.faceox.controller;

import com.backex.faceox.MainApp;
import com.backex.faceox.service.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;

import java.util.Objects;

public class DashboardController {

    @FXML private Label     avatarLabel;
    @FXML private Label     userNameLabel;
    @FXML private Label     userRoleLabel;
    @FXML private Label     pageTitle;
    @FXML private Label     topbarUser;
    @FXML private Label     topbarRole;
    @FXML private StackPane contentPane;

    // Sidebar nav buttons
    @FXML private Button navHome;
    @FXML private Button navAttendance;
    @FXML private Button navClasses;
    @FXML private Button navStudents;
    @FXML private Button navTeachers;
    @FXML private Button navLeaves;
    @FXML private Button navFees;

    private Button activeBtn;

    @FXML
    public void initialize() {
        SessionManager s = SessionManager.get();

        // Populate user info
        String name = s.getFullName() != null ? s.getFullName() : "User";
        userNameLabel.setText(name);
        userRoleLabel.setText(s.getRole());
        topbarUser.setText("  " + name);

        String initial = name.substring(0, 1).toUpperCase();
        avatarLabel.setText(initial);

        // Role badge colour
        String role = s.getRole();
        topbarRole.setText(role);
        topbarRole.getStyleClass().removeAll("admin", "teacher", "student");
        topbarRole.getStyleClass().add(role.toLowerCase());

        // Hide nav items based on role
        applyRoleVisibility(role);

        // Load default page
        showHome();
    }

    private void applyRoleVisibility(String role) {
        boolean isStudent = "STUDENT".equals(role);
        boolean isAdmin   = "ADMIN".equals(role);
        boolean isTeacher = "TEACHER".equals(role);

        // Students cannot manage students/teachers
        navStudents.setVisible(isAdmin || isTeacher);
        navStudents.setManaged(isAdmin || isTeacher);
        navTeachers.setVisible(isAdmin || isTeacher);
        navTeachers.setManaged(isAdmin || isTeacher);
    }

    // ── Navigation ──────────────────────────────────────────────────────────

    @FXML public void showHome()       { loadPage("/fxml/home.fxml",       "Dashboard",      navHome); }
    @FXML public void showAttendance() { loadPage("/fxml/attendance.fxml",  "Attendance",     navAttendance); }
    @FXML public void showClasses()    { loadPage("/fxml/classes.fxml",     "Class Schedule", navClasses); }
    @FXML public void showStudents()   { loadPage("/fxml/students.fxml",    "Students",       navStudents); }
    @FXML public void showTeachers()   { loadPage("/fxml/teachers.fxml",    "Teachers",       navTeachers); }
    @FXML public void showLeaves()     { loadPage("/fxml/leaves.fxml",      "Leaves",         navLeaves); }
    @FXML public void showFees()       { loadPage("/fxml/fees.fxml",        "Fee Management", navFees); }

    private void loadPage(String fxmlPath, String title, Button btn) {
        try {
            Node page = FXMLLoader.load(Objects.requireNonNull(
                    getClass().getResource(fxmlPath)));
            contentPane.getChildren().setAll(page);
            pageTitle.setText(title);

            // Update active button style
            if (activeBtn != null) activeBtn.getStyleClass().remove("active");
            btn.getStyleClass().add("active");
            activeBtn = btn;
        } catch (Exception e) {
            showError("Failed to load page: " + e.getMessage());
        }
    }

    // ── Logout ──────────────────────────────────────────────────────────────

    @FXML
    public void handleLogout() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                "Are you sure you want to logout?",
                ButtonType.YES, ButtonType.NO);
        alert.setTitle("Logout");
        alert.setHeaderText(null);
        styleDialog(alert);
        alert.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.YES) {
                SessionManager.get().clear();
                try { MainApp.showLogin(); } catch (Exception ex) { ex.printStackTrace(); }
            }
        });
    }

    private void showError(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
        styleDialog(a);
        a.showAndWait();
    }

    static void styleDialog(Alert a) {
        a.getDialogPane().getStylesheets().add(Objects.requireNonNull(
                DashboardController.class.getResource("/styles/main.css")).toExternalForm());
    }
}

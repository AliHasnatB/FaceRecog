package com.backex.faceox.controller;

import com.backex.faceox.MainApp;
import com.backex.faceox.service.ApiService;
import com.fasterxml.jackson.databind.JsonNode;
import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamResolution;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.concurrent.atomic.AtomicBoolean;

public class RegisterController {

    // ── Step progress UI ────────────────────────────────────────────────────
    @FXML private Circle  step1Circle, step2Circle, step3Circle;
    @FXML private Label   step1Label,  step2Label,  step3Label;
    @FXML private Label   step2Text,   step3Text;
    @FXML private Rectangle connector1, connector2;

    // ── Step panes ───────────────────────────────────────────────────────────
    @FXML private VBox step1Pane, step2Pane, step3Pane, successPane;

    // ── Step 1 fields ────────────────────────────────────────────────────────
    @FXML private TextField      tfFullName, tfEmail, tfUsername;
    @FXML private PasswordField  tfPassword, tfConfirmPassword;
    @FXML private ComboBox<String> cbRole;
    @FXML private Label          step1Error;

    // ── Step 2 fields ────────────────────────────────────────────────────────
    @FXML private TextField      tfRollNumber, tfSemester, tfEmployeeId, tfDesignation;
    @FXML private ComboBox<Long> cbDepartment;      // value = deptId
    @FXML private Label          step2Subtitle, step2Error;
    @FXML private Label          lblRollNumber, lblSemester, lblEmployeeId, lblDesignation;

    // ── Step 3 — camera ──────────────────────────────────────────────────────
    @FXML private ImageView  cameraView, capturedView;
    @FXML private Circle     cameraRing;
    @FXML private Label      cameraStatusLabel, faceDetectLabel, step3Error, noCapturePlaceholder;
    @FXML private Button     btnToggleCamera, btnCapture, btnRetake, btnRegister;
    @FXML private ProgressBar registerProgress;

    // ── Success pane ─────────────────────────────────────────────────────────
    @FXML private Label successMsg, successUsername, successRole;

    // ── State ────────────────────────────────────────────────────────────────
    private final ApiService api = new ApiService();
    private Webcam webcam;
    private final AtomicBoolean cameraRunning = new AtomicBoolean(false);
    private Thread cameraThread;
    private byte[] capturedJpeg;      // stored after clicking Capture
    private int currentStep = 1;

    // Department display names (parallel to cbDepartment values)
    private final java.util.List<String> deptNames = new java.util.ArrayList<>();

    // ── Init ─────────────────────────────────────────────────────────────────

    @FXML
    public void initialize() {
        // Role choices
        cbRole.getItems().addAll("STUDENT", "TEACHER");
        cbRole.setValue("STUDENT");
        cbRole.setOnAction(e -> adjustStep2ForRole());

        // Camera clip
        Circle clip = new Circle(130, 130, 130);
        cameraView.setClip(clip);

        // Load departments from backend
        loadDepartments();

        showStep(1);
    }

    // ── Step navigation ───────────────────────────────────────────────────────

    @FXML
    public void goStep1() { showStep(1); }

    @FXML
    public void goStep2() {
        if (!validateStep1()) return;
        adjustStep2ForRole();
        showStep(2);
    }

    @FXML
    public void goStep3() {
        if (!validateStep2()) return;
        showStep(3);
    }

    @FXML
    public void goToLogin() {
        stopCamera();
        try { MainApp.showLogin(); } catch (Exception e) { e.printStackTrace(); }
    }

    private void showStep(int step) {
        currentStep = step;

        // Hide all
        setVisible(step1Pane, false);
        setVisible(step2Pane, false);
        setVisible(step3Pane, false);
        setVisible(successPane, false);

        // Show active
        switch (step) {
            case 1 -> setVisible(step1Pane, true);
            case 2 -> setVisible(step2Pane, true);
            case 3 -> setVisible(step3Pane, true);
            case 4 -> setVisible(successPane, true);
        }

        // Update step indicators
        updateStepIndicator(step1Circle, step1Label, step >= 1, step > 1);
        updateStepIndicator(step2Circle, step2Label, step >= 2, step > 2);
        updateStepIndicator(step3Circle, step3Label, step >= 3, step > 3);

        connector1.setFill(step >= 2
                ? Color.web("#4f8ef7") : Color.web("#21293a"));
        connector2.setFill(step >= 3
                ? Color.web("#4f8ef7") : Color.web("#21293a"));

        updateStepLabel(step2Text, step >= 2, "Role Details");
        updateStepLabel(step3Text, step >= 3, "Face Capture");
    }

    private void updateStepIndicator(Circle circle, Label label, boolean active, boolean done) {
        if (done) {
            circle.setFill(Color.web("#3fb950"));
            label.setText("✓");
            label.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 13px;");
        } else if (active) {
            circle.setFill(Color.web("#4f8ef7"));
            label.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 13px;");
        } else {
            circle.setFill(Color.web("#21293a"));
            label.setStyle("-fx-text-fill: #6e7681; -fx-font-weight: bold; -fx-font-size: 13px;");
        }
    }

    private void updateStepLabel(Label label, boolean active, String text) {
        label.setText(text);
        label.setStyle(active
                ? "-fx-text-fill: #4f8ef7; -fx-font-size: 11px; -fx-font-weight: bold;"
                : "-fx-text-fill: #6e7681; -fx-font-size: 11px;");
    }

    // ── Step 1 validation ─────────────────────────────────────────────────────

    private boolean validateStep1() {
        step1Error.setVisible(false);
        step1Error.setManaged(false);

        if (tfFullName.getText().isBlank())   { showStep1Error("Full name is required."); return false; }
        if (tfEmail.getText().isBlank())       { showStep1Error("Email is required."); return false; }
        if (!tfEmail.getText().contains("@")) { showStep1Error("Enter a valid email address."); return false; }
        if (tfUsername.getText().isBlank())    { showStep1Error("Username is required."); return false; }
        if (tfUsername.getText().length() < 3){ showStep1Error("Username must be at least 3 characters."); return false; }
        if (tfPassword.getText().length() < 6){ showStep1Error("Password must be at least 6 characters."); return false; }
        if (!tfPassword.getText().equals(tfConfirmPassword.getText())) {
            showStep1Error("Passwords do not match."); return false;
        }
        if (cbRole.getValue() == null) { showStep1Error("Please select a role."); return false; }
        return true;
    }

    private void showStep1Error(String msg) {
        step1Error.setText("⚠  " + msg);
        step1Error.setVisible(true);
        step1Error.setManaged(true);
    }

    // ── Step 2 — role-specific field toggling ─────────────────────────────────

    private void adjustStep2ForRole() {
        boolean isStudent = "STUDENT".equals(cbRole.getValue());
        step2Subtitle.setText(isStudent
                ? "Student registration details"
                : "Teacher registration details");

        // Student fields
        setVisible(lblRollNumber, isStudent);
        setVisible(tfRollNumber,  isStudent);
        setVisible(lblSemester,   isStudent);
        setVisible(tfSemester,    isStudent);

        // Teacher fields
        setVisible(lblEmployeeId,  !isStudent);
        setVisible(tfEmployeeId,   !isStudent);
        setVisible(lblDesignation, !isStudent);
        setVisible(tfDesignation,  !isStudent);
    }

    private boolean validateStep2() {
        step2Error.setVisible(false);
        step2Error.setManaged(false);
        boolean isStudent = "STUDENT".equals(cbRole.getValue());

        if (isStudent && tfRollNumber.getText().isBlank()) {
            showStep2Error("Roll number is required."); return false;
        }
        if (!isStudent && tfEmployeeId.getText().isBlank()) {
            showStep2Error("Employee ID is required."); return false;
        }
        return true;
    }

    private void showStep2Error(String msg) {
        step2Error.setText("⚠  " + msg);
        step2Error.setVisible(true);
        step2Error.setManaged(true);
    }

    // ── Step 3 — Camera ───────────────────────────────────────────────────────

    @FXML
    public void toggleCamera() {
        if (!cameraRunning.get()) startCamera();
        else stopCamera();
    }

    private void startCamera() {
        cameraRunning.set(true);
        btnToggleCamera.setText("⏹ Stop Camera");
        btnCapture.setDisable(false);
        cameraStatusLabel.setText("Camera active — position your face");
        cameraStatusLabel.setStyle("-fx-text-fill: #4f8ef7; -fx-font-size: 12px;");

        cameraRing.setStroke(Color.web("#4f8ef7"));

        cameraThread = new Thread(() -> {
            try {
                if (webcam == null) {
                    webcam = Webcam.getDefault();
                    if (webcam != null) webcam.setViewSize(WebcamResolution.VGA.getSize());
                }
                if (webcam != null && !webcam.isOpen()) webcam.open();

                while (cameraRunning.get() && webcam != null && webcam.isOpen()) {
                    BufferedImage img = webcam.getImage();
                    if (img != null) {
                        WritableImage fx = SwingFXUtils.toFXImage(img, null);
                        Platform.runLater(() -> cameraView.setImage(fx));
                    }
                    Thread.sleep(33);
                }
                if (webcam != null && webcam.isOpen()) webcam.close();
                Platform.runLater(() -> cameraView.setImage(null));
            } catch (Exception e) { e.printStackTrace(); }
        });
        cameraThread.setDaemon(true);
        cameraThread.start();
    }

    private void stopCamera() {
        cameraRunning.set(false);
        if (webcam != null && webcam.isOpen()) webcam.close();
        btnToggleCamera.setText("▶ Start Camera");
        btnCapture.setDisable(true);
        cameraStatusLabel.setText("Camera off");
        cameraStatusLabel.setStyle("-fx-text-fill: #6e7681; -fx-font-size: 12px;");
        cameraRing.setStroke(Color.web("#21293a"));
    }

    @FXML
    public void captureFrame() {
        if (webcam == null || !webcam.isOpen()) {
            showStep3Error("Camera is not active. Start the camera first.");
            return;
        }

        BufferedImage img = webcam.getImage();
        if (img == null) {
            showStep3Error("Failed to capture image. Try again.");
            return;
        }

        try {
            // Convert to JPEG bytes
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(img, "jpg", baos);
            capturedJpeg = baos.toByteArray();

            // Show preview
            WritableImage preview = SwingFXUtils.toFXImage(img, null);
            capturedView.setImage(preview);
            noCapturePlaceholder.setVisible(false);
            noCapturePlaceholder.setManaged(false);

            // Stop live feed
            stopCamera();

            // Validate face with backend
            btnCapture.setDisable(true);
            btnRetake.setDisable(false);
            cameraStatusLabel.setText("Checking for face…");
            cameraStatusLabel.setStyle("-fx-text-fill: #e3b341; -fx-font-size: 12px;");

            byte[] jpegCopy = capturedJpeg;
            api.validateFace(jpegCopy).thenAccept(json -> Platform.runLater(() -> {
                boolean detected = json.path("faceDetected").asBoolean(false);
                if (detected) {
                    showFaceDetected(true);
                    btnRegister.setDisable(false);
                } else {
                    showFaceDetected(false);
                    btnRegister.setDisable(true);
                    showStep3Error("No face detected in photo. Please retake.");
                }
            })).exceptionally(e -> {
                // If backend not reachable, still allow register (backend will validate)
                Platform.runLater(() -> {
                    showFaceDetected(true);
                    btnRegister.setDisable(false);
                });
                return null;
            });

        } catch (Exception e) {
            showStep3Error("Capture error: " + e.getMessage());
        }
    }

    @FXML
    public void retakePhoto() {
        capturedJpeg = null;
        capturedView.setImage(null);
        noCapturePlaceholder.setVisible(true);
        noCapturePlaceholder.setManaged(true);
        faceDetectLabel.setVisible(false);
        faceDetectLabel.setManaged(false);
        step3Error.setVisible(false);
        step3Error.setManaged(false);
        btnRetake.setDisable(true);
        btnRegister.setDisable(true);
        startCamera();
    }

    private void showFaceDetected(boolean ok) {
        faceDetectLabel.setVisible(true);
        faceDetectLabel.setManaged(true);
        if (ok) {
            faceDetectLabel.setText("✓ Face detected — ready to register");
            faceDetectLabel.setStyle(
                    "-fx-text-fill: #3fb950; -fx-font-size: 12px; -fx-font-weight: bold;" +
                    "-fx-padding: 6 16; -fx-background-radius: 20;" +
                    "-fx-background-color: rgba(63,185,80,0.15);");
            cameraStatusLabel.setText("✓ Face captured successfully");
            cameraStatusLabel.setStyle("-fx-text-fill: #3fb950; -fx-font-size: 12px;");
        } else {
            faceDetectLabel.setText("✗ No face detected");
            faceDetectLabel.setStyle(
                    "-fx-text-fill: #f85149; -fx-font-size: 12px; -fx-font-weight: bold;" +
                    "-fx-padding: 6 16; -fx-background-radius: 20;" +
                    "-fx-background-color: rgba(248,81,73,0.15);");
        }
    }

    // ── Submit registration ───────────────────────────────────────────────────

    @FXML
    public void submitRegistration() {
        if (capturedJpeg == null) {
            showStep3Error("Please capture your face photo first.");
            return;
        }

        // Lock UI
        btnRegister.setDisable(true);
        registerProgress.setVisible(true);
        registerProgress.setManaged(true);
        step3Error.setVisible(false);
        step3Error.setManaged(false);

        // Gather all form data
        String fullName    = tfFullName.getText().trim();
        String username    = tfUsername.getText().trim();
        String password    = tfPassword.getText();
        String email       = tfEmail.getText().trim();
        String role        = cbRole.getValue();
        String rollNumber  = tfRollNumber.getText().trim();
        String employeeId  = tfEmployeeId.getText().trim();
        String semester    = tfSemester.getText().trim();
        String designation = tfDesignation.getText().trim();
        Long   deptId      = cbDepartment.getValue();

        byte[] faceBytes = capturedJpeg;

        api.registerAccount(
                fullName, username, password, email, role,
                rollNumber, employeeId, deptId, semester, designation,
                faceBytes
        ).thenAccept(json -> Platform.runLater(() -> {
            registerProgress.setVisible(false);
            registerProgress.setManaged(false);

            if (json.has("userId") || json.has("profileId")) {
                // Success
                stopCamera();
                successMsg.setText("Your account has been created successfully.\n" +
                        "You can now log in with your face or password.");
                successUsername.setText(username);
                successRole.setText(role);
                showStep(4);
            } else {
                // Error returned by backend
                String err = json.path("error").asText(
                             json.path("message").asText("Registration failed. Please try again."));
                showStep3Error(err);
                btnRegister.setDisable(false);
            }
        })).exceptionally(e -> {
            Platform.runLater(() -> {
                registerProgress.setVisible(false);
                registerProgress.setManaged(false);
                showStep3Error("Connection error: " + e.getMessage());
                btnRegister.setDisable(false);
            });
            return null;
        });
    }

    // ── Departments ───────────────────────────────────────────────────────────

    private void loadDepartments() {
        api.getDepartments().thenAccept(json -> Platform.runLater(() -> {
            cbDepartment.getItems().clear();
            deptNames.clear();
            if (json.isArray()) {
                json.forEach(n -> {
                    Long id = n.path("id").asLong();
                    String name = n.path("name").asText("Dept " + id);
                    cbDepartment.getItems().add(id);
                    deptNames.add(name);
                });
                // Show name in combo, store id as value
                cbDepartment.setConverter(new javafx.util.StringConverter<Long>() {
                    @Override public String toString(Long id) {
                        if (id == null) return "Select department";
                        int idx = cbDepartment.getItems().indexOf(id);
                        return idx >= 0 && idx < deptNames.size() ? deptNames.get(idx) : id.toString();
                    }
                    @Override public Long fromString(String s) { return null; }
                });
            }
        })).exceptionally(e -> null); // Silently ignore if backend not reachable yet
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void setVisible(javafx.scene.Node node, boolean visible) {
        node.setVisible(visible);
        node.setManaged(visible);
    }

    private void showStep3Error(String msg) {
        step3Error.setText("⚠  " + msg);
        step3Error.setVisible(true);
        step3Error.setManaged(true);
    }
}

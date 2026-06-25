package com.backex.faceox.controller;

import com.backex.faceox.MainApp;
import com.backex.faceox.service.ApiService;
import com.backex.faceox.service.FaceWebSocketService;
import com.backex.faceox.service.SessionManager;
import com.fasterxml.jackson.databind.JsonNode;
import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamResolution;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.shape.Circle;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class LoginController {

    @FXML private ImageView cameraView;
    @FXML private Circle    cameraBorder;
    @FXML private Label     faceStatusLabel;
    @FXML private Button    startCameraBtn;
    @FXML private Button    faceLoginBtn;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label     errorLabel;

    private Webcam webcam;
    private final AtomicBoolean cameraRunning = new AtomicBoolean(false);
    private Thread cameraThread;
    private FaceWebSocketService wsService;
    private volatile byte[] latestFrame;
    private final ApiService apiService = new ApiService();

    @FXML
    public void initialize() {
        // Circular clip for the camera feed
        Circle clip = new Circle(125, 125, 125);
        cameraView.setClip(clip);
    }

    // ── Camera ──────────────────────────────────────────────────────────────

    @FXML
    public void handleToggleCamera() {
        if (!cameraRunning.get()) {
            startCamera();
        } else {
            stopCamera();
        }
    }

    private void startCamera() {
        cameraRunning.set(true);
        startCameraBtn.setText("Stop Camera");
        faceLoginBtn.setDisable(false);
        setFaceStatus("scanning", "● Scanning for face…");

        // Connect WebSocket
        try {
            wsService = new FaceWebSocketService(
                    "ws://localhost:8080/ws/face",
                    this::handleFaceResult,
                    status -> Platform.runLater(() -> {
                        if ("DISCONNECTED".equals(status)) setFaceStatus("failed", "● Disconnected");
                    })
            );
            wsService.connect();
        } catch (Exception e) {
            showError("WebSocket error: " + e.getMessage());
        }

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
                        // Store JPEG bytes for face auth
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        ImageIO.write(img, "jpg", baos);
                        latestFrame = baos.toByteArray();
                    }
                    Thread.sleep(33);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        cameraThread.setDaemon(true);
        cameraThread.start();
    }

    private void stopCamera() {
        cameraRunning.set(false);
        startCameraBtn.setText("Start Camera");
        faceLoginBtn.setDisable(true);
        setFaceStatus("scanning", "● Camera off");
        if (webcam != null && webcam.isOpen()) webcam.close();
        if (wsService != null && wsService.isOpen()) wsService.close();
        Platform.runLater(() -> cameraView.setImage(null));
    }

    // ── Face login ───────────────────────────────────────────────────────────

    @FXML
    public void handleFaceLogin() {
        if (latestFrame == null) {
            showError("No camera frame available. Start the camera first.");
            return;
        }
        setFaceStatus("scanning", "● Authenticating…");
        faceLoginBtn.setDisable(true);

        if (wsService != null && wsService.isOpen()) {
            wsService.sendFrame(latestFrame);
        } else {
            showError("Not connected to recognition service.");
            faceLoginBtn.setDisable(false);
        }
    }

    private void handleFaceResult(JsonNode result) {
        Platform.runLater(() -> {
            faceLoginBtn.setDisable(false);
            boolean auth = result.path("authenticated").asBoolean(false);
            String msg   = result.path("message").asText("Unknown error");

            if (auth) {
                setFaceStatus("detected", "✓ Authenticated!");
                String token       = result.path("token").asText(null);
                String role        = result.path("role").asText("STUDENT");
                String fullName    = result.path("fullName").asText("User");
                long   profileId   = result.path("profileId").asLong(0);
                String subjectName = result.path("subjectName").asText(null);

                if (token != null) {
                    SessionManager.get().setSession(token, role, fullName, profileId, subjectName);
                    stopCamera();
                    try { MainApp.showDashboard(); } catch (Exception e) { showError(e.getMessage()); }
                } else {
                    showError("Login failed: no token received.");
                }
            } else {
                setFaceStatus("failed", "✗ Not recognised");
                showError(msg);
            }
        });
    }

    // ── Password login ───────────────────────────────────────────────────────

    @FXML
    public void handlePasswordLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showError("Please enter username and password.");
            return;
        }

        hideError();
        apiService.login(username, password).thenAccept(json -> Platform.runLater(() -> {
            if (json.has("token")) {
                SessionManager.get().setSession(
                        json.path("token").asText(),
                        json.path("role").asText("STUDENT"),
                        json.path("fullName").asText(username),
                        json.path("profileId").asLong(0),
                        json.path("subjectName").asText(null)
                );
                stopCamera();
                try { MainApp.showDashboard(); } catch (Exception e) { showError(e.getMessage()); }
            } else {
                showError(json.path("message").asText("Invalid credentials."));
            }
        })).exceptionally(ex -> {
            Platform.runLater(() -> showError("Connection error: " + ex.getMessage()));
            return null;
        });
    }

    // ── Navigate to register ──────────────────────────────────────────────────

//    @FXML
//    public void goToRegister() {
//        stopCamera();
//        try { MainApp.showRegister(); } catch (Exception e) { showError(e.getMessage()); }
//    }
@FXML
public void goToRegister() {
    stopCamera();
    System.out.println("[DEBUG] goToRegister clicked");
    MainApp.showRegister();
}
    // ── Helpers ──────────────────────────────────────────────────────────────

    private void setFaceStatus(String styleClass, String text) {
        faceStatusLabel.getStyleClass().removeAll("scanning", "detected", "failed");
        faceStatusLabel.getStyleClass().add(styleClass);
        faceStatusLabel.setText(text);
    }

    private void showError(String msg) {
        errorLabel.setText(msg);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    private void hideError() {
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }
}

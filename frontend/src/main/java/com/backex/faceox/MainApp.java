package com.backex.faceox;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import java.util.Objects;

public class MainApp extends Application {

    private static Stage primaryStage;

    @Override
    public void start(Stage stage) {
        primaryStage = stage;
        stage.setTitle("FaceOx — Attendance System");
        stage.setMinWidth(1000);
        stage.setMinHeight(650);
        try {
            stage.getIcons().add(new Image(Objects.requireNonNull(
                    getClass().getResourceAsStream("/styles/icon.png"))));
        } catch (Exception ignored) {}
        showLogin();
        stage.show();
    }

    public static void showLogin() {
        try {
            var url = MainApp.class.getResource("/fxml/login.fxml");
            System.out.println("[DEBUG] login.fxml = " + url);
            Parent root = FXMLLoader.load(Objects.requireNonNull(url));
            Scene scene = new Scene(root, 1000, 650);
            scene.getStylesheets().add(Objects.requireNonNull(
                    MainApp.class.getResource("/styles/main.css")).toExternalForm());
            primaryStage.setScene(scene);
            primaryStage.setResizable(false);
            primaryStage.centerOnScreen();
        } catch (Exception e) {
            System.err.println("[ERROR] showLogin failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void showRegister() {
        try {
            var url = MainApp.class.getResource("/fxml/register.fxml");
            System.out.println("[DEBUG] register.fxml = " + url);
            if (url == null) {
                System.err.println("[ERROR] register.fxml NOT FOUND in resources/fxml/");
                return;
            }
            Parent root = FXMLLoader.load(url);
            Scene scene = new Scene(root, 1050, 680);
            scene.getStylesheets().add(Objects.requireNonNull(
                    MainApp.class.getResource("/styles/main.css")).toExternalForm());
            primaryStage.setScene(scene);
            primaryStage.setResizable(false);
            primaryStage.centerOnScreen();
        } catch (Exception e) {
            System.err.println("[ERROR] showRegister failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void showDashboard() {
        try {
            var url = MainApp.class.getResource("/fxml/dashboard.fxml");
            System.out.println("[DEBUG] dashboard.fxml = " + url);
            Parent root = FXMLLoader.load(Objects.requireNonNull(url));
            Scene scene = new Scene(root, 1280, 800);
            scene.getStylesheets().add(Objects.requireNonNull(
                    MainApp.class.getResource("/styles/main.css")).toExternalForm());
            primaryStage.setScene(scene);
            primaryStage.setResizable(true);
            primaryStage.centerOnScreen();
        } catch (Exception e) {
            System.err.println("[ERROR] showDashboard failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static Stage getStage() { return primaryStage; }

    public static void main(String[] args) { launch(args); }
}
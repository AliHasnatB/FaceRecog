module com.backex.faceox {
    // ── JavaFX ────────────────────────────────────────────────────
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.swing;
    requires javafx.graphics;
    requires javafx.base;

    // ── Networking ────────────────────────────────────────────────
    requires java.net.http;
    requires org.java_websocket;
    requires org.apache.httpcomponents.client5.httpclient5;
    requires org.apache.httpcomponents.core5.httpcore5;

    // ── JSON ──────────────────────────────────────────────────────
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.annotation;

    // ── Webcam ────────────────────────────────────────────────────
    requires webcam.capture;
    requires java.desktop;       // AWT / BufferedImage used by webcam

    // ── Misc ──────────────────────────────────────────────────────
    requires java.logging;
    requires org.slf4j;

    // ── FXML reflection access ────────────────────────────────────
    opens com.backex.faceox            to javafx.fxml;
    opens com.backex.faceox.controller to javafx.fxml;

    // ── JavaFX property binding reflection ────────────────────────
    opens com.backex.faceox.model      to javafx.base;
    opens com.backex.faceox.service    to javafx.fxml;

    // ── Exports ───────────────────────────────────────────────────
    exports com.backex.faceox;
    exports com.backex.faceox.controller;
    exports com.backex.faceox.model;
    exports com.backex.faceox.service;
}

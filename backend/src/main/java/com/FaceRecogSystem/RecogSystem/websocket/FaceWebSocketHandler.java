package com.FaceRecogSystem.RecogSystem.websocket;

import com.FaceRecogSystem.RecogSystem.dto.RecognitionResponse;
import com.FaceRecogSystem.RecogSystem.dto.AuthResponse;
import com.FaceRecogSystem.RecogSystem.entity.Student;
import com.FaceRecogSystem.RecogSystem.entity.User;
import com.FaceRecogSystem.RecogSystem.service.*;
import com.FaceRecogSystem.RecogSystem.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.BinaryWebSocketHandler;

import java.io.IOException;
import java.util.Optional;

@Component
public class FaceWebSocketHandler extends BinaryWebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(FaceWebSocketHandler.class);

    @Autowired private CompareFaceService compareFaceService;
    @Autowired private AttendanceService attendanceService;
    @Autowired private StudentService studentService;
    @Autowired private AuthService authService;
    @Autowired private UserRepository userRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        log.info("WS connected: {}", session.getId());
    }

    @Override
    protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) {
        try {
            byte[] imageBytes = message.getPayload().array();

            // 1. Recognize face via CompareFace
            RecognitionResponse result = compareFaceService.recognizeFromBytes(imageBytes);

            if (result.isAuthenticated() && result.getSubjectName() != null) {
                String subjectName = result.getSubjectName();

                // 2. Populate full name from Student record
                studentService.getBySubjectName(subjectName)
                        .ifPresent(s -> result.setFullName(s.getFullName()));

                // 3. Determine role from linked User account
                userRepository.findBySubjectName(subjectName).ifPresent(u -> {
                    result.setRole(u.getRole().name());
                    result.setProfileId(u.getId());
                });

                // 4. Mark attendance (only for students)
                String attendanceStatus = attendanceService.markAttendance(subjectName, result.getSimilarity());
                result.setAttendanceStatus(attendanceStatus);

                // 5. Issue JWT so JavaFX can authenticate immediately
                try {
                    AuthResponse auth = authService.loginByFace(subjectName);
                    result.setToken(auth.getToken());
                    result.setProfileId(auth.getProfileId());
                    if (result.getFullName() == null) result.setFullName(auth.getFullName());
                } catch (RuntimeException ignored) {}

                // 6. Build human-readable message
                result.setMessage(switch (attendanceStatus) {
                    case "MARKED"          -> "✅ Welcome, " + result.getFullName() + "! Attendance marked.";
                    case "ALREADY_MARKED"  -> "👋 Already marked today: " + result.getFullName();
                    case "STUDENT_NOT_FOUND" -> "⚠️ Face recognised but not registered: " + subjectName;
                    default                -> "✅ Face recognised: " + result.getFullName();
                });
            }

            send(session, objectMapper.writeValueAsString(result));

        } catch (Exception e) {
            log.error("Frame processing error", e);
            send(session, "{\"authenticated\":false,\"message\":\"Server error\",\"attendanceStatus\":\"FAILED\"}");
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        log.info("WS closed: {} - {}", session.getId(), status);
    }

    private void send(WebSocketSession session, String json) {
        try {
            if (session.isOpen()) session.sendMessage(new TextMessage(json));
        } catch (IOException e) {
            log.error("Send failed: {}", e.getMessage());
        }
    }
}

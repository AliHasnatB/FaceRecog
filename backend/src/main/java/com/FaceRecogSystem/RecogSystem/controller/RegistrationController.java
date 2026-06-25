package com.FaceRecogSystem.RecogSystem.controller;

import com.FaceRecogSystem.RecogSystem.dto.RegisterRequest;
import com.FaceRecogSystem.RecogSystem.service.RegistrationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class RegistrationController {

    @Autowired private RegistrationService registrationService;
    private final ObjectMapper mapper = new ObjectMapper();

    /**
     * POST /api/auth/register
     *
     * Multipart form:
     *   - data       : JSON string of RegisterRequest fields
     *   - faceImage  : JPEG/PNG face photo
     *
     * Returns: { userId, profileId, subjectName, role, fullName, message }
     * HTTP 200 on success, 400 on validation error.
     */
    @PostMapping(value = "/register", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> register(
            @RequestPart("data")      String dataJson,
            @RequestPart("faceImage") MultipartFile faceImage) {
        try {
            RegisterRequest req = mapper.readValue(dataJson, RegisterRequest.class);

            // Basic validations
            if (faceImage.isEmpty())
                return ResponseEntity.badRequest().body(Map.of("error", "Face image is required."));
            if (req.getUsername() == null || req.getUsername().isBlank())
                return ResponseEntity.badRequest().body(Map.of("error", "Username is required."));
            if (req.getPassword() == null || req.getPassword().length() < 6)
                return ResponseEntity.badRequest().body(Map.of("error", "Password must be at least 6 characters."));

            Map<String, Object> result = registrationService.register(req, faceImage);
            return ResponseEntity.ok(result);

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Registration failed: " + e.getMessage()));
        }
    }

    /**
     * POST /api/auth/validate-face
     * Quick face-detection check before full registration.
     * Returns { "faceDetected": true/false }
     */
    @PostMapping(value = "/validate-face", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> validateFace(@RequestPart("faceImage") MultipartFile faceImage) {
        boolean detected = registrationService.validateFaceImage(faceImage);
        return ResponseEntity.ok(Map.of("faceDetected", detected));
    }
}

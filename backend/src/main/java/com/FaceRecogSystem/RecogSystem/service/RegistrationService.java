package com.FaceRecogSystem.RecogSystem.service;

import com.FaceRecogSystem.RecogSystem.dto.RegisterRequest;
import com.FaceRecogSystem.RecogSystem.entity.*;
import com.FaceRecogSystem.RecogSystem.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class RegistrationService {

    @Autowired private UserRepository       userRepository;
    @Autowired private StudentRepository    studentRepository;
    @Autowired private TeacherRepository    teacherRepository;
    @Autowired private DepartmentRepository departmentRepository;
    @Autowired private CompareFaceService   compareFaceService;
    @Autowired private PasswordEncoder      passwordEncoder;

    /**
     * Registers a new user account + face in CompareFace in one transaction.
     * Works for both STUDENT and TEACHER roles.
     *
     * @param req       form data (name, credentials, role, etc.)
     * @param faceImage JPEG/PNG face image
     * @return map containing { "userId", "profileId", "subjectName", "role", "fullName" }
     */
    @Transactional
    public Map<String, Object> register(RegisterRequest req, MultipartFile faceImage) throws Exception {

        // ── Validate uniqueness ──────────────────────────────────────────────
        if (userRepository.existsByUsername(req.getUsername()))
            throw new RuntimeException("Username already taken: " + req.getUsername());
        if (userRepository.existsByEmail(req.getEmail()))
            throw new RuntimeException("Email already registered: " + req.getEmail());

        String role = req.getRole().toUpperCase();
        if (!role.equals("STUDENT") && !role.equals("TEACHER"))
            throw new RuntimeException("Self-registration only allowed for STUDENT or TEACHER roles.");

        // ── Generate a unique CompareFace subject name ───────────────────────
        // Format: role_username_shortUUID  e.g. "student_ali_a3f2"
        String subjectName = role.toLowerCase() + "_" + req.getUsername() + "_"
                + UUID.randomUUID().toString().substring(0, 6);

        // ── Register face in CompareFace ─────────────────────────────────────
        boolean faceAdded = compareFaceService.addFace(subjectName, faceImage);
        if (!faceAdded) throw new RuntimeException("Face registration failed. Please retake the photo and try again.");

        // ── Create User account ──────────────────────────────────────────────
        User user = new User();
        user.setUsername(req.getUsername());
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        user.setEmail(req.getEmail());
        user.setRole(User.Role.valueOf(role));
        user.setSubjectName(subjectName);
        user.setActive(true);
        userRepository.save(user);

        Long profileId = null;

        // ── Create profile record ────────────────────────────────────────────
        if (role.equals("STUDENT")) {
            if (req.getRollNumber() == null || req.getRollNumber().isBlank())
                throw new RuntimeException("Roll number is required for students.");
            if (studentRepository.existsByRollNumber(req.getRollNumber()))
                throw new RuntimeException("Roll number already exists: " + req.getRollNumber());

            Student student = new Student();
            student.setSubjectName(subjectName);
            student.setFullName(req.getFullName());
            student.setEmail(req.getEmail());
            student.setRollNumber(req.getRollNumber());
            student.setSemester(req.getSemester());
            student.setUser(user);
            if (req.getDepartmentId() != null)
                departmentRepository.findById(req.getDepartmentId())
                        .ifPresent(student::setDepartment);
            profileId = studentRepository.save(student).getId();

        } else { // TEACHER
            if (req.getEmployeeId() == null || req.getEmployeeId().isBlank())
                throw new RuntimeException("Employee ID is required for teachers.");
            if (teacherRepository.existsByEmployeeId(req.getEmployeeId()))
                throw new RuntimeException("Employee ID already exists: " + req.getEmployeeId());

            Teacher teacher = new Teacher();
            teacher.setSubjectName(subjectName);
            teacher.setFullName(req.getFullName());
            teacher.setEmail(req.getEmail());
            teacher.setEmployeeId(req.getEmployeeId());
            teacher.setDesignation(req.getDesignation());
            teacher.setUser(user);
            if (req.getDepartmentId() != null)
                departmentRepository.findById(req.getDepartmentId())
                        .ifPresent(teacher::setDepartment);
            profileId = teacherRepository.save(teacher).getId();
        }

        Map<String, Object> result = new HashMap<>();
        result.put("userId",      user.getId());
        result.put("profileId",   profileId);
        result.put("subjectName", subjectName);
        result.put("role",        role);
        result.put("fullName",    req.getFullName());
        result.put("message",     "Registration successful! You can now log in.");
        return result;
    }

    /**
     * Validates that a face image contains exactly one detectable face
     * by doing a dry-run recognition attempt.
     * Returns true if face is detected, false otherwise.
     */
    public boolean validateFaceImage(MultipartFile faceImage) {
        try {
            byte[] bytes = faceImage.getBytes();
            var result = compareFaceService.recognizeFromBytes(bytes);
            // We don't care about identity — just that a face was detected
            // CompareFace returns NO_FACE status when nothing is found
            return !"NO_FACE".equals(result.getAttendanceStatus());
        } catch (Exception e) {
            return false;
        }
    }
}

package com.FaceRecogSystem.RecogSystem.service;

import com.FaceRecogSystem.RecogSystem.dto.AuthResponse;
import com.FaceRecogSystem.RecogSystem.dto.RecognitionResponse;
import com.FaceRecogSystem.RecogSystem.entity.Student;
import com.FaceRecogSystem.RecogSystem.entity.Teacher;
import com.FaceRecogSystem.RecogSystem.entity.User;
import com.FaceRecogSystem.RecogSystem.repository.StudentRepository;
import com.FaceRecogSystem.RecogSystem.repository.TeacherRepository;
import com.FaceRecogSystem.RecogSystem.repository.UserRepository;
import com.FaceRecogSystem.RecogSystem.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {

    @Autowired private AuthenticationManager authManager;
    @Autowired private JwtUtil jwtUtil;
    @Autowired private UserRepository userRepository;
    @Autowired private StudentRepository studentRepository;
    @Autowired private TeacherRepository teacherRepository;

    /**
     * Standard username/password login.
     */
    public AuthResponse login(String username, String password) {
        try {
            authManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
        } catch (AuthenticationException e) {
            throw new RuntimeException("Invalid credentials");
        }
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        String token = jwtUtil.generateToken(username, user.getRole().name());
        return buildAuthResponse(user, token);
    }

    /**
     * Called after face recognition succeeds — issues JWT for the matched subject.
     */
    public AuthResponse loginByFace(String subjectName) {
        User user = userRepository.findBySubjectName(subjectName)
                .orElseThrow(() -> new RuntimeException("No account linked to face: " + subjectName));
        String token = jwtUtil.generateToken(user.getUsername(), user.getRole().name());
        return buildAuthResponse(user, token);
    }

    private AuthResponse buildAuthResponse(User user, String token) {
        String fullName = user.getUsername();
        Long profileId = null;

        switch (user.getRole()) {
            case STUDENT -> {
                Optional<Student> s = studentRepository.findBySubjectName(user.getSubjectName());
                if (s.isPresent()) { fullName = s.get().getFullName(); profileId = s.get().getId(); }
            }
            case TEACHER -> {
                Optional<Teacher> t = teacherRepository.findBySubjectName(user.getSubjectName());
                if (t.isPresent()) { fullName = t.get().getFullName(); profileId = t.get().getId(); }
            }
            default -> {}
        }
        return new AuthResponse(token, user.getRole().name(), fullName, profileId, user.getSubjectName());
    }
}

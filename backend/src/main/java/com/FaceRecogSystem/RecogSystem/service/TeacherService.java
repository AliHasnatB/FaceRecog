package com.FaceRecogSystem.RecogSystem.service;

import com.FaceRecogSystem.RecogSystem.dto.TeacherRequest;
import com.FaceRecogSystem.RecogSystem.entity.*;
import com.FaceRecogSystem.RecogSystem.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.Optional;

@Service
public class TeacherService {

    @Autowired private TeacherRepository teacherRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private DepartmentRepository departmentRepository;
    @Autowired private CompareFaceService compareFaceService;
    @Autowired private PasswordEncoder passwordEncoder;

    @Transactional
    public Teacher registerTeacher(TeacherRequest req, MultipartFile faceImage) throws Exception {
        if (teacherRepository.existsByEmployeeId(req.getEmployeeId()))
            throw new RuntimeException("Employee ID already exists");
        if (userRepository.existsByUsername(req.getUsername()))
            throw new RuntimeException("Username already taken");

        // Optional face registration for teacher face-login
        if (faceImage != null && !faceImage.isEmpty() && req.getSubjectName() != null) {
            compareFaceService.addFace(req.getSubjectName(), faceImage);
        }

        User user = new User();
        user.setUsername(req.getUsername());
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        user.setEmail(req.getEmail());
        user.setRole(User.Role.TEACHER);
        user.setSubjectName(req.getSubjectName());
        userRepository.save(user);

        Teacher teacher = new Teacher();
        teacher.setSubjectName(req.getSubjectName());
        teacher.setFullName(req.getFullName());
        teacher.setEmail(req.getEmail());
        teacher.setEmployeeId(req.getEmployeeId());
        teacher.setDesignation(req.getDesignation());
        teacher.setUser(user);
        if (req.getDepartmentId() != null)
            departmentRepository.findById(req.getDepartmentId()).ifPresent(teacher::setDepartment);

        return teacherRepository.save(teacher);
    }

    public List<Teacher> getAll() { return teacherRepository.findAll(); }
    public Optional<Teacher> getById(Long id) { return teacherRepository.findById(id); }
    public List<Teacher> getByDepartment(Long deptId) { return teacherRepository.findByDepartmentId(deptId); }
}

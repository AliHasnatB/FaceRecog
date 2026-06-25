package com.FaceRecogSystem.RecogSystem.service;

import com.FaceRecogSystem.RecogSystem.dto.StudentRequest;
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
public class StudentService {

    @Autowired private StudentRepository studentRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private DepartmentRepository departmentRepository;
    @Autowired private CompareFaceService compareFaceService;
    @Autowired private PasswordEncoder passwordEncoder;

    @Transactional
    public Student registerStudent(StudentRequest req, MultipartFile faceImage) throws Exception {
        if (studentRepository.existsBySubjectName(req.getSubjectName()))
            throw new RuntimeException("Subject name already exists");
        if (studentRepository.existsByRollNumber(req.getRollNumber()))
            throw new RuntimeException("Roll number already exists");
        if (userRepository.existsByUsername(req.getUsername()))
            throw new RuntimeException("Username already taken");

        // Register face
        boolean faceAdded = compareFaceService.addFace(req.getSubjectName(), faceImage);
        if (!faceAdded) throw new RuntimeException("Face registration failed");

        // Create user account
        User user = new User();
        user.setUsername(req.getUsername());
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        user.setEmail(req.getEmail());
        user.setRole(User.Role.STUDENT);
        user.setSubjectName(req.getSubjectName());
        userRepository.save(user);

        // Create student record
        Student student = new Student();
        student.setSubjectName(req.getSubjectName());
        student.setFullName(req.getFullName());
        student.setEmail(req.getEmail());
        student.setRollNumber(req.getRollNumber());
        student.setSemester(req.getSemester());
        student.setUser(user);
        if (req.getDepartmentId() != null)
            departmentRepository.findById(req.getDepartmentId()).ifPresent(student::setDepartment);

        return studentRepository.save(student);
    }

    public List<Student> getAllStudents() { return studentRepository.findAll(); }

    public Optional<Student> getById(Long id) { return studentRepository.findById(id); }

    public Optional<Student> getBySubjectName(String subjectName) {
        return studentRepository.findBySubjectName(subjectName);
    }

    public List<Student> getByDepartment(Long deptId) {
        return studentRepository.findByDepartmentId(deptId);
    }

    @Transactional
    public void deleteStudent(Long id) {
        Student s = studentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Student not found"));
        compareFaceService.deleteFace(s.getSubjectName());
        if (s.getUser() != null) userRepository.delete(s.getUser());
        studentRepository.delete(s);
    }
}

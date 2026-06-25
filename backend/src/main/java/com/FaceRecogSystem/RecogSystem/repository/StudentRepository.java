package com.FaceRecogSystem.RecogSystem.repository;

import com.FaceRecogSystem.RecogSystem.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, Long> {
    Optional<Student> findBySubjectName(String subjectName);
    Optional<Student> findByRollNumber(String rollNumber);
    boolean existsBySubjectName(String subjectName);
    boolean existsByRollNumber(String rollNumber);
    List<Student> findByDepartmentId(Long departmentId);
    List<Student> findByDepartmentIdAndSemester(Long departmentId, String semester);
}

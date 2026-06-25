package com.FaceRecogSystem.RecogSystem.repository;

import com.FaceRecogSystem.RecogSystem.entity.Teacher;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface TeacherRepository extends JpaRepository<Teacher, Long> {
    Optional<Teacher> findBySubjectName(String subjectName);
    Optional<Teacher> findByEmployeeId(String employeeId);
    boolean existsBySubjectName(String subjectName);
    boolean existsByEmployeeId(String employeeId);
    List<Teacher> findByDepartmentId(Long departmentId);
}

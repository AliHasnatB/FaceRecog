package com.FaceRecogSystem.RecogSystem.repository;

import com.FaceRecogSystem.RecogSystem.entity.SubjectClass;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SubjectClassRepository extends JpaRepository<SubjectClass, Long> {
    List<SubjectClass> findByTeacherId(Long teacherId);
    List<SubjectClass> findByDepartmentId(Long departmentId);
    List<SubjectClass> findByDepartmentIdAndSemester(Long departmentId, String semester);
    List<SubjectClass> findByDepartmentIdAndActive(Long departmentId, boolean active);
}

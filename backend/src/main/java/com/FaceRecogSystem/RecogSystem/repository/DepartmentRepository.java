package com.FaceRecogSystem.RecogSystem.repository;
import com.FaceRecogSystem.RecogSystem.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;
public interface DepartmentRepository extends JpaRepository<Department, Long> {
    boolean existsByName(String name);
}

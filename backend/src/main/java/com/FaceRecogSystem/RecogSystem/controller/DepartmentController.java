package com.FaceRecogSystem.RecogSystem.controller;

import com.FaceRecogSystem.RecogSystem.entity.Department;
import com.FaceRecogSystem.RecogSystem.repository.DepartmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/departments")
public class DepartmentController {

    @Autowired private DepartmentRepository departmentRepository;

    @GetMapping
    public ResponseEntity<?> all() {
        return ResponseEntity.ok(departmentRepository.findAll());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> create(@RequestBody Department department) {
        if (departmentRepository.existsByName(department.getName()))
            return ResponseEntity.badRequest().body("Department already exists");
        return ResponseEntity.ok(departmentRepository.save(department));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        departmentRepository.deleteById(id);
        return ResponseEntity.ok("Deleted");
    }
}

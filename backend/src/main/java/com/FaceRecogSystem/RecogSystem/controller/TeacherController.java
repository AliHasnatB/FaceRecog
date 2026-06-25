package com.FaceRecogSystem.RecogSystem.controller;

import com.FaceRecogSystem.RecogSystem.dto.TeacherRequest;
import com.FaceRecogSystem.RecogSystem.service.TeacherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/teachers")
public class TeacherController {

    @Autowired private TeacherService teacherService;

    @PostMapping("/register")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> register(
            @RequestPart("data") TeacherRequest req,
            @RequestPart(value = "faceImage", required = false) MultipartFile faceImage) {
        try {
            return ResponseEntity.ok(teacherService.registerTeacher(req, faceImage));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER','STUDENT')")
    public ResponseEntity<?> getAll() { return ResponseEntity.ok(teacherService.getAll()); }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        return teacherService.getById(id).map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/department/{deptId}")
    public ResponseEntity<?> getByDept(@PathVariable Long deptId) {
        return ResponseEntity.ok(teacherService.getByDepartment(deptId));
    }
}

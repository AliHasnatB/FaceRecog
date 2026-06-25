package com.FaceRecogSystem.RecogSystem.controller;

import com.FaceRecogSystem.RecogSystem.dto.ClassScheduleRequest;
import com.FaceRecogSystem.RecogSystem.service.SubjectClassService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/classes")
public class SubjectClassController {

    @Autowired private SubjectClassService classService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public ResponseEntity<?> create(@Valid @RequestBody ClassScheduleRequest req) {
        try { return ResponseEntity.ok(classService.createClass(req)); }
        catch (RuntimeException e) { return ResponseEntity.badRequest().body(e.getMessage()); }
    }

    @GetMapping
    public ResponseEntity<?> all() { return ResponseEntity.ok(classService.getAll()); }

    @GetMapping("/teacher/{teacherId}")
    public ResponseEntity<?> byTeacher(@PathVariable Long teacherId) {
        return ResponseEntity.ok(classService.getByTeacher(teacherId));
    }

    @GetMapping("/department/{deptId}")
    public ResponseEntity<?> byDept(@PathVariable Long deptId) {
        return ResponseEntity.ok(classService.getByDepartment(deptId));
    }

    @GetMapping("/department/{deptId}/semester/{semester}")
    public ResponseEntity<?> byDeptSemester(@PathVariable Long deptId,
                                             @PathVariable String semester) {
        return ResponseEntity.ok(classService.getByDepartmentAndSemester(deptId, semester));
    }

    @PutMapping("/{id}/toggle")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public ResponseEntity<?> toggle(@PathVariable Long id) {
        classService.toggleActive(id);
        return ResponseEntity.ok("Toggled");
    }
}

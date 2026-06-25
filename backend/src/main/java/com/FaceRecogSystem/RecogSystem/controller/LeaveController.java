package com.FaceRecogSystem.RecogSystem.controller;

import com.FaceRecogSystem.RecogSystem.dto.LeaveRequest;
import com.FaceRecogSystem.RecogSystem.service.LeaveService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/leaves")
public class LeaveController {

    @Autowired private LeaveService leaveService;

    @PostMapping("/apply")
    @PreAuthorize("hasAnyRole('STUDENT','ADMIN')")
    public ResponseEntity<?> apply(@Valid @RequestBody LeaveRequest req) {
        try { return ResponseEntity.ok(leaveService.applyLeave(req)); }
        catch (RuntimeException e) { return ResponseEntity.badRequest().body(e.getMessage()); }
    }

    @GetMapping("/student/{studentId}")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER','STUDENT')")
    public ResponseEntity<?> byStudent(@PathVariable Long studentId) {
        return ResponseEntity.ok(leaveService.getByStudent(studentId));
    }

    @GetMapping("/pending")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public ResponseEntity<?> pending() { return ResponseEntity.ok(leaveService.getPending()); }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public ResponseEntity<?> all() { return ResponseEntity.ok(leaveService.getAll()); }

    @PutMapping("/{id}/review")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public ResponseEntity<?> review(@PathVariable Long id,
                                    @RequestParam String status,
                                    Authentication auth) {
        try {
            return ResponseEntity.ok(leaveService.reviewLeave(id, status, auth.getName()));
        } catch (Exception e) { return ResponseEntity.badRequest().body(e.getMessage()); }
    }

    @GetMapping("/count/{studentId}")
    public ResponseEntity<?> count(@PathVariable Long studentId) {
        return ResponseEntity.ok(leaveService.countLeavesByStudent(studentId));
    }
}

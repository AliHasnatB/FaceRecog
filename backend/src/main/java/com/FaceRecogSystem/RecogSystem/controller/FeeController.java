package com.FaceRecogSystem.RecogSystem.controller;

import com.FaceRecogSystem.RecogSystem.service.FeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/fees")
public class FeeController {

    @Autowired private FeeService feeService;

    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> create(
            @RequestParam Long studentId,
            @RequestParam String semester,
            @RequestParam double totalAmount,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dueDate) {
        return ResponseEntity.ok(feeService.createFee(studentId, semester, totalAmount, dueDate));
    }

    @PostMapping("/{feeId}/pay")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> pay(@PathVariable Long feeId,
                                  @RequestParam double amount,
                                  @RequestParam(required = false) String transactionId) {
        try { return ResponseEntity.ok(feeService.recordPayment(feeId, amount, transactionId)); }
        catch (RuntimeException e) { return ResponseEntity.badRequest().body(e.getMessage()); }
    }

    @GetMapping("/student/{studentId}")
    @PreAuthorize("hasAnyRole('ADMIN','STUDENT')")
    public ResponseEntity<?> byStudent(@PathVariable Long studentId) {
        return ResponseEntity.ok(feeService.getByStudent(studentId));
    }

    @GetMapping("/student/{studentId}/summary")
    @PreAuthorize("hasAnyRole('ADMIN','STUDENT')")
    public ResponseEntity<?> summary(@PathVariable Long studentId) {
        return ResponseEntity.ok(feeService.getStudentFeeSummary(studentId));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> all() { return ResponseEntity.ok(feeService.getAll()); }
}

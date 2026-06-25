package com.FaceRecogSystem.RecogSystem.controller;

import com.FaceRecogSystem.RecogSystem.service.AttendanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/attendance")
public class AttendanceController {

    @Autowired private AttendanceService attendanceService;

    @GetMapping("/today")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public ResponseEntity<?> today() {
        return ResponseEntity.ok(attendanceService.getTodayAttendance());
    }

    @GetMapping("/student/{subjectName}")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER','STUDENT')")
    public ResponseEntity<?> byStudent(@PathVariable String subjectName) {
        return ResponseEntity.ok(attendanceService.getByStudent(subjectName));
    }

    @GetMapping("/date/{date}")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public ResponseEntity<?> byDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(attendanceService.getByDate(date));
    }

    @GetMapping("/range")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public ResponseEntity<?> range(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        return ResponseEntity.ok(attendanceService.getBetweenDates(start, end));
    }

    @GetMapping("/stats/{subjectName}")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER','STUDENT')")
    public ResponseEntity<?> stats(@PathVariable String subjectName) {
        return ResponseEntity.ok(attendanceService.getStudentStats(subjectName));
    }
}

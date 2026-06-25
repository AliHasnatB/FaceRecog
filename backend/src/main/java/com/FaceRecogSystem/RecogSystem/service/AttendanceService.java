package com.FaceRecogSystem.RecogSystem.service;

import com.FaceRecogSystem.RecogSystem.entity.Attendance;
import com.FaceRecogSystem.RecogSystem.entity.Student;
import com.FaceRecogSystem.RecogSystem.repository.AttendanceRepository;
import com.FaceRecogSystem.RecogSystem.repository.StudentRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class AttendanceService {

    @Autowired private AttendanceRepository attendanceRepository;
    @Autowired private StudentRepository studentRepository;

    @Transactional
    public String markAttendance(String subjectName, double similarity) {
        Optional<Student> studentOpt = studentRepository.findBySubjectName(subjectName);
        if (studentOpt.isEmpty()) return "STUDENT_NOT_FOUND";
        if (attendanceRepository.existsBySubjectNameAndAttendanceDate(subjectName, LocalDate.now()))
            return "ALREADY_MARKED";

        Attendance a = new Attendance();
        a.setSubjectName(subjectName);
        a.setFullName(studentOpt.get().getFullName());
        a.setSimilarity(similarity);
        a.setStatus(Attendance.AttendanceStatus.PRESENT);
        attendanceRepository.save(a);
        return "MARKED";
    }

    public List<Attendance> getTodayAttendance() {
        return attendanceRepository.findTodaysAttendance(LocalDate.now());
    }

    public List<Attendance> getByStudent(String subjectName) {
        return attendanceRepository.findBySubjectNameOrderByAttendanceDateDesc(subjectName);
    }

    public List<Attendance> getByDate(LocalDate date) {
        return attendanceRepository.findByAttendanceDateOrderByMarkedAtAsc(date);
    }

    public List<Attendance> getBetweenDates(LocalDate start, LocalDate end) {
        return attendanceRepository.findByAttendanceDateBetweenOrderByAttendanceDateDesc(start, end);
    }

    public Map<String, Object> getStudentStats(String subjectName) {
        long present = attendanceRepository.countBySubjectName(subjectName);
        Map<String, Object> stats = new HashMap<>();
        stats.put("presentDays", present);
        stats.put("subjectName", subjectName);
        return stats;
    }
}

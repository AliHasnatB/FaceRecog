package com.FaceRecogSystem.RecogSystem.service;

import com.FaceRecogSystem.RecogSystem.dto.LeaveRequest;
import com.FaceRecogSystem.RecogSystem.entity.Leave;
import com.FaceRecogSystem.RecogSystem.entity.Student;
import com.FaceRecogSystem.RecogSystem.repository.LeaveRepository;
import com.FaceRecogSystem.RecogSystem.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class LeaveService {

    @Autowired private LeaveRepository leaveRepository;
    @Autowired private StudentRepository studentRepository;

    @Transactional
    public Leave applyLeave(LeaveRequest req) {
        Student student = studentRepository.findById(req.getStudentId())
                .orElseThrow(() -> new RuntimeException("Student not found"));
        Leave leave = new Leave();
        leave.setStudent(student);
        leave.setFromDate(req.getFromDate());
        leave.setToDate(req.getToDate());
        leave.setReason(req.getReason());
        leave.setStatus(Leave.LeaveStatus.PENDING);
        return leaveRepository.save(leave);
    }

    public List<Leave> getByStudent(Long studentId) {
        return leaveRepository.findByStudentIdOrderByAppliedAtDesc(studentId);
    }

    public List<Leave> getPending() {
        return leaveRepository.findByStatusOrderByAppliedAtDesc(Leave.LeaveStatus.PENDING);
    }

    public List<Leave> getAll() {
        return leaveRepository.findAllByOrderByAppliedAtDesc();
    }

    @Transactional
    public Leave reviewLeave(Long leaveId, String status, String reviewerName) {
        Leave leave = leaveRepository.findById(leaveId)
                .orElseThrow(() -> new RuntimeException("Leave not found"));
        leave.setStatus(Leave.LeaveStatus.valueOf(status));
        leave.setReviewedBy(reviewerName);
        leave.setReviewedAt(LocalDateTime.now());
        return leaveRepository.save(leave);
    }

    public long countLeavesByStudent(Long studentId) {
        return leaveRepository.countByStudentIdAndStatus(studentId, Leave.LeaveStatus.APPROVED);
    }
}

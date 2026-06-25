package com.FaceRecogSystem.RecogSystem.repository;

import com.FaceRecogSystem.RecogSystem.entity.Leave;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface LeaveRepository extends JpaRepository<Leave, Long> {
    List<Leave> findByStudentIdOrderByAppliedAtDesc(Long studentId);
    List<Leave> findByStatusOrderByAppliedAtDesc(Leave.LeaveStatus status);
    long countByStudentIdAndStatus(Long studentId, Leave.LeaveStatus status);
    List<Leave> findAllByOrderByAppliedAtDesc();
}

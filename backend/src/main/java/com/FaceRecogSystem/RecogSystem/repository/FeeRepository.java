package com.FaceRecogSystem.RecogSystem.repository;

import com.FaceRecogSystem.RecogSystem.entity.Fee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface FeeRepository extends JpaRepository<Fee, Long> {
    List<Fee> findByStudentIdOrderByCreatedAtDesc(Long studentId);
    List<Fee> findByStatus(Fee.FeeStatus status);

    @Query("SELECT COALESCE(SUM(f.totalAmount),0) FROM Fee f WHERE f.student.id = :sid")
    double sumTotalByStudentId(@Param("sid") Long studentId);

    @Query("SELECT COALESCE(SUM(f.paidAmount),0) FROM Fee f WHERE f.student.id = :sid")
    double sumPaidByStudentId(@Param("sid") Long studentId);
}

package com.FaceRecogSystem.RecogSystem.repository;

import com.FaceRecogSystem.RecogSystem.entity.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    boolean existsBySubjectNameAndAttendanceDate(String subjectName, LocalDate date);
    List<Attendance> findByAttendanceDateOrderByMarkedAtAsc(LocalDate date);
    List<Attendance> findBySubjectNameOrderByAttendanceDateDesc(String subjectName);
    List<Attendance> findByAttendanceDateBetweenOrderByAttendanceDateDesc(LocalDate start, LocalDate end);
    long countBySubjectName(String subjectName);
    long countBySubjectNameAndStatus(String subjectName, Attendance.AttendanceStatus status);

    @Query("SELECT a FROM Attendance a WHERE a.attendanceDate = :date")
    List<Attendance> findTodaysAttendance(@Param("date") LocalDate date);

    @Query("SELECT COUNT(a) FROM Attendance a WHERE a.subjectClass.id = :classId AND a.attendanceDate BETWEEN :start AND :end")
    long countByClassAndDateRange(@Param("classId") Long classId,
                                  @Param("start") LocalDate start,
                                  @Param("end") LocalDate end);
}

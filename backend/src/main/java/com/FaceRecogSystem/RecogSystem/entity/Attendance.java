package com.FaceRecogSystem.RecogSystem.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "attendance",
       uniqueConstraints = @UniqueConstraint(columnNames = {"subjectName", "attendanceDate", "classId"}))
public class Attendance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String subjectName;

    @Column(nullable = false)
    private String fullName;

    @Column(nullable = false)
    private double similarity;

    @Column(nullable = false)
    private LocalDate attendanceDate;

    @Column(nullable = false)
    private LocalDateTime markedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AttendanceStatus status = AttendanceStatus.PRESENT;

    /** Optional link to which class this attendance belongs */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_id")
    private SubjectClass subjectClass;

    @PrePersist
    public void prePersist() {
        this.attendanceDate = LocalDate.now();
        this.markedAt = LocalDateTime.now();
    }

    public enum AttendanceStatus { PRESENT, ABSENT, LATE }
}

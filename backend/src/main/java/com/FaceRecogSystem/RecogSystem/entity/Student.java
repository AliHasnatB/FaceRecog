package com.FaceRecogSystem.RecogSystem.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "students")
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** CompareFace subject name — unique identifier */
    @Column(nullable = false, unique = true)
    private String subjectName;

    @Column(nullable = false)
    private String fullName;

    @Column
    private String email;

    @Column(nullable = false, unique = true)
    private String rollNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    /** Semester / batch year */
    @Column
    private String semester;

    /** Linked user account */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() { this.createdAt = LocalDateTime.now(); }
}

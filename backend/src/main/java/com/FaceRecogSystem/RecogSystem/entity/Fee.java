package com.FaceRecogSystem.RecogSystem.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "fees")
public class Fee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @Column(nullable = false)
    private String semester;

    @Column(nullable = false)
    private double totalAmount;

    @Column(nullable = false)
    private double paidAmount;

    @Column(nullable = false)
    private LocalDate dueDate;

    @Column
    private LocalDate paidDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FeeStatus status = FeeStatus.PENDING;

    @Column
    private String transactionId;

    @Column
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() { this.createdAt = LocalDateTime.now(); }

    public double getPendingAmount() { return totalAmount - paidAmount; }

    public enum FeeStatus { PENDING, PARTIAL, PAID, OVERDUE }
}

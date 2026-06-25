package com.FaceRecogSystem.RecogSystem.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "departments")
public class Department {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;   // e.g. "Computer Science"

    @Column
    private String code;   // e.g. "CS"

    @Column
    private String description;
}

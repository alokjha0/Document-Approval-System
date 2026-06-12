package com.company.das.department.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;


@Entity
@Table(name = "departments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Department {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
            name = "department_name",
            nullable = false,
            length = 100
    )
    private String departmentName;

    @Column(nullable = false)
    private Boolean isDeleted = false;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private LocalDateTime deletedAt;

    @PrePersist
    public void prePersist() {

        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();

        if (this.isDeleted == null) {
            this.isDeleted = false;
        }
    }

    @PreUpdate
    public void preUpdate() {

        this.updatedAt = LocalDateTime.now();
    }
}
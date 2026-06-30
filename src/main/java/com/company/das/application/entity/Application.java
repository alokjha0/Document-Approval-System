package com.company.das.application.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

import com.company.das.department.entity.Department;

@Entity
@Table(name = "applications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Application {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "department_id",
            nullable = false
    )
    private Department department;

    @Column(
            nullable = false,
            length = 100
    )
    private String applicationName;

    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    private Boolean isDeleted = false;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private LocalDateTime deletedAt;

    @PrePersist
    public void prePersist() {

        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();

        if (isDeleted == null) {
            isDeleted = false;
        }
    }

    @PreUpdate
    public void preUpdate() {

        updatedAt = LocalDateTime.now();
    }
}
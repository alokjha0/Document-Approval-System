package com.company.das.application.entity;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

import com.company.das.common.enums.WorkflowStage;
import com.company.das.department.entity.Department;

@Entity
@Table(
        name = "hierarchy_steps",
        uniqueConstraints = {
                @UniqueConstraint(
                        columnNames = {
                                "hierarchy_id",
                                "step_order"
                        })
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HierarchyStep {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "hierarchy_id",
            nullable = false
    )
    private Hierarchy hierarchy;

    @Column(nullable = false)
    private Integer stepOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WorkflowStage stage;

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

package com.company.das.application.entity;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "application_hierarchy_mapping",
        uniqueConstraints = {
                @UniqueConstraint(
                        columnNames = {
                                "application_id"
                        })
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplicationHierarchyMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "application_id",
            nullable = false
    )
    private Application application;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "hierarchy_id",
            nullable = false
    )
    private Hierarchy hierarchy;

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

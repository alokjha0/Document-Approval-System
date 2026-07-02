package com.company.das.document.entity;

import com.company.das.application.entity.Application;
import com.company.das.common.enums.DocumentStatus;
import com.company.das.department.entity.Department;
import com.company.das.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "documents")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
            unique = true,
            nullable = false
    )
    private String documentNumber;

    @Column(nullable = false)
    private String title;

    @Column(length = 1000)
    private String description;
    
    @Column(columnDefinition = "TEXT", nullable = false)
    private String documentContent;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DocumentStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    private User owner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "application_id",
            nullable = false
    )
    private Application application;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {

        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {

        updatedAt = LocalDateTime.now();
    }
}
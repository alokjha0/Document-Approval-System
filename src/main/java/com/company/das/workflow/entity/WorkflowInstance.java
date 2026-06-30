package com.company.das.workflow.entity;

import com.company.das.common.enums.WorkflowStage;
import com.company.das.common.enums.WorkflowStatus;
import com.company.das.document.entity.Document;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "workflow_instances")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkflowInstance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(
            name = "document_id",
            nullable = false
    )
    private Document document;

    @Enumerated(EnumType.STRING)
    private WorkflowStatus status;

    @Enumerated(EnumType.STRING)
    private WorkflowStage currentStage;
    
    @Column(nullable = false)
    private Integer currentStep;

    private LocalDateTime startedAt;

    private LocalDateTime completedAt;

    @PrePersist
    public void prePersist() {

        startedAt = LocalDateTime.now();
    }
}
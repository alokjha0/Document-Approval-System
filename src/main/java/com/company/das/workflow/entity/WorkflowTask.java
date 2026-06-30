package com.company.das.workflow.entity;

import com.company.das.common.enums.TaskStatus;
import com.company.das.common.enums.WorkflowStage;
import com.company.das.department.entity.Department;
import com.company.das.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "workflow_tasks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkflowTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(
            name = "workflow_instance_id",
            nullable = false
    )
    private WorkflowInstance workflowInstance;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "department_id",
            nullable = false
    )
    private Department department;
    
    @ManyToOne
    @JoinColumn(name = "action_taken_by")
    private User actionTakenBy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WorkflowStage stage;
    
    @Column(nullable = false)
    private Integer stepOrder;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskStatus status = TaskStatus.PENDING;
    
    
    private LocalDateTime assignedAt;

    private LocalDateTime completedAt;

    @PrePersist
    public void prePersist() {

        assignedAt = LocalDateTime.now();
    }
}
package com.company.das.comment.entity;

import com.company.das.common.enums.CommentType;
import com.company.das.document.entity.Document;
import com.company.das.user.entity.User;
import com.company.das.workflow.entity.WorkflowTask;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "document_comments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentComment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "document_id",
            nullable = false
    )
    private Document document;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "workflow_task_id"
    )
    private WorkflowTask workflowTask;

    @Column(
            nullable = false,
            length = 2000
    )
    private String comment;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CommentType commentType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "commented_by",
            nullable = false
    )
    private User commentedBy;

    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {

        createdAt = LocalDateTime.now();
    }
}
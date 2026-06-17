package com.company.das.audit.entity;

import com.company.das.common.enums.AuditAction;
import com.company.das.common.enums.DocumentStatus;
import com.company.das.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long documentId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuditAction action;

    @Enumerated(EnumType.STRING)
    private DocumentStatus fromStatus;

    @Enumerated(EnumType.STRING)
    private DocumentStatus toStatus;

    @Column(length = 1000)
    private String remarks;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "performed_by")
    private User performedBy;

    private LocalDateTime actionAt;

    @PrePersist
    public void prePersist() {
        actionAt = LocalDateTime.now();
    }
}
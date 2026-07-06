package com.company.das.workflow.dto;

import java.time.LocalDateTime;

import com.company.das.common.enums.AuditAction;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkflowHistoryDto {

    private AuditAction action;
    
    private String actionDisplayName;
    
    private String actionIcon;

    private String performedBy;

    private String designation;

    private String remarks;

    private String comment;

    private LocalDateTime actionTime;

    private Integer versionNumber;

    private Long versionId;

    private boolean pdfAvailable;

}
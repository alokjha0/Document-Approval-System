package com.company.das.workflow.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewDocumentDto {

    private Long taskId;

    private Long documentId;

    private String documentNumber;

    private String title;

    private String description;

    private String submittedBy;

    private String requestorDepartment;

    private String status;

    private String currentStage;
}
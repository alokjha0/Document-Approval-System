package com.company.das.workflow.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewTaskDto {

    private Long taskId;

    private Long documentId;

    private String documentNumber;

    private String title;
    
    private String requestorDepartment;

    private String submittedBy;

    private String stage;

    private String status;
}
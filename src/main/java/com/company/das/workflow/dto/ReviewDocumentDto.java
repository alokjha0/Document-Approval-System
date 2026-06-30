package com.company.das.workflow.dto;

import com.company.das.common.enums.CommentType;

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
    
//Additional fields for comments and workflow
    
    private String latestComment;

    private String latestCommentBy;

    private String latestCommentStage;

    private String latestCommentDepartment;

    private String latestCommentTime;
    
    private CommentType latestCommentType;

}
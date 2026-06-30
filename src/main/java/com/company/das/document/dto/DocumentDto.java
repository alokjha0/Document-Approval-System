package com.company.das.document.dto;

import com.company.das.common.enums.CommentType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentDto {

    private Long id;
    
    @NotNull(message = "Application is required")
    private Long applicationId;
    
    private String applicationName;
    
    private String documentNumber;

    @NotBlank(message = "Title is required")
    @Size(max = 200)
    private String title;

    @NotBlank(message = "Description is required")
    @Size(max = 1000)
    private String description;

    @NotNull(message = "Department is required")
    private Long departmentId;
    
    private String status;
    
    private String currentStage;

    private String departmentName;
    
//Additional fields for comments and workflow
    
    private String latestComment;

    private String latestCommentBy;

    private String latestCommentStage;

    private String latestCommentDepartment;

    private String latestCommentTime;
    
    private String responseComment;
    
    private CommentType latestCommentType;

}
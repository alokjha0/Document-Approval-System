package com.company.das.comment.dto;

import com.company.das.common.enums.CommentType;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentCommentDto {

    private Long id;

    private String comment;

    private CommentType commentType;

    private String commentedBy;

    private String stage;

    private String department;

    private String createdAt;
}
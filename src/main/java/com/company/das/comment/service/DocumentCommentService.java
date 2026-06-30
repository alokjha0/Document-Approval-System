package com.company.das.comment.service;

import com.company.das.comment.dto.DocumentCommentDto;
import com.company.das.common.enums.CommentType;

public interface DocumentCommentService {

	DocumentCommentDto getLatestComment(Long documentId, CommentType commentType);

}
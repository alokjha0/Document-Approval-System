package com.company.das.workflow.service;

import com.company.das.workflow.dto.ReviewDocumentDto;
import com.company.das.workflow.dto.ReviewTaskDto;

import java.util.List;

public interface WorkflowService {

    List<ReviewTaskDto> getReviewerTasks(
            String reviewerEmail
    );
    
    void approveByReviewer(
            Long taskId,
            String reviewerEmail
    );
    
    ReviewDocumentDto getDocumentForReview(
            Long taskId,
            String reviewerEmail
    );
    
    void requestInfo(
            Long taskId,
            String reviewerEmail
    );

    void rejectDocument(
            Long taskId,
            String reviewerEmail
    );
}
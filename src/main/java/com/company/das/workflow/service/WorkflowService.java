package com.company.das.workflow.service;

import com.company.das.workflow.dto.ReviewTaskDto;

import java.util.List;

public interface WorkflowService {

    List<ReviewTaskDto> getReviewerTasks(
            String reviewerEmail
    );
}
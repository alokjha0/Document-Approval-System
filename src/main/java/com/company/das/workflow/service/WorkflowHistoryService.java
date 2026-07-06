package com.company.das.workflow.service;

import java.util.List;

import com.company.das.workflow.dto.WorkflowHistoryDto;

public interface WorkflowHistoryService {

    List<WorkflowHistoryDto> getWorkflowHistory(Long documentId);

}
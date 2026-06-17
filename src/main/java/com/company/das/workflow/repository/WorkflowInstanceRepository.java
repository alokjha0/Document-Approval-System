package com.company.das.workflow.repository;

import com.company.das.workflow.entity.WorkflowInstance;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkflowInstanceRepository
        extends JpaRepository<WorkflowInstance, Long> {
}
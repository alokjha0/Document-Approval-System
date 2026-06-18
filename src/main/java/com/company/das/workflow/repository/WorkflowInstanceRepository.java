package com.company.das.workflow.repository;

import com.company.das.document.entity.Document;
import com.company.das.workflow.entity.WorkflowInstance;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkflowInstanceRepository
        extends JpaRepository<WorkflowInstance, Long> {
	Optional<WorkflowInstance> findByDocument(
	        Document document);
}
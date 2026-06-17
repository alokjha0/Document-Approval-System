package com.company.das.workflow.repository;

import com.company.das.workflow.entity.WorkflowTask;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import com.company.das.common.enums.TaskStatus;
import com.company.das.common.enums.WorkflowStage;
import com.company.das.department.entity.Department;

public interface WorkflowTaskRepository
        extends JpaRepository<WorkflowTask, Long> {
	
	 List<WorkflowTask>
	    findByDepartmentAndStageAndStatus(
	            Department department,
	            WorkflowStage stage,
	            TaskStatus status
	    );
}
package com.company.das.workflow.service.impl;

import com.company.das.common.enums.TaskStatus;

import com.company.das.common.enums.WorkflowStage;
import com.company.das.department.entity.Department;
import com.company.das.user.entity.User;
import com.company.das.user.entity.UserRole;
import com.company.das.user.repository.UserRepository;
import com.company.das.workflow.dto.ReviewTaskDto;
import com.company.das.workflow.entity.WorkflowTask;
import com.company.das.workflow.repository.WorkflowTaskRepository;
import com.company.das.workflow.service.WorkflowService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WorkflowServiceImpl
        implements WorkflowService {

    private final WorkflowTaskRepository workflowTaskRepository;

    private final UserRepository userRepository;

    @Override
    public List<ReviewTaskDto> getReviewerTasks(
            String reviewerEmail) {

        User reviewer =
                userRepository
                        .findByEmailAndIsDeletedFalse(
                                reviewerEmail)
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "Reviewer not found"));

        if(reviewer.getRole()
                != UserRole.REVIEWER) {

            throw new RuntimeException(
                    "Only reviewers can access this page");
        }

        Department department =
                reviewer.getDepartment();

        return workflowTaskRepository
                .findByDepartmentAndStageAndStatus(
                        department,
                        WorkflowStage.REVIEWER,
                        TaskStatus.PENDING)
                .stream()
                .map(task ->

                        ReviewTaskDto.builder()
                                .taskId(task.getId())
                                .documentId(
                                        task.getWorkflowInstance()
                                                .getDocument()
                                                .getId())
                                .documentNumber(
                                        task.getWorkflowInstance()
                                                .getDocument()
                                                .getDocumentNumber())
                                .title(
                                        task.getWorkflowInstance()
                                                .getDocument()
                                                .getTitle())
                                .requestorDepartment(
                                        task.getWorkflowInstance()
                                                .getDocument()
                                                .getOwner()
                                                .getDepartment()
                                                .getDepartmentName())
                                .submittedBy(
                                        task.getWorkflowInstance()
                                                .getDocument()
                                                .getOwner()
                                                .getName())
                                .stage(
                                        task.getStage().name())
                                .status(
                                        task.getStatus().name())
                                .build()

                )
                .toList();
    }
}
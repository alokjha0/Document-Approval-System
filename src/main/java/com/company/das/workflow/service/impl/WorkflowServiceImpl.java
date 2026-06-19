package com.company.das.workflow.service.impl;

import com.company.das.audit.entity.AuditLog;
import com.company.das.audit.repository.AuditLogRepository;
import com.company.das.common.enums.AuditAction;
import com.company.das.common.enums.DocumentStatus;
import com.company.das.common.enums.TaskStatus;

import com.company.das.common.enums.WorkflowStage;
import com.company.das.common.enums.WorkflowStatus;
import com.company.das.department.entity.Department;
import com.company.das.document.entity.Document;
import com.company.das.document.repository.DocumentRepository;
import com.company.das.user.entity.User;
import com.company.das.user.entity.UserRole;
import com.company.das.user.repository.UserRepository;
import com.company.das.workflow.dto.ReviewDocumentDto;
import com.company.das.workflow.dto.ReviewTaskDto;
import com.company.das.workflow.entity.WorkflowInstance;
import com.company.das.workflow.entity.WorkflowTask;
import com.company.das.workflow.repository.WorkflowInstanceRepository;
import com.company.das.workflow.repository.WorkflowTaskRepository;
import com.company.das.workflow.service.WorkflowService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WorkflowServiceImpl implements WorkflowService {

	private final WorkflowTaskRepository workflowTaskRepository;

	private final UserRepository userRepository;

	private final WorkflowInstanceRepository workflowInstanceRepository;

	private final AuditLogRepository auditLogRepository;

	private final DocumentRepository documentRepository;

	@Override
	public List<ReviewTaskDto> getReviewerTasks(String reviewerEmail) {

		User reviewer = userRepository.findByEmailAndIsDeletedFalse(reviewerEmail)
				.orElseThrow(() -> new RuntimeException("Reviewer not found"));

		if (reviewer.getRole() != UserRole.REVIEWER) {

			throw new RuntimeException("Only reviewers can access this page");
		}

		Department department = reviewer.getDepartment();

		return workflowTaskRepository
				.findByDepartmentAndStageAndStatus(department, WorkflowStage.REVIEWER, TaskStatus.PENDING).stream()
				.map(task ->

				ReviewTaskDto.builder().taskId(task.getId())
						.documentId(task.getWorkflowInstance().getDocument().getId())
						.workflowInstanceId(task.getWorkflowInstance().getId())
						.documentNumber(task.getWorkflowInstance().getDocument().getDocumentNumber())
						.title(task.getWorkflowInstance().getDocument().getTitle())
						.requestorDepartment(
								task.getWorkflowInstance().getDocument().getOwner().getDepartment().getDepartmentName())
						.submittedBy(task.getWorkflowInstance().getDocument().getOwner().getName())
						.stage(task.getStage().name()).status(task.getStatus().name()).build()

				).toList();
	}

	@Override
	@Transactional
	public void approveByReviewer(Long taskId, String reviewerEmail) {

		User reviewer = userRepository.findByEmailAndIsDeletedFalse(reviewerEmail)
				.orElseThrow(() -> new RuntimeException("Reviewer not found"));

		WorkflowTask currentTask = workflowTaskRepository.findById(taskId)
				.orElseThrow(() -> new RuntimeException("Task not found"));

		if (currentTask.getStatus() != TaskStatus.PENDING) {

			throw new RuntimeException("Task already processed");
		}

		currentTask.setStatus(TaskStatus.COMPLETED);

		currentTask.setCompletedAt(LocalDateTime.now());

		currentTask.setActionTakenBy(reviewer);

		workflowTaskRepository.save(currentTask);

		WorkflowInstance workflowInstance = currentTask.getWorkflowInstance();

		Document document = workflowInstance.getDocument();

		document.setStatus(DocumentStatus.UNDER_REVIEW);

		documentRepository.save(document);

		workflowInstance.setCurrentStage(WorkflowStage.APPROVER);

		workflowInstanceRepository.save(workflowInstance);

		WorkflowTask nextTask = WorkflowTask.builder().workflowInstance(workflowInstance)
				.department(currentTask.getDepartment()).stage(WorkflowStage.APPROVER).status(TaskStatus.PENDING)
				.build();

		workflowTaskRepository.save(nextTask);

		AuditLog auditLog = AuditLog.builder().documentId(workflowInstance.getDocument().getId())
				.action(AuditAction.REVIEW_APPROVED).performedBy(reviewer).remarks("Reviewer approved document")
				.fromStatus(DocumentStatus.SUBMITTED).toStatus(DocumentStatus.UNDER_REVIEW).build();

		auditLogRepository.save(auditLog);
	}

	@Override
	public ReviewDocumentDto getDocumentForReview(Long taskId, String reviewerEmail) {

		User reviewer = userRepository.findByEmailAndIsDeletedFalse(reviewerEmail)
				.orElseThrow(() -> new RuntimeException("User not found"));

		WorkflowTask task = workflowTaskRepository.findById(taskId)
				.orElseThrow(() -> new RuntimeException("Task not found"));

		if (!task.getDepartment().getId().equals(reviewer.getDepartment().getId())) {

			throw new RuntimeException("Unauthorized access");
		}

		Document document = task.getWorkflowInstance().getDocument();

		return ReviewDocumentDto.builder().taskId(task.getId()).documentId(document.getId())
				.documentNumber(document.getDocumentNumber()).title(document.getTitle())
				.description(document.getDescription()).submittedBy(document.getOwner().getName())
				.requestorDepartment(document.getOwner().getDepartment().getDepartmentName())
				.status(document.getStatus().name()).currentStage(task.getWorkflowInstance().getCurrentStage().name())
				.build();
	}

	@Override
	@Transactional
	public void requestInfo(Long taskId, String reviewerEmail) {

		User reviewer = userRepository.findByEmailAndIsDeletedFalse(reviewerEmail)
				.orElseThrow(() -> new RuntimeException("Reviewer not found"));

		WorkflowTask task = workflowTaskRepository.findById(taskId)
				.orElseThrow(() -> new RuntimeException("Task not found"));

		if (task.getStatus() != TaskStatus.PENDING) {

			throw new RuntimeException("Task already processed");
		}

		task.setStatus(TaskStatus.COMPLETED);

		task.setCompletedAt(LocalDateTime.now());

		task.setActionTakenBy(reviewer);

		workflowTaskRepository.save(task);

		Document document = task.getWorkflowInstance().getDocument();

		document.setStatus(DocumentStatus.ADDITIONAL_INFO_REQUESTED);

		documentRepository.save(document);

		AuditLog auditLog = AuditLog.builder().documentId(document.getId()).action(AuditAction.INFO_REQUESTED)
				.fromStatus(DocumentStatus.SUBMITTED).toStatus(DocumentStatus.ADDITIONAL_INFO_REQUESTED)
				.performedBy(reviewer).remarks("Additional information requested").build();

		auditLogRepository.save(auditLog);
	}

	@Override
	@Transactional
	public void rejectDocument(Long taskId, String reviewerEmail) {

		User reviewer = userRepository.findByEmailAndIsDeletedFalse(reviewerEmail)
				.orElseThrow(() -> new RuntimeException("Reviewer not found"));

		WorkflowTask task = workflowTaskRepository.findById(taskId)
				.orElseThrow(() -> new RuntimeException("Task not found"));

		if (task.getStatus() != TaskStatus.PENDING) {

			throw new RuntimeException("Task already processed");
		}

		// Complete Current Task

		task.setStatus(TaskStatus.COMPLETED);

		task.setCompletedAt(LocalDateTime.now());

		task.setActionTakenBy(reviewer);

		workflowTaskRepository.save(task);

		// Reject Document

		Document document = task.getWorkflowInstance().getDocument();

		document.setStatus(DocumentStatus.REJECTED);

		documentRepository.save(document);

		// Complete Workflow Instance

		WorkflowInstance workflowInstance = task.getWorkflowInstance();

		workflowInstance.setStatus(WorkflowStatus.COMPLETED);

		workflowInstance.setCompletedAt(LocalDateTime.now());

		workflowInstanceRepository.save(workflowInstance);

		// Audit Log

		AuditLog auditLog = AuditLog.builder().documentId(document.getId()).action(AuditAction.DOCUMENT_REJECTED)
				.fromStatus(DocumentStatus.SUBMITTED).toStatus(DocumentStatus.REJECTED).performedBy(reviewer)
				.remarks("Document rejected").build();

		auditLogRepository.save(auditLog);
	}

	@Override
	public List<ReviewTaskDto> getApproverTasks(String approverEmail) {

		User approver = userRepository.findByEmailAndIsDeletedFalse(approverEmail)
				.orElseThrow(() -> new RuntimeException("Approver not found"));

		if (approver.getRole() != UserRole.APPROVER) {

			throw new RuntimeException("Only approvers can access this page");
		}

		Department department = approver.getDepartment();

		return workflowTaskRepository
				.findByDepartmentAndStageAndStatus(department, WorkflowStage.APPROVER, TaskStatus.PENDING).stream()
				.map(task ->

				ReviewTaskDto.builder().taskId(task.getId())
						.documentId(task.getWorkflowInstance().getDocument().getId())
						.documentNumber(task.getWorkflowInstance().getDocument().getDocumentNumber())
						.title(task.getWorkflowInstance().getDocument().getTitle())
						.submittedBy(task.getWorkflowInstance().getDocument().getOwner().getName())
						.requestorDepartment(
								task.getWorkflowInstance().getDocument().getOwner().getDepartment().getDepartmentName())
						.stage(task.getStage().name()).status(task.getStatus().name()).build()

				).toList();
	}

	@Override
	@Transactional
	public void approveByApprover(Long taskId, String approverEmail) {

		User approver = userRepository.findByEmailAndIsDeletedFalse(approverEmail)
				.orElseThrow(() -> new RuntimeException("Approver not found"));

		WorkflowTask currentTask = workflowTaskRepository.findById(taskId)
				.orElseThrow(() -> new RuntimeException("Task not found"));

		if (currentTask.getStatus() != TaskStatus.PENDING) {

			throw new RuntimeException("Task already processed");
		}

		currentTask.setStatus(TaskStatus.COMPLETED);

		currentTask.setCompletedAt(LocalDateTime.now());

		currentTask.setActionTakenBy(approver);

		workflowTaskRepository.save(currentTask);

		WorkflowInstance workflowInstance = currentTask.getWorkflowInstance();

		Document document = workflowInstance.getDocument();

		document.setStatus(DocumentStatus.UNDER_REVIEW);

		documentRepository.save(document);

		workflowInstance.setCurrentStage(WorkflowStage.SENIOR_APPROVER);

		workflowInstanceRepository.save(workflowInstance);

		WorkflowTask nextTask = WorkflowTask.builder().workflowInstance(workflowInstance)
				.department(currentTask.getDepartment()).stage(WorkflowStage.SENIOR_APPROVER).status(TaskStatus.PENDING)
				.build();

		workflowTaskRepository.save(nextTask);

		AuditLog auditLog = AuditLog.builder().documentId(document.getId()).action(AuditAction.APPROVER_APPROVED)
				.fromStatus(DocumentStatus.UNDER_REVIEW).toStatus(DocumentStatus.UNDER_REVIEW).performedBy(approver)
				.remarks("Approved by approver").build();

		auditLogRepository.save(auditLog);
	}
	
	@Override
	@Transactional
	public void requestInfoByApprover(
	        Long taskId,
	        String approverEmail) {

	    User approver =
	            userRepository
	                    .findByEmailAndIsDeletedFalse(
	                            approverEmail)
	                    .orElseThrow(
	                            () -> new RuntimeException(
	                                    "Approver not found"));

	    WorkflowTask task =
	            workflowTaskRepository
	                    .findById(taskId)
	                    .orElseThrow(
	                            () -> new RuntimeException(
	                                    "Task not found"));

	    if (task.getStatus()
	            != TaskStatus.PENDING) {

	        throw new RuntimeException(
	                "Task already processed");
	    }

	    task.setStatus(
	            TaskStatus.COMPLETED);

	    task.setCompletedAt(
	            LocalDateTime.now());

	    task.setActionTakenBy(
	            approver);

	    workflowTaskRepository.save(
	            task);

	    Document document =
	            task.getWorkflowInstance()
	                    .getDocument();

	    document.setStatus(
	            DocumentStatus.ADDITIONAL_INFO_REQUESTED);

	    documentRepository.save(
	            document);

	    AuditLog auditLog =
	            AuditLog.builder()
	                    .documentId(
	                            document.getId())
	                    .action(
	                            AuditAction.INFO_REQUESTED)
	                    .fromStatus(
	                            DocumentStatus.UNDER_REVIEW)
	                    .toStatus(
	                            DocumentStatus.ADDITIONAL_INFO_REQUESTED)
	                    .performedBy(
	                            approver)
	                    .remarks(
	                            "Additional information requested by approver")
	                    .build();

	    auditLogRepository.save(
	            auditLog);
	}
	
	@Override
	@Transactional
	public void rejectByApprover(Long taskId, String approverEmail) {

		User approver = userRepository.findByEmailAndIsDeletedFalse(approverEmail)
				.orElseThrow(() -> new RuntimeException("Approver not found"));

		WorkflowTask task = workflowTaskRepository.findById(taskId)
				.orElseThrow(() -> new RuntimeException("Task not found"));
		if (task.getStatus() != TaskStatus.PENDING) {

			throw new RuntimeException("Task already processed");
		}
		// Complete Current Task
		task.setStatus(TaskStatus.COMPLETED);
		task.setCompletedAt(LocalDateTime.now());
		task.setActionTakenBy(approver);
		workflowTaskRepository.save(task);
		
		// Reject Document
		Document document = task.getWorkflowInstance().getDocument();
		document.setStatus(DocumentStatus.REJECTED);
		documentRepository.save(document);
		
		// Complete Workflow Instance
		WorkflowInstance workflowInstance = task.getWorkflowInstance();
		workflowInstance.setStatus(WorkflowStatus.COMPLETED);
		workflowInstance.setCompletedAt(LocalDateTime.now());
		workflowInstanceRepository.save(workflowInstance);
			
		// Audit Log
		AuditLog auditLog = AuditLog.builder()
				.documentId(document.getId())
				.action(AuditAction.DOCUMENT_REJECTED)
				.fromStatus(DocumentStatus.UNDER_REVIEW)
				.toStatus(DocumentStatus.REJECTED)
				.performedBy(approver)
				.remarks("Document rejected by approver")
				.build();
		auditLogRepository.save(auditLog);
		}
		
	

	@Override
	public List<ReviewTaskDto> getSeniorApproverTasks(String approverEmail) {

		User seniorApprover = userRepository.findByEmailAndIsDeletedFalse(approverEmail)
				.orElseThrow(() -> new RuntimeException("Senior Approver not found"));

		if (seniorApprover.getRole() != UserRole.SENIOR_APPROVER) {

			throw new RuntimeException("Only senior approvers can access this page");
		}

		Department department = seniorApprover.getDepartment();

		return workflowTaskRepository
				.findByDepartmentAndStageAndStatus(department, WorkflowStage.SENIOR_APPROVER, TaskStatus.PENDING)
				.stream().map(task ->

				ReviewTaskDto.builder().taskId(task.getId())
						.documentId(task.getWorkflowInstance().getDocument().getId())
						.documentNumber(task.getWorkflowInstance().getDocument().getDocumentNumber())
						.title(task.getWorkflowInstance().getDocument().getTitle())
						.submittedBy(task.getWorkflowInstance().getDocument().getOwner().getName())
						.requestorDepartment(
								task.getWorkflowInstance().getDocument().getOwner().getDepartment().getDepartmentName())
						.stage(task.getStage().name()).status(task.getStatus().name()).build()

				).toList();
	}

	@Override
	@Transactional
	public void approveBySeniorApprover(Long taskId, String email) {

		User seniorApprover = userRepository.findByEmailAndIsDeletedFalse(email)
				.orElseThrow(() -> new RuntimeException("Senior Approver not found"));

		WorkflowTask currentTask = workflowTaskRepository.findById(taskId)
				.orElseThrow(() -> new RuntimeException("Task not found"));

		if (currentTask.getStatus() != TaskStatus.PENDING) {

			throw new RuntimeException("Task already processed");
		}

		currentTask.setStatus(TaskStatus.COMPLETED);

		currentTask.setCompletedAt(LocalDateTime.now());

		currentTask.setActionTakenBy(seniorApprover);

		workflowTaskRepository.save(currentTask);

		WorkflowInstance workflowInstance = currentTask.getWorkflowInstance();

		Document document = workflowInstance.getDocument();

		document.setStatus(DocumentStatus.APPROVED);

		documentRepository.save(document);

		workflowInstance.setStatus(WorkflowStatus.COMPLETED);

		workflowInstance.setCompletedAt(LocalDateTime.now());

		workflowInstanceRepository.save(workflowInstance);

		AuditLog auditLog = AuditLog.builder().documentId(document.getId()).action(AuditAction.SENIOR_APPROVER_APPROVED)
				.fromStatus(DocumentStatus.UNDER_REVIEW).toStatus(DocumentStatus.APPROVED).performedBy(seniorApprover)
				.remarks("Approved by senior approver").build();

		auditLogRepository.save(auditLog);
	}
	
	
	@Override
	@Transactional
	public void requestInfoBySeniorApprover(
	        Long taskId,
	        String seniorApproverEmail) {

	    User seniorApprover =
	            userRepository
	                    .findByEmailAndIsDeletedFalse(
	                            seniorApproverEmail)
	                    .orElseThrow(
	                            () -> new RuntimeException(
	                                    "Senior Approver not found"));

	    WorkflowTask task =
	            workflowTaskRepository
	                    .findById(taskId)
	                    .orElseThrow(
	                            () -> new RuntimeException(
	                                    "Task not found"));

	    if (task.getStatus()
	            != TaskStatus.PENDING) {

	        throw new RuntimeException(
	                "Task already processed");
	    }

	    task.setStatus(
	            TaskStatus.COMPLETED);

	    task.setCompletedAt(
	            LocalDateTime.now());

	    task.setActionTakenBy(
	            seniorApprover);

	    workflowTaskRepository.save(
	            task);

	    Document document =
	            task.getWorkflowInstance()
	                    .getDocument();

	    document.setStatus(
	            DocumentStatus.ADDITIONAL_INFO_REQUESTED);

	    documentRepository.save(
	            document);

	    AuditLog auditLog =
	            AuditLog.builder()
	                    .documentId(
	                            document.getId())
	                    .action(
	                            AuditAction.INFO_REQUESTED)
	                    .fromStatus(
	                            DocumentStatus.UNDER_REVIEW)
	                    .toStatus(
	                            DocumentStatus.ADDITIONAL_INFO_REQUESTED)
	                    .performedBy(
	                            seniorApprover)
	                    .remarks(
	                            "Additional information requested by senior approver")
	                    .build();

	    auditLogRepository.save(
	            auditLog);
	}
	
	@Override
	@Transactional
	public void rejectBySeniorApprover(Long taskId, String seniorApproverEmail) {

		User seniorApprover = userRepository.findByEmailAndIsDeletedFalse(seniorApproverEmail)
				.orElseThrow(() -> new RuntimeException("Senior Approver not found"));

		WorkflowTask task = workflowTaskRepository.findById(taskId)
				.orElseThrow(() -> new RuntimeException("Task not found"));

		if (task.getStatus() != TaskStatus.PENDING) {

			throw new RuntimeException("Task already processed");
		}

		// Complete Current Task

		task.setStatus(TaskStatus.COMPLETED);

		task.setCompletedAt(LocalDateTime.now());

		task.setActionTakenBy(seniorApprover);

		workflowTaskRepository.save(task);

		// Reject Document

		Document document = task.getWorkflowInstance().getDocument();

		document.setStatus(DocumentStatus.REJECTED);

		documentRepository.save(document);

		// Complete Workflow Instance

		WorkflowInstance workflowInstance = task.getWorkflowInstance();

		workflowInstance.setStatus(WorkflowStatus.COMPLETED);

		workflowInstance.setCompletedAt(LocalDateTime.now());

		workflowInstanceRepository.save(workflowInstance);

		// Audit Log

		AuditLog auditLog = AuditLog.builder().documentId(document.getId()).action(AuditAction.DOCUMENT_REJECTED)
				.fromStatus(DocumentStatus.UNDER_REVIEW).toStatus(DocumentStatus.REJECTED).performedBy(seniorApprover)
				.remarks("Document rejected by senior approver").build();

		auditLogRepository.save(auditLog);
	}
	
}
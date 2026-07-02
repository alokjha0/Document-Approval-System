package com.company.das.workflow.service.impl;

import com.company.das.application.entity.Application;
import com.company.das.application.entity.ApplicationHierarchyMapping;
import com.company.das.application.entity.HierarchyStep;
import com.company.das.application.repository.ApplicationHierarchyMappingRepository;
import com.company.das.application.repository.HierarchyStepRepository;
import com.company.das.audit.entity.AuditLog;
import com.company.das.audit.repository.AuditLogRepository;
import com.company.das.comment.dto.DocumentCommentDto;
import com.company.das.comment.entity.DocumentComment;
import com.company.das.comment.repository.DocumentCommentRepository;
import com.company.das.comment.service.DocumentCommentService;
import com.company.das.common.enums.AuditAction;
import com.company.das.common.enums.CommentType;
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

	private final ApplicationHierarchyMappingRepository applicationHierarchyMappingRepository;

	private final HierarchyStepRepository hierarchyStepRepository;

	private final DocumentCommentRepository documentCommentRepository;

	private final DocumentCommentService documentCommentService;

	@Transactional
	private void requestInfoInternal(Long taskId, String comment, String email, AuditAction action, String remarks) {

		User user = userRepository.findByEmailAndIsDeletedFalse(email)
				.orElseThrow(() -> new RuntimeException("User not found"));

		WorkflowTask task = workflowTaskRepository.findById(taskId)
				.orElseThrow(() -> new RuntimeException("Task not found"));

		if (task.getStatus() != TaskStatus.PENDING) {

			throw new RuntimeException("Task already processed");
		}

		WorkflowInstance workflowInstance = task.getWorkflowInstance();

		if (workflowInstance.getStatus() == WorkflowStatus.COMPLETED) {

			throw new RuntimeException("Workflow already completed");
		}

		task.setStatus(TaskStatus.COMPLETED);

		task.setCompletedAt(LocalDateTime.now());

		task.setActionTakenBy(user);

		workflowTaskRepository.save(task);

		Document document = workflowInstance.getDocument();

		DocumentStatus previousStatus = document.getStatus();

		document.setStatus(DocumentStatus.ADDITIONAL_INFO_REQUESTED);

		documentRepository.save(document);

		DocumentComment documentComment = DocumentComment.builder().document(document).workflowTask(task)
				.comment(comment).commentType(CommentType.REQUEST_INFO).commentedBy(user).build();

		documentCommentRepository.save(documentComment);

		AuditLog auditLog = AuditLog.builder().documentId(document.getId()).action(action).fromStatus(previousStatus)
				.toStatus(DocumentStatus.ADDITIONAL_INFO_REQUESTED).performedBy(user).remarks(remarks).build();

		auditLogRepository.save(auditLog);
	}

	@Transactional
	private void rejectInternal(Long taskId, String comment, String email, AuditAction action, String remarks) {

		User user = userRepository.findByEmailAndIsDeletedFalse(email)
				.orElseThrow(() -> new RuntimeException("User not found"));

		WorkflowTask task = workflowTaskRepository.findById(taskId)
				.orElseThrow(() -> new RuntimeException("Task not found"));

		if (task.getStatus() != TaskStatus.PENDING) {

			throw new RuntimeException("Task already processed");
		}

		WorkflowInstance workflowInstance = task.getWorkflowInstance();

		if (workflowInstance.getStatus() == WorkflowStatus.COMPLETED) {

			throw new RuntimeException("Workflow already completed");
		}

		// Complete Current Task

		task.setStatus(TaskStatus.COMPLETED);

		task.setCompletedAt(LocalDateTime.now());

		task.setActionTakenBy(user);

		workflowTaskRepository.save(task);

		// Reject Document

		Document document = workflowInstance.getDocument();

		DocumentStatus previousStatus = document.getStatus();

		document.setStatus(DocumentStatus.REJECTED);

		documentRepository.save(document);

		DocumentComment documentComment = DocumentComment.builder().document(document).workflowTask(task)
				.comment(comment).commentType(CommentType.REJECTION).commentedBy(user).build();

		documentCommentRepository.save(documentComment);

		// Complete Workflow

		workflowInstance.setStatus(WorkflowStatus.COMPLETED);

		workflowInstance.setCompletedAt(LocalDateTime.now());

		workflowInstanceRepository.save(workflowInstance);

		// Audit Log

		AuditLog auditLog = AuditLog.builder().documentId(document.getId()).action(action).fromStatus(previousStatus)
				.toStatus(DocumentStatus.REJECTED).performedBy(user).remarks(remarks).build();

		auditLogRepository.save(auditLog);
	}

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

				ReviewTaskDto.builder()
						.taskId(task.getId())
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

		Application application = document.getApplication();

		ApplicationHierarchyMapping mapping = applicationHierarchyMappingRepository
				.findByApplicationAndIsDeletedFalse(application)
				.orElseThrow(() -> new RuntimeException("Workflow mapping not found"));

		List<HierarchyStep> hierarchySteps = hierarchyStepRepository
				.findByHierarchyAndIsDeletedFalseOrderByStepOrderAsc(mapping.getHierarchy());

		HierarchyStep nextStep = hierarchySteps.stream()
				.filter(step -> step.getStepOrder() > currentTask.getStepOrder()).findFirst().orElse(null);

		// NO NEXT STEP -> WORKFLOW COMPLETED

		if (nextStep == null) {

			document.setStatus(DocumentStatus.APPROVED);

			documentRepository.save(document);

			workflowInstance.setStatus(WorkflowStatus.COMPLETED);

			workflowInstance.setCompletedAt(LocalDateTime.now());

			workflowInstanceRepository.save(workflowInstance);

			AuditLog auditLog = AuditLog.builder().documentId(document.getId()).action(AuditAction.REVIEW_APPROVED)
					.performedBy(reviewer).remarks("Final workflow step approved")
					.fromStatus(DocumentStatus.UNDER_REVIEW).toStatus(DocumentStatus.APPROVED).build();

			auditLogRepository.save(auditLog);

			return;
		}

		Department targetDepartment;

		if (nextStep.getDepartment() == null) {

			targetDepartment = document.getDepartment();

		} else {

			targetDepartment = nextStep.getDepartment();
		}

		workflowInstance.setCurrentStep(nextStep.getStepOrder());

		workflowInstance.setCurrentStage(nextStep.getStage());

		workflowInstanceRepository.save(workflowInstance);

		WorkflowTask nextTask = WorkflowTask.builder().workflowInstance(workflowInstance).department(targetDepartment)
				.stage(nextStep.getStage()).stepOrder(nextStep.getStepOrder()).status(TaskStatus.PENDING).build();

		workflowTaskRepository.save(nextTask);

		AuditLog auditLog = AuditLog.builder().documentId(document.getId()).action(AuditAction.REVIEW_APPROVED)
				.performedBy(reviewer).remarks("Reviewer approved document").fromStatus(DocumentStatus.SUBMITTED)
				.toStatus(DocumentStatus.UNDER_REVIEW).build();

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

		DocumentCommentDto latestComment = documentCommentService.getLatestComment(document.getId(),
				CommentType.RESPONSE);

		return ReviewDocumentDto.builder().taskId(task.getId()).documentId(document.getId())
				.documentNumber(document.getDocumentNumber()).title(document.getTitle())
				.description(document.getDescription()).submittedBy(document.getOwner().getName())
				.requestorDepartment(document.getOwner().getDepartment().getDepartmentName())
				.status(document.getStatus().name()).currentStage(task.getWorkflowInstance().getCurrentStage().name())
				.latestComment(latestComment != null ? latestComment.getComment() : null)

				.latestCommentBy(latestComment != null ? latestComment.getCommentedBy() : null)

				.latestCommentStage(latestComment != null ? latestComment.getStage() : null)

				.latestCommentDepartment(latestComment != null ? latestComment.getDepartment() : null)

				.latestCommentTime(latestComment != null ? latestComment.getCreatedAt() : null)

				.latestCommentType(latestComment != null ? latestComment.getCommentType() : null)

				.build();
	}

	@Override
	@Transactional
	public void requestInfo(Long taskId, String reviewerEmail, String comment) {

		requestInfoInternal(taskId, comment, reviewerEmail, AuditAction.INFO_REQUESTED,
				"Additional information requested by reviewer");
	}

	@Override
	@Transactional
	public void rejectDocument(Long taskId, String reviewerEmail, String comment) {

		rejectInternal(taskId, comment, reviewerEmail, AuditAction.DOCUMENT_REJECTED, "Document rejected by reviewer");
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

		Application application = document.getApplication();

		ApplicationHierarchyMapping mapping = applicationHierarchyMappingRepository
				.findByApplicationAndIsDeletedFalse(application)
				.orElseThrow(() -> new RuntimeException("Workflow mapping not found"));

		List<HierarchyStep> hierarchySteps = hierarchyStepRepository
				.findByHierarchyAndIsDeletedFalseOrderByStepOrderAsc(mapping.getHierarchy());

		HierarchyStep nextStep = hierarchySteps.stream()
				.filter(step -> step.getStepOrder() > currentTask.getStepOrder()).findFirst().orElse(null);

		// FINAL STEP REACHED

		if (nextStep == null) {

			document.setStatus(DocumentStatus.APPROVED);

			documentRepository.save(document);

			workflowInstance.setStatus(WorkflowStatus.COMPLETED);

			workflowInstance.setCompletedAt(LocalDateTime.now());

			workflowInstanceRepository.save(workflowInstance);

			AuditLog auditLog = AuditLog.builder().documentId(document.getId()).action(AuditAction.APPROVER_APPROVED)
					.performedBy(approver).remarks("Final workflow step approved")
					.fromStatus(DocumentStatus.UNDER_REVIEW).toStatus(DocumentStatus.APPROVED).build();

			auditLogRepository.save(auditLog);

			return;
		}

		Department targetDepartment;

		if (nextStep.getDepartment() == null) {

			targetDepartment = document.getDepartment();

		} else {

			targetDepartment = nextStep.getDepartment();
		}

		workflowInstance.setCurrentStep(nextStep.getStepOrder());

		workflowInstance.setCurrentStage(nextStep.getStage());

		workflowInstanceRepository.save(workflowInstance);

		WorkflowTask nextTask = WorkflowTask.builder().workflowInstance(workflowInstance).department(targetDepartment)
				.stage(nextStep.getStage()).stepOrder(nextStep.getStepOrder()).status(TaskStatus.PENDING).build();

		workflowTaskRepository.save(nextTask);

		AuditLog auditLog = AuditLog.builder().documentId(document.getId()).action(AuditAction.APPROVER_APPROVED)
				.performedBy(approver).remarks("Approver approved document").fromStatus(DocumentStatus.UNDER_REVIEW)
				.toStatus(DocumentStatus.UNDER_REVIEW).build();

		auditLogRepository.save(auditLog);
	}

	@Override
	@Transactional
	public void requestInfoByApprover(Long taskId, String approverEmail, String comment) {

		requestInfoInternal(taskId, comment, approverEmail, AuditAction.INFO_REQUESTED,
				"Additional information requested by approver");
	}

	@Override
	@Transactional
	public void rejectByApprover(Long taskId, String approverEmail, String comment) {

		rejectInternal(taskId, comment, approverEmail, AuditAction.DOCUMENT_REJECTED, "Document rejected by approver");
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

		document.setStatus(DocumentStatus.UNDER_REVIEW);

		documentRepository.save(document);

		Application application = document.getApplication();

		ApplicationHierarchyMapping mapping = applicationHierarchyMappingRepository
				.findByApplicationAndIsDeletedFalse(application)
				.orElseThrow(() -> new RuntimeException("Workflow mapping not found"));

		List<HierarchyStep> hierarchySteps = hierarchyStepRepository
				.findByHierarchyAndIsDeletedFalseOrderByStepOrderAsc(mapping.getHierarchy());

		HierarchyStep nextStep = hierarchySteps.stream()
				.filter(step -> step.getStepOrder() > currentTask.getStepOrder()).findFirst().orElse(null);

		// FINAL STEP REACHED

		if (nextStep == null) {

			document.setStatus(DocumentStatus.APPROVED);

			documentRepository.save(document);

			workflowInstance.setStatus(WorkflowStatus.COMPLETED);

			workflowInstance.setCompletedAt(LocalDateTime.now());

			workflowInstanceRepository.save(workflowInstance);

			AuditLog auditLog = AuditLog.builder().documentId(document.getId())
					.action(AuditAction.SENIOR_APPROVER_APPROVED).performedBy(seniorApprover)
					.remarks("Final workflow step approved").fromStatus(DocumentStatus.UNDER_REVIEW)
					.toStatus(DocumentStatus.APPROVED).build();

			auditLogRepository.save(auditLog);

			return;
		}

		Department targetDepartment;

		if (nextStep.getDepartment() == null) {

			targetDepartment = document.getDepartment();

		} else {

			targetDepartment = nextStep.getDepartment();
		}

		workflowInstance.setCurrentStep(nextStep.getStepOrder());

		workflowInstance.setCurrentStage(nextStep.getStage());

		workflowInstanceRepository.save(workflowInstance);

		WorkflowTask nextTask = WorkflowTask.builder().workflowInstance(workflowInstance).department(targetDepartment)
				.stage(nextStep.getStage()).stepOrder(nextStep.getStepOrder()).status(TaskStatus.PENDING).build();

		workflowTaskRepository.save(nextTask);

		AuditLog auditLog = AuditLog.builder().documentId(document.getId()).action(AuditAction.SENIOR_APPROVER_APPROVED)
				.performedBy(seniorApprover).remarks("Senior Approver approved document")
				.fromStatus(DocumentStatus.UNDER_REVIEW).toStatus(DocumentStatus.UNDER_REVIEW).build();

		auditLogRepository.save(auditLog);
	}

	@Override
	@Transactional
	public void requestInfoBySeniorApprover(Long taskId, String seniorApproverEmail, String comment) {

		requestInfoInternal(taskId, comment, seniorApproverEmail, AuditAction.INFO_REQUESTED,
				"Additional information requested by senior approver");
	}

	@Override
	@Transactional
	public void rejectBySeniorApprover(Long taskId, String seniorApproverEmail, String comment) {

		rejectInternal(taskId, comment, seniorApproverEmail, AuditAction.DOCUMENT_REJECTED,
				"Document rejected by senior approver");
	}

}
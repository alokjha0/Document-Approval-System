package com.company.das.document.service.impl;

import com.company.das.application.entity.Application;
import com.company.das.application.entity.ApplicationHierarchyMapping;
import com.company.das.application.entity.HierarchyStep;
import com.company.das.application.repository.ApplicationHierarchyMappingRepository;
import com.company.das.application.repository.ApplicationRepository;
import com.company.das.application.repository.HierarchyStepRepository;
import com.company.das.audit.entity.AuditLog;
import com.company.das.audit.repository.AuditLogRepository;
import com.company.das.comment.dto.DocumentCommentDto;
import com.company.das.comment.entity.DocumentComment;
import com.company.das.comment.repository.DocumentCommentRepository;
import com.company.das.comment.service.DocumentCommentService;
import com.company.das.common.enums.*;
import com.company.das.common.exception.ResourceNotFoundException;
import com.company.das.department.entity.Department;
import com.company.das.department.repository.DepartmentRepository;
import com.company.das.document.dto.DocumentDto;
import com.company.das.document.entity.Document;
import com.company.das.document.repository.DocumentRepository;
import com.company.das.document.service.DocumentService;
import com.company.das.documentversion.service.DocumentVersionService;
import com.company.das.user.entity.User;
import com.company.das.user.repository.UserRepository;
import com.company.das.workflow.entity.WorkflowInstance;
import com.company.das.workflow.entity.WorkflowTask;
import com.company.das.workflow.repository.WorkflowInstanceRepository;
import com.company.das.workflow.repository.WorkflowTaskRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class DocumentServiceImpl implements DocumentService {

	private final DocumentRepository documentRepository;

	private final DepartmentRepository departmentRepository;

	private final UserRepository userRepository;

	private final WorkflowInstanceRepository workflowInstanceRepository;

	private final WorkflowTaskRepository workflowTaskRepository;

	private final AuditLogRepository auditLogRepository;

	private final ApplicationRepository applicationRepository;

	private final ApplicationHierarchyMappingRepository applicationHierarchyMappingRepository;

	private final HierarchyStepRepository hierarchyStepRepository;

	private final DocumentCommentRepository documentCommentRepository;

	private final DocumentCommentService documentCommentService;
	
	private final DocumentVersionService documentVersionService;

	@Override
	public void createDocument(DocumentDto documentDto, String loggedInUserEmail) {

		User owner = userRepository.findByEmailAndIsDeletedFalse(loggedInUserEmail)
				.orElseThrow(() -> new RuntimeException("User not found"));

		Department department = departmentRepository.findById(documentDto.getDepartmentId())
				.orElseThrow(() -> new RuntimeException("Department not found"));

		Application application = applicationRepository.findById(documentDto.getApplicationId())
				.orElseThrow(() -> new ResourceNotFoundException("Application not found"));

		String documentNumber = generateDocumentNumber();

		Document document = Document.builder().documentNumber(documentNumber).title(documentDto.getTitle())
				.description(documentDto.getDescription()).documentContent(documentDto.getDocumentContent()).department(department).application(application)
				.status(DocumentStatus.DRAFT).owner(owner).build();

		document = documentRepository.save(document);

		AuditLog auditLog = AuditLog.builder().documentId(document.getId()).action(AuditAction.DOCUMENT_CREATED)
				.toStatus(DocumentStatus.DRAFT).performedBy(owner).remarks("Document created as draft").build();

		auditLogRepository.save(auditLog);
	}

	private String generateDocumentNumber() {

		return "DOC-" + System.currentTimeMillis();
	}

	@Override
	public void submitDocument(Long documentId, String loggedInUserEmail) {

		User currentUser = userRepository.findByEmailAndIsDeletedFalse(loggedInUserEmail)
				.orElseThrow(() -> new RuntimeException("User not found"));

		Document document = documentRepository.findById(documentId)
				.orElseThrow(() -> new RuntimeException("Document not found"));

		if (!document.getOwner().getId().equals(currentUser.getId())) {

			throw new RuntimeException("Only owner can submit document");
		}

		if (document.getStatus() != DocumentStatus.DRAFT) {

			throw new RuntimeException("Only draft document can be submitted");
		}

		document.setStatus(DocumentStatus.SUBMITTED);

		documentRepository.save(document);

		Application application = document.getApplication();

		ApplicationHierarchyMapping mapping = applicationHierarchyMappingRepository
				.findByApplicationAndIsDeletedFalse(application)
				.orElseThrow(() -> new RuntimeException("Workflow mapping not found"));

		List<HierarchyStep> hierarchySteps = hierarchyStepRepository
				.findByHierarchyAndIsDeletedFalseOrderByStepOrderAsc(mapping.getHierarchy());

		if (hierarchySteps.isEmpty()) {

			throw new RuntimeException("Workflow hierarchy steps not configured");
		}

		HierarchyStep firstStep = hierarchySteps.get(0);

		Department targetDepartment;

		if (firstStep.getDepartment() == null) {

			targetDepartment = document.getDepartment();

		} else {

			targetDepartment = firstStep.getDepartment();
		}
		
		
		
		documentVersionService.createVersion(document);


		WorkflowInstance workflowInstance = WorkflowInstance.builder().document(document)
				.status(WorkflowStatus.IN_PROGRESS).currentStage(firstStep.getStage())
				.currentStep(firstStep.getStepOrder()).build();

		workflowInstance = workflowInstanceRepository.save(workflowInstance);

		WorkflowTask workflowTask = WorkflowTask.builder().workflowInstance(workflowInstance)
				.department(targetDepartment).stage(firstStep.getStage()).stepOrder(firstStep.getStepOrder())
				.status(TaskStatus.PENDING).build();

		workflowTaskRepository.save(workflowTask);

		AuditLog auditLog = AuditLog.builder().documentId(document.getId()).action(AuditAction.DOCUMENT_SUBMITTED)
				.fromStatus(DocumentStatus.DRAFT).toStatus(DocumentStatus.SUBMITTED).performedBy(currentUser)
				.remarks("Document submitted").build();

		auditLogRepository.save(auditLog);
	}

	@Override
	public List<DocumentDto> getDocumentsByOwner(String email) {

		User owner = userRepository.findByEmailAndIsDeletedFalse(email)
				.orElseThrow(() -> new RuntimeException("User not found"));

		return documentRepository.findByOwnerOrderByCreatedAtDesc(owner).stream().map(document -> {

			WorkflowInstance workflowInstance = workflowInstanceRepository.findByDocument(document).orElse(null);

			return DocumentDto.builder().id(document.getId()).documentNumber(document.getDocumentNumber())
					.title(document.getTitle()).description(document.getDescription())
					.status(document.getStatus().name()).applicationId(document.getApplication().getId())
					.applicationName(document.getApplication().getApplicationName())
					.currentStage(workflowInstance != null ? workflowInstance.getCurrentStage().name() : "-")
					.departmentId(document.getDepartment().getId())
					.departmentName(document.getDepartment().getDepartmentName()).build();
		}).toList();
	}

	@Override
	public DocumentDto getDocumentById(Long id) {

		Document document = documentRepository.findById(id)
				.orElseThrow(() -> new RuntimeException("Document not found"));

		return DocumentDto.builder().id(document.getId()).documentNumber(document.getDocumentNumber())
				.title(document.getTitle()).description(document.getDescription())
				.documentContent(document.getDocumentContent())
				.departmentId(document.getDepartment().getId()).applicationId(document.getApplication().getId())
				.applicationName(document.getApplication().getApplicationName())
				.departmentName(document.getDepartment().getDepartmentName()).status(document.getStatus().name())
				.build();
	}

	@Override
	public void updateDocument(Long id, DocumentDto documentDto, String loggedInUserEmail) {

		User currentUser = userRepository.findByEmailAndIsDeletedFalse(loggedInUserEmail)
				.orElseThrow(() -> new RuntimeException("User not found"));

		Document document = documentRepository.findById(id)
				.orElseThrow(() -> new RuntimeException("Document not found"));

		Application application = applicationRepository.findById(documentDto.getApplicationId())
				.orElseThrow(() -> new ResourceNotFoundException("Application not found"));

		if (!document.getOwner().getId().equals(currentUser.getId())) {

			throw new RuntimeException("You are not owner of this document");
		}

		if (document.getStatus() != DocumentStatus.DRAFT) {

			throw new RuntimeException("Only draft document can be edited");
		}

		Department department = departmentRepository.findById(documentDto.getDepartmentId())
				.orElseThrow(() -> new RuntimeException("Department not found"));

		document.setTitle(documentDto.getTitle());

		document.setDescription(documentDto.getDescription());
		
		document.setDocumentContent(documentDto.getDocumentContent());
		
		document.setDepartment(department);

		document.setApplication(application);

		documentDto.setApplicationId(document.getApplication().getId());

		documentRepository.save(document);

		AuditLog auditLog = AuditLog.builder().documentId(document.getId()).action(AuditAction.DOCUMENT_UPDATED)
				.performedBy(currentUser).remarks("Draft document updated").build();

		auditLogRepository.save(auditLog);
	}

	@Transactional
	public void submitResponse(Long documentId, DocumentDto documentDto, String email) {

		User employee = userRepository.findByEmailAndIsDeletedFalse(email)
				.orElseThrow(() -> new RuntimeException("User not found"));

		Document document = documentRepository.findById(documentId)
				.orElseThrow(() -> new RuntimeException("Document not found"));

		if (!document.getOwner().getId().equals(employee.getId())) {

			throw new RuntimeException("Only owner can respond");
		}

		if (document.getStatus() != DocumentStatus.ADDITIONAL_INFO_REQUESTED) {

			throw new RuntimeException("Document is not waiting for information");
		}

		document.setDescription(documentDto.getDescription());
		
		document.setDocumentContent(documentDto.getDocumentContent());

		document.setStatus(DocumentStatus.RESUBMITTED);

		documentRepository.save(document);
		
		documentVersionService.createVersion(document);

		WorkflowInstance workflowInstance = workflowInstanceRepository.findByDocument(document)
				.orElseThrow(() -> new RuntimeException("Workflow not found"));

		Application application = document.getApplication();

		ApplicationHierarchyMapping mapping = applicationHierarchyMappingRepository
				.findByApplicationAndIsDeletedFalse(application)
				.orElseThrow(() -> new RuntimeException("Workflow mapping not found"));

		List<HierarchyStep> hierarchySteps = hierarchyStepRepository
				.findByHierarchyAndIsDeletedFalseOrderByStepOrderAsc(mapping.getHierarchy());

		HierarchyStep currentStep = hierarchySteps.stream()
				.filter(step -> step.getStepOrder().equals(workflowInstance.getCurrentStep())).findFirst()
				.orElseThrow(() -> new RuntimeException("Current workflow step not found"));

		Department targetDepartment;

		if (currentStep.getDepartment() == null) {

			targetDepartment = document.getDepartment();

		} else {

			targetDepartment = currentStep.getDepartment();
		}

		WorkflowTask newTask = workflowTaskRepository.save(

				WorkflowTask.builder().workflowInstance(workflowInstance).department(targetDepartment)
						.stage(currentStep.getStage()).stepOrder(currentStep.getStepOrder()).status(TaskStatus.PENDING)
						.build()

		);

		if (documentDto.getResponseComment() == null || documentDto.getResponseComment().trim().isEmpty()) {

			throw new RuntimeException("Response comment is required.");
		}

		DocumentComment documentComment = DocumentComment.builder().document(document).workflowTask(newTask)
				.comment(documentDto.getResponseComment().trim()).commentType(CommentType.RESPONSE)
				.commentedBy(employee).build();

		documentCommentRepository.save(documentComment);

		AuditLog auditLog = AuditLog.builder().documentId(document.getId()).action(AuditAction.INFO_PROVIDED)
				.fromStatus(DocumentStatus.ADDITIONAL_INFO_REQUESTED).toStatus(DocumentStatus.RESUBMITTED)
				.performedBy(employee).remarks("Additional information provided").build();

		auditLogRepository.save(auditLog);
	}

	@Override
	@Transactional(readOnly = true)
	public DocumentDto getDocumentDetails(Long id, String email) {

		User employee = userRepository.findByEmailAndIsDeletedFalse(email)
				.orElseThrow(() -> new RuntimeException("User not found"));

		Document document = documentRepository.findById(id)
				.orElseThrow(() -> new RuntimeException("Document not found"));

		if (!document.getOwner().getId().equals(employee.getId())) {

			throw new RuntimeException("You cannot view this document");
		}

		DocumentDto dto = DocumentDto.builder().id(document.getId()).documentNumber(document.getDocumentNumber())
				.title(document.getTitle()).description(document.getDescription())
				.documentContent(document.getDocumentContent())
				.status(document.getStatus().name())
				.departmentName(document.getDepartment().getDepartmentName())
				.applicationName(document.getApplication().getApplicationName()).build();

		WorkflowInstance workflow = workflowInstanceRepository.findByDocument(document).orElse(null);

		if (workflow != null) {

			dto.setCurrentStage(workflow.getCurrentStage().name());
		}

		DocumentCommentDto latestComment;

		if (document.getStatus() == DocumentStatus.REJECTED) {

			latestComment = documentCommentService.getLatestComment(document.getId(), CommentType.REJECTION);

		} else {

			latestComment = documentCommentService.getLatestComment(document.getId(), CommentType.REQUEST_INFO);
		}

		if (latestComment != null) {

			dto.setLatestComment(latestComment.getComment());

			dto.setLatestCommentBy(latestComment.getCommentedBy());

			dto.setLatestCommentStage(latestComment.getStage());

			dto.setLatestCommentDepartment(latestComment.getDepartment());

			dto.setLatestCommentTime(latestComment.getCreatedAt());

			dto.setLatestCommentType(latestComment.getCommentType());
		}

		return dto;
	}

}
package com.company.das.document.service.impl;

import com.company.das.audit.entity.AuditLog;
import com.company.das.audit.repository.AuditLogRepository;
import com.company.das.common.enums.*;
import com.company.das.department.entity.Department;
import com.company.das.department.repository.DepartmentRepository;
import com.company.das.document.dto.DocumentDto;
import com.company.das.document.entity.Document;
import com.company.das.document.repository.DocumentRepository;
import com.company.das.document.service.DocumentService;
import com.company.das.user.entity.User;
import com.company.das.user.repository.UserRepository;
import com.company.das.workflow.entity.WorkflowInstance;
import com.company.das.workflow.entity.WorkflowTask;
import com.company.das.workflow.repository.WorkflowInstanceRepository;
import com.company.das.workflow.repository.WorkflowTaskRepository;
import jakarta.transaction.Transactional;
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

	@Override
	public void createDocument(DocumentDto documentDto, String loggedInUserEmail) {

		User owner = userRepository.findByEmailAndIsDeletedFalse(loggedInUserEmail)
				.orElseThrow(() -> new RuntimeException("User not found"));

		Department department = departmentRepository.findById(documentDto.getDepartmentId())
				.orElseThrow(() -> new RuntimeException("Department not found"));

		String documentNumber = generateDocumentNumber();

		Document document = Document.builder().documentNumber(documentNumber).title(documentDto.getTitle())
				.description(documentDto.getDescription()).status(DocumentStatus.DRAFT).owner(owner)
				.department(department).build();

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

		WorkflowInstance workflowInstance = WorkflowInstance.builder().document(document)
				.status(WorkflowStatus.IN_PROGRESS).currentStage(WorkflowStage.REVIEWER).build();

		workflowInstance = workflowInstanceRepository.save(workflowInstance);

		WorkflowTask workflowTask = WorkflowTask.builder().workflowInstance(workflowInstance)
				.department(document.getDepartment()).stage(WorkflowStage.REVIEWER).status(TaskStatus.PENDING).build();

		workflowTaskRepository.save(workflowTask);

		AuditLog auditLog = AuditLog.builder().documentId(document.getId()).action(AuditAction.DOCUMENT_SUBMITTED)
				.fromStatus(DocumentStatus.DRAFT).toStatus(DocumentStatus.SUBMITTED).performedBy(currentUser)
				.remarks("Document submitted").build();

		auditLogRepository.save(auditLog);
	}

	@Override
	public List<DocumentDto> getDocumentsByOwner(String email) {

	    User owner =
	            userRepository
	                    .findByEmailAndIsDeletedFalse(email)
	                    .orElseThrow(() ->
	                            new RuntimeException("User not found"));

	    return documentRepository
	            .findByOwnerOrderByCreatedAtDesc(owner)
	            .stream()
	            .map(document -> {

	                WorkflowInstance workflowInstance =
	                        workflowInstanceRepository
	                                .findByDocument(document)
	                                .orElse(null);

	                return DocumentDto.builder()
	                        .id(document.getId())
	                        .documentNumber(
	                                document.getDocumentNumber())
	                        .title(
	                                document.getTitle())
	                        .description(
	                                document.getDescription())
	                        .status(
	                                document.getStatus().name())
	                        .currentStage(
	                                workflowInstance != null
	                                        ? workflowInstance
	                                                .getCurrentStage()
	                                                .name()
	                                        : "-")
	                        .departmentId(
	                                document.getDepartment()
	                                        .getId())
	                        .departmentName(
	                                document.getDepartment()
	                                        .getDepartmentName())
	                        .build();
	            })
	            .toList();
	}

	@Override
	public DocumentDto getDocumentById(Long id) {

		Document document = documentRepository.findById(id)
				.orElseThrow(() -> new RuntimeException("Document not found"));

		return DocumentDto.builder().id(document.getId()).documentNumber(document.getDocumentNumber())
				.title(document.getTitle()).description(document.getDescription())
				.departmentId(document.getDepartment().getId())
				.departmentName(document.getDepartment().getDepartmentName()).status(document.getStatus().name())
				.build();
	}

	@Override
	public void updateDocument(Long id, DocumentDto documentDto, String loggedInUserEmail) {

		User currentUser = userRepository.findByEmailAndIsDeletedFalse(loggedInUserEmail)
				.orElseThrow(() -> new RuntimeException("User not found"));

		Document document = documentRepository.findById(id)
				.orElseThrow(() -> new RuntimeException("Document not found"));

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

		document.setDepartment(department);

		documentRepository.save(document);

		AuditLog auditLog = AuditLog.builder().documentId(document.getId()).action(AuditAction.DOCUMENT_UPDATED)
				.performedBy(currentUser).remarks("Draft document updated").build();

		auditLogRepository.save(auditLog);
	}
	
	@Override
	@Transactional
	public void respondToInfoRequest(
	        Long documentId,
	        String email) {

	    User employee =
	            userRepository
	                    .findByEmailAndIsDeletedFalse(
	                            email)
	                    .orElseThrow(
	                            () -> new RuntimeException(
	                                    "User not found"));

	    Document document =
	            documentRepository
	                    .findById(documentId)
	                    .orElseThrow(
	                            () -> new RuntimeException(
	                                    "Document not found"));

	    if(!document.getOwner()
	            .getId()
	            .equals(employee.getId())) {

	        throw new RuntimeException(
	                "Only owner can respond");
	    }

	    if(document.getStatus()
	            != DocumentStatus.ADDITIONAL_INFO_REQUESTED) {

	        throw new RuntimeException(
	                "Document is not waiting for information");
	    }

	    document.setStatus(
	            DocumentStatus.RESUBMITTED);

	    documentRepository.save(
	            document);

	    WorkflowInstance workflowInstance =
	            workflowInstanceRepository
	                    .findByDocument(document)
	                    .orElseThrow(
	                            () -> new RuntimeException(
	                                    "Workflow not found"));

	    workflowInstance.setCurrentStage(
	            WorkflowStage.REVIEWER);

	    workflowInstanceRepository.save(
	            workflowInstance);

	    WorkflowTask workflowTask =
	            WorkflowTask.builder()
	                    .workflowInstance(
	                            workflowInstance)
	                    .department(
	                            document.getDepartment())
	                    .stage(
	                            WorkflowStage.REVIEWER)
	                    .status(
	                            TaskStatus.PENDING)
	                    .build();

	    workflowTaskRepository.save(
	            workflowTask);

	    AuditLog auditLog =
	            AuditLog.builder()
	                    .documentId(
	                            document.getId())
	                    .action(
	                            AuditAction.INFO_PROVIDED)
	                    .fromStatus(
	                            DocumentStatus.ADDITIONAL_INFO_REQUESTED)
	                    .toStatus(
	                            DocumentStatus.RESUBMITTED)
	                    .performedBy(
	                            employee)
	                    .remarks(
	                            "Additional information provided")
	                    .build();

	    auditLogRepository.save(
	            auditLog);
	}
}
package com.company.das.workflow.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.company.das.audit.entity.AuditLog;
import com.company.das.audit.repository.AuditLogRepository;
import com.company.das.comment.entity.DocumentComment;
import com.company.das.common.enums.AuditAction;
import com.company.das.documentversion.entity.DocumentVersion;
import com.company.das.user.entity.User;

import com.company.das.workflow.dto.WorkflowHistoryDto;
import com.company.das.workflow.service.WorkflowHistoryService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WorkflowHistoryServiceImpl implements WorkflowHistoryService {

	private final AuditLogRepository auditLogRepository;

	@Override
	public List<WorkflowHistoryDto> getWorkflowHistory(Long documentId) {

		List<AuditLog> auditLogs = auditLogRepository.findByDocumentIdOrderByActionAtAsc(documentId);

		return auditLogs.stream().map(this::convertToDto).toList();
	}

	private WorkflowHistoryDto convertToDto(AuditLog auditLog) {

		User user = auditLog.getPerformedBy();

		DocumentComment comment = auditLog.getDocumentComment();

		DocumentVersion version = auditLog.getDocumentVersion();

		return WorkflowHistoryDto.builder()

				.action(auditLog.getAction())

				.actionDisplayName(getActionDisplayName(auditLog.getAction()))

				.actionIcon(getActionIcon(auditLog.getAction()))

				.performedBy(user.getName())

				.designation(buildDesignation(user))

				.remarks(auditLog.getRemarks())

				.comment(comment != null ? comment.getComment() : null)

				.actionTime(auditLog.getActionAt())

				.versionNumber(version != null ? version.getVersionNumber() : null)

				.versionId(version != null ? version.getId() : null)

				.pdfAvailable(version != null)

				.build();

	}

	private String buildDesignation(User user) {

		String department = user.getDepartment().getDepartmentName();

		String role = switch (user.getRole()) {

		case EMPLOYEE -> "Employee";

		case REVIEWER -> "Reviewer";

		case APPROVER -> "Approver";

		case SENIOR_APPROVER -> "Senior Approver";

		case COMPLIANCE_OFFICER -> "Compliance Officer";

		case ADMIN -> "Administrator";
		};

		return department + " " + role;

	}

	private String getActionDisplayName(AuditAction action) {

		return switch (action) {

		case DOCUMENT_CREATED -> "Document Created";

		case DOCUMENT_UPDATED -> "Document Updated";

		case DOCUMENT_SUBMITTED -> "Document Submitted";

		case DOCUMENT_ASSIGNED -> "Document Assigned";

		case REVIEW_APPROVED -> "Reviewer Approved";

		case INFO_REQUESTED -> "Information Requested";

		case INFO_PROVIDED -> "Response Submitted";

		case APPROVER_APPROVED -> "Approver Approved";

		case SENIOR_APPROVER_APPROVED -> "Final Approval";

		case DOCUMENT_APPROVED -> "Document Approved";

		case DOCUMENT_REJECTED -> "Document Rejected";

		case DOCUMENT_ARCHIVED -> "Document Archived";
		};

	}

	private String getActionIcon(AuditAction action) {

		return switch (action) {

		case DOCUMENT_CREATED -> "bi-file-earmark-plus";

		case DOCUMENT_SUBMITTED -> "bi-upload";

		case INFO_REQUESTED -> "bi-question-circle";

		case INFO_PROVIDED -> "bi-arrow-repeat";

		case REVIEW_APPROVED, APPROVER_APPROVED, DOCUMENT_APPROVED -> "bi-check-circle";

		case SENIOR_APPROVER_APPROVED -> "bi-award";

		case DOCUMENT_REJECTED -> "bi-x-circle";

		default -> "bi-clock-history";
		};

	}

}
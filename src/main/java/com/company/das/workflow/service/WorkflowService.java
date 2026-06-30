package com.company.das.workflow.service;

import com.company.das.workflow.dto.ReviewDocumentDto;
import com.company.das.workflow.dto.ReviewTaskDto;

import java.util.List;

public interface WorkflowService {

	List<ReviewTaskDto> getReviewerTasks(String reviewerEmail);

	void approveByReviewer(Long taskId, String reviewerEmail);

	ReviewDocumentDto getDocumentForReview(Long taskId, String reviewerEmail);

	void requestInfo(Long taskId, String reviewerEmail, String comment);

	void rejectDocument(Long taskId, String reviewerEmail, String comment);

	List<ReviewTaskDto> getApproverTasks(String approverEmail);

	void approveByApprover(Long taskId, String approverEmail);

	void requestInfoByApprover(Long taskId, String approverEmail, String comment);

	void rejectByApprover(Long taskId, String approverEmail, String comment);

	List<ReviewTaskDto> getSeniorApproverTasks(String email);

	void approveBySeniorApprover(Long taskId, String email);

	void requestInfoBySeniorApprover(Long taskId, String seniorApproverEmail, String comment);

	void rejectBySeniorApprover(Long taskId, String seniorApproverEmail, String comment);
}
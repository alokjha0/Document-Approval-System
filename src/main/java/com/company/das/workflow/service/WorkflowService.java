package com.company.das.workflow.service;

import com.company.das.workflow.dto.ReviewDocumentDto;
import com.company.das.workflow.dto.ReviewTaskDto;

import java.util.List;

public interface WorkflowService {

	List<ReviewTaskDto> getReviewerTasks(String reviewerEmail);

	void approveByReviewer(Long taskId, String reviewerEmail);

	ReviewDocumentDto getDocumentForReview(Long taskId, String reviewerEmail);

	void requestInfo(Long taskId, String reviewerEmail);

	void rejectDocument(Long taskId, String reviewerEmail);

	List<ReviewTaskDto> getApproverTasks(String approverEmail);

	void approveByApprover(Long taskId, String approverEmail);

	void requestInfoByApprover(Long taskId, String approverEmail);

	void rejectByApprover(Long taskId, String approverEmail);

	List<ReviewTaskDto> getSeniorApproverTasks(String email);

	void approveBySeniorApprover(Long taskId, String email);

	void requestInfoBySeniorApprover(Long taskId, String seniorApproverEmail);

	void rejectBySeniorApprover(Long taskId, String seniorApproverEmail);
}
package com.company.das.workflow.controller;

import com.company.das.workflow.service.WorkflowService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/workflow")
@RequiredArgsConstructor
public class WorkflowController {

    private final WorkflowService workflowService;

    @GetMapping("/reviewer")
    public String reviewerDashboard(
            Authentication authentication,
            Model model) {

        model.addAttribute(
                "tasks",
                workflowService.getReviewerTasks(
                        authentication.getName()));

        return "workflow/reviewer-dashboard";
    }
    
    @GetMapping("/review/{taskId}")
    public String reviewDocument(
            @PathVariable Long taskId,
            Authentication authentication,
            Model model) {

        model.addAttribute(
                "document",
                workflowService
                        .getDocumentForReview(
                                taskId,
                                authentication.getName()));

        return "workflow/review-document";
    }
    
    @GetMapping("/reviewer/approve/{taskId}")
    public String approveByReviewer(
            @PathVariable Long taskId,
            Authentication authentication) {

        workflowService.approveByReviewer(
                taskId,
                authentication.getName());

        return "redirect:/workflow/reviewer";
    }
    
    @GetMapping("/reviewer/request-info/{taskId}")
    public String requestInfo(
            @PathVariable Long taskId,
            Authentication authentication) {

        workflowService.requestInfo(
                taskId,
                authentication.getName());

        return "redirect:/workflow/reviewer";
    }
    
    @GetMapping("/reviewer/reject/{taskId}")
    public String rejectDocument(
            @PathVariable Long taskId,
            Authentication authentication) {

        workflowService.rejectDocument(
                taskId,
                authentication.getName());

        return "redirect:/workflow/reviewer";
    }
    
    @GetMapping("/approver")
    public String approverDashboard(
            Authentication authentication,
            Model model) {

        model.addAttribute(
                "tasks",
                workflowService
                        .getApproverTasks(
                                authentication.getName()));

        return "workflow/approver-dashboard";
    }
    
    
    
    @GetMapping("/approver/review/{taskId}")
    public String approverReviewDocument(
            @PathVariable Long taskId,
            Authentication authentication,
            Model model) {

        model.addAttribute(
                "document",
                workflowService.getDocumentForReview(
                        taskId,
                        authentication.getName()));

        return "workflow/approver-document";
    }
    
    @GetMapping("/approver/approve/{taskId}")
    public String approveByApprover(
            @PathVariable Long taskId,
            Authentication authentication) {

        workflowService.approveByApprover(
                taskId,
                authentication.getName());

        return "redirect:/workflow/approver";
    }
    
    @GetMapping("/approver/reject/{taskId}")
    public String rejectByApprover(
			@PathVariable Long taskId,
			Authentication authentication) {

		workflowService.rejectByApprover(
				taskId,
				authentication.getName());

		return "redirect:/workflow/approver";
	}
    
    @GetMapping("/senior-approver")
    public String seniorApproverDashboard(
            Authentication authentication,
            Model model) {

        model.addAttribute(
                "tasks",
                workflowService.getSeniorApproverTasks(
                        authentication.getName()));

        return "workflow/senior-approver-dashboard";
    }
    
    
    @GetMapping("/senior-approver/review/{taskId}")
    public String seniorApproverReviewDocument(
            @PathVariable Long taskId,
            Authentication authentication,
            Model model) {

        model.addAttribute(
                "document",
                workflowService.getDocumentForReview(
                        taskId,
                        authentication.getName()));

        return "workflow/senior-approver-document";
    }
    
    @GetMapping("/senior-approver/approve/{taskId}")
    public String approveBySeniorApprover(
            @PathVariable Long taskId,
            Authentication authentication) {

        workflowService.approveBySeniorApprover(
                taskId,
                authentication.getName());

        return "redirect:/workflow/senior-approver";
    }
    
    @GetMapping("/approver/request-info/{taskId}")
    public String requestInfoByApprover(
            @PathVariable Long taskId,
            Authentication authentication) {

        workflowService.requestInfoByApprover(
                taskId,
                authentication.getName());

        return "redirect:/workflow/approver";
    }
    
    @GetMapping("/senior-approver/request-info/{taskId}")
    public String requestInfoBySeniorApprover(
            @PathVariable Long taskId,
            Authentication authentication) {

        workflowService.requestInfoBySeniorApprover(
                taskId,
                authentication.getName());

        return "redirect:/workflow/senior-approver";
    }
    
    @GetMapping("/senior-approver/reject/{taskId}")
    public String rejectBySeniorApprover(
			@PathVariable Long taskId,
			Authentication authentication) {

		workflowService.rejectBySeniorApprover(
				taskId,
				authentication.getName());

		return "redirect:/workflow/senior-approver";
	}
}
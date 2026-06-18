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
    
}
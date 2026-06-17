package com.company.das.workflow.controller;

import com.company.das.workflow.service.WorkflowService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
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
}
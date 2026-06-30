package com.company.das.application.controller;

import com.company.das.application.dto.ApplicationDto;
import com.company.das.application.service.ApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/applications")
@RequiredArgsConstructor
public class ApplicationController {

    private final ApplicationService applicationService;

    @GetMapping("/by-department/{departmentId}")
    public List<ApplicationDto>
    getApplicationsByDepartment(
            @PathVariable Long departmentId) {

        return applicationService
                .getApplicationsByDepartment(
                        departmentId);
    }
}
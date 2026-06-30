package com.company.das.application.service;

import com.company.das.application.dto.ApplicationDto;

import java.util.List;

public interface ApplicationService {

    List<ApplicationDto>
    getApplicationsByDepartment(Long departmentId);

}
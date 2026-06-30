package com.company.das.application.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.company.das.application.dto.ApplicationDto;
import com.company.das.application.repository.ApplicationRepository;
import com.company.das.application.service.ApplicationService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ApplicationServiceImpl
        implements ApplicationService {

    private final ApplicationRepository applicationRepository;

    @Override
    public List<ApplicationDto>
    getApplicationsByDepartment(Long departmentId) {

        return applicationRepository
                .findByDepartmentIdAndIsDeletedFalse(
                        departmentId)
                .stream()
                .map(application ->
                        ApplicationDto.builder()
                                .id(application.getId())
                                .applicationName(
                                        application.getApplicationName())
                                .build())
                .toList();
    }
}

package com.company.das.department.service;

import com.company.das.department.dto.DepartmentDto;
import org.springframework.data.domain.Page;

public interface DepartmentService {

	void saveDepartment(DepartmentDto dto);

	void updateDepartment(Long id, DepartmentDto dto);

	void deleteDepartment(Long id);

	DepartmentDto getDepartmentById(Long id);

	Page<DepartmentDto> getDepartments(String keyword, int page, int size);
}
package com.company.das.department.service.impl;

import com.company.das.common.exception.ResourceNotFoundException;
import com.company.das.department.dto.DepartmentDto;
import com.company.das.department.entity.Department;
import com.company.das.department.repository.DepartmentRepository;
import com.company.das.department.service.DepartmentService;
import com.company.das.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class DepartmentServiceImpl implements DepartmentService {

	private final DepartmentRepository departmentRepository;
	private final UserRepository userRepository;

	@Override
	public void saveDepartment(DepartmentDto dto) {

		String departmentName = dto.getDepartmentName().trim();

		if (departmentRepository.existsByDepartmentNameIgnoreCaseAndIsDeletedFalse(departmentName)) {

			throw new RuntimeException("Department already exists");
		}

		Department department = Department.builder().departmentName(departmentName).build();

		departmentRepository.save(department);
	}

	@Override
	public void updateDepartment(Long id, DepartmentDto dto) {

		Department department = departmentRepository.findByIdAndIsDeletedFalse(id)
				.orElseThrow(() -> new ResourceNotFoundException("Department not found"));

		String newName = dto.getDepartmentName().trim();

		if (!department.getDepartmentName().equalsIgnoreCase(newName)
				&& departmentRepository.existsByDepartmentNameIgnoreCaseAndIsDeletedFalse(newName)) {

			throw new RuntimeException("Department already exists");
		}

		department.setDepartmentName(newName);

		departmentRepository.save(department);
	}

	@Override
	public void deleteDepartment(Long id) {

	    Department department = departmentRepository.findByIdAndIsDeletedFalse(id)
	            .orElseThrow(() -> new ResourceNotFoundException("Department not found"));

	    boolean userExists =
	            userRepository.existsByDepartmentAndIsDeletedFalse(department);

	    if (userExists) {
	        throw new IllegalStateException(
	                "Cannot delete department. Users are associated with this department.");
	    }

	    department.setIsDeleted(true);
	    department.setDeletedAt(LocalDateTime.now());

	    departmentRepository.save(department);
	}

	@Override
	public DepartmentDto getDepartmentById(Long id) {

		Department department = departmentRepository.findByIdAndIsDeletedFalse(id)
				.orElseThrow(() -> new ResourceNotFoundException("Department not found"));

		return DepartmentDto.builder().id(department.getId()).departmentName(department.getDepartmentName()).build();
	}

	@Override
	public Page<DepartmentDto> getDepartments(String keyword, int page, int size) {

		Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());

		Page<Department> departments;

		if (keyword != null && !keyword.trim().isEmpty()) {

			departments = departmentRepository.findByDepartmentNameContainingIgnoreCaseAndIsDeletedFalse(keyword,
					pageable);
		} else {

			departments = departmentRepository.findByIsDeletedFalse(pageable);
		}

		return departments.map(department -> DepartmentDto.builder().id(department.getId())
				.departmentName(department.getDepartmentName()).build());
	}
}
package com.company.das.department.repository;

import com.company.das.department.entity.Department;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DepartmentRepository extends JpaRepository<Department, Long> {

	Page<Department> findByIsDeletedFalse(Pageable pageable);

	Page<Department> findByDepartmentNameContainingIgnoreCaseAndIsDeletedFalse(String keyword, Pageable pageable);

	Optional<Department> findByIdAndIsDeletedFalse(Long id);

	boolean existsByDepartmentNameIgnoreCase(String departmentName);
	
	boolean existsByDepartmentNameIgnoreCaseAndIsDeletedFalse(String departmentName);
}
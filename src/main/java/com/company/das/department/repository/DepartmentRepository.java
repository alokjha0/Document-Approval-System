package com.company.das.department.repository;

import com.company.das.department.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DepartmentRepository
        extends JpaRepository<Department,Long> {

    List<Department> findByIsDeletedFalse();
}
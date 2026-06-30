package com.company.das.application.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.company.das.application.entity.Application;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {

List<Application> findByDepartmentIdAndIsDeletedFalse(Long departmentId);

}
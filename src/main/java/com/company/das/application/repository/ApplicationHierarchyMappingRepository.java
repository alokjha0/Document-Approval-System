package com.company.das.application.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.company.das.application.entity.Application;
import com.company.das.application.entity.ApplicationHierarchyMapping;

@Repository
public interface ApplicationHierarchyMappingRepository extends JpaRepository<ApplicationHierarchyMapping, Long> {

	Optional<ApplicationHierarchyMapping> findByApplicationAndIsDeletedFalse(Application application);

}
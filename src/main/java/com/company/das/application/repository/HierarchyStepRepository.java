package com.company.das.application.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.company.das.application.entity.Hierarchy;
import com.company.das.application.entity.HierarchyStep;

@Repository
public interface HierarchyStepRepository extends JpaRepository<HierarchyStep, Long> {

	List<HierarchyStep> findByHierarchyAndIsDeletedFalseOrderByStepOrderAsc(Hierarchy hierarchy);

}

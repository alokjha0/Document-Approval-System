package com.company.das.application.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.company.das.application.entity.Hierarchy;

@Repository
public interface HierarchyRepository
        extends JpaRepository<Hierarchy, Long> {

}
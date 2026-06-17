package com.company.das.document.repository;

import com.company.das.common.enums.DocumentStatus;

import com.company.das.document.entity.Document;
import com.company.das.user.entity.User;
import com.company.das.workflow.entity.WorkflowTask;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentRepository
        extends JpaRepository<Document, Long> {

    boolean existsByDocumentNumber(
            String documentNumber
    );
    
    Optional<Document> findByIdAndStatus(
            Long id,
            DocumentStatus status
    );

    List<Document> findByOwnerAndStatus(
            User owner,
            DocumentStatus status
    );
    List<Document> findByOwnerOrderByCreatedAtDesc(
            User owner
    );
    
}
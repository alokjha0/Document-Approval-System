package com.company.das.documentversion.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.company.das.document.entity.Document;
import com.company.das.documentversion.entity.DocumentVersion;

public interface DocumentVersionRepository extends JpaRepository<DocumentVersion, Long> {

    Optional<DocumentVersion> findByDocumentAndIsCurrentTrue(Document document);

    List<DocumentVersion> findByDocumentOrderByVersionNumberDesc(Document document);

    Optional<DocumentVersion> findTopByDocumentOrderByVersionNumberDesc(Document document);

}
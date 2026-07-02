package com.company.das.documentversion.service.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.company.das.document.entity.Document;
import com.company.das.documentversion.entity.DocumentVersion;
import com.company.das.documentversion.repository.DocumentVersionRepository;
import com.company.das.documentversion.service.DocumentVersionService;
import com.company.das.pdf.service.PdfService; 

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class DocumentVersionServiceImpl implements DocumentVersionService {

    private final DocumentVersionRepository documentVersionRepository;

    private final PdfService pdfService;

    @Override
    public DocumentVersion createVersion(Document document) {

        int nextVersion = 1;

        Optional<DocumentVersion> latestVersion =
                documentVersionRepository.findTopByDocumentOrderByVersionNumberDesc(document);

        if (latestVersion.isPresent()) {

            nextVersion = latestVersion.get().getVersionNumber() + 1;

        }

        String pdfPath = pdfService.generateDocumentPdf(document, nextVersion);

        documentVersionRepository
                .findByDocumentAndIsCurrentTrue(document)
                .ifPresent(current -> {

                    current.setIsCurrent(false);

                    documentVersionRepository.save(current);

                });

        DocumentVersion version = DocumentVersion.builder()
                .document(document)
                .versionNumber(nextVersion)
                .pdfPath(pdfPath)
                .isCurrent(true)
                .build();

        return documentVersionRepository.save(version);

    }

    @Override
    public DocumentVersion getCurrentVersion(Document document) {

        return documentVersionRepository
                .findByDocumentAndIsCurrentTrue(document)
                .orElseThrow(() ->
                        new RuntimeException("Current document version not found"));

    }

    @Override
    public List<DocumentVersion> getAllVersions(Document document) {

        return documentVersionRepository
                .findByDocumentOrderByVersionNumberDesc(document);

    }

}
package com.company.das.pdf.service;

import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;

import com.company.das.document.entity.Document;

public interface PdfService {

    String generateDocumentPdf(Document document,
                               Integer versionNumber);

    ResponseEntity<Resource> viewPdf(Long documentId);

    ResponseEntity<Resource> downloadPdf(Long documentId);

}
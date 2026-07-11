package com.company.das.pdf.service;

import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import com.company.das.document.entity.Document;

public interface PdfService {

	String generateDocumentPdf(Document document, Integer versionNumber);

	String storeUploadedPdf(Document document, MultipartFile file, Integer versionNumber);
	
	String finalizeUploadedDraft(Document document, Integer versionNumber);

	ResponseEntity<Resource> viewPdf(Long documentId);

	ResponseEntity<Resource> downloadPdf(Long documentId);
	
	ResponseEntity<Resource> viewPdfByVersion(Long versionId);

	ResponseEntity<Resource> downloadPdfByVersion(Long versionId);

}
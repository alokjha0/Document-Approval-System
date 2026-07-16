package com.company.das.pdf.service.impl;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import org.springframework.web.multipart.MultipartFile;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.company.das.common.enums.DocumentSource;
import com.company.das.common.enums.DocumentStatus;
import com.company.das.common.exception.ResourceNotFoundException;
import com.company.das.document.entity.Document;
import com.company.das.document.repository.DocumentRepository;
import com.company.das.documentversion.entity.DocumentVersion;
import com.company.das.documentversion.repository.DocumentVersionRepository;
import com.company.das.pdf.service.PdfService;

import lombok.RequiredArgsConstructor;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document.OutputSettings;
import org.jsoup.nodes.Entities;

import java.io.FileOutputStream;
import java.io.OutputStream;

import java.nio.file.Files;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

@Service
@RequiredArgsConstructor
public class PdfServiceImpl implements PdfService {

	private final DocumentRepository documentRepository;

	private final DocumentVersionRepository documentVersionRepository;

	@Value("${app.pdf.storage-path}")
	private String storagePath;

	
	
	private File getPdfFile(Long documentId) {

		Document document = documentRepository.findById(documentId)
				.orElseThrow(() -> new ResourceNotFoundException("Document not found"));

		// Draft uploaded PDF
		if (document.getDocumentSource() == DocumentSource.FILE_UPLOAD
		        && document.getUploadedPdfPath() != null) {

		    return new File(document.getUploadedPdfPath());

		}

		// Existing versioned PDF
		DocumentVersion version = documentVersionRepository
				.findByDocumentAndIsCurrentTrue(document)
				.orElseThrow(() -> new ResourceNotFoundException("Current document version not found"));

		return new File(version.getPdfPath());
	}
	
	private File getPdfFileByVersion(Long versionId) {

	    DocumentVersion version = documentVersionRepository
	            .findById(versionId)
	            .orElseThrow(() ->
	                    new ResourceNotFoundException("Document version not found"));

	    return new File(version.getPdfPath());

	}

	@Override
	public String generateDocumentPdf(Document document, Integer versionNumber) {

		try {

			String documentFolder = storagePath + File.separator + document.getDocumentNumber();

			File folder = new File(documentFolder);

			if (!folder.exists()) {
				folder.mkdirs();
			}

			String pdfPath = documentFolder + File.separator + "version-" + versionNumber + ".pdf";

			String html = String.format("""
					<?xml version="1.0" encoding="UTF-8"?>
					<!DOCTYPE html>

					<html xmlns="http://www.w3.org/1999/xhtml">

					<head>

					<meta charset="UTF-8"/>

					<title>Document</title>

					<style>

					@page{

					    size:A4;

					    margin:22mm 18mm 22mm 18mm;

					    @bottom-center{

					        content:"Page " counter(page) " of " counter(pages);

					        font-size:10px;

					        color:#666666;

					    }

					}

					body{

					    font-family:Arial, Helvetica, sans-serif;

					    margin:0;

					    font-size:12pt;

					    line-height:1.55;

					    color:#222222;

					    word-wrap:break-word;

					}

					.header{

					    text-align:center;

					    border-bottom:2px solid #0d6efd;

					    padding-bottom:14px;

					    margin-bottom:30px;

					}

					.header h1{

					    margin:0;

					    font-size:24pt;

					    color:#0d6efd;

					}

					.header p{

					    margin-top:6px;

					    color:#666666;

					    font-size:11pt;

					}

					.footer{

					    margin-top:35px;

					    border-top:1px solid #cccccc;

					    padding-top:10px;

					    text-align:center;

					    font-size:10pt;

					    color:#666666;

					}

					h1,h2,h3,h4,h5,h6{

					    page-break-after:avoid;

					    page-break-inside:avoid;

					    margin-top:22px;

					    margin-bottom:10px;

					    color:#1f2937;

					}

					p{

					    margin:8px 0;

					    text-align:justify;

					    page-break-inside:avoid;

					}

					ul,ol{

					    page-break-inside:avoid;

					}

					li{

					    margin-bottom:5px;

					}

					table{

					    width:100%%;

					    border-collapse:collapse;

					    margin:18px 0;

					    page-break-inside:auto;

					}

					thead{

					    display:table-header-group;

					}

					tfoot{

					    display:table-footer-group;

					}

					tr{

					    page-break-inside:avoid;

					    page-break-after:auto;

					}

					table,
					th,
					td{

					    border:1px solid #888888;

					}

					th{

					    background:#f2f5fa;

					    font-weight:bold;

					    text-align:left;

					}

					th,
					td{

					    padding:8px;

					    vertical-align:top;

					}

					img{

					    max-width:100%%;

					    height:auto;

					    page-break-inside:avoid;

					}

					blockquote{

					    border-left:4px solid #0d6efd;

					    margin:15px 0;

					    padding-left:12px;

					    color:#555555;

					    font-style:italic;

					}

					pre{

					    white-space:pre-wrap;

					    word-wrap:break-word;

					}

					code{

					    font-family:Consolas, monospace;

					}

					</style>

					</head>

					<body>

					<div class="header">

					<h1>DOCUMENT APPROVAL SYSTEM</h1>

	

					</div>

					%s

					<div class="footer">

					<div>
					Generated by Document Approval System
					</div>

					<div>
					Version : %d
					</div>

					</div>

					</body>

					</html>
					""", cleanHtml(document.getDocumentContent()), versionNumber);

			try (OutputStream os = new FileOutputStream(pdfPath)) {

				PdfRendererBuilder builder = new PdfRendererBuilder();

				builder.withHtmlContent(html, null);

				builder.toStream(os);

				builder.run();
			}

			return pdfPath;

		} catch (Exception e) {

			throw new RuntimeException("Unable to generate PDF", e);
		}
	}
	
	
	@Override
	public String storeUploadedPdf(Document document,
	                               MultipartFile file,
	                               Integer versionNumber) {

	    try {

	        if (file == null || file.isEmpty()) {

	            throw new RuntimeException("Please select a PDF file.");

	        }

	        if (!"application/pdf".equalsIgnoreCase(file.getContentType())) {

	            throw new RuntimeException("Only PDF files are allowed.");

	        }

	        String documentFolder =
	                storagePath + File.separator + document.getDocumentNumber();

	        File folder = new File(documentFolder);

	        if (!folder.exists()) {

	            folder.mkdirs();

	        }

	        String pdfPath;

	        if (versionNumber == 0) {

	            pdfPath = documentFolder
	                    + File.separator
	                    + "draft.pdf";

	        } else {

	            pdfPath = documentFolder
	                    + File.separator
	                    + "version-" + versionNumber + ".pdf";

	        }

	        Files.copy(
	                file.getInputStream(),
	                Path.of(pdfPath),
	                StandardCopyOption.REPLACE_EXISTING
	        );

	        return pdfPath;

	    } catch (IOException e) {

	        throw new RuntimeException("Unable to store uploaded PDF.", e);

	    }

	}

	@Override
	public String finalizeUploadedDraft(Document document,
	                                    Integer versionNumber) {

	    try {

	        if (document.getUploadedPdfPath() == null) {

	            throw new RuntimeException("Draft PDF not found.");

	        }

	        File draftFile = new File(document.getUploadedPdfPath());

	        if (!draftFile.exists()) {

	            throw new RuntimeException("Draft PDF does not exist.");

	        }

	        String documentFolder =
	                storagePath + File.separator + document.getDocumentNumber();

	        String versionPdfPath =
	                documentFolder
	                        + File.separator
	                        + "version-" + versionNumber + ".pdf";

	        Files.copy(
	                draftFile.toPath(),
	                Path.of(versionPdfPath),
	                StandardCopyOption.REPLACE_EXISTING);

	        return versionPdfPath;

	    } catch (IOException e) {

	        throw new RuntimeException(
	                "Unable to finalize uploaded PDF.",
	                e);

	    }

	}
	
	@Override
	public ResponseEntity<Resource> viewPdf(Long documentId) {

		try {

			File file = getPdfFile(documentId);

			if (!file.exists()) {
				throw new RuntimeException("PDF file not found");
			}

			ByteArrayResource resource = new ByteArrayResource(Files.readAllBytes(file.toPath()));

			return ResponseEntity.ok()
					.header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + file.getName() + "\"")
					.contentType(MediaType.APPLICATION_PDF).contentLength(file.length()).body(resource);

		} catch (IOException e) {

			throw new RuntimeException("Unable to read PDF file", e);

		}

	}
	
	@Override
	public ResponseEntity<Resource> viewPdfByVersion(Long versionId) {

		try {

			File file = getPdfFileByVersion(versionId);

			if (!file.exists()) {
				throw new RuntimeException("PDF file not found");
			}

			ByteArrayResource resource = new ByteArrayResource(Files.readAllBytes(file.toPath()));

			return ResponseEntity.ok()
					.header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + file.getName() + "\"")
					.contentType(MediaType.APPLICATION_PDF).contentLength(file.length()).body(resource);

		} catch (IOException e) {

			throw new RuntimeException("Unable to read PDF file", e);

		}

	}

	private String cleanHtml(String html) {

		if (html == null || html.isBlank()) {
			return "";
		}

		org.jsoup.nodes.Document document = Jsoup.parseBodyFragment(html);

		document.outputSettings().syntax(OutputSettings.Syntax.xml).escapeMode(Entities.EscapeMode.xhtml)
				.prettyPrint(false);

		return document.body().html();
	}

	@Override
	public ResponseEntity<Resource> downloadPdf(Long documentId) {

		try {

			File file = getPdfFile(documentId);

			if (!file.exists()) {
				throw new RuntimeException("PDF file not found");
			}

			ByteArrayResource resource = new ByteArrayResource(Files.readAllBytes(file.toPath()));

			return ResponseEntity.ok()
					.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getName() + "\"")
					.contentType(MediaType.APPLICATION_PDF).contentLength(file.length()).body(resource);

		} catch (IOException e) {

			throw new RuntimeException("Unable to read PDF file", e);

		}

	}
	
	@Override
	public ResponseEntity<Resource> downloadPdfByVersion(Long versionId) {

		try {

			File file = getPdfFileByVersion(versionId);

			if (!file.exists()) {
				throw new RuntimeException("PDF file not found");
			}

			ByteArrayResource resource = new ByteArrayResource(Files.readAllBytes(file.toPath()));

			return ResponseEntity.ok()
					.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getName() + "\"")
					.contentType(MediaType.APPLICATION_PDF).contentLength(file.length()).body(resource);

		} catch (IOException e) {

			throw new RuntimeException("Unable to read PDF file", e);

		}

	}

}
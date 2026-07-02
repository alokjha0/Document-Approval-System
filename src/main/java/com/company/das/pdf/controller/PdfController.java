package com.company.das.pdf.controller;

import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.company.das.pdf.service.PdfService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/documents")
public class PdfController {

    private final PdfService pdfService;

    @GetMapping("/{documentId}/pdf/view")
    public ResponseEntity<Resource> viewPdf(
            @PathVariable Long documentId) {

        return pdfService.viewPdf(documentId);

    }

    @GetMapping("/{documentId}/pdf/download")
    public ResponseEntity<Resource> downloadPdf(
            @PathVariable Long documentId) {

        return pdfService.downloadPdf(documentId);

    }

}
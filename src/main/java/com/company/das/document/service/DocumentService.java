package com.company.das.document.service;

import java.util.List;

import com.company.das.document.dto.DocumentDto;

public interface DocumentService {

    void createDocument(
            DocumentDto documentDto,
            String loggedInUserEmail
    );
    
    void submitDocument(
            Long documentId,
            String loggedInUserEmail
    );
    
    List<DocumentDto> getDocumentsByOwner(
            String email
    );
    DocumentDto getDocumentById(Long id);

    void updateDocument(
            Long id,
            DocumentDto documentDto,
            String loggedInUserEmail
    );
}
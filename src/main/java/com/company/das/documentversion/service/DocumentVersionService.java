package com.company.das.documentversion.service;

import java.util.List;

import com.company.das.document.entity.Document;
import com.company.das.documentversion.entity.DocumentVersion;

public interface DocumentVersionService {

    DocumentVersion createVersion(Document document);

    DocumentVersion getCurrentVersion(Document document);

    List<DocumentVersion> getAllVersions(Document document);

}
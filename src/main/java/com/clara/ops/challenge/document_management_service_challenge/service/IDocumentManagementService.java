package com.clara.ops.challenge.document_management_service_challenge.service;

import com.clara.ops.challenge.document_management_service_challenge.service.dto.*;

public interface IDocumentManagementService {
  DocumentDTO uploadDocument(UploadDocumentDTO uploadDocumentDTO);

  PaginatedDocumentSearchDTO searchDocuments(DocumentSearchDTO documentSearchDTO);

  DocumentDownloadUrlDTO downloadDocument(String documentId);
}

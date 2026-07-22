package com.clara.ops.challenge.document_management_service_challenge.service;

import com.clara.ops.challenge.document_management_service_challenge.service.dto.*;

public interface IDocumentManagementService {
  DocumentDTO validateStrategyAndUploadFile(UploadDocumentDTO uploadDocumentDTO);

  PaginatedDocumentSearchDTO searchDocuments(DocumentSearchDTO documentSearchDTO);

  DocumentDownloadUrlDTO downloadDocument(Integer documentId);
}

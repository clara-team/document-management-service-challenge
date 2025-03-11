package com.clara.ops.challenge.document_management_service_challenge.service;

import com.clara.ops.challenge.document_management_service_challenge.service.dto.*;
import org.springframework.stereotype.Service;

@Service
public class DocumentManagementServiceImp implements IDocumentManagementService {
  @Override
  public DocumentDTO uploadDocument(UploadDocumentDTO uploadDocumentDTO) {
    return DocumentDTO.builder().build();
  }

  @Override
  public PaginatedDocumentSearchDTO searchDocuments(DocumentSearchDTO documentSearchDTO) {
    return PaginatedDocumentSearchDTO.builder().build();
  }

  @Override
  public DocumentDownloadUrlDTO downloadDocument(String documentId) {
    return DocumentDownloadUrlDTO.builder().build();
  }
}

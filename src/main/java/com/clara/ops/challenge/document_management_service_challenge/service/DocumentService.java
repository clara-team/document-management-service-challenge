package com.clara.ops.challenge.document_management_service_challenge.service;

import com.clara.ops.challenge.document_management_service_challenge.model.dto.*;
import org.springframework.web.multipart.MultipartFile;

public interface DocumentService {
  DocumentInfo uploadDocument(UploadRequest uploadRequest, MultipartFile file);

  SearchResponse searchDocuments(SearchRequest searchRequest);

  DownloadResponse getDocumentDownloadUrl(Long documentId);
}

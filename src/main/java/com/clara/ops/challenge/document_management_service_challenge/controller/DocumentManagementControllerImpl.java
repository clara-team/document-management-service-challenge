package com.clara.ops.challenge.document_management_service_challenge.controller;

import com.clara.ops.challenge.document_management_service_challenge.dto.DocumentDownloadUrl;
import com.clara.ops.challenge.document_management_service_challenge.dto.DocumentSearchFilters;
import com.clara.ops.challenge.document_management_service_challenge.dto.PaginatedDocumentSearch;
import com.clara.ops.challenge.document_management_service_challenge.dto.UploadDocument;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class DocumentManagementControllerImpl implements DocumentManagementControllerInterface {

  private static final Logger log = LoggerFactory.getLogger(DocumentManagementControllerImpl.class);

  @Autowired
  public DocumentManagementControllerImpl() {}

  public ResponseEntity<DocumentDownloadUrl> downloadDocument(String documentId) {
    return new ResponseEntity<DocumentDownloadUrl>(HttpStatus.NOT_IMPLEMENTED);
  }

  public ResponseEntity<PaginatedDocumentSearch> searchDocuments(
      DocumentSearchFilters body, Integer page, Integer size, List<String> sort) {
    return new ResponseEntity<PaginatedDocumentSearch>(HttpStatus.NOT_IMPLEMENTED);
  }

  public ResponseEntity<Void> uploadDocument(MultipartFile file, UploadDocument body) {
    return new ResponseEntity<Void>(HttpStatus.NOT_IMPLEMENTED);
  }
}

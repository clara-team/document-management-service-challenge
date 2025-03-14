package com.clara.ops.challenge.document_management_service_challenge.controller;

import com.clara.ops.challenge.document_management_service_challenge.controller.dto.request.DocumentSearchFilters;
import com.clara.ops.challenge.document_management_service_challenge.controller.dto.response.Document;
import com.clara.ops.challenge.document_management_service_challenge.controller.dto.response.DocumentDownloadUrl;
import com.clara.ops.challenge.document_management_service_challenge.controller.dto.response.PaginatedDocumentSearch;
import com.clara.ops.challenge.document_management_service_challenge.mapper.IDocumentManagementMapper;
import com.clara.ops.challenge.document_management_service_challenge.service.IDocumentManagementService;
import com.clara.ops.challenge.document_management_service_challenge.service.dto.DocumentSearchDTO;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class DocumentManagementControllerImp implements IDocumentManagementController {

  @Autowired private IDocumentManagementMapper documentManagementMapper;
  @Autowired private IDocumentManagementService documentManagementService;

  public ResponseEntity<DocumentDownloadUrl> downloadDocument(Integer documentId) {
    DocumentDownloadUrl documentDownloadUrl =
        documentManagementMapper.mapDocumentDownloadUrl(
            documentManagementService.downloadDocument(documentId));
    return new ResponseEntity<>(documentDownloadUrl, HttpStatus.OK);
  }

  public ResponseEntity<PaginatedDocumentSearch> searchDocuments(
      DocumentSearchFilters filter, Integer page, Integer size, String sort) {
    DocumentSearchDTO documentSearchDTO =
        documentManagementMapper.mapToDocumentSearchDTO(filter, page, size, sort);
    PaginatedDocumentSearch paginatedDocumentSearch =
        documentManagementMapper.mapToPaginatedDocumentSearch(
            documentManagementService.searchDocuments(documentSearchDTO));
    return new ResponseEntity<>(paginatedDocumentSearch, HttpStatus.OK);
  }

  @Override
  public ResponseEntity<Document> uploadDocument(
      String user, String name, List<String> tags, MultipartFile file) {
    Document document =
        documentManagementMapper.mapToDocument(
            documentManagementService.uploadDocument(
                documentManagementMapper.mapToUploadDocumentDTO(user, name, tags, file)));
    return new ResponseEntity<>(document, HttpStatus.CREATED);
  }
}

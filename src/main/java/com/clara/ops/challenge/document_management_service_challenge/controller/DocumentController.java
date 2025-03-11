package com.clara.ops.challenge.document_management_service_challenge.controller;

import com.clara.ops.challenge.document_management_service_challenge.model.dto.*;
import com.clara.ops.challenge.document_management_service_challenge.service.DocumentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/document")
@Tag(
    name = "Documents Management",
    description = "APIs for managing documents catalogue by Juan Pachon")
public class DocumentController {

  private final DocumentService documentService;

  @Autowired
  public DocumentController(DocumentService documentService) {
    this.documentService = documentService;
  }

  @Operation(summary = "Upload a new document", description = "Upload a new document")
  @ApiResponse(responseCode = "201", description = "Document uploaded successfully")
  @PostMapping(
      value = "/upload",
      consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<DocumentInfo> uploadDocument(
      @RequestParam("user") String userId,
      @RequestParam("document_name") String documentName,
      @RequestParam("tags") List<String> tags,
      @RequestPart("file") MultipartFile file) {

    if (file.isEmpty() || !Objects.equals(file.getContentType(), MediaType.APPLICATION_PDF_VALUE)) {
      return ResponseEntity.badRequest().build();
    }

    UploadRequest uploadRequest =
        UploadRequest.builder().userId(userId).documentName(documentName).tags(tags).build();

    DocumentInfo documentInfo = documentService.uploadDocument(uploadRequest, file);
    return ResponseEntity.status(HttpStatus.CREATED).body(documentInfo);
  }

  @GetMapping("/search")
  public ResponseEntity<SearchResponse> searchDocuments(
      @RequestParam(required = false) String userId,
      @RequestParam(required = false) String documentName,
      @RequestParam(required = false) List<String> tags,
      @RequestParam(defaultValue = "0") Integer page,
      @RequestParam(defaultValue = "10") Integer size) {

    SearchRequest searchRequest =
        SearchRequest.builder()
            .userId(userId)
            .documentName(documentName)
            .tags(tags)
            .page(page)
            .size(size)
            .build();

    SearchResponse searchResponse = documentService.searchDocuments(searchRequest);
    return ResponseEntity.ok(searchResponse);
  }

  @GetMapping("/{id}/download")
  public ResponseEntity<DownloadResponse> getDocumentDownloadUrl(@PathVariable Long id) {
    DownloadResponse downloadResponse = documentService.getDocumentDownloadUrl(id);
    return ResponseEntity.ok(downloadResponse);
  }
}

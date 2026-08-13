package com.clara.ops.challenge.document_management_service_challenge.controller;

import com.clara.ops.challenge.document_management_service_challenge.dto.*;
import com.clara.ops.challenge.document_management_service_challenge.service.DocumentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@Tag(
    name = "Document-Management API Challenge",
    description = "Operations that are related to document upload, search, and download.")
public class DocumentController {

  private final DocumentService documentService;

  public DocumentController(DocumentService documentService) {
    this.documentService = documentService;
  }

  @Operation(
      summary = "Upload a document Endpoint",
      description = "Upload a document with metadata including user, name, and tags.",
      responses = {
        @ApiResponse(responseCode = "201", description = "Document successfully uploaded"),
        @ApiResponse(responseCode = "400", description = "Invalid input data")
      })
  @PostMapping(
      value = "/document-management/upload",
      consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<Document> uploadDocument(
      @RequestPart("metadata") UploadDocument uploadRequest,
      @RequestPart("file") MultipartFile file) {

    com.clara.ops.challenge.document_management_service_challenge.entities.Document document =
        documentService.uploadDocument(
            uploadRequest.getUsername(), uploadRequest.getName(), uploadRequest.getTags(), file);
    Document documentDTO = mapDocumentToDTO(document);
    return ResponseEntity.status(HttpStatus.CREATED).body(documentDTO);
  }

  @Operation(
      summary = "Search documents Endpoint",
      description =
          "Search for documents based on filters such as user, name, and tags. Supports pagination"
              + " and sorting.",
      responses = {
        @ApiResponse(responseCode = "200", description = "Documents found"),
        @ApiResponse(responseCode = "400", description = "Invalid search filters")
      })
  @PostMapping(value = "/document-management/search", consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<PaginatedDocumentSearch> searchDocuments(
      @RequestBody DocumentSearchFilters filters,
      @RequestParam(value = "page", defaultValue = "0") int page,
      @RequestParam(value = "size", defaultValue = "20") int size,
      @RequestParam(value = "sort", required = false) List<String> sort) {

    PageRequest pageable;
    if (sort != null && !sort.isEmpty()) {
      List<Sort.Order> orders = new ArrayList<>();
      for (String s : sort) {
        String[] parts = s.split(",");
        if (parts.length == 2) {
          String property = parts[0];
          Sort.Direction direction =
              parts[1].equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
          orders.add(new Sort.Order(direction, property));
        }
      }
      pageable = PageRequest.of(page, size, Sort.by(orders));
    } else {
      pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
    }

    Page<com.clara.ops.challenge.document_management_service_challenge.entities.Document>
        documentPage =
            documentService.searchDocuments(
                Optional.ofNullable(filters.getUsername()),
                Optional.ofNullable(filters.getName()),
                Optional.ofNullable(filters.getTags()),
                pageable);

    PaginatedDocumentSearch response = new PaginatedDocumentSearch();
    Metadata metadata = new Metadata();
    metadata.setCurrentPage(documentPage.getNumber());
    metadata.setItemsPerPage(documentPage.getSize());
    metadata.setCurrentItems(documentPage.getNumberOfElements());
    metadata.setTotalPages(documentPage.getTotalPages());
    metadata.setTotalItems(documentPage.getTotalElements());
    response.setMetadata(metadata);

    List<Document> dtos =
        documentPage.getContent().stream().map(this::mapDocumentToDTO).collect(Collectors.toList());
    response.setDocuments(dtos);

    return ResponseEntity.ok(response);
  }

  @Operation(
      summary = "Download a document Endpoint",
      description = "Get a pre-signed URL to download the document based on the document ID.",
      responses = {
        @ApiResponse(responseCode = "200", description = "Document download URL returned"),
        @ApiResponse(responseCode = "404", description = "Document not found")
      })
  @GetMapping("/document-management/download/{documentId}")
  public ResponseEntity<DocumentDownloadUrlDTO> downloadDocument(
      @PathVariable("documentId") String documentId) {
    String downloadUrl = documentService.getDownloadUrl(Long.valueOf(documentId));
    DocumentDownloadUrlDTO dto = new DocumentDownloadUrlDTO();
    dto.setUrl(downloadUrl);
    return ResponseEntity.ok(dto);
  }

  private Document mapDocumentToDTO(
      com.clara.ops.challenge.document_management_service_challenge.entities.Document doc) {
    Document dto = new Document();
    dto.setId(doc.getId().toString());
    dto.setUsername(doc.getUsername());
    dto.setName(doc.getDocumentName());
    dto.setTags(doc.getTags());
    dto.setSize(doc.getFileSize());
    dto.setType(doc.getFileType());
    dto.setCreatedAt(doc.getCreatedAt());
    return dto;
  }
}

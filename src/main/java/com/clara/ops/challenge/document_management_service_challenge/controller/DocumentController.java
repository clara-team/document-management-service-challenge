package com.clara.ops.challenge.document_management_service_challenge.controller;

import static org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE;

import com.clara.ops.challenge.document_management_service_challenge.controller.model.request.DocumentSearchFilter;
import com.clara.ops.challenge.document_management_service_challenge.controller.model.response.DocumentResponse;
import com.clara.ops.challenge.document_management_service_challenge.domain.Document;
import com.clara.ops.challenge.document_management_service_challenge.service.DocumentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Tag(
    name = "Documents",
    description = "Operations for uploading, retrieving, and searching pdf documents")
@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/documents")
public class DocumentController {

  private final DocumentService documentService;

  @Operation(
      summary = "Upload a document",
      description = "Uploads a file and stores its metadata. Tags are optional.")
  @ApiResponses({
    @ApiResponse(responseCode = "201", description = "Document uploaded successfully"),
    @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
    @ApiResponse(responseCode = "500", description = "Storage or database error")
  })
  @PostMapping(value = "/upload", consumes = MULTIPART_FORM_DATA_VALUE)
  @ResponseStatus(HttpStatus.CREATED)
  public void upload(
      @Parameter(description = "Document name", required = true) @NotBlank @RequestParam("name")
          String name,
      @Parameter(description = "user ID", required = true) @NotBlank @RequestParam("user")
          String user,
      @Parameter(description = "list of tags") @RequestParam(value = "tags", required = false)
          List<String> tags,
      @Parameter(description = "Pdf file to upload", required = true) @RequestPart("file")
          MultipartFile file) {
    var document =
        Document.builder()
            .userId(user)
            .documentName(name)
            .tags(tags)
            .fileType(file.getContentType())
            .fileSize(file.getSize())
            .build();

    documentService.upload(document, file);
  }

  @Operation(
      summary = "Search documents",
      description =
          "Filters documents by user, name, and tags. Return documents that match all conditions")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Paginated list of matching documents"),
    @ApiResponse(responseCode = "400", description = "Invalid filter or pagination parameters")
  })
  @PostMapping(value = "/search")
  public Page<Document> search(
      @RequestBody DocumentSearchFilter filter,
      @Parameter(description = "Page number (0-based)")
          @RequestParam(required = false, defaultValue = "0")
          int page,
      @Parameter(description = "Page size") @RequestParam(required = false, defaultValue = "10")
          int size) {
    return documentService.filter(filter, PageRequest.of(page, size));
  }

  @Operation(
      summary = "Get document download URL",
      description = "Returns a pre-signed download URL for the document with the given ID.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Download URL generated successfully"),
    @ApiResponse(responseCode = "404", description = "Document not found")
  })
  @GetMapping("/{id}")
  public ResponseEntity<DocumentResponse> getDocument(
      @Parameter(description = "Document ID", required = true) @PathVariable Long id) {
    return ResponseEntity.ok(documentService.getDownloadUrlById(id));
  }
}

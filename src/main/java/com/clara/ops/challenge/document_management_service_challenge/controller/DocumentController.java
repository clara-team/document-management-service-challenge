package com.clara.ops.challenge.document_management_service_challenge.controller;

import com.clara.ops.challenge.document_management_service_challenge.exception.ErrorResponse;
import com.clara.ops.challenge.document_management_service_challenge.model.dto.*;
import com.clara.ops.challenge.document_management_service_challenge.service.DocumentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
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

  @Operation(
      summary = "Upload a new document",
      description =
          "Upload a new PDF document with associated metadata including user ID, document name, and"
              + " tags. The system will store the document in MinIO and save the metadata in the"
              + " database.")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "201",
            description = "Document uploaded successfully",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = DocumentInfo.class))),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid input - empty file, non-PDF file, or missing required fields",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(
            responseCode = "413",
            description = "Document too large (exceeds 500MB)",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error or storage failure",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class)))
      })
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

    if (file.getSize() > 500 * 1024 * 1024) {
      throw new MaxUploadSizeExceededException(500 * 1024 * 1024);
    }

    UploadRequest uploadRequest =
        UploadRequest.builder().userId(userId).documentName(documentName).tags(tags).build();

    DocumentInfo documentInfo = documentService.uploadDocument(uploadRequest, file);
    return ResponseEntity.status(HttpStatus.CREATED).body(documentInfo);
  }

  @Operation(
      summary = "Search documents",
      description =
          "Search for documents with optional filters for user ID, document name, and tags. "
              + "Results are paginated and ordered by creation date in descending order. "
              + "If no filters are provided, returns all documents.")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Documents retrieved successfully",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = SearchResponse.class))),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid parameters",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class)))
      })
  @Parameters({
    @Parameter(
        name = "userId",
        description = "Filter documents by user ID",
        in = ParameterIn.QUERY,
        schema = @Schema(type = "string"),
        required = false),
    @Parameter(
        name = "documentName",
        description = "Filter documents by name (partial match, case-insensitive)",
        in = ParameterIn.QUERY,
        schema = @Schema(type = "string"),
        required = false),
    @Parameter(
        name = "tags",
        description = "Filter documents by tags (documents must have all specified tags)",
        in = ParameterIn.QUERY,
        schema = @Schema(type = "array", implementation = String.class),
        required = false),
    @Parameter(
        name = "page",
        description = "Page number (zero-based)",
        in = ParameterIn.QUERY,
        schema = @Schema(type = "integer", defaultValue = "0"),
        required = false),
    @Parameter(
        name = "size",
        description = "Number of documents per page",
        in = ParameterIn.QUERY,
        schema = @Schema(type = "integer", defaultValue = "10"),
        required = false)
  })
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

  @Operation(
      summary = "Get document download URL",
      description =
          "Generate a temporary pre-signed URL to download a document. "
              + "The URL will be valid for a limited time (typically 30 minutes).")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Download URL generated successfully",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = DownloadResponse.class))),
        @ApiResponse(
            responseCode = "404",
            description = "Document not found",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(
            responseCode = "500",
            description = "Error generating download URL or storage service issue",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class)))
      })
  @GetMapping("/{id}/download")
  public ResponseEntity<DownloadResponse> getDocumentDownloadUrl(@PathVariable Long id) {
    DownloadResponse downloadResponse = documentService.getDocumentDownloadUrl(id);
    return ResponseEntity.ok(downloadResponse);
  }
}

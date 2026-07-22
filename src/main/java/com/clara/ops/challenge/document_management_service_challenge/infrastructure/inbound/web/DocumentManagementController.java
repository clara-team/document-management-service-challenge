package com.clara.ops.challenge.document_management_service_challenge.infrastructure.inbound.web;

import com.clara.ops.challenge.document_management_service_challenge.application.port.in.DownloadDocumentUserCase;
import com.clara.ops.challenge.document_management_service_challenge.application.port.in.FindDocumentMetadataUserCase;
import com.clara.ops.challenge.document_management_service_challenge.application.service.UploadDocumentService;
import com.clara.ops.challenge.document_management_service_challenge.domain.model.Document;
import com.clara.ops.challenge.document_management_service_challenge.infrastructure.inbound.dto.DocumentMetadata;
import com.clara.ops.challenge.document_management_service_challenge.infrastructure.inbound.dto.UploadDocumentResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * Controller providing endpoints for managing PDF documents. It offers functionality for uploading
 * and downloading documents as well as handling their associated metadata.
 */
@RestController
@RequestMapping("/api/v1/document-management")
@RequiredArgsConstructor
@Tag(name = "Document Management Service", description = "API for managing PDF documents")
public class DocumentManagementController {

  private final UploadDocumentService service;
  private final DownloadDocumentUserCase downloadDocumentUserCase;
  private final FindDocumentMetadataUserCase documentQueryUseCase;

  @Operation(
      summary = "Upload a document",
      description = "Uploads a PDF document with metadata and returns its unique ID.",
      responses = {
        @ApiResponse(
            responseCode = "201",
            description = "Document uploaded successfully",
            content =
                @Content(
                    schema = @Schema(implementation = UploadDocumentResponse.class),
                    examples =
                        @ExampleObject(
                            value = "{\"documentId\":\"a12b34cd-56ef-7890-ab12-cd34ef56ab78\"}"))),
        @ApiResponse(responseCode = "400", description = "Invalid metadata or file"),
        @ApiResponse(responseCode = "409", description = "Duplicate document detected"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      },
      requestBody =
          @RequestBody(
              description =
                  """
                  Multipart request containing a PDF file (up to 500 MB) and its metadata in JSON
                  format.""",
              required = true,
              content =
                  @Content(
                      mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                      schema = @Schema(implementation = DocumentMetadata.class))))
  @PostMapping(path = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<UploadDocumentResponse> upload(
      @RequestPart("file") MultipartFile file, @RequestPart @Valid DocumentMetadata metadata) {

    return ResponseEntity.status(HttpStatus.CREATED).body(service.upload(file, metadata));
  }

  @Operation(
      summary = "Download a document",
      description = "Downloads the document content by its unique ID.",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "OK",
            content = @Content(mediaType = "application/pdf")),
        @ApiResponse(responseCode = "404", description = "Document not found")
      })
  @GetMapping("/{id}/download")
  public ResponseEntity<InputStreamResource> download(
      @Parameter(
              description = "Unique ID of the document",
              example = "a12b34cd-56ef-7890-ab12-cd34ef56ab78")
          @PathVariable
          UUID id) {

    Document document = documentQueryUseCase.findMetadata(id);
    InputStreamResource resource = downloadDocumentUserCase.downloadFile(id);

    return ResponseEntity.ok()
        .contentLength(document.getFileSize())
        .contentType(MediaType.APPLICATION_PDF)
        .header(
            HttpHeaders.CONTENT_DISPOSITION,
            "attachment; filename=\"" + document.getDocumentName() + "\"")
        .body(resource);
  }
}

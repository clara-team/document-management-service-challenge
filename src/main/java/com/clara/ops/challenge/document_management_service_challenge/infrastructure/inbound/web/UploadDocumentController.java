package com.clara.ops.challenge.document_management_service_challenge.infrastructure.inbound.web;

import com.clara.ops.challenge.document_management_service_challenge.application.service.UploadDocumentService;
import com.clara.ops.challenge.document_management_service_challenge.infrastructure.inbound.dto.DocumentMetadata;
import com.clara.ops.challenge.document_management_service_challenge.infrastructure.inbound.dto.UploadDocumentResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/documents")
@RequiredArgsConstructor
@Tag(name = "Document Management Service", description = "API for managing PDF documents")
public class UploadDocumentController {

  // private final UploadDocumentUserCase uploadDocumentUserCase;
  private final UploadDocumentService service;

  @Operation(
      summary = "Upload a PDF document",
      description =
          """
           Handles PDF upload along with its metadata.
           The file is stored in MinIO and the metadata is persisted
          """,
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
                      schema = @Schema(implementation = DocumentMetadata.class))),
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Document uploaded successfully",
            content = @Content(schema = @Schema(implementation = UploadDocumentResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input parameters"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  @PostMapping(path = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<UploadDocumentResponse> upload(
      @RequestPart("file") MultipartFile file,
      @RequestPart @Valid DocumentMetadata metadata,
      @AuthenticationPrincipal Jwt jwt) {

    try {
      return ResponseEntity.ok(service.upload(file, metadata, jwt.getClaimAsString("sub")));
    } catch (Exception e) {
      throw new RuntimeException("Error processing file upload", e);
    }
  }
}

package com.clara.ops.challenge.document_management_service_challenge.controller;

import com.clara.ops.challenge.document_management_service_challenge.domain.dto.DocumentDto;
import com.clara.ops.challenge.document_management_service_challenge.domain.dto.DownloadUrlDto;
import com.clara.ops.challenge.document_management_service_challenge.service.DocumentService;
import io.minio.errors.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/** Controller for managing documents. */
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/documents")
public class DocumentController {

  private final DocumentService documentService;

  /**
   * Get documents endpoint.
   *
   * @param user The user to filter documents by.
   * @param documentName The name of the document to filter documents by.
   * @param tags The tags to filter documents by.
   * @param page The page number to return.
   * @param size The number of documents to return per page.
   * @return A response containing the list of documents.
   */
  @GetMapping
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Documents found",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = Page.class),
                    array = @ArraySchema(schema = @Schema(implementation = DocumentDto.class)))),
        @ApiResponse(responseCode = "404", description = "Documents not found")
      })
  public ResponseEntity<Page<DocumentDto>> getDocuments(
      @RequestParam(value = "user", required = false) String user,
      @RequestParam(value = "documentName", required = false) String documentName,
      @RequestParam(value = "tags", required = false) String tags,
      @RequestParam(defaultValue = "1") int page,
      @RequestParam(defaultValue = "10") int size) {
    return ResponseEntity.ok(documentService.getDocuments(user, documentName, tags, page, size));
  }

  /**
   * Download a document.
   *
   * @param id The ID of the document to download.
   * @return A response containing the download URL.
   */
  @GetMapping("/{id}/download")
  @Operation(description = "Download a document")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Download URL found",
            content =
                @Content(
                    mediaType = "text/plain",
                    schema = @Schema(implementation = DownloadUrlDto.class))),
        @ApiResponse(responseCode = "404", description = "Document not found"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
      })
  public ResponseEntity<DownloadUrlDto> downloadDocument(@PathVariable Long id)
      throws MinioException, IOException, NoSuchAlgorithmException, InvalidKeyException {
    return ResponseEntity.ok(documentService.generateDownloadUrl(id));
  }

  /**
   * Upload a document endpoint.
   *
   * @param file The file to upload.
   * @param metadata The metadata associated with the file.
   * @return A response indicating the success or failure of the upload.
   */
  @PostMapping("upload")
  @Operation(description = "Upload a document")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "File uploaded successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid Request"),
        @ApiResponse(responseCode = "404", description = "User not found"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
      })
  public ResponseEntity createDocument(
      @RequestParam("file") MultipartFile file, @RequestParam Map<String, String> metadata)
      throws ServerException,
          InsufficientDataException,
          ErrorResponseException,
          IOException,
          NoSuchAlgorithmException,
          InvalidKeyException,
          InvalidResponseException,
          XmlParserException,
          InternalException,
          InterruptedException {

    if (file.getSize() > 500 * 1024 * 1024) { // 500 MB limit
      return ResponseEntity.badRequest().body("File size exceeds the maximum limit of 500MB.");
    }

    documentService.uploadFile(file, metadata);
    return ResponseEntity.ok("File uploaded successfully");
  }
}

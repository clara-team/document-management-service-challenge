package com.clara.ops.challenge.document_management_service_challenge.application.service;

import com.clara.ops.challenge.document_management_service_challenge.application.port.in.UploadDocumentUserCase;
import com.clara.ops.challenge.document_management_service_challenge.application.port.out.DocumentRepositoryPort;
import com.clara.ops.challenge.document_management_service_challenge.application.port.out.DocumentStoragePort;
import com.clara.ops.challenge.document_management_service_challenge.domain.model.Document;
import com.clara.ops.challenge.document_management_service_challenge.domain.validator.DocumentValidator;
import com.clara.ops.challenge.document_management_service_challenge.infrastructure.exception.ChecksumException;
import com.clara.ops.challenge.document_management_service_challenge.infrastructure.exception.DocumentValidationException;
import com.clara.ops.challenge.document_management_service_challenge.infrastructure.inbound.dto.DocumentMetadata;
import com.clara.ops.challenge.document_management_service_challenge.infrastructure.inbound.dto.UploadDocumentResponse;
import com.clara.ops.challenge.document_management_service_challenge.infrastructure.util.CheckSum;
import com.clara.ops.challenge.document_management_service_challenge.infrastructure.util.Result;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class UploadDocumentService implements UploadDocumentUserCase {

  private static final String INVALID_FILENAME_ERROR =
      "Invalid file: Filename must not be null and should contain a file extension.";

  private final DocumentStoragePort documentStoragePort;
  private final DocumentRepositoryPort documentRepositoryPort;
  private final DocumentValidator validator;

  @Override
  public UploadDocumentResponse upload(MultipartFile file, DocumentMetadata metadata, String userId)
      throws IOException {

    try (InputStream inputStream = file.getInputStream()) {

      Result<String, Exception> checksumResult =
          CheckSum.generateCheckSum(inputStream, file.getOriginalFilename());
      String checksum =
          checksumResult.fold(
              value -> value,
              error -> {
                log.error(
                    "Failed to generate checksum for file '{}'", file.getOriginalFilename(), error);
                throw new ChecksumException("Failed to generate checksum", error);
              });
    }

    return validateAndBuildDocument(file, metadata, userId)
        .map(this::saveDocument)
        .orElseGet(
            () -> {
              log.error(
                  "Failed to upload document (userId: {}, fileName: {})",
                  userId,
                  file.getOriginalFilename());
              throw new DocumentValidationException(
                  "Failed to upload document (userId: {}, fileName: {})",
                  userId,
                  file.getOriginalFilename());
            });
  }

  private Optional<Document> validateAndBuildDocument(
      MultipartFile file, DocumentMetadata metadata, String userId) {
    return Optional.of(file)
        .filter(this::isFileValid)
        .map(validFile -> buildDocument(validFile, metadata, userId));
  }

  private boolean isFileValid(MultipartFile file) {
    try {
      validator.validateFile(file);
      return true;
    } catch (IllegalArgumentException e) {
      log.error("File validation failed: {}", e.getMessage());
      return false;
    }
  }

  private Document buildDocument(MultipartFile file, DocumentMetadata metadata, String userId) {
    String fileExtension = extractFileExtension(file.getOriginalFilename());
    return Document.builder()
        .id(UUID.randomUUID().toString())
        .userId(BigInteger.valueOf(Long.parseLong(userId)))
        .documentName(file.getOriginalFilename())
        .fileSize(file.getSize())
        .checksum(123L) // Placeholder until checksum logic is implemented.
        .tags(metadata.tags())
        .fileType(fileExtension)
        .build();
  }

  private String extractFileExtension(String filename) {
    return Optional.ofNullable(filename)
        .filter(name -> name.contains("."))
        .map(name -> name.substring(name.lastIndexOf(".") + 1))
        .orElseThrow(() -> new IllegalArgumentException(INVALID_FILENAME_ERROR));
  }

  private UploadDocumentResponse saveDocument(Document document) {
    try {
      documentRepositoryPort.save(document);
      log.info("Document successfully saved: {}", document.getDocumentName());
      return UploadDocumentResponse.builder()
          .documentId(new BigInteger(document.getId()))
          .documentName(document.getDocumentName())
          .fileSize(document.getFileSize())
          .uploadDate(Instant.now())
          .build();
    } catch (Exception e) {
      log.error("Error occurred while saving document: {}", document.getDocumentName(), e);
      return null;
    }
  }
}

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

/**
 * Service responsible for handling the document upload process. This class validates the document,
 * generates a checksum, stores the file, and persists the metadata associated with the uploaded
 * document.
 *
 * <p>Implements the {@link UploadDocumentUserCase} interface.
 */
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
  public UploadDocumentResponse upload(MultipartFile file, DocumentMetadata metadata)
      throws ChecksumException, DocumentValidationException {

    return generateChecksum(file)
        .fold(
            checksum -> {
              if (documentRepositoryPort.existsByUserIdAndChecksum(
                  new BigInteger(metadata.userId()), checksum)) {
                throw new DocumentValidationException(
                    "User already uploaded this document.",
                    metadata.userId(),
                    file.getOriginalFilename());
              }
              validateFile(file);
              String minioPath = storeFile(file, metadata.userId());
              String fileUrl = documentStoragePort.generatePresignedUrl(minioPath);

              return persistMetadata(file, metadata, checksum, minioPath, fileUrl);
            },
            error -> {
              logError("Checksum generation failed", file, error);
              throw new ChecksumException("Failed to generate checksum.", error);
            });
  }

  private Result<String, Exception> generateChecksum(MultipartFile file) {
    try (InputStream inputStream = file.getInputStream()) {
      return CheckSum.generateCheckSum(inputStream, file.getOriginalFilename());
    } catch (IOException e) {
      logError("I/O exception while generating checksum", file, e);
      return Result.failure(e);
    }
  }

  private void validateFile(MultipartFile file) {
    Result.of(
            () -> {
              validator.validateFile(file);
              return true;
            })
        .fold(
            value -> true,
            error -> {
              throw new DocumentValidationException(
                  "Invalid file.", "N/A", file.getOriginalFilename());
            });
  }

  private String storeFile(MultipartFile file, String userId) {
    try {
      String path = documentStoragePort.uploadFile(file, userId);
      log.info("File '{}' uploaded to MinIO at '{}'", file.getOriginalFilename(), path);
      return path;
    } catch (IOException e) {
      throw new RuntimeException("Error uploading file to MinIO", e);
    }
  }

  private UploadDocumentResponse persistMetadata(
      MultipartFile file,
      DocumentMetadata metadata,
      String checksum,
      String minioPath,
      String fileUrl) {

    Document document =
        Document.builder()
            .id(UUID.randomUUID())
            .userId(new BigInteger(metadata.userId()))
            .documentName(file.getOriginalFilename())
            .fileSize(file.getSize())
            .checksum(checksum)
            .tags(metadata.tags())
            .fileType(extractFileExtension(file.getOriginalFilename()))
            .minioPath(minioPath)
            .status("UPLOADED")
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .documentUrl(fileUrl)
            .build();

    try {
      documentRepositoryPort.save(document);
      return UploadDocumentResponse.builder().documentId(document.getId()).build();
    } catch (Exception e) {
      // Rollback is save document fails
      documentStoragePort.delete(minioPath);
      log.warn("Rollback completed for document '{}'.", document.getDocumentName());
      throw new RuntimeException("Failed to save document metadata.", e);
    }
  }

  private String extractFileExtension(String filename) {
    return Optional.ofNullable(filename)
        .filter(name -> name.contains("."))
        .map(name -> name.substring(name.lastIndexOf('.') + 1))
        .orElseThrow(() -> new IllegalArgumentException(INVALID_FILENAME_ERROR));
  }

  private void logError(String message, MultipartFile file, Throwable error) {
    log.error(
        "{} for file '{}': {}", message, file.getOriginalFilename(), error.getMessage(), error);
  }
}

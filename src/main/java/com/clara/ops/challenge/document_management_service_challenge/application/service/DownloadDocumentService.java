package com.clara.ops.challenge.document_management_service_challenge.application.service;

import com.clara.ops.challenge.document_management_service_challenge.application.port.in.DownloadDocumentUserCase;
import com.clara.ops.challenge.document_management_service_challenge.application.port.out.DocumentRepositoryPort;
import com.clara.ops.challenge.document_management_service_challenge.application.port.out.DocumentStoragePort;
import com.clara.ops.challenge.document_management_service_challenge.domain.model.Document;
import com.clara.ops.challenge.document_management_service_challenge.infrastructure.exception.DocumentNotFoundException;
import com.clara.ops.challenge.document_management_service_challenge.infrastructure.exception.DownloadDocumentException;
import java.io.InputStream;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Service;

/**
 * Service implementation for downloading documents from storage.
 *
 * <p>This class handles the process of retrieving documents by their unique identifier, interacting
 * with the repository for document metadata and the storage for the actual file. It also includes
 * error handling for scenarios where the document is not found or an issue occurs during the
 * download process.
 *
 * <p>Dependencies: - DocumentRepositoryPort: For retrieving document metadata from the repository.
 * - DocumentStoragePort: For handling file operations in the storage system.
 *
 * <p>Responsibilities: - Retrieve document metadata from the repository using the provided document
 * ID. - Download the document file from the storage system using the metadata. - Return an
 * InputStreamResource for the downloaded document for further processing or response generation. -
 * Log relevant operations, including successful downloads and errors.
 *
 * <p>Exceptions: - Throws DocumentNotFoundException if the document metadata is not found in the
 * repository. - Throws DownloadDocumentException if any issue occurs during the file retrieval
 * process from the storage system.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DownloadDocumentService implements DownloadDocumentUserCase {

  private final DocumentRepositoryPort repository;
  private final DocumentStoragePort storage;

  @Override
  public InputStreamResource downloadFile(UUID documentId) {
    Document document =
        repository
            .findById(documentId)
            .orElseThrow(() -> new DocumentNotFoundException(documentId.toString()));
    log.info("Downloading document '{}' from MinIO ", documentId);

    try {
      InputStream stream = storage.download(document.getMinioPath(), document.getDocumentName());
      return new InputStreamResource(stream) {
        @Override
        public String getFilename() {
          return document.getDocumentName();
        }
      };
    } catch (Exception e) {
      log.error("Failed to download '{}': {}", document.getDocumentName(), e.getMessage());
      throw new DownloadDocumentException("Error downloading document from MinIO", e);
    }
  }
}

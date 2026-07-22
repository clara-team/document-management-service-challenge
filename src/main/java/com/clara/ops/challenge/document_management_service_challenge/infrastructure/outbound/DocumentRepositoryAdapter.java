package com.clara.ops.challenge.document_management_service_challenge.infrastructure.outbound;

import com.clara.ops.challenge.document_management_service_challenge.application.port.out.DocumentRepositoryPort;
import com.clara.ops.challenge.document_management_service_challenge.domain.model.Document;
import com.clara.ops.challenge.document_management_service_challenge.infrastructure.exception.ApplicationException;
import com.clara.ops.challenge.document_management_service_challenge.infrastructure.exception.ErrorCode;
import com.clara.ops.challenge.document_management_service_challenge.infrastructure.outbound.persistence.DocumentEntity;
import com.clara.ops.challenge.document_management_service_challenge.infrastructure.outbound.persistence.DocumentMapper;
import com.clara.ops.challenge.document_management_service_challenge.infrastructure.outbound.persistence.DocumentRepository;
import java.math.BigInteger;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Adapter implementation of the {@link DocumentRepositoryPort} interface for managing
 * document-related operations. It serves as a bridge between the domain layer and the persistence
 * layer, providing a concrete implementation to handle interactions with the {@link
 * DocumentRepository}.
 *
 * <p>This class utilizes the {@link DocumentMapper} to facilitate conversions between the domain
 * model {@link Document} and the persistence entity {@link DocumentEntity}.
 *
 * <p>Key responsibilities: - Saving documents to the database. - Validating the existence of
 * documents by user ID and checksum or by ID and checksum. - Retrieving documents based on ID or
 * document name.
 *
 * <p>Logging is performed using the {@code @Slf4j} annotation to track errors or pertinent events.
 *
 * <p>Transactional management is applied to the save method to ensure data consistency.
 *
 * <p>Errors during database operations are caught and wrapped inside an {@link
 * ApplicationException}, providing a consistent error handling mechanism across the application.
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class DocumentRepositoryAdapter implements DocumentRepositoryPort {

  private final DocumentRepository documentRepository;
  private final DocumentMapper mapper;
  private final DocumentMapper documentMapper;

  @Override
  @Transactional
  public Document save(Document document) {

    try {

      DocumentEntity entity = mapper.toEntity(document);
      return mapper.toDomain(documentRepository.save(entity));

    } catch (DataAccessException e) {
      log.error(
          "Failed to save document with ID {}: {}",
          document.getId(),
          e.getMostSpecificCause().getMessage(),
          e);
      throw new ApplicationException(ErrorCode.DOCUMENT_NOT_CREATED);
    }
  }

  @Override
  public boolean existsByUserIdAndChecksum(BigInteger userId, String checksum) {
    return documentRepository.existsByUserIdAndChecksum(userId, checksum);
  }

  @Override
  public boolean existsByIdAndChecksum(UUID uuid, String checksum) {
    return documentRepository.existsByIdAndChecksum(uuid, checksum);
  }

  @Override
  public Optional<Document> findById(UUID documentId) {
    return documentRepository.findById(documentId).map(mapper::toDomain);
  }

  @Override
  public Optional<DocumentEntity> findByName(String fileName) {
    return documentRepository.findByDocumentName(fileName);
  }
}

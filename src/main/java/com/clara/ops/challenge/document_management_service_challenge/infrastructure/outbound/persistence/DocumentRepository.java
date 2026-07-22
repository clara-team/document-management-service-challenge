package com.clara.ops.challenge.document_management_service_challenge.infrastructure.outbound.persistence;

import java.math.BigInteger;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository interface for managing {@link DocumentEntity} persistence operations. Extends the
 * {@link JpaRepository} to provide basic data access and manipulation methods.
 *
 * <p>This interface includes additional query methods for specific use cases related to the
 * application's business requirements.
 *
 * <p>Key Methods: - {@code existsByChecksum(String checksum)}: Checks whether a document with the
 * specified checksum exists. - {@code findByDocumentName(String fileName)}: Retrieves a document
 * entity based on its document name.
 *
 * <p>Relationships: - Operates on {@link DocumentEntity}, which represents the database entity for
 * documents.
 */
public interface DocumentRepository extends JpaRepository<DocumentEntity, UUID> {
  boolean existsByIdAndChecksum(UUID uuid, String checksum);

  boolean existsByUserIdAndChecksum(BigInteger userId, String checksum);

  Optional<DocumentEntity> findByDocumentName(String fileName);
}

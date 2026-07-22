package com.clara.ops.challenge.document_management_service_challenge.application.port.out;

import com.clara.ops.challenge.document_management_service_challenge.domain.model.Document;
import com.clara.ops.challenge.document_management_service_challenge.infrastructure.outbound.persistence.DocumentEntity;
import java.math.BigInteger;
import java.util.Optional;
import java.util.UUID;

/**
 * Port interface for performing operations on documents in a repository. Acts as an abstraction for
 * the persistence logic related to document management.
 */
public interface DocumentRepositoryPort {
  Document save(Document document);

  boolean existsByUserIdAndChecksum(BigInteger userId, String checksum);

  boolean existsByIdAndChecksum(UUID uuid, String checksum);

  Optional<Document> findById(UUID uuid);

  Optional<DocumentEntity> findByName(String fileName);
}

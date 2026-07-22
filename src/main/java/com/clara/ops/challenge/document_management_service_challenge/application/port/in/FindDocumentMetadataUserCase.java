package com.clara.ops.challenge.document_management_service_challenge.application.port.in;

import com.clara.ops.challenge.document_management_service_challenge.domain.model.Document;
import java.util.UUID;

/**
 * Interface for retrieving metadata of a specific document. Provides functionality to fetch
 * document-related metadata using the document's unique identifier.
 *
 * <p>The returned metadata is encapsulated in a {@link Document} domain model.
 */
public interface FindDocumentMetadataUserCase {
  Document findMetadata(UUID documentId);
}

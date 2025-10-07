package com.clara.ops.challenge.document_management_service_challenge.infrastructure.inbound.dto;

import java.util.UUID;
import lombok.Builder;

/**
 * Represents the response returned after successfully uploading a document. This response
 * encapsulates the unique identifier (UUID) of the uploaded document.
 */
@Builder
public record UploadDocumentResponse(UUID documentId) {}
